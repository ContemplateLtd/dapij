/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

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
}
