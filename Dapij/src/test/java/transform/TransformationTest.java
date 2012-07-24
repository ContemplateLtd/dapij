/**
 * TODO: doc comment
 */
package transform;

import TestUtil.TestClassLoader;
import TestUtil.TransformerTest;
import agent.Agent;
import agent.Breakpoint;
import agent.RuntimeEventRegister;
import agent.Settings;
import comms.CommsProto;
import comms.TestEventClient;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * A class containing tests for the dapij package.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TransformationTest extends TransformerTest {

// TODO: test InsnOggsetVisitor, InstanceCreationTracker (for concurrency &
// consistency), InstanceCreationVisitor (test directly if possible), XMLWriter,
// Settings (for concurrency).
    
    /**
     * Tests whether object creations are detected and information about the
     * creations is stored in the concurrent map. This implicitly tests the
     * injected code performing this task.
     * 
     * @throws Exception
     */
    @Test
    public void constructorIsInstrumented() throws Exception {
        runtimeSetup(new Runnable() {
            @Override
            public void run() {
                System.out.println("Creating an integer ...");
                //new Integer(5); // intentional object creation
            }
        });
        //runtimeSetup(r);
        /*
         * Create an instance of it to invoke its 'create' method to create
         * and return object.
         */
        //Integer mapEntry = (Integer) clazz.getMethod("create").invoke(
        //        clazz.newInstance());
        
        /*
         * Get a reference to the concurrent map that contains info about
         * objects created by the instrumented program (ObjectInstance class)
         */
        //Map<Object, InstanceCreationStats> m = getCreationMap();
        
        /* Check if there is info in the map for the created object. */
        //assertEquals("Creation recorded: ", 1, m.size());
    }
    
//    public static class CreateInstance implements Runnable {
//        @Override
//        public void run() {
//            System.out.println("Creating an integer ...");
//            //new Integer(5); // intentional object creation
//        }
//    }

//    private Map<Object, InstanceCreationStats> getCreationMap() {
//        try {
//            Class<?> clazz = 
//                    cl.loadClass(RuntimeEventRegister.class.getName());
//            Field field = clazz.getField("INSTANCE");
//            Object instance = field.get(null);
//            Field mapField = instance.getClass().
//                    getDeclaredField("instanceMap");
//            mapField.setAccessible(true);
//            @SuppressWarnings({"rawtypes", "unchecked"})
//            Map<Object, InstanceCreationStats> map = (Map) mapField.get(
//                    instance);
//            return map;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
    
    
    
    /*
     * Test agent's EventServer with a test EventClient on HelloAzura::main();.
     */   
//    @Test
//    public void dapijEventServerTest() throws Exception {
//        /* overwrite xml_out_sett setting */
//        Settings.INSTANCE.setSett(Settings.SETT_XML_OUT,
//                Settings.INSTANCE.getSett(Settings.SETT_CWD) + "/output.xml");
//        
//        initNetwork(); /* start server && client */
//        
//        /* add a breakpoint */
//        Settings.INSTANCE.addBreakpt(new Breakpoint("HelloAzura.java", 38,
//                true));
//        
//        /* TODO:
//         * Supply the server from this class loader's Settings.INSTANCE to
//         * the Settings.INSTANCE available to the HelloAzura::main() method
//         * that is going to be executed in this test.
//         */
//
//        Class<?> haCls = cl.loadClass(HelloAzura.class.getName());
//        String args = null;
//        haCls.getMethod("main", String[].class).invoke(null, args);
//        
//        /* TODO: get client ref & check if all event msgs correctly received */
//        assertEquals("Azura main: ", true, true);  // For now - an empty test
//    }
//
//    private void initNetwork() {
//        /* Start a client to receive and process events. */
//        /* Start a server for receiving & forwarding events to one client. */
//        setupEventClient(); // waits for server to start // TODO: start client independently?
//        Agent.setupEventServer(); // waits for client to connect
//    }
//
//    public static void setupEventClient() {
//        final TestEventClient ec = new TestEventClient(CommsProto.host,
//                CommsProto.port);
//        ec.setDaemon(true);
//        
//        /* For gracefully shutdown when user program ends. */
//        Thread sh = new Thread() {
//            @Override
//            public void run() {
//                ec.shutdown();
//            }
//        };
//        Runtime.getRuntime().addShutdownHook(sh);
//        ec.start(); /* Start client. */
//    }
    
    /**
     * A test of the Settings singleton class
     * @throws Exception
     */
//    @Test
//    public void settingsTest() throws Exception {
//        Settings s = Settings.INSTANCE;
//        
//        String settNm1 = "s1", settVl1 = "v1", settVl2 = "v2";
//        s.setSett(settNm1, settVl1);
//        
//        /* Test wheter setting inserted and can obtain same value2. */
//        assertEquals("Setterly inserted: ", true,
//                s.getSett(settNm1).equals(settVl1));
//        
//        /* Test wheter setting successfully overwritten. */
//        s.setSett(settNm1, settVl2);
//        assertEquals("Setterly overwritten: ", true,
//                s.getSett(settNm1).equals(settVl2));
//        
//        /* Test wheter root proejct path is correct. */
//        ProtectionDomain pd = this.getClass().getProtectionDomain();
//        try {
//            File pkgDir = new File(pd.getCodeSource().getLocation().toURI());
//            assertEquals("Is root path valid: ", true,
//                    s.getSett(Settings.SETT_CWD).equals(
//                            pkgDir.getParentFile().getParent()));
//        } catch (URISyntaxException e) {
//            System.out.println("Could not obtain project root path.");
//            throw new RuntimeException(e);
//        }
//        
//        /* Test whether unsetSett properly removes a setting. */
//        s.setSett(settNm1, settVl1);
//        s.unsetSett(settNm1);
//        assertEquals("Settings successfully removed: ", true,
//                s.isSetSett(settNm1) == false);
//    }
}
