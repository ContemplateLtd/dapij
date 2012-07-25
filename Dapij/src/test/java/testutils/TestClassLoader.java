/**
 * TODO: doc comment
 */
package testutils;

import agent.Agent;
import comms.CommsProto;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
import plugin.PluginIJ;
import transform.StatsCollector;
import transform.TransformationTest;
import static transform.Transformer.transformClass;


/**
 * TODO: doc comment
 */
public class TestClassLoader extends ClassLoader {
    
    /* Main classes root */
    private static File mainClsRt = classpathRoot(Agent.class);
    
    /* Test classes root */
    private static File tstClsRt = classpathRoot(TransformerTest.class);
    
    /* proj pkg dirs & flags indicating wheter to instument their classes */
    private HashMap<String, PkgLdPolicy> pkgLdPolicies;


    public TestClassLoader(HashMap<String, PkgLdPolicy> pkgLdPolicies) {
        this.pkgLdPolicies = pkgLdPolicies;
    }
    
    @Override
    protected synchronized Class<?> loadClass(String clsBinName,
            boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(clsBinName);   /* Get if already loaded. */

        /* If not, search class in appropriate CL (this/parent/System CL). */
        if (c == null) {
            
            /*
             * Look for a package (in the project) that contains the class
             * and has a defined load policy and retrieve that policy.
             */
            File pkg = getPkgFullPath(clsBinName);
            PkgLdPolicy p = getClsLdPolicy(pkg);  /* get policy if exists */

            /* 
             * If found, load class with child class loader according to
             * the load policy for the package of that class
             */
            if (p != null &&  p.isChildFirst()) {
                try {
                    c = findClass(clsBinName,
                            new File(pkg, getNmFromPth(binNmToPth(clsBinName))),
                            p.isInstrumented());
                } catch (ClassNotFoundException е) {}   /* Ignore. */
            }
            
            /*
             * If not found or load policy specifies parent-first loading,
             * use parent or system cl (if no parent available) to load class.
             */
            boolean chld = true;    // TODO: remove + usages
            if (c == null) {
                ClassLoader cl = (getParent() != null) ?
                        getParent() : getSystemClassLoader();
                c = cl.loadClass(clsBinName);
                chld = false;
            }
            //TODO: Remove commet + boolean variable
//            /System.out.println("LD in-"+((chld) ? "child" : "parent")+((p != null && p.isInstrumented()) ? " [inst] " : " ")+clsBinName);
        }
        
        if (resolve) {
          resolveClass(c);
        }
        
        return c;
    }

    /**
     * Search for class in local project and load it if present and transform
     * it according to the package load policy for its package (if any).
     * 
     * @param binClsName the binary class name of the class to be loaded
     */
    protected Class<?> findClass(String clsBinName, File clsFullPath,
            boolean transform) throws ClassNotFoundException {
        byte[] clsBytes = readClass(clsBinName, clsFullPath);
        return defineClass(clsBinName,
                (transform) ? transformClass(clsBytes) : clsBytes);
    }

    private Class<?> defineClass(String clsBinName, byte[] bytecode) {
        Class clz = super.defineClass(clsBinName, bytecode, 0, bytecode.length);
        return clz;
    }

    private byte[] readClass(String clsName, File file)
            throws ClassNotFoundException {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new ClassNotFoundException(
                    "Could not load class '" + clsName + "'!", e);
        }
    }
    
    /**
     * For a given class name, returns a File representing the full path to
     * the package of that class.
     * 
     * @param clsBinName the binary name of the class
     * @return The package direc as a or null if the
     * class is not a part of the project packages.
     */
    private File getPkgFullPath(String clsBinName) {
        File pkgFP = null;
        String clsRelPath = binNmToPth(clsBinName);
        File mainCls = new File(mainClsRt,clsRelPath);
        if (mainCls.exists()) {
            pkgFP = mainCls.getParentFile(); /* Return package full path. */
        }
        
        File testCls = new File(tstClsRt, clsRelPath);
        if (testCls.exists()) {
            pkgFP = testCls.getParentFile(); /* Return package full path. */
        }
        return pkgFP;
    }
    
    /**
     * Given a full path File object for a package, retrieves a load policy for
     * the class files contained in this package.
     * 
     * @param pkgFP A file object representing the full path to the package.
     * @returns The package loading policy or null if no policy found.
     */
    private PkgLdPolicy getClsLdPolicy(File pkgFP) {
        /* If in project classes, search & return policy (or null if none). */
        return (pkgFP != null && pkgLdPolicies.containsKey(pkgFP.getPath())) ?
                pkgLdPolicies.get(pkgFP.getPath()) : null;
    }
    
    /**
     * Constructs a configuration (conditioned on package) for lading and
     * instrumenting the classes of the project. This configuration is
     * hardcoded and needs to be changed by hand in the future.
     * 
     * NOTE: can be done automatically, but policies still have to be set
     * manually, so not much improvement can be introduced.
     * .
     * @return A HashMap<File, PkgLoadConf> containing the load/instrument
     * policies for the project's classes conditioned on the packages.
     */
    public static HashMap<String, PkgLdPolicy> genLdPolicyByPkg() {
        HashMap<String, PkgLdPolicy> pkgLdPolicies =
                new HashMap<String, PkgLdPolicy>();
        try {
            /* MAIN CLASSES, do not transform. */
            /* agent package - child-fst, don't instr */
            File pkgFP = new File(mainClsRt, binNmToPth(Agent.class.getName()))
                    .getParentFile();
            pkgLdPolicies.put(pkgFP.getPath(),
                    new PkgLdPolicy(true, false));

            /* comms package - child-fst, don't instr */
            pkgFP = new File(mainClsRt, binNmToPth(CommsProto.class.getName()))
                    .getParentFile();
            pkgLdPolicies.put(pkgFP.getPath(),
                    new PkgLdPolicy(true, false));

            /* NOTE: currently not used, change in future */
            /* Get package full path & set policy - child-fst, don't instr */
            pkgFP = new File(mainClsRt, binNmToPth(PluginIJ.class.getName()))
                    .getParentFile();
            pkgLdPolicies.put(pkgFP.getPath(), new PkgLdPolicy(true, false));

            /* transform package - parent-fst, don't instr */
            pkgFP = new File(mainClsRt,
                    binNmToPth(StatsCollector.class.getName())).getParentFile();
            pkgLdPolicies.put(pkgFP.getPath(), new PkgLdPolicy(false, false));

            /* TEST CLASSES, transform. */
            /* Get package full path & set policy - parent-fst, don't instr */
            pkgFP = new File(tstClsRt,
                            binNmToPth(TransformationTest.class.getName())
                    ).getParentFile();
            pkgLdPolicies.put(pkgFP.getPath(), new PkgLdPolicy(true, true));
        
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pkgLdPolicies;
    }
    
    private PkgLdPolicy getPkgLdPolicy(File pkgFullPath) {
        return (pkgLdPolicies.containsKey(pkgFullPath.getPath())) ?
                pkgLdPolicies.get(pkgFullPath.getPath()) : null;
    }
    
    private static File classpathRoot(Class<?> clazz) {
        try {
            ProtectionDomain pd = clazz.getProtectionDomain();
            return new File(pd.getCodeSource().getLocation().toURI());// getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static String binNmToPth(String clsBinName) {
        return clsBinName.replace('.', '/') + ".class";
    }
    
    /**
     * Used to get the file or directory name pointed by the given path.
     * 
     * @param path a relative of full path
     * @return a String representing the name.
     */
    private static String getNmFromPth(String path) {
        return new File(path).getName();
    }
}
