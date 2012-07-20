/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AgentTest {

    ClassLoader cl; /* reset to a new custom class loader for each test */
    
    /**
     * Prepares a custom class loader for each test so that each test can have
     * a new clean environment.
     */
    @org.junit.Before
    public void ProvideNewTestClassLoader() {
        cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return new TestClassLoader();
                }
        });
    }
}
