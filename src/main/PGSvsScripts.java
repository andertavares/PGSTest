package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.abstraction.WorkerRush;
import ai.core.AI;

public class PGSvsScripts {

	public static void main(String[] args) {
		Map<String,AI> matchups = new HashMap<>();
		
		matchups.put("maps/8x8/basesWorkers8x8A.xml", new WorkerRush(types));
		matchups.put("maps/24x24/basesWorkers24x24A.xml", new WorkerRush(types));
		matchups.put("maps/24x24/basesWorkers24x24A.xml", new WorkerRush(types));
		
		/*(Arrays.asList(
                "maps/24x24/basesWorkers24x24A.xml",
                "maps/DoubleGame24x24.xml",
                "maps/32x32/basesWorkers32x32A.xml",
                "maps/BWDistantResources32x32.xml",
                "maps/BroodWar/(4)BloodBath.scmB.xml",
				"maps/8x8/basesWorkers8x8A.xml",
                "maps/16x16/BasesWithWalls16x16.xml"
                
        ));*/

	}

}
