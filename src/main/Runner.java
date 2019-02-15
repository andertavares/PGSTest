package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import ai.core.AI;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

public class Runner {
	public  void headlessMatch(AI ai1, AI ai2, String map, UnitTypeTable types) throws Exception{
		ArrayList<String> log = new ArrayList<>();
		Duration duracao;
		Instant timeInicial = Instant.now();
		
		PhysicalGameState physicalGameState = null;
		try {
			physicalGameState = PhysicalGameState.load(map, types);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

        GameState gs = new GameState(physicalGameState, types);
        int MAXCYCLES = 20000;
        int PERIOD = 20;
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
        
        /*
        Variáveis para coleta de tempo
	     */
	    double ai1TempoMin = 9999, ai1TempoMax = -9999;
	    double ai2TempoMin = 9999, ai2TempoMax = -9999;
	    double sumAi1 = 0, sumAi2 = 0;
	    int totalAction = 0;
	
	    log.add("---------AIs---------");
	    log.add("AI 1 = " + ai1.toString());
	    log.add("AI 2 = " + ai2.toString() + "\n");
	
	    log.add("---------Map---------");
	    log.add("Map= " + map + "\n");
	
	    long startTime;
	    long timeTemp;
	    long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
	    do {
	        if (System.currentTimeMillis() >= nextTimeToUpdate) {
	            totalAction++;
	            startTime = System.currentTimeMillis();
	
	            PlayerAction pa1 = ai1.getAction(0, gs);
	            //dados de tempo ai1
	            timeTemp = (System.currentTimeMillis() - startTime);
	            sumAi1 += timeTemp;
	            //coleto tempo mínimo
	            if (ai1TempoMin > timeTemp) {
	                ai1TempoMin = timeTemp;
	            }
	            //coleto tempo maximo
	            if (ai1TempoMax < timeTemp) {
	                ai1TempoMax = timeTemp;
	            }
	
	            startTime = System.currentTimeMillis();
	            PlayerAction pa2 = ai2.getAction(1, gs);
	            //dados de tempo ai2
	            timeTemp = (System.currentTimeMillis() - startTime);
	            sumAi2 += timeTemp;
	            //coleto tempo mínimo
	            if (ai2TempoMin > timeTemp) {
	                ai2TempoMin = timeTemp;
	            }
	            //coleto tempo maximo
	            if (ai2TempoMax < timeTemp) {
	                ai2TempoMax = timeTemp;
	            }
	
	            gs.issueSafe(pa1);
	            gs.issueSafe(pa2);
	
	            // simulate:
	            gameover = gs.cycle();
	            nextTimeToUpdate += PERIOD;
	        } else {
	            try {
	                Thread.sleep(1);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        //avaliacao de tempo
	        duracao = Duration.between(timeInicial, Instant.now());
	
	    } while (!gameover && (gs.getTime() < 5000) && (duracao.toMinutes() < 7));
	
	    log.add("Total de actions= " + totalAction + " sumAi1= " + sumAi1 + " sumAi2= " + sumAi2 + "\n");
	
	    log.add("Tempos de AI 1 = " + ai1.toString());
	    log.add("Tempo minimo= " + ai1TempoMin + " Tempo maximo= " + ai1TempoMax + " Tempo medio= " + (sumAi1 / (long) totalAction));
	
	    log.add("Tempos de AI 2 = " + ai2.toString());
	    log.add("Tempo minimo= " + ai2TempoMin + " Tempo maximo= " + ai2TempoMax + " Tempo medio= " + (sumAi2 / (long) totalAction) + "\n");
	
	    log.add("Winner " + Integer.toString(gs.winner()));
	    log.add("Game Over");
	
	    if (gs.winner() == -1) {
	        System.out.println("Empate!" + ai1.toString() + " vs " + ai2.toString() + " Max Cycles =" + MAXCYCLES + " Time:" + duracao.toMinutes());
	    }
	
	    recordLog(log, ai1.toString(), ai2.toString(), map, ".");
	    //System.exit(0);
    
	}
	
	private void recordLog(ArrayList<String> log, String sIA1, String sIA2, String sMap, String pathLog) throws IOException {
        if (!pathLog.endsWith("/")) {
            pathLog += "/";
        }
        String nameArquivo = pathLog + "match_" + sIA1 + "_" + sIA2 + "_" + sMap + "_" + ".scv";
        File arqLog = new File(nameArquivo);
        if (!arqLog.exists()) {
            arqLog.createNewFile();
        }
        //abre o arquivo e grava o log
        try {
            FileWriter arq = new FileWriter(arqLog, false);
            PrintWriter gravarArq = new PrintWriter(arq);
            for (String l : log) {
                gravarArq.println(l);
            }

            gravarArq.flush();
            gravarArq.close();
            arq.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
