/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import com.google.common.collect.MapMaker;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceCreationTracker {
    public final static InstanceCreationTracker INSTANCE = 
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
    //public void put(Object key, String method, int offset, long threadId) {
        instanceMap.putIfAbsent(key, new InstanceCreationStats(clazz, method,
                offset, threadId));
        System.out.println(instanceMap.get(key));
    }
  
    public boolean hasKey(Object key) {
        return instanceMap.containsKey(key);
    }
  
    public int getSize() {
        return instanceMap.size();
    }
}
