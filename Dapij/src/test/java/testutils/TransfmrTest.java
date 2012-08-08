package testutils;

import comms.CommsProto;
import comms.TestEventClnt;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * A class that provides utilities (via subclassing) to test methods to test
 * classes for easier testing of agent code that performs instrumentation. As a
 * part of a small testing framework for testing agent code that performs
 * instrumentation, this class allows creating & customising a clean isolated
 * test environment for each test method of a test class.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TransfmrTest {

    private static HashMap<Package, PkgLdPolicy> loadPolicy =
            TestClassLoader.getPkgLoadPolicy();
    protected ClassLoader cl; /* set to a new instance for each test mwthod */

    /**
     * Resets the class loader field to provide a new one for each test
     * resulting in a new test environment per test method. TODO: Test if cl is
     * different for each test when they run concurrently.
     */
    @org.junit.Before
    public void setupPerTestCl() {
        cl = new TestClassLoader(loadPolicy); /* A new cl for each test */
    }

    protected void runtimeSetup(String name) {

        /* Load given class throug the newly created cl */
        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(name);
        } catch (ClassNotFoundException e) {
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
     * Provides an convenient way to config a per-test-method runtime setup by
     * loading a Runnable implementation into the newly created classloader for
     * the test (created by setupPerTestCl). Achieved by creating an instance
     * and executing its run using reflection.
     *
     * @param clbl
     *            the Callable implementation - usually an anonymous inner
     *            class. Pasing of local parameters not currently supported.
     */
    @SuppressWarnings("unchecked")
    protected <T extends Object> T runtimeSetup(Callable<T> clbl) {
        try {
            Class<?> clazz = cl.loadClass(clbl.getClass().getName());   /* Load in cl. */
            Constructor<?> cnstr = clazz.getDeclaredConstructors()[0];  /* Get default */
            cnstr.setAccessible(true);
            if (cnstr.getParameterTypes().length > 1) {
                throw new RuntimeException("Test inner callables do not allow local variable"
                        + " argument passing due to type incompatibility between classloaders.");
            }

            /* Create new instance passing null for the outer object arg. */
            Callable<?> newClbl = (Callable<?>) cnstr.newInstance(new Object[] {null});

            return (T) newClbl.call(); /* Execute test setup. */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* Starts a test client for receiving events from the agent's server. */
    public static TestEventClnt setupEventClnt() {
        final TestEventClnt tec = new TestEventClnt(CommsProto.HOST, CommsProto.PORT);
        tec.setDaemon(true);

        return tec;
    }
}
