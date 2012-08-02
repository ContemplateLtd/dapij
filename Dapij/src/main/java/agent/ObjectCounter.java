/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import java.lang.reflect.Modifier;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class ObjectCounter {
    
    /*
     * A simple class used for assigning unique identifiers to objects
     */
    private static int nextObjectID = 0;
    
    public static int getNextID() {
        return nextObjectID++;
    }
    
    public static void assignId(Object ref) {
        try {
            ref.getClass().getField("__DAPIJ_ID").setInt(ref, getNextID());
        } catch(Exception ex) {
            IDMap.INSTANCE.put(ref, getNextID());
        }
    }
    
    public static int getId(Object ref) {
        
        /* Returns the object ID, or -1 in case of failure */
        int objId;
        
        if((ref.getClass().getModifiers() & Modifier.PUBLIC)==0) {
             System.out.println("Can't access object: " + ref);
        }
        
        try {
            objId = ref.getClass().getField("__DAPIJ_ID").getInt(ref);
        } catch(Exception ex) {
            objId = IDMap.INSTANCE.get(ref);
        }
        
        return objId;
    }
}