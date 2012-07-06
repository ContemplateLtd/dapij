package dapij;

import static dapij.Dapij.transformClass;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;

import org.apache.commons.io.FileUtils;

class TestClassLoader extends ClassLoader {

    private final File testClasses = classpathRoot(TestClassLoader.class);
    private final File mainClasses = classpathRoot(Dapij.class);

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);

        // if not loaded, search the local (child) resources
        if (c == null) {
            try {
                c = findClass(name);
            } catch(ClassNotFoundException cnfe) {
              // ignore
            }
        }

        // if we could not find it, delegate to parent
        // Note that we don't attempt to catch any ClassNotFoundException
        if (c == null) {
          if (getParent() != null) {
            c = getParent().loadClass(name);
          } else {
            c = getSystemClassLoader().loadClass(name);
          }
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
        File testPath = new File(testClasses, path);
        if (testPath.exists()) {
            return defineClass(name, transformClass(readClass(name, testPath)));
        }

        File mainPath = new File(mainClasses, path);
        if (mainPath.exists()) {
            return defineClass(name, readClass(name, mainPath));
        }

        throw new ClassNotFoundException(name);
    }

    private Class<?> defineClass(String name, byte[] bytecode) {
        return super.defineClass(name, bytecode, 0, bytecode.length);        
    }

    private byte[] readClass(String name, File file) throws ClassNotFoundException {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException ex) {
            throw new ClassNotFoundException("Could not load class " + name, ex);
        }
    }

}
