/*
 * TODO: doc comment
 */
package agent;

import com.google.common.collect.MapMaker;
import comms.AgentEventSrv;
import java.util.concurrent.ConcurrentMap;
import transform.CreatEvent;
import transform.CreatEventLisnr;
import transform.InstCreatStats;

/**
 * A creation event listener that maintains a concurrent identity weak hash map
 * for storing information about created objects (in a central location) during
 * execution of instrumented client code.
 * 
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstCreatTracker implements CreatEventLisnr {

    /**
     * A weak concurrent hash map that registers statistics for created
     * object instances.
     */
    private ConcurrentMap<Object, InstCreatStats> instanceMap;
  
    public InstCreatTracker(AgentEventSrv aes) {
        instanceMap = new MapMaker().weakKeys().makeMap();
    }
    
    public InstCreatTracker() {
        this(null);
    }
    
    public InstCreatStats getStats(Object key) {
        return instanceMap.get(key);
    }
    
    public boolean hasKey(Object key) {
        return instanceMap.containsKey(key);
    }
  
    public int getSize() {
        return instanceMap.size();
    }
    
    @Override
    public void handleCreationEvent(CreatEvent e) {
        instanceMap.putIfAbsent(e.getRef(), e.getStats());
    }
}
