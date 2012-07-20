/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import com.google.common.collect.MapMaker;
import comms.CommsProto;
import comms.EventServer;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceCreationTracker {
    public static final  InstanceCreationTracker INSTANCE = 
            new InstanceCreationTracker();
    
    private ConcurrentMap<Object, InstanceCreationStats> instanceMap;
    
    private InstanceCreationTracker() {
        instanceMap = new MapMaker().weakKeys().makeMap();
    }
    
    public InstanceCreationStats get(Object key) {
        return instanceMap.get(key);
    }
    
    public void put(Object key, Class clazz, String method, int offset,
            long threadId) {
        InstanceCreationStats stats = new InstanceCreationStats(clazz, method,
                offset, threadId);
        
        /* Send message to client if event server started */
        EventServer es = Settings.INSTANCE.getEventServer();
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
    
    public void displayInfo() {
        System.out.println("Objects in use:");
        for(InstanceCreationStats info : instanceMap.values()) {
            System.out.println(info);
        }
    }
    
    /* Write the content of the map to an XML file */
    public void writeXmlSnapshot(String xmlLogFilePath, Breakpoint b)
            throws IOException {
        File f = new File(xmlLogFilePath);
        XMLWriter.snapshotToXml(f, b);
    }
    
    public void registerAccess(Object ref, long threadId) {
        //dummy implementation for now
        String msg = "Object " + ref + " accessed from thread " + threadId;
        
        /* Send message to client if event server started */
        EventServer es = Settings.INSTANCE.getEventServer();
        if (es != null) {
            es.sendEvent(CommsProto.constrAccEventMsg(msg));
        }
    }
}
