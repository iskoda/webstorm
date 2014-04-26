/**
 * AnalyzerBolt.java
 *
 * Created on 4. 3. 2014, 11:42:04 by burgetr
 */
package org.fit.burgetr.webstorm.bolts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * A bolt that analyzes a web page and emits the discovered name-keyword and name-image relationships.
 * Accepts: (title, base_url, html_code)
 * Emits: (name, keyword, base_url)+
 *        (name, image_url, base_url)+
 * @author burgetr
 */
public class AnalyzerBolt implements IRichBolt
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AnalyzerBolt.class);
    
    private OutputCollector collector;
    private String kwStreamId;
    private String imgStreamId;
    
    /**
     * Creates a new AnalyzerBolt.
     * @param kwStreamId the identifier of the name-keyword output stream
     * @param imgStreamId the identifier of the name-image output stream 
     */
    public AnalyzerBolt(String kwStreamId, String imgStreamId)
    {
        this.kwStreamId = kwStreamId;
        this.imgStreamId = imgStreamId;
    }

    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector)
    {
        this.collector = collector;
    }

    public void execute(Tuple input)
    {
	        String baseurl = input.getString(1);
	        String html = input.getString(2);
	        HashMap<String,byte[]> allImg = (HashMap<String, byte[]>) input.getValue(3);
	        String uuid=input.getString(4);
	        DateTime now = DateTime.now();
	        String dateString=String.valueOf(now.getYear())+"-"+String.valueOf(now.getMonthOfYear())+"-"+String.valueOf(now.getDayOfMonth())+"-"+String.valueOf(now.getHourOfDay())+"-"+String.valueOf(now.getMinuteOfHour())+"-"+String.valueOf(now.getSecondOfMinute())+"-"+String.valueOf(now.getMillisOfSecond());
	        log.info("DateTime:"+dateString+", Analyzing url: " + baseurl+" ("+uuid+")");
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
	                        if (!keyword.equals(name))
	                            collector.emit(kwStreamId, new Values(name, keyword, baseurl));
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
	                        
	                        if (image_data!=null)
	                        	collector.emit(imgStreamId, new Values(name, url.toString(), image_data,uuid));
	                    }
	                }
	                collector.ack(input);
	            }
	            else
	                collector.fail(input);
	        }
	        catch (MalformedURLException e)
	        {
	            collector.fail(input);
	        } catch (URISyntaxException e) {
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
    
    private LogicalTagLookup processUrl(String html, URL baseurl)
    {
        try
        {
            InputStream is = new ByteArrayInputStream(html.getBytes("UTF-8"));
            Segmentator segm = new Segmentator();
            segm.segmentInputStream(is, baseurl);
            LogicalTagLookup lookup = new LogicalTagLookup(segm.getLogicalTree());
            return lookup;
        } catch (Exception e)
        {
            //e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }
    }

    private Map<String, Set<String>> extractKeywords(LogicalTagLookup lookup)
    {
        Tagger p = new PersonsTagger(1);
        Map<String, List<String>> related = lookup.findRelatedText(p);
        Map<String, Set<String>> keywords = lookup.extractRelatedKeywords(related);
        return keywords;
    }
    
    private Map<String, Set<URL>> extractImages(LogicalTagLookup lookup)
    {
        Tagger p = new PersonsTagger(1);
        return lookup.extractRelatedImages(p);
    }
    
}