/**
 * TODO: doc comment
 */
package dapij;

import comms.EventServer;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.objectweb.asm.Type;

/**
 * A class containing tests for the dapij package.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class DapijTest {

// TODO: test InsnOggsetVisitor, InstanceCreationTracker (for concurrency &
// consistency), InstanceCreationVisitor (test directly if possible), XMLWriter,
// Settings (for concurrency).
    
    private void initNetwork() {
        /* Start a client to receive and process events. */
        /* Start a server for receiving & forwarding events to one client. */
        Dapij.setupEventClient(); // waits for server to start // TODO: start client independently?
        Dapij.setupEventServer(); // waits for client to connect
    }
    
    /**
     * This tests whether creations of objects are detected and informations
     * is properly stored in the concurrent map. This implicitly tests the
     * injected code performing this task.
     * 
     * @throws Exception 
     */
    @Test
    public void constructorIsInstrumented() throws Exception {
        ClassLoader cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return new TestClassLoader();
                }
        });
        
        /* Load the ObjectInstance class */
        Class<?> clazz = cl.loadClass(ObjectInstance.class.getName());
   
        /*
         * Create an instance of it to invoke its 'create' method to create
         * and return object.
         */
        Integer mapEntry = (Integer) clazz.getMethod("create").invoke(
                clazz.newInstance());
        
        /*
         * Get a reference to the concurrent map that contains info about
         * objects created by the instrumented program (ObjectInstance class)
         */
        Map<Object, InstanceCreationStats> m = getCreationMap(cl);
        
        /* Check if there is info in the map for the created object. */
        assertEquals("Creation recorded: ", true, m.containsKey(mapEntry));
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
    
    /*
     * Test agent's EventServer with a test EventClient on HelloAzura::main();.
     */
    @Test
    public void dapijEventServerTest() throws Exception {
        ClassLoader cl = AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return new TestClassLoader();
                }
        });
        
        /* overwrite xml_out_sett setting */
        Settings.INSTANCE.setProp(Settings.XML_OUT_SETT,
                Settings.INSTANCE.getProp(Settings.CWD) + "/output.xml");
        
        initNetwork(); /* start server && client */
        
        /* add a breakpoint */
        Settings.INSTANCE.addBreakpt(new Breakpoint("HelloAzura.java", 38,
                true));
        
        /*
         * supply the server from this class loader's Settings.INSTANCE to
         * the Settings.INSTANCE available to the HelloAzura::main() method
         * that is going to be executed in this test.
         */
        try {
            Class<?> settingsCls = cl.loadClass(Settings.class.getName());
            Object instance = settingsCls.getField("INSTANCE").get(null);
            Field esField = settingsCls.getDeclaredField("eventServer");
            esField.setAccessible(true);
            Object es = esField.get(instance);
            es = Settings.INSTANCE.getEventServer();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        Class<?> haCls = cl.loadClass(HelloAzura.class.getName());
        String args = null;
        haCls.getMethod("main", String[].class).invoke(null, args);
        
        /* TODO: get client & check if all event msgs correctly received */
        assertEquals("Azura main: ", true, true);  // For now - an empty test
    }

    /**
     * A test of the Settings singleton class
     * @throws Exception
     */
    @Test
    public void settingsTest() throws Exception {
        Settings s = Settings.INSTANCE;
        
        String settNm1 = "s1", settVl1 = "v1", settVl2 = "v2";
        s.setProp(settNm1, settVl1);
        
        /* test wheter setting inserted and can obtain same value2 */
        assertEquals("Properly inserted: ", true,
                s.getProp(settNm1).equals(settVl1));
        
        /* test wheter setting successfully overwritten */
        s.setProp(settNm1, settVl2);
        assertEquals("Properly overwritten: ", true,
                s.getProp(settNm1).equals(settVl2));
        
        /* test wheter root proejct path is correct */
        ProtectionDomain pd = this.getClass().getProtectionDomain();
        try {
            File pkgDir = new File(pd.getCodeSource().getLocation().toURI());
            assertEquals("Is root path valid: ", true,
                    s.getProp(Settings.CWD).equals(
                            pkgDir.getParentFile().getParent()));
        } catch (URISyntaxException e) {
            System.out.println("Could not obtain project root path.");
            throw new RuntimeException(e);
        }
    }
}
