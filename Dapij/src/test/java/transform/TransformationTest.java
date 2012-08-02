/**
 * TODO: doc comment
 */
package transform;

import static agent.Agent.setupEventSrv;
import agent.CreatEventNetSndr;
import agent.InstIdentifier;
import agent.RuntmEventSrc;
import agent.Settings;
import comms.AgentEventSrv;
import comms.TestEventClnt;
import java.io.File;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
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
        HashMap<Integer, InstCreatData> map = runtimeSetup(
                new Callable<HashMap<Integer, InstCreatData>>() {
            
            @Override
            public HashMap<Integer, InstCreatData> call() throws Exception {
                CreatEventLisnr l = new CreatEventLisnr() {
                    
                    public HashMap<Integer, InstCreatData> map =
                            new HashMap<Integer, InstCreatData>();
                    
                    @Override
                    public void handleCreationEvent(CreatEvent e) {
                        /* Collect creation events' data in a map. */
                        map.put(e.getObjData().getObjId(), e.getObjData());
                    }
                };
                RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(l);
                
                return (HashMap<Integer, InstCreatData>)
                        l.getClass().getField("map").get(l);
            }
        });
        
        /* Create some objects, get their refs & test if events registered. */
        Object[] refs = runtimeSetup(new Callable<Object[]>() {
            
            /* Create & return an annon class instance in a private method. */
            private Runnable anotherMethod() {
                return new Runnable() {
                    @Override
                    public void run() {}
                };
            }
            
            @Override
            public Object[] call() {
                String.valueOf(5);          /* Insert insn to change ofst. */
                Integer i = new Integer(5); /* Create object Int. */
                
                return new Object[]{i, anotherMethod()};
            }
        });
        
        Identifier idfr = runtimeSetup(new Callable<Identifier>() {
            @Override
            public Identifier call() {
                return InstIdentifier.INSTANCE;
            }
        });
        
        /* Check if map contains info for the Integer object. */
        int i = idfr.getId(refs[0]);
        assertEquals("Intger map entry exists", true, map.containsKey(i));
        
        /* Check if info obj fields correct (one by one). */
        InstCreatData icsInt = (InstCreatData) map.get(i);
        assertEquals("Class corretly read & set", Integer.class,
                icsInt.getClazz());
        assertEquals("Method name correctly read & set", "call",
                icsInt.getMethod());
        assertEquals("Offset correctly read & set", true,
                icsInt.getOffset() == 3);
        assertEquals("Thread id correctly read & set", 1, icsInt.getThreadId());
        
        /* Check if map contains info for the inner anonymous Runnable obj. */
        int r = idfr.getId(refs[1]);
        assertEquals("Rnbl map entry exists", true, map.containsKey(r));
        
         /* Check if info obj fields correct (one by one). */
         InstCreatData icsRnbl = (InstCreatData) map.get(r);
        // Not testing class constant as it's an inner tpye and not Runnable.
        assertEquals("Method name correctly read & set", "anotherMethod",
                icsRnbl.getMethod());
        assertEquals("Offset correctly read & set", true,
                icsRnbl.getOffset() == 0);
        assertEquals("Thread id correctly read & set", 1,
                icsRnbl.getThreadId());
    }

    /**
     * Tests the Settings singleton class
     * TODO: move to agent test package
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
        Integer[] objIds = runtimeSetup(new Callable<Integer[]>() {
            @Override
            public Integer[] call() {
                
                /*Check whether all objects are assigned unique identifiers */
                /* Create some objects */
                Object obj = new Object();
                String str = new String("A string");
                Integer itg = new Integer(1);
                ConcurrentHashMap hash = new ConcurrentHashMap();
                MickeyMaus mickey = new MickeyMaus(2); /* an inner class */

                /* Create an array of object identifiers */
                Integer[] objIds = new Integer[5];
                //InstIdentifier.INSTANCE.put(obj, InstIdentifier.getNextID());

                objIds[0] =  InstIdentifier.INSTANCE.getId(obj);
                objIds[1] =  InstIdentifier.INSTANCE.getId(str);
                objIds[2] =  InstIdentifier.INSTANCE.getId(itg);
                objIds[3] =  InstIdentifier.INSTANCE.getId(hash);
                objIds[4] =  InstIdentifier.INSTANCE.getId(mickey);
                
                return objIds;
            }
        });
        
        /* The IDs should be non-negative and unique */
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(objIds[i] >= 1);
            for (int j = 0; j < i; j++) {
                Assert.assertFalse(objIds[i] == objIds[j]);
            }
        }
        aes.shutdown();
        tec.shutdown();
    }
}
