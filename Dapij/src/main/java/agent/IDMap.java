/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import com.google.common.collect.MapMaker;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class IDMap {
    public static final  IDMap INSTANCE = new IDMap();
    
    /*
     * A map storing IDs of objects whose classes could not be modified
     */
    private ConcurrentMap<Object, Integer> idMap;
    
    private IDMap() {
        idMap = new MapMaker().weakKeys().makeMap();
    }
    
    public void put(Object key, int id) {
        
        /* Store in the concurrent map. */
        idMap.putIfAbsent(key, new Integer(id));
    }
    
    public int get(Object key) {
        /* Get the ID of the given object stored in the map */
        if(idMap.containsKey(key)) {
            return idMap.get(key).intValue();
        }
        
        /* If object not found in the map- return -1 */
        System.out.println("Lookup failed: " + key);
        return -1;
    }
}
