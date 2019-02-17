package data;

import java.io.File;
import java.time.Duration;


/**
 * Stores data about a match.
 * All fields are public for easy access, except for the map, 
 * which can be initialized to the path but is treated to return 
 * the name if necessary
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
	 * Name of the first player
	 */
	public String p1Name;
	
	/**
	 * Name of the second player
	 */
	public String p2Name;
	
	/**
	 * Path to the map
	 */
	private String mapFileName;
	
	/**
	 * Total number of frames played
	 */
	public long frames;
	
	// times, in milliseconds spent by first AI's reasoning: total, minimum, maximum and average 
	public int totalTimeAI1, minTimeAI1, maxTime1; 
	public float avgTime1;
	
	// times, in milliseconds spent by first AI's reasoning: total, minimum, maximum and average
	public int totalTimeAI2, minTime2, maxTime2; 
	public float avgTime2;
	
	/**
	 * Wall-clock time elapsed during the match
	 */
	public Duration duration;
	
	/**
	 * Does nothing, all attributes must be assigned
	 */
	public MatchData(String p1Name, String p2Name, String map){ 
		this.p1Name = p1Name;
		this.p2Name = p2Name;
		this.mapFileName = map;
	} 
	
	/**
	 * Returns a csv string with the names of the fields.
	 * The header is preceded by a # by default and finishes with a newline
	 * @return
	 */
	public static String header(){
		return "#winner,frames,duration(s),totalActions,totalTimeAI1,minTime1,maxTime1,avgTime1,totalTimeAI2,minTime2,maxTime2,avgTime2\n";
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
	 * Returns the (possibly complete) path to the map file
	 * @return
	 */
	public String getMap(){
		return mapFileName;
	}
	
	/**
	 * Returns a csv string related to this data.
	 * The order of the fields is:
	 * winner,totalActions,sumAI1,sumAI2,minTime1,maxTime1,avgTime1,minTime2,maxTime2,avgTime2
	 * as given by the {@link #header()} 
	 */
	public String toString(){
		return String.format(
			"%d,%ld,%ld,%d,%d,%d,%f,%d,%d,%d,%f",
			winner, duration.getSeconds(), frames, totalTimeAI1, minTimeAI1, maxTime1, avgTime1,
			totalTimeAI2, minTime2, maxTime2, avgTime2
		);
	}

}
