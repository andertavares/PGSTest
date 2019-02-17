package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

import ai.core.AI;
import data.MatchData;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

public class Runner {
	
	public MatchData headlessMatch(AI ai1, AI ai2, String map, int roundNumber, UnitTypeTable types) throws Exception{
		ArrayList<String> log = new ArrayList<>();
		Duration matchDuration;
		Instant timeInicial = Instant.now();
		
		MatchData data = new MatchData(ai1, ai2, map);
		
		PhysicalGameState physicalGameState = null;
		try {
			physicalGameState = PhysicalGameState.load(map, types);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

        GameState gs = new GameState(physicalGameState, types);
        int MAXCYCLES = 20000;
        boolean gameover = false;

        if (physicalGameState.getHeight() == 8) {
            MAXCYCLES = 4000;
        }
        if (physicalGameState.getHeight() == 16) {
            MAXCYCLES = 5000;
        }
        if (physicalGameState.getHeight() == 24) {
            MAXCYCLES = 6000;
        }
        if (physicalGameState.getHeight() == 32) {
            MAXCYCLES = 7000;
        }
        if (physicalGameState.getHeight() == 64) {
            MAXCYCLES = 12000;
        }
        
        MAXCYCLES = 10; //uncomment to run super short matches
        
        /*
        Variáveis para coleta de tempo
	     */
	    data.minTimeP1 = 9999; 
	    data.maxTimeP1 = -9999;
	    data.totalTimeP1 = 0;
	    
	    data.minTimeP2 = 9999;
	    data.maxTimeP2 = -9999;
	    //int totalTimeAi1 = 0, totalTimeAi2 = 0;
	
	    log.add("---------AIs---------");
	    log.add("AI 1 = " + ai1.toString());
	    log.add("AI 2 = " + ai2.toString() + "\n");
	
	    log.add("---------Map---------");
	    log.add("Map= " + map + "\n");
	
	    long startTime;
	    long timeTemp;
	    do {
            startTime = System.currentTimeMillis();

            PlayerAction pa1 = ai1.getAction(0, gs);
            //dados de tempo ai1
            timeTemp = (System.currentTimeMillis() - startTime);
            data.totalTimeP1 += timeTemp;
            //coleto tempo mínimo
            if (data.minTimeP1 > timeTemp) {
            	data.minTimeP1 = (int) timeTemp;
            }
            //coleto tempo maximo
            if (data.maxTimeP1 < timeTemp) {
            	data.maxTimeP1 = (int) timeTemp;
            }

            startTime = System.currentTimeMillis();
            PlayerAction pa2 = ai2.getAction(1, gs);
            //dados de tempo ai2
            timeTemp = (System.currentTimeMillis() - startTime);
            data.totalTimeP2 += timeTemp;
            
            //coleto tempo mínimo
            if (data.minTimeP2 > timeTemp) {
            	data.minTimeP2 = (int) timeTemp;
            }
            //coleto tempo maximo
            if (data.maxTimeP2 < timeTemp) {
            	data.maxTimeP2 = (int) timeTemp;
            }

            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            // simulate:
            gameover = gs.cycle();
            System.out.print(String.format("\rExecuted %8d frames.", gs.getTime()));

            //avaliacao de tempo
	        matchDuration = Duration.between(timeInicial, Instant.now());
	
	    } while (!gameover && (gs.getTime() < MAXCYCLES) && (matchDuration.toMinutes() < 7));

	    // game over, record post-game data
	    data.winner = gs.winner();
	    data.frames = gs.getTime();
	    data.duration = matchDuration;

	    
	    		
	    // compute the average time per frame
	    data.avgTimeP1 = (data.totalTimeP1 / (float) gs.getTime());
	    data.avgTimeP2 = (data.totalTimeP2 / (float) gs.getTime());
	
	    log.add("Frames= " + gs.getTime() + " sumAi1= " + data.totalTimeP1 + " sumAi2= " + data.totalTimeP2 + "\n");
	
	    log.add("Tempos de AI 1 = " + ai1.toString());
	    log.add("Tempo minimo= " + data.minTimeP1 + " Tempo maximo= " + data.maxTimeP1 + " Tempo medio= " + data.avgTimeP1 );
	
	    log.add("Tempos de AI 2 = " + ai2.toString());
	    log.add("Tempo minimo= " + data.minTimeP2 + " Tempo maximo= " + data.maxTimeP2 + " Tempo medio= " + data.avgTimeP2 + "\n");
	
	    log.add("Winner " + gs.winner());
	    log.add("Game Over");
	
	    if (gs.winner() == -1) {
	        System.out.println("Empate!" + ai1.toString() + " vs " + ai2.toString() + " Max Cycles =" + MAXCYCLES + " Time:" + matchDuration.toMinutes());
	    }
		return data;
	
	    //recordLog("output", log, ai1.toString(), ai2.toString(), map);
	}
	
	public void recordMatchData(String outDir, MatchData data){
		
		//ensures outDir has a trailing slash
		if (!outDir.endsWith("/")) { 
			 outDir += "/";
        }
		
		String fileName = outDir + data.getP1Name() + "_" + data.getP2Name() + "_" + data.getMapName() + ".csv";
        File output = new File(fileName);
        
        System.out.println("Output file: " + output.getAbsolutePath());
        
        if(!output.exists()){ // creates a new file and writes the header
    		System.out.println("Output file didn't exist, creating and writing header");
    		FileWriter newFile;
			try {
				newFile = new FileWriter(output, false); //test if the file exists first, because it creates the file upon instantiation
				newFile.write("#winner,frames,duration,totalTime1,minTime1,maxTime1,avgTime1,totalTimeAI2,minTime2,maxTime2,avgTime2\n");
	    		newFile.close();
			} catch (IOException e) {
				System.err.println("Error while creating the output file");
				e.printStackTrace();
				return;
			} 
    	}
        
        // appends one line with each weight value separated by a comma
    	FileWriter appender;
		try {
			appender = new FileWriter(output, true);
			// field order: winner,frames,duration(s),totalTimeAI1,minTime1,maxTime1,avgTime1,totalTimeAI2,minTime2,maxTime2,avgTime2
			appender.write(String.format( Locale.US, //ensure dot as a decimal separator
				"%d,%d,%d," //winner,frames,duration
				+ "%d,%d,%d,%f," //total1,min1,max1,avg1
				+ "%d,%d,%d,%f%n", //total2,min2,max2,avg2,line break 
				
				data.winner, data.frames, data.duration.getSeconds(), 
				data.totalTimeP1, data.minTimeP1, data.maxTimeP1, data.avgTimeP1,
				data.totalTimeP2, data.minTimeP2, data.maxTimeP2, data.avgTimeP2
			));
			//logger.debug("Successfully wrote to {}", path);
	    	appender.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} // end of recordMatchData
	
	
	
	/*
	private void recordLog(ArrayList<String> log, String sIA1, String sIA2, String sMap, int roundNumber, String pathLog) throws IOException {
        if (!pathLog.endsWith("/")) {
            pathLog += "/";
        }

        // obtains the file name strips the full directory
        File mapFile = new File(sMap);
        sMap = mapFile.getName();
        
        String nameArquivo = pathLog + "match_" + roundNumber + "_"+ sIA1 + "_" + sIA2 + "_" + sMap + ".csv";
        File output = new File(nameArquivo);
        
        System.out.println("Output file: " + output.getAbsolutePath());
        
        if(!output.exists()){ // creates a new file and writes the header
    		System.out.println("Output file didn't exist, creating and writing header");
    		FileWriter newFile = new FileWriter(output, false); //must be after the test, because it creates the file upon instantiation
    		newFile.write("#winner,totalActions,sumAI1,sumAI2,minTime1,maxTime1,avgTime1,minTime2,maxTime2,avgTime2\n");
    		newFile.close();
    	}
        
        // appends one line with each weight value separated by a comma
    	FileWriter writer = new FileWriter(output, true); 
    	writer.write(String.format("%d,%d,%s,%s\n", result, duration, start, finish));
    	
    	writer.close();
        
        try {
            FileWriter appender = new FileWriter(output, true);
            
            // appends one line with each weight value separated by a comma
        	appender = new FileWriter(output, true); 
        	appender.write(String.format("%d,%d,%s,%s\n", result, duration, start, finish));
        	logger.debug("Successfully wrote to {}", path);
        	
        	appender.close();
            
            PrintWriter gravarArq = new PrintWriter(appender);
            for (String l : log) {
                gravarArq.println(l);
            }

            gravarArq.flush();
            gravarArq.close();
            appender.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    */
}
