/**
 * TODO: doc comment
 */
package transform;

import agent.Agent;
import agent.RuntimeEventRegister;
import agent.Settings;
import comms.AgentEventServer;
import comms.CommsProto;
import comms.TestEventClient;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;
import testutils.TransformerTest;


// TODO: test InsnOggsetVisitor, InstanceCreationTracker (for concurrency &
// consistency), InstanceCreationVisitor (test directly if possible), XMLWriter,
// Settings (for concurrency).

// TODO: add control over instrumentation of innter callables (all are currently
// instrumented).


/**
 * A class containing tests for the dapij package.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TransformationTest extends TransformerTest {
    
    /**
     * Tests whether object creations are detected and information about the
     * creations is stored in the concurrent map. This implicitly tests the
     * injected code performing this task.
     * 
     * @throws Exception
     */
    @Test
    public void constructorIsInstumented() throws Exception {
        
        /* Create some objects, get their refs & test if events registered. */
        Object[] refs = runtimeSetup(new Callable<Object[]>() {
            
            /* create an object using an inner anonymous class */
            private Runnable anotherMethod() {
                return new Runnable() {
                    @Override
                    public void run() {}    /* empty, just an object needed. */
                };
            }
            
            @Override
            public Object[] call() {
                String.valueOf(5);  /* Insert insn to test offset value. */
                Integer i = new Integer(5); /* Create another simple object. */
                return new Object[]{i, anotherMethod()};
            }
        });

        /* Get concurrent map keeping track of instance creations. */
        Map map = runtimeSetup(new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                Class regCls = RuntimeEventRegister.INSTANCE.getClass();
                Field f = regCls.getDeclaredField("instanceMap");
                f.setAccessible(true);
                return (Map) f.get(RuntimeEventRegister.INSTANCE);
            }
        });
        
        /* Check if map contains info for the Integer object. */
        Integer i = (Integer) refs[0];
        Assert.assertEquals("Intgr map entry exists", true, map.containsKey(i));
        
        /* Check if info obj fields correct (one by one). */
        InstanceCreationStats icsI = (InstanceCreationStats) map.get(i);
        Assert.assertEquals("Class corretly read & set", Integer.class,
                icsI.getClazz());
        Assert.assertEquals("Method name correctly read & set", "call",
                icsI.getMethod());
        Assert.assertEquals("Offset correctly read & set", true,
                icsI.getOffset() == 3);
        Assert.assertEquals("Thread id correctly read & set", 1,
                icsI.getThreadId());   /* currently a meaningless assert */
        
        /* TODO: test obj ref from the Runnable inner anonymous object. */
        Runnable r = (Runnable) refs[1];
        Assert.assertEquals("Rnbl map entry exists", true, map.containsKey(r));
        
         /* Check if info obj fields correct (one by one). */
         InstanceCreationStats icsR = (InstanceCreationStats) map.get(r);
        // Not testing class as it's some other innter tpye and not Runnable.
        Assert.assertEquals("Method name correctly read & set", "anotherMethod",
                icsR.getMethod());
        Assert.assertEquals("Offset correctly read & set", true,
                icsR.getOffset() == 0);
        Assert.assertEquals("Thread id correctly read & set", 1,
                icsR.getThreadId());   /* currently a meaningless assert */
    }

    public static TestEventClient startEventClient() {
        final TestEventClient tec = new TestEventClient(CommsProto.host,
                CommsProto.port);
        tec.setDaemon(true);
        
        /* For gracefully shutdown when user program ends. */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                tec.shutdown();
            }
        });
        tec.start(); /* Start client. */
        return tec;
    }
    
    /*
     * Test agent's EventServer with a test EventClient on HelloAzura::main();.
     */   
    @Test
    public void agentEventServerTest() throws Exception {
        
        /* Start a client first to receive and process events & get its ref. */
        TestEventClient tec = startEventClient();

        runtimeSetup(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                /*
                 * Start a server to recv & fwd events to a single client.
                 * This call will block until client connected.
                 */
                AgentEventServer aes = Agent.startEventServer();
                HelloAzura.main(new String[]{});    /* Call with not args. */
                aes.shutdown();
                return null;
            }
        });
        tec.shutdown();

        /* TODO: get client ref & check if all event msgs correctly received */
        Assert.assertEquals("Azura main: ", true, true);  //TODO: fix assertions
    }
    
    /**
     * A test of the Settings singleton class
     * @throws Exception
     */
    @Test
    public void settingsTest() throws Exception {
        Settings s = Settings.INSTANCE;
        
        String settNm1 = "s1", settVl1 = "v1", settVl2 = "v2";
        s.setSett(settNm1, settVl1);
        
        /* Test wheter setting inserted and can obtain same value2. */
        Assert.assertEquals("Setterly inserted: ", true,
                s.getSett(settNm1).equals(settVl1));
        
        /* Test wheter setting successfully overwritten. */
        s.setSett(settNm1, settVl2);
        Assert.assertEquals("Setterly overwritten: ", true,
                s.getSett(settNm1).equals(settVl2));
        
        /* Test wheter root proejct path is correct. */
        ProtectionDomain pd = this.getClass().getProtectionDomain();
        try {
            File pkgDir = new File(pd.getCodeSource().getLocation().toURI());
            Assert.assertEquals("Is root path valid: ", true,
                    s.getSett(Settings.SETT_CWD).equals(
                            pkgDir.getParentFile().getParent()));
        } catch (URISyntaxException e) {
            System.out.println("Could not obtain project root path.");
            throw new RuntimeException(e);
        }
        
        /* Test whether unsetSett properly removes a setting. */
        s.setSett(settNm1, settVl1);
        s.unsetSett(settNm1);
        Assert.assertEquals("Settings successfully removed: ", true,
                s.isSetSett(settNm1) == false);
    }
}
