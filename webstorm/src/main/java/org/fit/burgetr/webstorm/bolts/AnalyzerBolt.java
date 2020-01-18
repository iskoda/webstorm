/**
 * AnalyzerBolt.java
 *
 * Created on 4. 3. 2014, 11:42:04 by burgetr
 */
package org.fit.burgetr.webstorm.bolts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.burgetr.segm.Segmentator;
import org.burgetr.segm.tagging.taggers.PersonsTagger;
import org.burgetr.segm.tagging.taggers.Tagger;
import org.fit.burgetr.webstorm.util.LogicalTagLookup;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.storm.shade.org.apache.commons.lang.exception.ExceptionUtils;
//import org.apache.commons.lang.exception.ExceptionUtils;
//import cz.vutbr.fit.monitoring.Monitoring;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 * A bolt that analyzes a web page and emits the discovered name-keyword and name-image relationships.
 * Accepts: (title, base_url, html_code, extracted_images, tuple_uuid )
 * Emits: (name, keyword, base_url)+
 *        (name, image_url, base_url, image_data, tuple_uuid)+
 * @author burgetr, ikouril, iskoda
 */
public class AnalyzerBolt implements IRichBolt
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AnalyzerBolt.class);
    
    private String webstormId;
    
    private OutputCollector collector;
    private String kwStreamId;
    private String imgStreamId;
    //private Monitoring monitor;
    private String hostname;
    /**
     * Creates a new AnalyzerBolt.
     * @param kwStreamId the identifier of the name-keyword output stream
     * @param imgStreamId the identifier of the name-image output stream 
     * @param uuid the identifier of actual deployment
     * @throws SQLException 
     * @throws UnknownHostException 
     */
    public AnalyzerBolt(String kwStreamId, String imgStreamId,String uuid) throws SQLException
    {
        this.kwStreamId = kwStreamId;
        this.imgStreamId = imgStreamId;
        webstormId=uuid;
        //monitor=new Monitoring(webstormId,"knot28.fit.vutbr.cz","webstorm","webstormdb88pass","webstorm");
    }

    @SuppressWarnings("rawtypes")
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

    public void execute(Tuple input)
    {
    	    long startTime = System.nanoTime();
    	    
    		String baseurl = input.getString(1);
	        String html = input.getString(2);
	        
	        @SuppressWarnings("unchecked")
			HashMap<String,byte[]> allImg = (HashMap<String, byte[]>) input.getValue(3);
	        String uuid=input.getString(4);
	        DateTime now = DateTime.now();
	        String dateString=String.valueOf(now.getYear())+"-"+String.valueOf(now.getMonthOfYear())+"-"+String.valueOf(now.getDayOfMonth())+"-"+String.valueOf(now.getHourOfDay())+"-"+String.valueOf(now.getMinuteOfHour())+"-"+String.valueOf(now.getSecondOfMinute())+"-"+String.valueOf(now.getMillisOfSecond());
	        log.info("DateTime:"+dateString+", Analyzing url: " + baseurl+" ("+uuid+")");
	        
	        //
	        // Truncate too long HTMLs - as it probably breaks processing sometimes?? - the problem is with great amount of memory required
	        //
	        int maxLength = 120000;
	        if (html != null && html.length() > maxLength) {
	        	html = html.substring(0, maxLength);
		        log.warn("DateTime:"+dateString+", Analyzing url: " + baseurl+" ("+uuid+"), truncated HTML from "+Integer.toString(html.length()));
	        }
	        
	        // Start all analyzing stuff
	        try
	        {
	            LogicalTagLookup lookup = processUrl(html, new URL(baseurl));
	            Map<String, Set<String>> keywords = extractKeywords(lookup);
	            Map<String, Set<URL>> images = extractImages(lookup);
	            if (images!=null && keywords != null)
	            {
	                //emit name-keyword tuples
	                for (Map.Entry<String, Set<String>> entry : keywords.entrySet())
	                {
	                    String name = entry.getKey();
	                    for (String keyword : entry.getValue())
	                    {
	                        if (!keyword.equals(name)){
//	                        	try {
//									//monitor.MonitorTuple("AnalyzerBolt", uuid,1, hostname);
//								} catch (SQLException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
	                            collector.emit(kwStreamId, new Values(name, keyword, baseurl));
	                        }
	                    }
	                }
	                //emit name-image tuples
	                
	                for (Map.Entry<String, Set<URL>> entry : images.entrySet())
	                {
	                    String name = entry.getKey();
	                    for (URL url : entry.getValue())
	                    {
	                    	URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	                        String canonical = uri.toString();
	                        byte[] image_data=allImg.get(canonical);
	                        
	                        if (image_data!=null){
	                        	collector.emit(imgStreamId, new Values(name, url.toString(), image_data,uuid));
	                        }
	                    }
	                }
	                
	                Long estimatedTime = System.nanoTime() - startTime;
//	                try {
//	                	//monitor.MonitorTuple("AnalyzerBolt", uuid,1, hostname, estimatedTime);
//	                } catch (SQLException e) {
//	                	// TODO Auto-generated catch block
//	                	e.printStackTrace();
//	                }
	                
	                collector.ack(input);
	            }
	            else {
	            	log.error("Tuple failed - no images and no keywords.");
	                collector.fail(input);
	            }
	        }
	        catch (NullPointerException e)
	        {
	        	// Do not log this as it usually means that there was no data passed from Segmentator
	        	//log.error("Tuple failed: NullPointerException");
	        	//log.error("Tuple failed: " + e + " | " + e.getMessage() + " | " + ExceptionUtils.getStackTrace(e));
	        }
	        catch (MalformedURLException e)
	        {
	        	//log.error("Tuple failed: malformed URL.");
	            collector.fail(input);
	        } catch (URISyntaxException e) {
	        	//log.error("Tuple failed: URI syntax error.");
	        	collector.fail(input);
			}
	        catch (Exception e) {
	        	// We catch here the rest of exceptions so the bolt does not fail and restart
	        	log.error("Tuple failed: " + e + " | " + e.getMessage());
	        	//log.error("Tuple failed: " + e + " | " + e.getMessage() + " | " + ExceptionUtils.getStackTrace(e));
	        	collector.fail(input);
	        }
        
    }

    public void cleanup()
    {
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declareStream(kwStreamId, new Fields("name", "keyword", "baseurl"));
        declarer.declareStream(imgStreamId, new Fields("name", "image_url", "image_bytes","uuid"));
    }

    public Map<String, Object> getComponentConfiguration()
    {
        return null;
    }
    
    //===========================================================================================
    
    /**
     * Processes url to extract tags
     * @param html the incoming html page
     * @param baseurl the url of incoming page
     * @return LogicalTagLookup
     */
    private LogicalTagLookup processUrl(String html, URL baseurl)
    {
        try
        {
            InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));
            Segmentator segm = new Segmentator();
            segm.segmentInputStream(is, baseurl);
            LogicalTagLookup lookup = new LogicalTagLookup(segm.getLogicalTree());
            log.info("Lookup found: " + lookup);
            return lookup;
        } 
        catch (NullPointerException e) {
        	// Skip this log as it happens often - why??
        	//log.error("Segmentator NullPointerException: " + e.getMessage() + " | " + ExceptionUtils.getStackTrace(e));
        	
            return null;
        }
        catch (Throwable e)
        {
            //e.printStackTrace();
            log.error("Throwable: " + e.getMessage() + " | " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    /**
     * Extracts keywords from LogicalTagLookup
     * @param lookup the LogicalTaglookup object
     * @return the map of name and related keywords to them
     */
    private Map<String, Set<String>> extractKeywords(LogicalTagLookup lookup)
    {
        Tagger p = new PersonsTagger(1);
        Map<String, List<String>> related = lookup.findRelatedText(p);
        Map<String, Set<String>> keywords = lookup.extractRelatedKeywords(related);
        return keywords;
    }
    
    /**
     * Extracts image from LogicalTagLookup
     * @param lookup the LogicalTaglookup object
     * @return the map of name and related picture urls
     */
    private Map<String, Set<URL>> extractImages(LogicalTagLookup lookup)
    {
        Tagger p = new PersonsTagger(1);
        return lookup.extractRelatedImages(p);
    }
    
}