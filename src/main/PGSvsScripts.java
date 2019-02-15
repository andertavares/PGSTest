package main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.abstraction.EconomyMilitaryRush;
import ai.abstraction.HeavyDefense;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightDefense;
import ai.abstraction.LightRush;
import ai.abstraction.RangedDefense;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerDefense;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.PGS.PGSSCriptChoiceRandom;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.units.UnitTypeTable;

public class PGSvsScripts {

	public static void main(String[] args) throws Exception {
		Map<String,AI> matchups = new HashMap<>();
		
		UnitTypeTable types = new UnitTypeTable();
	    //PhysicalGameState pgs = PhysicalGameState.load(maps.get(map), utt);
		
		matchups.put("maps/8x8/basesWorkers8x8A.xml", new WorkerRush(types));
		matchups.put("maps/9x8/BlockDiagonal9x8.xml", new LightRush(types));
		matchups.put("maps/16x16/BasesWithWalls16x16.xml", new LightRush(types));
		matchups.put("maps/24x24/basesWorkers24x24A.xml", new WorkerRush(types));
		matchups.put("maps/24x24/basesWorkers24x24A.xml", new LightRush(types));
		matchups.put("maps/32x32/basesWorkers32x32A.xml", new LightRush(types));
		
		List<AI> portfolio = Arrays.asList(
			new WorkerRush(types), //begin: 4 rushes
			new LightRush(types),
			new HeavyRush(types),
			new RangedRush(types), //end: 4 rushes
			new WorkerDefense(types), //begin: 4 defenses
			new LightDefense(types),
			new HeavyDefense(types),
			new RangedDefense(types), //end: 4 defenses
			new EconomyMilitaryRush(types)
		);
		
		//instantiates PGS^s from the paper 'Evolving Action Abstractions'
		//parameters:
		int time = 100;		//time in milliseconds
		int playouts = -1; 	//do as many playouts as you can
		int lookahead = 100; //lookahead in number of frames
		int pgsIter = 1; //I in PGS class, not used
		int oppReps = 1; //R in PGS class, not used
		
		PGSSCriptChoiceRandom pgs_s = new PGSSCriptChoiceRandom(
			time, playouts, lookahead, pgsIter, oppReps, 
			new SimpleSqrtEvaluationFunction3(), types, 
			new AStarPathFinding(), portfolio
		);
		
		Runner runner = new Runner();
		
		//FIXME: erro porque randAI == null in PGS class
		for(Entry<String, AI> entry : matchups.entrySet()){
			//key is the map, value is the AI
			System.out.println("Match is PGS vs " + entry.getValue());
			//runs two matches switching the player positions
			runner.headlessMatch(pgs_s, entry.getValue(), entry.getKey(), types);
			System.out.println("- now it is " + entry.getValue() + " vs PGS");
			runner.headlessMatch(entry.getValue(), pgs_s, entry.getKey(), types);
		}

	}

}
