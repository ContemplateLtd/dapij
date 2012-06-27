
import dapij.ObjectCreationStats;
import java.util.TreeSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class ObjCrRun {
    
    public static void main(String args[]) {
        ObjCrTest testOne = null ;
        
        //if(testOne == null)
        System.out.println(testOne);
        //= new ObjCrTest();
        //testOne._info = new ObjectCreationStats(TreeSet.class, "argMethod", 2, 3);
        testOne = new ObjCrTest();
        System.out.println(testOne.toString());
        
    }
}
