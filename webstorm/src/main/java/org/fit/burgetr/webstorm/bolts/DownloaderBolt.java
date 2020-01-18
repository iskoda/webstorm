/**
 * DownloaderBolt.java
 *
 * Created on 3. 3. 2014, 13:39:59 by burgetr
 */
package org.fit.burgetr.webstorm.bolts;

import org.fit.burgetr.webstorm.util.StoreForReplayExperiments;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.apache.storm.shade.org.apache.commons.lang.exception.ExceptionUtils;
//import cz.vutbr.fit.monitoring.Monitoring;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * A bolt that downloads a HTML, the corresponding images
 * Accepts: (page_url, title, tuple_uuid)
 * Emits: (title, base_url, html_code, images, tuple_uuid)
 * 
 * @author burgetr and ikouril
 */
public class DownloaderBolt implements IRichBolt
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DownloaderBolt.class);
    private OutputCollector collector;
    private String webstormId;
    //private Monitoring monitor;
    private String hostname;
    
    /**
     * Creates a new DownloaderBolt.
     * @param uuid the identifier of actual deployment
     * @throws SQLException 
     * @throws UnknownHostException 
     */
    public DownloaderBolt(String uuid) throws SQLException {
    	webstormId=uuid;
    	//monitor=new Monitoring(webstormId,"knot28.fit.vutbr.cz","webstorm","webstormdb88pass","webstorm");
    }
    
    
    @SuppressWarnings("rawtypes")
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector)
    {
        this.collector = collector;
        
        // Set the correct hostname
        try{
			hostname=InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e){
			hostname="-unknown-";
		}
    } 
    
    /**
     * Downloads url to array of bytes
     * @param toDownload the url of page to be downloaded
     * @throws IOException 
     */
    private byte[] downloadUrl(URL toDownload) throws IOException 
    {
    	StoreForReplayExperiments replayStore = new StoreForReplayExperiments();
    	
    	// Transfor url to be get from replay server
    	String urlstring = toDownload.toString();
        urlstring = replayStore.transformUrlToReplay(urlstring);
        toDownload = new URL(urlstring);
    	
    	//replayStore.downloadStarted();
    	
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int bytesRead;
        InputStream stream = toDownload.openStream();

        while ((bytesRead = stream.read(chunk)) > 0) {
            outputStream.write(chunk, 0, bytesRead);
        }

        // Save data for later replay in experiments
        //replayStore.downloadEnded();
        //replayStore.saveForReplay(toDownload.toString(), outputStream.toByteArray());
        
        return outputStream.toByteArray();
    }

    @Override
    public void execute(Tuple input)
    {
    	long startTime = System.nanoTime();
    	
    	StoreForReplayExperiments replayStore = new StoreForReplayExperiments();
    	
    	String originalUrl;
    	String urlstring = originalUrl = input.getString(0);
        String title = input.getString(1);
        String uuid = input.getString(2);
        
        DateTime now = DateTime.now();
        String dateString=String.valueOf(now.getYear())+"-"+String.valueOf(now.getMonthOfYear())+"-"+String.valueOf(now.getDayOfMonth())+"-"+String.valueOf(now.getHourOfDay())+"-"+String.valueOf(now.getMinuteOfHour())+"-"+String.valueOf(now.getSecondOfMinute())+"-"+String.valueOf(now.getMillisOfSecond());
        //log.info("DateTime:"+dateString+", Downloading url: " + urlstring+" ("+uuid+")");
        
        try
        {
        	/*
            StyleImport si = new StyleImport(urlstring);
            StringWriter os = new StringWriter();
            si.dumpTo(new PrintWriter(os));
            os.close();
            */
        	//StringWriter os = new StringWriter();

            HashMap<String,byte[]> allImg=new HashMap<String,byte[]>();

            // Transform url to be get from replay server
            urlstring = replayStore.transformUrlToReplay(urlstring);
            
            // Download and parse HTML document
            //replayStore.downloadStarted();
            Document document = (Document) Jsoup.connect(urlstring).get();
            //replayStore.downloadEnded();
            // Prepare map of images in the document
            Elements images = document.select("img[src~=(?i)\\.(png|jpe?g|gif)]");

            // Save data for later replay in experiments
            //replayStore.saveForReplay(urlstring, document.outerHtml().getBytes());
            
            // Iterate image tags and download 'em
            for (Element image : images) {
                String src=image.attr("src");
                try {
	                URL u = new URL(src);
	                URI uri = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(), u.getRef());
	                String canonical = uri.toString();
                
                	allImg.put(canonical, downloadUrl(u));
                }
                catch(IOException e) {
                	//log.info("Fetch image failed: " + e.getMessage() + " URL: " + u);
                }
            }
            Long estimatedTime = System.nanoTime() - startTime;
            
            //monitor.MonitorTuple("DownloaderBolt", uuid, 1,hostname, estimatedTime);
            
            collector.emit(new Values(title, urlstring, document.html(), allImg, uuid));
            collector.ack(input);
        } 
        catch (Exception e)
        {
            log.warn("Fetch error: " + e.getClass() + " " + e.getMessage() + " URL: " + urlstring);
        	//log.error("Fetch error: " + ExceptionUtils.getStackTrace(e) + " Original URL:" + originalUrl);
        	//log.error("Fetch error: " + " Original URL:" + originalUrl);
            collector.fail(input);
        }
        
    }

    @Override
    public void cleanup()
    {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("title", "base_url", "html", "images","uuid"));

    }

    @Override
    public Map<String, Object> getComponentConfiguration()
    {
        return null;
    }

}
