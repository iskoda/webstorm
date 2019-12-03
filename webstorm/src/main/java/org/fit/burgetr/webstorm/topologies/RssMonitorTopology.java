/**
 * RssMonitorTopology.java
 *
 * Created on 28. 2. 2014, 13:24:53 by burgetr
 */
package org.fit.burgetr.webstorm.topologies;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

import org.fit.burgetr.webstorm.bolts.AnalyzerBolt;
import org.fit.burgetr.webstorm.bolts.DownloaderBolt;
import org.fit.burgetr.webstorm.bolts.ExtractFeaturesBolt;
import org.fit.burgetr.webstorm.bolts.FeedReaderBolt;
import org.fit.burgetr.webstorm.bolts.IndexBolt;
import org.fit.burgetr.webstorm.spouts.FeedURLSpout;
//import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public class RssMonitorTopology
{
	private static final Logger log = LoggerFactory.getLogger(RssMonitorTopology.class);

	/**
	 * 
	 * 
	 * Note that FeedURLSpout requires URL to list of downloadable RSS feeds!
	 * Currently we use private list that is static, so for experiments comparing performance we always
	 * download same stuff.
	 * 
	 * @param args
	 * @throws SQLException
	 * @throws UnknownHostException
	 */
    public static void main(String[] args) throws SQLException, UnknownHostException
    {
        //logging status
        //Logger logger = LoggerFactory.getLogger(RssMonitorTopology.class);
        //logger.debug("TOPOLOGY START");
        //LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        //StatusPrinter.print(lc);
        
        String uuid=UUID.randomUUID().toString();
        
        log.info("Deployment id: "+uuid);
        
        //create spouts and bolt
        //FeedURLSpout urlSpout = new FeedURLSpout("http://www.fit.vutbr.cz/~burgetr/public/rss.txt",uuid);
        //FeedURLSpout urlSpout = new FeedURLSpout("https://www.adaptine.com/_iskoda_webstorm/rss.txt",uuid);
        FeedURLSpout urlSpout = new FeedURLSpout("https://www.adaptine.com/_iskoda_webstorm/rss-live.txt",uuid);
        FeedReaderBolt reader = new FeedReaderBolt(uuid);
        DownloaderBolt downloader = new DownloaderBolt(uuid);
        AnalyzerBolt analyzer = new AnalyzerBolt("kw","img",uuid);
        ExtractFeaturesBolt extractor = new ExtractFeaturesBolt(uuid);
        IndexBolt indexer=new IndexBolt(uuid);
        //NKStoreBolt nkstore = new NKStoreBolt();
        
        //create the topology
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("url_spout", urlSpout);
        builder.setBolt("reader", reader).shuffleGrouping("url_spout");
        builder.setBolt("downloader", downloader, 1).shuffleGrouping("reader");
        builder.setBolt("analyzer", analyzer, 1).shuffleGrouping("downloader");
        //builder.setBolt("extractor", extractor,1).globalGrouping("analyzer", "img");
        //builder.setBolt("indexer", indexer,1).shuffleGrouping("extractor");
        
        //builder.setBolt("nkstore", nkstore, 1).globalGrouping("analyzer", "kw");

        Config conf = new Config();
        //conf.setDebug(true); // Enabling this, you get all transferred messages to be logged

        final LocalCluster cluster = new LocalCluster();
        
        /*Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Shutting down");
                cluster.shutdown();
            }
        });*/
        
        cluster.submitTopology("webstorm", conf, builder.createTopology());
    }

}
