package main;

import java.util.*;

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
import ai.asymmetric.PGS.PGSSCriptChoiceRandom;
import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import data.MatchData;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 * Runs matches between PGS and the best script for each map according to the paper of A1N, A2N and A3N.
 * PGS version is constructed according to the paper 'Evolving Action Abstractions (...)'
 * 
 * TODO parameterize output dir and update the script accordingly
 * 
 * @author anderson
 *
 */
public class PGSvsScripts {

	public static void main(String[] args) throws Exception {
		List<Pair<String,AI>> matchups = new ArrayList<>();
		
		UnitTypeTable types = new UnitTypeTable();
	    //PhysicalGameState pgs = PhysicalGameState.load(maps.get(map), utt);

		/* The 18 maps of 'Action Abstractions for CMAB Tree Search' are:
		basesWorkers8x8A
		FourBasesWorkers8x8
		NoWhereToRun9x8
		TwoBasesBarracks16x16
		basesWorkers16x16A
		DoubleGame24x24
		basesWorkers24x24A
		basesWorkers32x32A
		BWDistantResources32x32
		(4)BloodBath.scmA
		(4)BloodBath.scmB
		(4)BloodBath.scmC
		(4)BloodBath.scmD
		(4)Andromeda.scxE
		(4)CircuitBreaker.scxF
		(4)Fortress.scxA
		(4)Python.scxB
		(2)Destination.scxA
		*/
		
		matchups.add(new Pair<>("maps/8x8/basesWorkers8x8A.xml", new WorkerRush(types)));
		matchups.add(new Pair<>("maps/8x8/FourBasesWorkers8x8.xml", new WorkerRush(types)));

		matchups.add(new Pair<>("maps/9x8/NoWhereToRun9x8.xml", new LightRush(types)));

		matchups.add(new Pair<>("maps/16x16/TwoBasesBarracks16x16.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/16x16/basesWorkers16x16A.xml", new LightRush(types)));


		matchups.add(new Pair<>("maps/24x24/basesWorkers24x24A.xml", new WorkerRush(types)));
		matchups.add(new Pair<>("maps/24x24/basesWorkers24x24A.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/24x24/DoubleGame24x24.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/24x24/DoubleGame24x24.xml", new WorkerRush(types)));

		matchups.add(new Pair<>("maps/32x32/basesWorkers32x32A.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/BWDistantResources32x32.xml", new LightRush(types)));
		
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
		int rounds = 10;
		
		for(Pair<String, AI> entry : matchups){
			//m_a is the map, m_b is the AI
			
			for(int r = 1; r <= rounds; r++){
				MatchData data;
				
				System.out.println("Round: #" + r);
				System.out.println("Match is PGS vs " + entry.m_b);
				//runs two matches switching the player positions
				data = runner.headlessMatch(pgs_s, entry.m_b, entry.m_a, r, types);
				runner.recordMatchData("output", data);
				
				System.out.println("- now it is " + entry.m_b + " vs PGS");
				data = runner.headlessMatch(entry.m_b, pgs_s, entry.m_a, r, types);
				runner.recordMatchData("output", data);
			}
		}

	}

}
