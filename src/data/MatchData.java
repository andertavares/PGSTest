package data;

import java.io.File;
import java.time.Duration;

import ai.core.AI;


/**
 * Stores data about a match.
 * 
 * All fields are public for easy access, except for the map. 
 * The map can be initialized to the path but is treated to return the name if necessary.
 * 
 * @author anderson
 *
 */
public class MatchData {
	
	/**
	 * Winner of the match. 
	 * Player 1: 0
	 * Player 2: 1
	 * Draw or timeout: -1
	 */
	public int winner;
	
	/**
	 * First player
	 */
	private AI p1;
	
	/**
	 * Second player
	 */
	private AI p2;
	
	/**
	 * Path to the map
	 */
	private String mapFileName;
	
	/**
	 * Total number of frames played
	 */
	public long frames;
	
	// times, in milliseconds spent by first AI's reasoning: total, minimum, maximum and average 
	public int totalTimeP1, minTimeP1, maxTimeP1; 
	public float avgTimeP1;
	
	// times, in milliseconds spent by first AI's reasoning: total, minimum, maximum and average
	public int totalTimeP2, minTimeP2, maxTimeP2; 
	public float avgTimeP2;
	
	/**
	 * Wall-clock time elapsed for the match
	 */
	public Duration duration;
	
	/**
	 * Initializes with the static match data.
	 * All other fields must be directly assigned. 
	 * @param p1
	 * @param p2
	 * @param map
	 */
	public MatchData(AI p1, AI p2, String map){ 
		this.p1 = p1;
		this.p2 = p2;
		this.mapFileName = map;
	} 
	
	/**
	 * Returns the map name, without the complete path and file extension
	 * @return
	 */
	public String getMapName(){
		File mapFile = new File(mapFileName);
        return mapFile.getName().replaceFirst("[.][^.]+$", ""); //removes the trailing file extension
	}
	
	/**
	 * Return the name of the first player
	 * @return
	 */
	public String getP1Name(){
		return p1.toString();
	}
	
	/**
	 * Return the name of the second player
	 * @return
	 */
	public String getP2Name(){
		return p2.toString();
	}
	
	/**
	 * Returns the (possibly complete) path to the map file
	 * @return
	 */
	public String getMap(){
		return mapFileName;
	}
	
	

}
