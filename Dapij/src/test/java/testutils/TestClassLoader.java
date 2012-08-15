package testutils;

import agent.Agent;
import comms.CommsTest;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import static transform.Transfmr.transformClass;

/*
 * TODO: add control over instrumentation of innter Callable classes (all are
 * currently instrumented).
 */

/**
 * A custom class loader for loading & instrumenting classes based on predefined
 * load policies. As a part of a small testing framework for testing agent code
 * that performs instrumentation, this class provides the ability to clean
 * isolated test environments for the test methods of a test class.
 */
public class TestClassLoader extends ClassLoader {

    private static File mainClsRt = classpathRoot(Agent.class);     /* Main classes root */
    private static File tstClsRt = classpathRoot(CommsTest.class);  /* Test classes root */

    /**
     * A flag that allows to force class loading without instrumentation
     * regardless of the package loading/transformation policy for that
     * class.
     */
    private ArrayList<String> forceNoInstr;

    /* proj pkg dirs & flags indicating whether to instrument their classes */
    private HashMap<Package, PkgLdPolicy> pkgLdPolicies;

    public TestClassLoader(HashMap<Package, PkgLdPolicy> pkgLdPolicies) {
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
            } catch (ClassNotFoundException e) {
                /* Ignore. */
            }
        }

        /*
         * If not found or load policy specifies parent-first loading, use
         * parent or system cl (if no parent available) to load class.
         */
        if (c == null) {
            ClassLoader cl = (getParent() != null) ? getParent() : getSystemClassLoader();
            c = cl.loadClass(clsBinName);
        }

        /* NOTE: resolveClass() call is never reached, so it has been removed. */
        return c;
    }

    /**
     * Search for class in local project and load it if present and transform it
     * according to the package load policy for its package (if any).
     *
     * @param binClsName
     *            the binary class name of the class to be loaded
     */
    @Override
    protected Class<?> findClass(String clsBinName)
            throws ClassNotFoundException {

        /*
         * Get pkg for class (if pkg exists) and return its load policy if such
         * exists.
         */
        PkgLdPolicy p = getLdPolicy(getPkg(clsBinName));

        String clsRelPath = binNmToPth(clsBinName);
        File clsFullPath = new File(mainClsRt, clsRelPath);

        /*
         * If no load policy, a parent-fst main class or class not found - try
         * system cls ldr.
         */
        if (p == null || (clsFullPath.exists() && !p.isMainChldFst())) {
            return null; /* This handles cases when class does not exist. */
        }
        boolean instr;
        if (clsFullPath.exists()) {
            instr = false; /* Do not instrument a child-fst main class. */
        } else {
            clsFullPath = new File(tstClsRt, clsRelPath);
            if (clsFullPath.exists() && !p.isTstChldFst()) {
                return null; /* Delegate to parent/system if test class parent-fst. */
            }
            instr = p.isTstInstr();
        }
        byte[] clsBytes = readClass(clsBinName, clsFullPath);
        return defineClass(clsBinName, (instr && !forceNoInstr.contains(clsBinName)) ?
                transformClass(clsBytes) : clsBytes);
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
     * For a given class name, returns a File representing the full path to the
     * package of that class.
     *
     * @param clsBinName
     *            the binary name of the class
     * @return A Package object representing the package or null if it is
     *         unknown.
     */
    private Package getPkg(String clsBinName) {
        return Package.getPackage(getPgkName(clsBinName));
    }

    /**
     * Given a full path File object for a package, retrieves a load policy for
     * the class files contained in this package.
     *
     * @param p
     *            A Package object representing the package.
     * @returns The package loading policy or null if no policy found.
     */
    private PkgLdPolicy getLdPolicy(Package p) {

        /* If in project classes, search & return policy (or null if none). */
        return (p != null && pkgLdPolicies.containsKey(p)) ? pkgLdPolicies.get(p) : null;
    }

    /**
     * Constructs a configuration (conditioned on package) for lading and
     * instrumenting main package classes. This configuration is hardcoded and
     * needs to be changed by hand in the future.
     *
     * NOTE: Main classes are never instrumented, test classes are always child
     * first
     * TODO: improvement - load from a config file.
     *
     * @return A HashMap<File, PkgLoadConf> containing the load/instrument
     *         policies for the project's classes conditioned on the packages.
     */
    public static HashMap<Package, PkgLdPolicy> getPkgLoadPolicy() {
        HashMap<Package, PkgLdPolicy> pkgLdPolicies = new HashMap<Package, PkgLdPolicy>();

        /* main-chld-fst (contains state, refreshed in tst), tst-chld-fst, tst-instr */
        pkgLdPolicies.put(Package.getPackage("agent"), new PkgLdPolicy(true, true, true));

        /* main-parent-fst, tst-chld-fst, tst-instr */
        pkgLdPolicies.put(Package.getPackage("comms"), new PkgLdPolicy(false, true, true));

        /* main-parent-fst, tst-chld-fst, tst-instr */
        pkgLdPolicies.put(Package.getPackage("transform"), new PkgLdPolicy(false, true, true));

        /* main-parent-fst, tst-parent-fst, tst-no-instr */
        pkgLdPolicies.put(Package.getPackage("testutils"), new PkgLdPolicy(false, false, false));

        return pkgLdPolicies;
    }

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
