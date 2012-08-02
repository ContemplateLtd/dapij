/**
 * TODO: doc comment
 */
package transform;

import agent.*;
import static agent.Agent.setupEventSrv;
import comms.AgentEventSrv;
import comms.CommsProto;
import comms.TestEventClnt;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import org.junit.Test;
import testutils.TransfmrTest;

// TODO: test InsnOfstVisitor, InstanceCreationTracker (for concurrency &
// consistency), Settings (for concurrency).
// TODO: add control over instrumentation of innter Callable classes (all
// are currently instrumented).

/**
 * A class containing tests for the dapij.transform package.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TransformationTest extends TransfmrTest {
    
    /**
     * Tests whether object creations are detected and information about the
     * creations is stored in the concurrent map. This implicitly tests the
     * injected code performing this task.
     * 
     * @throws Exception
     */
    @Test
    public void constructorIsInstumented() throws Exception {
        
        /* Create & get the concurr map keeping track of instance creations. */
        Map map = runtimeSetup(new Callable<Map>() {
            @Override
            public Map call() throws Exception {
                InstCreatTracker ict = new InstCreatTracker();
                RuntmEventSrc.INSTANCE.getCreatEventSrc()
                        .addListener(ict);
                Field f = ict.getClass().getDeclaredField("instanceMap");
                f.setAccessible(true);
                
                return (Map) f.get(ict);
            }
        });
        
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

        /* Check if map contains info for the Integer object. */
        Integer i = (Integer) refs[0];
        Assert.assertEquals("Intgr map entry exists", true, map.containsKey(i));
        
        /* Check if info obj fields correct (one by one). */
        InstCreatStats icsInt = (InstCreatStats) map.get(i);
        Assert.assertEquals("Class corretly read & set", Integer.class,
                icsInt.getClazz());
        Assert.assertEquals("Method name correctly read & set", "call",
                icsInt.getMethod());
        Assert.assertEquals("Offset correctly read & set", true,
                icsInt.getOffset() == 3);
        Assert.assertEquals("Thread id correctly read & set", 1,
                icsInt.getThreadId());  /* currently a meaningless assert */
        
        /* Check if map contains info for the inner anonymous Runnable obj. */
        Runnable r = (Runnable) refs[1];
        Assert.assertEquals("Rnbl map entry exists", true, map.containsKey(r));
        
         /* Check if info obj fields correct (one by one). */
         InstCreatStats icsRnbl = (InstCreatStats) map.get(r);
        // Not testing class constant as it's an inner tpye and not Runnable.
        Assert.assertEquals("Method name correctly read & set", "anotherMethod",
                icsRnbl.getMethod());
        Assert.assertEquals("Offset correctly read & set", true,
                icsRnbl.getOffset() == 0);
        Assert.assertEquals("Thread id correctly read & set", 1,
                icsRnbl.getThreadId()); /* currently a meaningless assert */
    }

    /* Starts a test client for receiving events from the agent's server. */
    public static TestEventClnt setupEventClnt() {
        final TestEventClnt tec = new TestEventClnt(CommsProto.host,
                CommsProto.port);
        tec.setDaemon(true);
        
        return tec;
    }
    
    /* Test agent's event server with a test EventClient. */
    // TODO: move to comms test package
    @Test
    public void agentEventServerTest() throws Exception {
        
        /* Start a client first to receive and process events & get its ref. */
        TestEventClnt tec = setupEventClnt();
        tec.start(); /* Start client. */
        
        AgentEventSrv aes = runtimeSetup(new Callable<AgentEventSrv>() {
            @Override
            public AgentEventSrv call() {
                
                /*
                 * Start a srv to recv & fwd events to a single client. Call
                 * blocks until client connected.
                 */
                AgentEventSrv aes = setupEventSrv();
                
                /* Add a listener to send events to the server. */
                RuntmEventSrc.INSTANCE.getCreatEventSrc()
                        .addListener(new CreatEventNetSndr(aes));
                aes.start();
                
                return aes;
            }
        });
        
        /* Perform random actions to generate events. */
        runtimeSetup(new Callable<Object>() {
            @Override
            public Object call() {
               new String("Random test string: " +
                       new String(String.valueOf(new Integer(5))));
               
               return null;
            }
        });
        
        aes.shutdown();
        tec.shutdown();
        // TODO: check if all event msgs correctly received.
        Assert.assertEquals("Azura main: ", true, true);
    }
    
    /**
     * Tests the Settings singleton class
     * @throws Exception
     */
    @Test
    public void settingsTest() throws Exception {
        Settings s = Settings.INSTANCE;
        String settNm1 = "s1", settVl1 = "v1", settVl2 = "v2";
        s.set(settNm1, settVl1);
        
        /* Test wheter setting inserted and can obtain same value2. */
        Assert.assertEquals("Setterly inserted: ", true,
                s.get(settNm1).equals(settVl1));
        
        /* Test wheter setting successfully overwritten. */
        s.set(settNm1, settVl2);
        Assert.assertEquals("Setterly overwritten: ", true,
                s.get(settNm1).equals(settVl2));
        
        /* Test wheter root proejct path is correct. */
        ProtectionDomain pd = this.getClass().getProtectionDomain();
        try {
            File pkgDir = new File(pd.getCodeSource().getLocation().toURI());
            Assert.assertEquals("Is root path valid: ", true,
                    s.get(Settings.SETT_CWD).equals(
                            pkgDir.getParentFile().getParent()));
        } catch (URISyntaxException e) {
            System.out.println("Could not obtain project root path.");
            throw new RuntimeException(e);
        }
        
        /* Test whether unsetSett properly removes a setting. */
        s.set(settNm1, settVl1);
        s.rm(settNm1);
        Assert.assertEquals("Settings successfully removed: ", true,
                s.isSet(settNm1) == false);
    }
    
    public class MickeyMaus {
        int field;
        
        public MickeyMaus(int field) {
            this.field = field;
        }
        
        public int getTheField() {
            return field;
        }
    }
    
    @Test
    public void objectIDTest() throws Exception {
        
        /* Start a client first to receive and process events & get its ref. */
        TestEventClnt tec = setupEventClnt();
        tec.start(); /* Start client. */
        
        AgentEventSrv aes = runtimeSetup(new Callable<AgentEventSrv>() {
            @Override
            public AgentEventSrv call() {
                
                /*
                 * Start a srv to recv & fwd events to a single client. Call
                 * blocks until client connected.
                 */
                AgentEventSrv aes = setupEventSrv();
                
                /* Add a listener to send events to the server. */
                RuntmEventSrc.INSTANCE.getCreatEventSrc()
                        .addListener(new CreatEventNetSndr(aes));
                aes.start();
                
                return aes;
            }
        });
        
        /* Perform random actions to generate events. */
        runtimeSetup(new Callable<Object>() {
            @Override
            public Object call() {
                
                /*Check whether all objects are assigned unique identifiers */
                /* Create some objects */
                Object obj = new Object();
                String str = new String("A string");
                Integer itg = new Integer(1);
                ConcurrentHashMap hash = new ConcurrentHashMap();
                MickeyMaus mickey = new MickeyMaus(2); /* an inner class */

                /* Create an array of object identifiers */
                int[] objIDs = new int[5];
                IDMap.INSTANCE.put(obj, ObjectCounter.getNextID());

                objIDs[0] =  ObjectCounter.getId(obj);
                System.out.println("ID: " + objIDs[0]);
                objIDs[1] =  ObjectCounter.getId(str);
                System.out.println("ID: " + objIDs[1]);
                objIDs[2] =  ObjectCounter.getId(itg);
                System.out.println("ID: " + objIDs[2]);
                objIDs[3] =  ObjectCounter.getId(hash);
                System.out.println("ID: " + objIDs[3]);
                objIDs[4] =  ObjectCounter.getId(mickey);
                System.out.println("ID: " + objIDs[4]);

                /* The IDs should be non-negative and unique */
                for(int i=0; i<5; i++) {
                    //System.out.println("ID: " + objIDs[i]);
                    Assert.assertTrue(objIDs[i] >= 0);
                    for(int j=0; j<i; j++) {
                        Assert.assertFalse(objIDs[i] == objIDs[j]);
                    }
                }
               return null;
            }
        });
        
        aes.shutdown();
        tec.shutdown();
        
    }
}
