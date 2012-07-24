/*
 * TODO: doc comment
 */
package agent;

import com.google.common.collect.MapMaker;
import comms.AgentEventServer;
import comms.CommsProto;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import transform.EventRegister;
import transform.InstanceCreationStats;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class RuntimeEventRegister implements EventRegister {
    
    public static final  RuntimeEventRegister INSTANCE = 
            new RuntimeEventRegister();
    /**
     * A weak concurrent hash map that registers statistics for created
     * object instances.
     */
    private ConcurrentMap<Object, InstanceCreationStats> instanceMap;
    
    private RuntimeEventRegister() {
        instanceMap = new MapMaker().weakKeys().makeMap();
    }
    
    public InstanceCreationStats get(Object key) {
        return instanceMap.get(key);
    }
    
    @Override
    public void regCreation(Object key, Class clazz, String method, int offset,
            long threadId) {
        InstanceCreationStats stats = new InstanceCreationStats(clazz, method,
                offset, threadId);
        
        /* Send message to client if event server started */
        AgentEventServer es = Settings.INSTANCE.getEventServer();
        if (es != null) {
            es.sendEvent(CommsProto.constrAccEventMsg(stats.toString()));
        }
        
        /* Store in concurrent map. */
        instanceMap.putIfAbsent(key, stats);
    }
  
    public boolean hasKey(Object key) {
        return instanceMap.containsKey(key);
    }
  
    public int getSize() {
        return instanceMap.size();
    }
    
    public Collection<InstanceCreationStats> getValues() {
        return instanceMap.values();
    }
    
    /**
     * Writes the content of the instance map to an XML file
     */
    public void writeXmlSnapshot(String xmlLogFilePath, Breakpoint b)
            throws IOException {
        File f = new File(xmlLogFilePath);
        XMLWriter.snapshotToXml(f, b);
    }
    
    @Override
    public void regAccess(Object ref, long threadId) {
        // dummy implementation for now
        String msg = "Object " + ref + " accessed from thread " + threadId;
        
        /* Send message to client if event server started */
        AgentEventServer aes = Settings.INSTANCE.getEventServer();
        if (aes != null) {
            aes.sendEvent(CommsProto.constrAccEventMsg(msg));
        }
    }
    
    @Override
    public void regBreakpt() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
