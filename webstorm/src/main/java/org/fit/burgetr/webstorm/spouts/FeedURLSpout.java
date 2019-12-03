/**
 * FeedURLSpout.java
 *
 * Created on 28. 2. 2014, 11:39:44 by burgetr
 */
package org.fit.burgetr.webstorm.spouts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import cz.vutbr.fit.monitoring.Monitoring;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.fit.burgetr.webstorm.bolts.FeedReaderBolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A spouts that reads a list of feed urls and emits the urls repeatedly together with last fetch date.
 * Emits: (url, last_fetch_date[unix_time], tuple_uuid)
 * 
 * @author burgetr and ikouril
 */
public class FeedURLSpout extends BaseRichSpout
{

    private static final long serialVersionUID = 1L;
    private String webstormId;
    private static final Logger log = LoggerFactory.getLogger(FeedReaderBolt.class);
    private SpoutOutputCollector collector;
    private Map<String, Date> urls;
    private Iterator<Entry<String, Date>> urlIterator;
    private String listSourceUrl;
    //private Monitoring monitor;
    private String hostname;
    

    /**
     * Creates a new FeedUrlSpout
     * @param uuid the identifier of actual deployment
     * @throws SQLException 
     * @throws UnknownHostException 
     */
    public FeedURLSpout(String listSourceUrl,String uuid) throws SQLException
    {
        this.listSourceUrl = listSourceUrl;
        webstormId=uuid;
        //monitor=new Monitoring(webstormId,"knot28.fit.vutbr.cz","webstorm","webstormdb88pass","webstorm");
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector)
    {
        this.collector = collector;
        
        // Set the correct hostname
        try{
			hostname=InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e){
			hostname="-unknown-";
		}
        
        loadList(listSourceUrl);
    }
    
    @Override
    public void nextTuple()
    {
        if (urlIterator == null || !urlIterator.hasNext())
        {
            if (urlIterator != null)
            {
                try
                {
                	
                    //Thread.sleep(1000); //wait 1 second in order not to repeat the whole list with the same times
                	Thread.sleep(1000 * 3600); // For downloading for replay, we use just emmit once
                } catch (InterruptedException e) {}
            }
            urlIterator = urls.entrySet().iterator();
            log.info("RSS feed list starting from begining.");
        }
        Entry<String, Date> entry = urlIterator.next();
        Date now = new Date();
        String uuid=UUID.randomUUID().toString();
//        try {
//			monitor.MonitorTuple("FeedUrlSpout", uuid,1, hostname);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        collector.emit(new Values(entry.getKey(), entry.getValue().getTime(),uuid));
        entry.setValue(now);
    }

    @Override
    public void ack(Object id)
    {
    }

    @Override
    public void fail(Object id)
    {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("url", "lastfetch","uuid"));
    }

    //===============================================================================================
    
    /**
     * Loads urls from rss feed
     * @param url string of rss feed
     */
    private void loadList(String urlstring)
    {
        try {
            URL url = new URL(urlstring);
            URLConnection con = url.openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            urls = new HashMap<String, Date>();
            
            Date initDate = new Date(0); //initial date
            String line;
            while ((line = r.readLine()) != null)
            {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#"))
                    urls.put(line, initDate); 
            }
            r.close();
            
            //fallback - empty list
            if (urls.isEmpty())
                urls.put("http://rss.cnn.com/rss/cnn_latest.rss", initDate);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
