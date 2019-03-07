package main;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import data.MatchData;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Pair;

public class Runner {

	/**
	 * Runs matches of p1 in the specified matchups, where each has an opponent and a map.
	 * Each matchup is repeated for a number of rounds alternating the starting positions of each player.
	 * The results of each match are written to the specified output dir
	 * @param p1 the main AI under test
	 * @param matchups a list where each element is an opponent and a map.
	 * @param rounds the number of repetitions of each matchup
	 * @param types the rules under which the matches will be played
	 * @param outDir the directory here the results will be written
	 * @throws Exception
	 */
	public void repeatedMatches(AI p1, List<Pair<String, AI>> matchups, int rounds, UnitTypeTable types, String outDir) throws Exception{

		for(Pair<String, AI> entry : matchups){
			//m_a is the map, m_b is the AI

			for(int r = 1; r <= rounds; r++){
				MatchData data;

				System.out.println("Round: #" + r);
				System.out.println("Match is " + p1 + " vs " + entry.m_b + " in " + entry.m_a);
				//runs two matches switching the player positions
				data = headlessMatch(p1, entry.m_b, entry.m_a, r, types);
				recordMatchData(outDir, data);

				System.out.println("- now it is " + entry.m_b + " vs "+ p1);
				data = headlessMatch(entry.m_b, p1, entry.m_a, r, types);
				recordMatchData(outDir, data);
			}
		}
	}

	/**
	 * Returns whether the match with the given configuration has been played.
	 * It checks the existence of the file that would register the outcome, then
	 * it counts the lines to see if the specified round has been played.
	 * @param p1 the first player
	 * @param p2 the second player
	 * @param map the map
	 * @param round the round number (for repeated experiments)
	 * @param outDir the directory where output is being/will be written
	 * @return whether the match with the given configuration has been played.
	 * TODO implement a 'nextRound' which returns the index of the next round to play
	 */
	private boolean alreadyPlayed(AI p1, AI p2, String map, int round, String outDir){
		String fileName = getFileName(p1, p2, map, outDir);
		File underTest = new File(fileName);

		// if the file does not exist, no match has been played with such configuration
		if (!underTest.exists()){
			return false;
		}

		// if the file exists, we must check how much rounds have been played (code from https://stackoverflow.com/a/5342096/1251716)
		int numLines = 0;
		try {
			LineNumberReader count = new LineNumberReader(new FileReader(underTest));
			while (count.skip(Long.MAX_VALUE) > 0) {
				// Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
			}

			numLines = count.getLineNumber() + 1; // +1 because line index starts at 0
		}
		catch (IOException e){
			System.err.println(
				"An error occurred when trying to count the lines of file " + fileName +
				"\n. Will report match as not played"
			);
			e.printStackTrace();
			return false;
		}

		// discounts one from numLines because the first line is the header
		// adds one to round because it is zero-indexed
		return numLines - 1 >= round + 1;


	}

	/**
	 * Runs all possible pairings of the given AIs in each map.
	 * Each pairing plays in each map for a number of rounds.
	 * Each round has two matches, where the initial positions are swapped in the second
	 * @param maps
	 * @param aiList
	 * @param rounds
	 * @param types
	 * @param outDir
	 * @throws Exception
	 */
	public void mapsAndAIs(List<String> maps, List<AI> aiList, int rounds, UnitTypeTable types, String outDir) throws Exception{
		for(String map : maps){
			for(AI p1 : aiList){
				for(AI p2 : aiList) {
					if (p1 == p2) continue;

					for (int r = 1; r <= rounds; r++) {


						MatchData data;

						// if match of p1 vs p2 already exists, skip it
						if(alreadyPlayed(p1, p2, map, r, outDir)){
							System.out.println(String.format(
									"Skipping round %d of %s vs %s in map %s.",
									r, p1, p2, map
							));
						}
						else {

							System.out.println("Round: #" + r);
							System.out.println("Match is " + p1 + " vs " + p2 + " in " + map);
							//runs two matches switching the player positions
							data = headlessMatch(p1, p2, map, r, types);
							recordMatchData(outDir, data);
						}

						// if match of p2 vs p1 already exists, skip it
						if(alreadyPlayed(p2, p1, map, r, outDir)){
							System.out.println(String.format(
								"Skipping round %d of %s vs %s in map %s.",
								r, p2, p1, map
							));
						}
						else {
							System.out.println("- now it is " + p2 + " vs " + p1);
							data = headlessMatch(p2, p1, map, r, types);
							recordMatchData(outDir, data);
						}

					}
				}
			}
		}
	}

	private String getFileName(AI p1, AI p2, String map, String outDir){
		//ensures outDir has a trailing slash
		if (!outDir.endsWith("/")) {
			outDir += "/";
		}

		//removes the trailing file extension from the map (usually .xml)
		File mapFile = new File(map);
		map = mapFile.getName().replaceFirst("[.][^.]+$", "");

		return outDir + p1 + "_" + p2 + "_" + map + ".csv";
	}

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
        
        //MAXCYCLES = 10; //uncomment to run super short matches
        
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

	    // game over, record post-game data. TODO create a method in MatchData to record this
	    data.winner = gs.winner();
	    data.frames = gs.getTime();
	    data.duration = matchDuration;
	    data.finalScore = new SimpleSqrtEvaluationFunction3().evaluate(0, 1, gs);

	    
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
		
		String fileName = getFileName(data.getP1(), data.getP2(), data.getMap(), outDir);
        File output = new File(fileName);
        
        System.out.println("Output file: " + output.getAbsolutePath());
        
        if(!output.exists()){ // creates a new file and writes the header
    		System.out.println("Output file didn't exist, creating and writing header");
    		FileWriter newFile;
			try {
				newFile = new FileWriter(output, false); //test if the file exists first, because it creates the file upon instantiation
				newFile.write("#winner,frames,duration,totalTime1,minTime1,maxTime1,avgTime1,totalTimeAI2,minTime2,maxTime2,avgTime2,finalScore\n");
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
			// field order: winner,frames,duration(s),totalTimeAI1,minTime1,maxTime1,avgTime1,totalTimeAI2,minTime2,maxTime2,avgTime2,finalScore
			appender.write(String.format( Locale.US, //ensure dot as a decimal separator
				"%d,%d,%d," //winner,frames,duration
				+ "%d,%d,%d,%f," //total1,min1,max1,avg1
				+ "%d,%d,%d,%f,%f%n", //total2,min2,max2,avg2,finalScore line break

				data.winner, data.frames, data.duration.getSeconds(), 
				data.totalTimeP1, data.minTimeP1, data.maxTimeP1, data.avgTimeP1,
				data.totalTimeP2, data.minTimeP2, data.maxTimeP2, data.avgTimeP2,
				data.finalScore
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
