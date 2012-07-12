/**
 * TODO: doc comment
 */
package dapij;

import static dapij.Dapij.transformClass;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import org.apache.commons.io.FileUtils;

/**
 * TODO: doc comment
 */
class TestClassLoader extends ClassLoader {

    private final File testClasses = classpathRoot(TestClassLoader.class);
    private final File mainClasses = classpathRoot(Dapij.class);

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        
        /* Check if already loaded */
        Class<?> c = findLoadedClass(name);

        /* If not, search local (child) resources */
        if (c == null) {
            try {
                c = findClass(name);
            } catch(ClassNotFoundException cnfe) {} // ignore
        }

        /*
         * If not found, delegate to parent, no attempt to catch
         * ClassNotFoundException.
         */
        if (c == null) {
          c = (getParent() != null) ?
                  getParent().loadClass(name) :
                  getSystemClassLoader().loadClass(name);
        }

        if (resolve) {
          resolveClass(c);
        }

        return c;
    }

    private static File classpathRoot(Class<?> clazz) {
        try {
            ProtectionDomain pd = clazz.getProtectionDomain();
            return new File(pd.getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/') + ".class";
        
        /* If a test class, transform and return */
        File testPath = new File(testClasses, path);
        if (testPath.exists()) {
            //return defineClass(name, transformClass(readClass(name, testPath)));
        }
        
        /* If a main class, only return */
        File mainPath = new File(mainClasses, path);
        if (mainPath.exists()) {
            return defineClass(name, readClass(name, mainPath));
        }
        
        /* If something else, throw exception */
        throw new ClassNotFoundException(name);
    }

    private Class<?> defineClass(String name, byte[] bytecode) {
        return super.defineClass(name, bytecode, 0, bytecode.length);        
    }

    private byte[] readClass(String name, File file)
            throws ClassNotFoundException {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not load class " + name, e);
        }
    }
}
