package org.fit.burgetr.webstorm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Helper methods to realize storage of crawled data in order to 
 * replay exactly same datasets for experiments. 
 * 
 * @author iskoda
 *
 */
public class StoreForReplayExperiments {
	
	public Long timeStart;
	public Long timeEnd;
	
	// The root URL from which we get dataset data. Dataset contains only files named by URL's hash
	// these file are then retrieved when particular URL is requested
	public String replayServerBase = "http://knot38.fit.vutbr.cz/webstorm-downloaded/"; // Should end with '/' 
		
	/**
	 * Construct
	 */
	public void StoreForReplayExperiments()
	{
		timeStart = (long) 0;
		timeEnd = (long) 0;
	}
	
	/**
     * Saves data from URL to be later replayed for performance experiments.
     * 
     * Writes data to DB log file in tab delimited format:
     * url	filename	downloadtimeInMs
     * 
     */
    public void saveForReplay(String url, byte[] data) throws IOException
    {
    	// Switch off saving for replay
    	if(true) {
    		//return;
    	}
    	
    	String dataDir = "/tmp/webstorm-downloaded/"; // Should end with '/'
    	String dbFile = dataDir + "_db-file.txt";
    	
    	//String filename = url.replace('/', '-').replace('\\', '-').replace(':', '-').replace('?', '-');
    	// Use just hash as filename
    	String filename = (new Integer(url.hashCode())).toString();
    	
    	// Write downloaded file to disk so we can simulate replays of data for benchmarking
        FileOutputStream fileStream = new FileOutputStream(
    		new File(dataDir + filename)
		);
        fileStream.write(data);
        fileStream.close();
        
        int timeInMilis = (int)((timeEnd - timeStart));
     
        // Write log to DB file
        Writer db = new BufferedWriter(new FileWriter(dbFile, true));
        db.append(url + "\t" + filename + "\t" + timeInMilis + "\n");
        db.close();
    }
    
    /**
     * Called when download started to measure the time taken to get data.
     */
    public void downloadStarted() {
    	timeStart = System.currentTimeMillis();
    }
    
    /**
     * Called when download ended to measure the time taken to get data.
     */
    public void downloadEnded() {
    	timeEnd = System.currentTimeMillis();
    }
    
    /**
     * Transforms URL to URL for replaying from stored dataset. 
     */
    public String transformUrlToReplay(String url) {
    	return replayServerBase + url.hashCode();
    }
}
