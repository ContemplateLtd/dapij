/*
 * TODO: doc comment
 */
package testutils;

import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TransformerTest {

    protected ClassLoader cl;   /* set to a new instance for each test mwthod */
    private static HashMap<String, PkgLdPolicy> loadPolicy =
            TestClassLoader.genLdPolicyByPkg();
    
    /**
     * Prepares a custom class loader for each test so that each test can have
     * a new clean environment.
     */
    // TODO: Test whether cl is different if test run concurrently.
    @org.junit.Before
    public void provideChildCL() {
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
    
    protected void runtimeSetup(Runnable r) {
        /* Load given class throug the newly created cl */
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(r.getClass().getName());
        } catch (ClassNotFoundException e) {
            System.out.println("could not load class >>>"); // TODO: remove
            throw new RuntimeException(e);
        }
        
        TransformerTest outerObj = new TransformerTest();
        Constructor constructor;
        
        try {
            System.out.println(clazz);
            System.out.println(clazz.getDeclaredConstructors());
            System.out.println(clazz.getDeclaredConstructors().length);
            constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
        try {
            /*  */
            Runnable innerObj = (Runnable) constructor.newInstance(new Object[]{null});
            innerObj.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
