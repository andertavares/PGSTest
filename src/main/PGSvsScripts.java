package main;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.abstraction.EconomyMilitaryRush;
import ai.abstraction.HeavyDefense;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightDefense;
import ai.abstraction.LightRush;
import ai.abstraction.RangedDefense;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerDefense;
import ai.abstraction.WorkerRush;
import ai.asymmetric.PGS.PGSSCriptChoiceRandom;
import ai.core.AI;
import rts.units.UnitTypeTable;

public class PGSvsScripts {

	public static void main(String[] args) {
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
		PGSSCriptChoiceRandom pgs_s = new PGSSCriptChoiceRandom(types, portfolio);

	}

}
