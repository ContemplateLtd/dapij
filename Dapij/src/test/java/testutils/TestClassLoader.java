package testutils;

import agent.Agent;
import agent.Settings;
import comms.AgentServerTest;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import static transform.Transformer.transformClass;

/**
 * A custom class loader for loading & instrumenting classes based on predefined
 * load policies. As a part of a small testing framework for testing agent code
 * that performs instrumentation, this class provides the ability to clean
 * isolated test environments for the test methods of a test class.
 */
public class TestClassLoader extends ClassLoader {

    private static File mainClsRt = classpathRoot(Agent.class);     /* Main classes root */
    private static File tstClsRt = classpathRoot(AgentServerTest.class);  /* Test classes root */

    /**
     * A list for storing binary class names of classes that won't be
     * instrumented regardless of their package loading/transformation policy.
     */
    private ArrayList<String> forceNoInstr;

    /* proj pkg dirs & flags indicating whether to instrument their classes */
    private HashMap<Package, PackageLoadPolicy> pkgLdPolicies;

    public TestClassLoader(HashMap<Package, PackageLoadPolicy> pkgLdPolicies) {
        this.pkgLdPolicies = pkgLdPolicies;
        this.forceNoInstr = new ArrayList<String>();
    }

    @Override
    protected synchronized Class<?> loadClass(String clsBinName, boolean resolve)
            throws ClassNotFoundException {
        Class<?> c = findLoadedClass(clsBinName); /* Get if already loaded. */

        /* If not, search class in appropriate CL (this/parent/System CL). */
        if (c == null) {
            try {
                c = findClass(clsBinName);
            } catch (ClassNotFoundException e) {} /* Ignore. */
        }

        /*
         * If not found or load policy specifies parent-first loading, use
         * parent or system cl (if no parent available) to load class.
         */
        if (c == null) {
            ClassLoader cl = (getParent() != null) ? getParent() : getSystemClassLoader();
            c = cl.loadClass(clsBinName);
            Settings.INSTANCE.println("Loaded [PRNT]     " + clsBinName);
        }

        return c;
    }

    /**
     * Search for class in local project and load it if present and transform it
     * according to the {@link PackageLoadPolicy} object for its package (if
     * any).
     *
     * @param binClsName
     *            the binary class name of the class to be loaded
     */
    @Override
    protected Class<?> findClass(String clsBinName)
            throws ClassNotFoundException {

        /*
         * Get package for class (if package exists) and return its load policy
         * if one exists.
         */
        PackageLoadPolicy p = getLdPolicy(getPkg(clsBinName));

        String clsRelPath = binNmToPth(clsBinName);
        File classFullPath = new File(mainClsRt, clsRelPath);   /* Check if a main class first. */

        /*
         * If no load policy, a parent-first main class, or class not found - try
         * loading with system class loader.
         */
        if (p == null || (classFullPath.exists() && !p.isMainChldFst())) {
            return null; /* This handles cases when class does not exist. */
        }
        boolean instr = false; /* Do not instrument a child-fst main class. */
        if (!classFullPath.exists()) {
            classFullPath = new File(tstClsRt, clsRelPath);     /* A test class if not main. */
            if (classFullPath.exists() && !p.isTstChldFst()) {
                return null; /* Delegate to parent/system if test class parent-fst. */
            }
            instr = p.isTstInstr();
        }
        boolean shouldTransform = instr && !forceNoInstr.contains(clsBinName);
        byte[] clsBytes = readClass(clsBinName, classFullPath);
        Class<?> loadedClass = defineClass(clsBinName,
                (shouldTransform) ? transformClass(clsBytes) : clsBytes);
        Settings.INSTANCE.println("Loaded [CHLD] " + (shouldTransform ? "[i]" : "   ")
                + " " + clsBinName);
        return loadedClass;
    }

    private Class<?> defineClass(String clsBinName, byte[] bytecode) {
        return super.defineClass(clsBinName, bytecode, 0, bytecode.length);
    }

    private byte[] readClass(String clsName, File file) throws ClassNotFoundException {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class '" + clsName + "'!", e);
        }
    }

    /**
     * For a given class name, returns a {@link File} representing the full path
     * to the package of that class.
     *
     * @param clsBinName
     *            the binary name of the class
     * @return A {@link Package} object representing the package or null if it
     *         is unknown.
     */
    private Package getPkg(String clsBinName) {
        return Package.getPackage(getPgkName(clsBinName));
    }

    /**
     * Given a full path {@link File} object for a package, retrieves a load
     * policy for the class files contained in this package.
     *
     * @param p
     *            A {@link Package} object representing the package.
     * @returns The package loading policy or null if no policy found.
     */
    private PackageLoadPolicy getLdPolicy(Package p) {

        /* If in project classes, search & return policy (or null if none). */
        return (p != null && pkgLdPolicies.containsKey(p)) ? pkgLdPolicies.get(p) : null;
    }

    /**
     * Constructs a configuration (conditioned on package) for lading and
     * instrumenting main package classes. This configuration is hardcoded and
     * needs to be changed by hand in the future.
     *
     * NOTE: Main classes are never instrumented, test classes are always child
     * first. TODO: load package policy settings from the config file?
     *
     * @return A {@link HashMap}{@code <}{@link Package}{@code , }
     *         {@link PackageLoadPolicy}{@code >} containing the load/instrument
     *         policies for the project's classes conditioned on the packages.
     */
    public static HashMap<Package, PackageLoadPolicy> getPkgLoadPolicy() {
        HashMap<Package, PackageLoadPolicy> policies = new HashMap<Package, PackageLoadPolicy>();

        /* main-chld-fst (contains state, refreshed in test), test-chld-fst, test-instr */
        policies.put(Package.getPackage("agent"), new PackageLoadPolicy(true, true, true));

        /* main-parent-fst, test-chld-fst, test-instr */
        policies.put(Package.getPackage("comms"), new PackageLoadPolicy(false, true, true));

        /* main-parent-fst, test-chld-fst, test-instr */
        policies.put(Package.getPackage("transform"), new PackageLoadPolicy(false, true, true));

        /* main-parent-fst, test-parent-fst, test-no-instr */
        policies.put(Package.getPackage("testutils"), new PackageLoadPolicy(false, false, false));

        return policies;
    }

    /**
     * Provides the classpath root for a given class.
     *
     * @param clazz
     *            the class.
     * @return {@link File} object representing the classpath root.
     */
    private static File classpathRoot(Class<?> clazz) {
        try {
            ProtectionDomain pd = clazz.getProtectionDomain();

            return new File(pd.getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the package name for a class.
     *
     * @param clsBinName
     *            The binary name for the given class.
     * @return The package name for the given binary class name.
     */
    private String getPgkName(String clsBinName) {
        return FilenameUtils.removeExtension(clsBinName);
    }

    private static String binNmToPth(String clsBinName) {
        return clsBinName.replace('.', '/') + ".class";
    }

    /**
     * Adds a class to a list of classes that are not going to be instrumented.
     *
     * @param forceNoInstr
     *            The binary name of the class that should not be instrumented
     *            (regardless of its package load policy).
     */
    public void addNoInstr(String name) {
        this.forceNoInstr.add(name);
    }
}
