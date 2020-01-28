package org.fit.burgetr.webstorm.util;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;

/**
 * This is just a stub in case we need to add some metrics to be monitored.
 * The strategy should be based on http://storm.apache.org/releases/1.2.3/metrics_v2.html (Tuple Counter Bolt - example)  
 * 
 * Bolts to be monitored should then extend this abstract class, the execute() method could be implemented here and
 * user would then implement some inner class called from execute() (e.g. executeMeasured())
 * 
 * @author pecas
 *
 */
public abstract class AbstractDetailedMetricsBolt implements IRichBolt {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -6800612369473614294L;

	@Override
	@SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector)
    {
		
        
    }
}
