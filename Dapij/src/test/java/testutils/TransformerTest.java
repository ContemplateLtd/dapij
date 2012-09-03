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
public class TransformerTest {

    private static HashMap<Package, PackageLoadPolicy> loadPolicy =
            TestClassLoader.getPkgLoadPolicy();
    private TestClassLoader cl; /* Create a new instance for each test mеthod. */

    /**
     * Instantiates a new {@link TestClassLoader} class loader instance for each
     * test resulting in a new test environment per test method.
     */
    @org.junit.Before
    public void setupPerTestCl() {
        cl = new TestClassLoader(loadPolicy); /* Create a new class loader for each test. */
    }

    /**
     * Loads the class of the passed {@link Callable} object using the new class
     * loader. The loaded class is not instrumented. A new instance is created
     * and it's {@code call()} method is executed.
     *
     * @param clbl
     *            the {@link Callable} object to load.
     * @return the {@code call()} method result.
     */
    protected <T> T noInstrSetup(Callable<T> clbl) {
        dontInstr(clbl.getClass());
        return setup(clbl);
    }

    /**
     * Loads the class of the passed {@link Callable} object using the new class
     * loader. The {@link Callable} class is instrumented. A new instance is
     * created and it's {@code call()} method is executed.
     *
     * @param clbl
     *            the {@link Callable} object to load.
     * @return the {@code call()} method result.
     */
    protected <T> T instrSetup(Callable<T> clbl) {
        return setup(clbl);
    }

    /**
     * Simply loads a class using the current test's class loader without
     * instrumenting it.
     *
     * @param c
     *            the class to load.
     */
    protected <T> void dontInstr(Class<T> clazz) {
        cl.addNoInstr(clazz.getName());
    }

    /**
     * Provides an convenient way to config a per-test-method runtime setup by
     * loading a {@link Runnable} implementation into the newly created class
     * loader for the test (created by {@code setupPerTestCl()}). Achieved by
     * creating an instance and executing its run using reflection.
     *
     * @param clbl
     *            the {@link Callable} implementation - usually an anonymous
     *            inner class. Passing of local parameters not currently
     *            supported.
     */
    private <T> T setup(Callable<T> clbl) {
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
            @SuppressWarnings("unchecked")
            Callable<T> newClbl = (Callable<T>) cnstr.newInstance(new Object[] {null});

            return newClbl.call(); /* Execute test setup. */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides access to the current test method's class loader instance.
     *
     * @return the {@link TestClassLoader} class loader instance for this test
     *         method.
     */
    public ClassLoader getClassLoader() {
        return this.cl;
    }
}
