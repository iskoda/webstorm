/**
 * FeedReaderBolt.java
 *
 * Created on 28. 2. 2014, 11:34:14 by burgetr
 */
package org.fit.burgetr.webstorm.bolts;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

import cz.vutbr.fit.monitoring.Monitoring;

import org.rometools.fetcher.FeedFetcher;
import org.rometools.fetcher.impl.HttpURLFeedFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.fit.burgetr.webstorm.util.StoreForReplayExperiments;

/**
 * A bolt that decodes a feed at the given URL and extracts new entries
 * Accepts: (feed_url, time_of_last_fetch)
 * Emits: (extracted_url, title, tuple_uuid)+
 * 
 * @author burgetr and ikouril
 */
public class FeedReaderBolt implements IRichBolt
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(FeedReaderBolt.class);
    private OutputCollector collector;
    private String webstormId;
    //private Monitoring monitor;
    private String hostname;
    
    
    
    /**
     * Creates a new FeedReaderBolt.
     * @param uuid the identifier of actual deployment
     * @throws SQLException 
     * @throws UnknownHostException 
     */
    public FeedReaderBolt(String uuid) throws SQLException {
    	webstormId=uuid;
    	//monitor=new Monitoring(webstormId,"knot28.fit.vutbr.cz","webstorm","webstormdb88pass","webstorm");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector)
    {
        this.collector = collector;
        try{
			hostname=InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e){
			hostname="-unknown-";
		}
    }

    @Override
    public void execute(Tuple input)
    {
    	StoreForReplayExperiments replayStore = new StoreForReplayExperiments();
    	
    	long startTime = System.nanoTime();
    	
        String urlstring = input.getString(0);
        Date date = new Date(input.getLong(1));
        String uuid=input.getString(2);
        
        //log.info("Processing RSS feed: " + urlstring + " last modified on " + date);
        
        // Count number of urls in RSS feed to eliminate empty or problematic feeds
        int emmitedFromFeed = 0;
        
        try
        {
            FeedFetcher feedFetcher = new HttpURLFeedFetcher();
            
            // Transfor url to be get from replay server
            urlstring = replayStore.transformUrlToReplay(urlstring);
            
            SyndFeed feed = feedFetcher.retrieveFeed(new URL(urlstring));
            
            /*
            //   
            // Used only to store data from real servers for replaying
            //
            replayStore.downloadStarted();
            // Read rss feed on our own as feed cannot be returned back in xml
            StringBuilder feedXmlData = new StringBuilder();
            byte[] buf = new byte[1024];
            int length;
            try (InputStream is = (new URL(urlstring)).openStream()) {
              while ((length = is.read(buf)) != -1) {
            	  feedXmlData.append(new String(buf, 0, length));
              }
            }
            replayStore.downloadEnded();
                 
            // Save data for later replay in experiments
            replayStore.saveForReplay(urlstring, feedXmlData.toString().getBytes());
            
            */
            
            List<?> entries = feed.getEntries();
            for (Object e : entries)
            {
                if (e instanceof SyndEntry)
                {
                    SyndEntry entry = (SyndEntry) e;
                    
                    // 
//                    if (date.compareTo(entry.getPublishedDate()) <= 0) // Probably limiting to new records - for testing just ignore
//                    {
                        //log.info("New entry: " + entry.getTitle() + " " + entry.getUri() + " " + entry.getPublishedDate());
                        Long estimatedTime = System.nanoTime() - startTime;
                        //monitor.MonitorTuple("FeedReaderBolt", uuid,1, hostname, estimatedTime);
                        collector.emit(new Values(entry.getUri(), entry.getTitle(),uuid));
                        emmitedFromFeed ++;
//                    }
                }
            }
            
            collector.ack(input);
        } 
        catch (Exception e)
        {
            log.error("Fetch error: " + e);
            if(emmitedFromFeed == 0) { // Print stack for useless (empty) RSS feeds
            	//e.printStackTrace();
            }
            //collector.fail(input);
        }
        finally {
        	log.info("RSS feed emmited " + emmitedFromFeed + " URLs. RSS feed: " + urlstring);
		}
        
        
        collector.ack(input);
        
    }

    @Override
    public void cleanup()
    {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("url", "title","uuid"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration()
    {
        return null;
    }

}
