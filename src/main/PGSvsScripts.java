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

		if(args.length == 0){
			System.out.println("Please specify either 'scripts' or 'time' to run the tests");
			System.exit(0);
		}

		if(args[0].equalsIgnoreCase("scripts")){
			testVsScripts();
		}

		else if (args[0].equalsIgnoreCase(("time"))){
			testPlanningTime();
		}

		System.out.println("Done.");
	}


	public static void testVsScripts(){
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

		matchups.add(new Pair<>("maps/NoWhereToRun9x8.xml", new LightRush(types)));

		matchups.add(new Pair<>("maps/16x16/TwoBasesBarracks16x16.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/16x16/basesWorkers16x16A.xml", new LightRush(types)));


		matchups.add(new Pair<>("maps/24x24/basesWorkers24x24A.xml", new WorkerRush(types)));
		matchups.add(new Pair<>("maps/24x24/basesWorkers24x24A.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/DoubleGame24x24.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/DoubleGame24x24.xml", new WorkerRush(types)));

		matchups.add(new Pair<>("maps/32x32/basesWorkers32x32A.xml", new LightRush(types)));
		matchups.add(new Pair<>("maps/BWDistantResources32x32.xml", new LightRush(types)));

		List<AI> portfolio = getPGSSPortfolio(types);

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
		try {
			runner.repeatedMatches(pgs_s, matchups, rounds, types, "output");
		}
		catch (Exception e){
			System.err.println("An error occurred");
			e.printStackTrace();
		}
	}

	public static void testPlanningTime(){
		UnitTypeTable types = new UnitTypeTable();

		List<String> maps = new ArrayList<>();

		maps.add("maps/8x8/basesWorkers8x8A.xml");
		maps.add("maps/8x8/FourBasesWorkers8x8.xml");

		maps.add("maps/NoWhereToRun9x8.xml");

		maps.add("maps/16x16/TwoBasesBarracks16x16.xml");
		maps.add("maps/16x16/basesWorkers16x16A.xml");

		maps.add("maps/24x24/basesWorkers24x24A.xml");
		maps.add("maps/24x24/basesWorkers24x24A.xml");
		maps.add("maps/DoubleGame24x24.xml");
		maps.add("maps/DoubleGame24x24.xml");

		maps.add("maps/32x32/basesWorkers32x32A.xml");
		maps.add("maps/BWDistantResources32x32.xml");

		List<AI> portfolio = getPGSSPortfolio(types);

		//instantiates PGS^s from the paper 'Evolving Action Abstractions'
		//parameters:
		int playouts = -1; 	//do as many playouts as you can
		int lookahead = 100; //lookahead in number of frames
		int pgsIter = 1; //I in PGS class, not used
		int oppReps = 1; //R in PGS class, not used

		// creates the instances of PGS.s with different timeouts, indicated in the name complements
		PGSSCriptChoiceRandom pgs_400 = new PGSSCriptChoiceRandom(
				400, playouts, lookahead, pgsIter, oppReps,
				new SimpleSqrtEvaluationFunction3(), types,
				new AStarPathFinding(), portfolio
		);
		pgs_400.setNameComplement("-s400");

		PGSSCriptChoiceRandom pgs_200 = new PGSSCriptChoiceRandom(
				200, playouts, lookahead, pgsIter, oppReps,
				new SimpleSqrtEvaluationFunction3(), types,
				new AStarPathFinding(), portfolio
		);
		pgs_200.setNameComplement("-s200");

		PGSSCriptChoiceRandom pgs_100 = new PGSSCriptChoiceRandom(
				100, playouts, lookahead, pgsIter, oppReps,
				new SimpleSqrtEvaluationFunction3(), types,
				new AStarPathFinding(), portfolio
		);
		pgs_100.setNameComplement("-s100");
		List<AI> pgsList = new ArrayList<>();
		pgsList.add(pgs_400);
		pgsList.add(pgs_200);
		pgsList.add(pgs_100);

		Runner runner = new Runner();
		int rounds = 10;

		try {
			runner.mapsAndAIs(maps, pgsList, rounds, types, "output");
		}
		catch (Exception e){
			System.err.println("An error occurred");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the 9 scripts of PGS.s
	 * @param types the UnitTypeTable that some scripts require
	 * @return the portfolio
	 */
	private static List<AI> getPGSSPortfolio(UnitTypeTable types) {
		return Arrays.asList(
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
	}

}
