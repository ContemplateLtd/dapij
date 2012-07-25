/*
 * TODO: doc comment
 */
package testutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * A parent class providing a miniature test framework for testing
 * code that uses an agent to perform instrumentation.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TransformerTest {

    protected ClassLoader cl;   /* set to a new instance for each test mwthod */
    private static HashMap<String, PkgLdPolicy> loadPolicy =
            TestClassLoader.genLdPolicyByPkg();
    
    /**
     * Resets the class loader field to provide a new one for each test
     * resulting in a new test environment per test method.
     */
    // TODO: Test if cl is different for each test when they run concurrently.
    @org.junit.Before
    public void setupPerTestCl() {
        cl = new TestClassLoader(loadPolicy);   /* A new cl for each test */
    }
    
    protected void runtimeSetup(String name) {
        /* Load given class throug the newly created cl */
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(name);
        } catch (ClassNotFoundException e) {
            System.out.println("could not load class >>>"); // TODO: remove
            throw new RuntimeException(e);
        }
        
        /* Create an instance of the given class */
        Runnable rInst = null;
        try {
            rInst = (Runnable) clazz.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        rInst.run();
    }
    
    /**
     * TODO: Is type safe?
     * Provides an convenient way to config a per-test-method runtime setup
     * by loading a Runnable implementation into the newly created classloader
     * for the test (created by setupPerTestCl). Achieved by creating an
     * instance and executing its run using reflection.
     * @param r the Runnable implementation. Can be an anonymous inner class.
     */
    protected <T extends Object> T runtimeSetup(Callable<T> clbl) {
        try {
            /* Load given class throug the cl. */
            Class<?> clazz = cl.loadClass(clbl.getClass().getName());
            
            /* Get (default) constructor. */
            Constructor cnstr = clazz.getDeclaredConstructors()[0];
            cnstr.setAccessible(true);
            
            if (cnstr.getParameterTypes().length > 1) {
                throw new RuntimeException("Test inner callables do not " +
                        "allow local variable argument passing due to " +
                        "type incompatibility between classloaders.");
            /*
             TODO: recreate newClbl instance with arguments from clbl
            ArrayList<Object> args = new ArrayList<Object>();
            for (int i = 0; i < cnstr.getParameterTypes().length; i++) {
                args.add(null);
            }
            // Set instance fields
            System.out.println("FIELDS:");
            for (Field f: newClbl.getClass().getDeclaredFields()) {
                System.out.print(f.getName()+", ");
                
                Field newF = clazz.getField(f.getName());
                Class<?> type = f.getType();
                newF.set(newClbl, type.cast(f.get(clbl)));
            }
            System.out.println();
            */
            }
            
            /* Create new instance passing null for the outer object arg. */
            Callable newClbl = (Callable) cnstr.newInstance(new Object[]{null});
            
            return (T) newClbl.call();  /* Execute setup. */
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }
}
