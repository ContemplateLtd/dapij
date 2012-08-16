package testutils;

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

    private static HashMap<Package, PkgLdPolicy> loadPolicy = TestClassLoader.getPkgLoadPolicy();
    protected TestClassLoader cl; /* Create a new instance for each test mеthod. */

    /**
     * Resets the class loader field to provide a new one for each test
     * resulting in a new test environment per test method. TODO: Test if cl is
     * different for each test when they run concurrently.
     */
    @org.junit.Before
    public void setupPerTestCl() {
        cl = new TestClassLoader(loadPolicy); /* A new cl for each test */
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> T noInstrSetup(Callable<T> clbl) {
        cl.addNoInstr(clbl.getClass().getName());
        Object classInst = setup(clbl);
        return (T) classInst;
    }

    protected <T extends Object> T instrSetup(Callable<T> clbl) {
        return (T) setup(clbl);
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
    private <T extends Object> T setup(Callable<T> clbl) {
        try {
            /* Load in custom class loader. */
            Class<?> clazz = cl.loadClass(clbl.getClass().getName());
            Constructor<?> cnstr = clazz.getDeclaredConstructors()[0]; /* Get default constr. */
            cnstr.setAccessible(true);

            /* Throw an ex if not a default - i.e. when final args passed to inner annon class. */
            /* TODO: add support for passing arguments */
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
}
