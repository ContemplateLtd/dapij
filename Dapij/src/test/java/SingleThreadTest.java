/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import dapij.InstanceCreationTracker;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class SingleThreadTest {

    @Test
    public void testObjectCreationDetection() {
        HelloAzura ha = new HelloAzura(2);  /* create object to test agent */
        
        InstanceCreationTracker map = InstanceCreationTracker.INSTANCE;
        assertEquals("Size: ", 0, map.getSize());
        
        /* Does not work, classes are not instrumented yet. */
        assertEquals("Exists in map: ", true, map.hasKey(ha));
    }
}
