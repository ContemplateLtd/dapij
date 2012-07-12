/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import com.google.common.collect.MapMaker;
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
        instanceMap.putIfAbsent(key, new InstanceCreationStats(clazz, method,
                offset, threadId));
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
        System.out.println("Object " + ref + " accessed from thread " +
                threadId);
    }
}
