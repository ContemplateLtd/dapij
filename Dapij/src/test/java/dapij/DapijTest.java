package dapij;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DapijTest {

    @Test
    public void constructorIsInstrumented() throws Exception {
        ClassLoader cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return new TestClassLoader();
                }
        });
        Class<?> clazz = cl.loadClass(ObjectInstance.class.getName());
        
        Map<Object, InstanceCreationStats> m = getCreationMap(cl);
        
        // Create an object  & test wheter recorded
        Integer mapEntry = (Integer) clazz.getMethod("create").invoke(
                clazz.newInstance());
        
        assertEquals("Creation recorded: ", true, m.containsKey(mapEntry));
    }

    /*
     * An empty test just for executing HelloAzura::main();.
     */
    @Test
    public void azuraTest() throws Exception {
        ClassLoader cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return new TestClassLoader();
                }
        });
        File rt;
        try {
            ProtectionDomain pd = this.getClass().getProtectionDomain();
            rt = new File(pd.getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        Settings.INSTANCE.set(Settings.XML_OUT_SETT,
                rt.getParentFile().getParent() + "/output.xml");
        Settings.INSTANCE.addBreakpt(new Breakpoint("HelloAzura.java", 38,
                true));

        Class<?> haCls = cl.loadClass(HelloAzura.class.getName());
        String args = null;
        haCls.getMethod("main", String[].class).invoke(null, args);
        
        assertEquals("Azura main: ", true, true);  // An empty test
    }
    
    private Map<Object, InstanceCreationStats> getCreationMap(ClassLoader cl) {
        try {
            Class<?> clazz = cl.loadClass(
                    InstanceCreationTracker.class.getName());
            Field field = clazz.getField("INSTANCE");
            Object instance = field.get(null);
            Field mapField = instance.getClass().
                    getDeclaredField("instanceMap");
            mapField.setAccessible(true);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Map<Object, InstanceCreationStats> map = (Map) mapField.get(
                    instance);
            
            return map;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class ObjectInstance {
        public ObjectInstance() {}
        
        public Integer create() {
            return new Integer(5); // intentional object creation
        }
    }
}
