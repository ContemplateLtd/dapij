package transform;

import agent.InstIdentifier;
import agent.RuntmEventSrc;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import testutils.TransfmrTest;

/* TODO: test InsnOfstVisitor, InstIdentifier (for concurrency & consistency). */

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
        HashMap<Integer, InstCreatData> map = noInstrSetup(
                new Callable<HashMap<Integer, InstCreatData>>() {

            @SuppressWarnings("unchecked")
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

                return (HashMap<Integer, InstCreatData>) l.getClass().getField("map").get(l);
            }
        });

        /* Create some objects, get their refs & test if events registered. */
        Object[] refs = instrSetup(new Callable<Object[]>() {

            /* Create & return an annon class instance in a private method. */
            private Runnable anotherMethod() {
                final int i = 1;

                /* Create a runnable obj, use a final lcl var to generate access in constructor. */
                return new Runnable() {

                    @Override
                    public void run() {
                        Integer.valueOf(i);
                    }
                };
            }

            @Override
            public Object[] call() {
                String.valueOf(5);          /* Insert insn to change ofst. */
                Integer i = new Integer(5); /* Create object Int. */

                return new Object[] { i, anotherMethod() };
            }
        });
        Identifier idfr = noInstrSetup(new Callable<Identifier>() {

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
        assertEquals("Class corretly read & set", Integer.class, icsInt.getClazz());
        assertEquals("Method name correctly read & set", "call", icsInt.getMethod());
        assertEquals("Offset correctly read & set", true, icsInt.getOffset() == 3);
        assertEquals("Thread id correctly read & set", 1, icsInt.getThreadId());

        /* Check if map contains info for the inner anonymous Runnable obj. */
        int r = idfr.getId(refs[1]);
        assertEquals("Rnbl map entry exists", true, map.containsKey(r));

        /* Check if info obj fields correct (one by one). */
        InstCreatData icsRnbl = (InstCreatData) map.get(r);
        // TODO: test if subclass of Runnable
        // ArrayList(this.getClass().getClasses()).containes(Runnable.class);
        assertEquals("Method name correctly read & set", "anotherMethod", icsRnbl.getMethod());
        assertEquals("Offset correctly read & set", 2, icsRnbl.getOffset());
        assertEquals("Thread id correctly read & set", 1, icsRnbl.getThreadId());
    }

    public static class MickeyMaus {
        int field;

        public MickeyMaus(int field) {
            this.field = field;
        }

        public int getTheField() {
            return field;
        }
    }

    /*
     * TODO: FIX: generate multiple objects of a not instrumented type (to test IDs recored in
     * the InstIdentifier concurrent map. Generate multiple objects of an inner class
     * (e.g. MickeyMaus) to test ids instrumented.
     */
    @Test
    public void objectIDTest() throws Exception {
        Integer[] objIds = instrSetup(new Callable<Integer[]>() {

            @Override
            public Integer[] call() {

                /* Create many objects. */
                Object obj = new Object();
                String str = new String("A string");
                Integer itg = new Integer(1);
                ConcurrentHashMap<String, String> hash = new ConcurrentHashMap<String, String>();
                MickeyMaus mickey = new MickeyMaus(2); /* an inner class */

                /* Record their object identifiers in an array. */
                Integer[] objIds = new Integer[5];
                objIds[0] = InstIdentifier.INSTANCE.getId(obj);
                objIds[1] = InstIdentifier.INSTANCE.getId(str);
                objIds[2] = InstIdentifier.INSTANCE.getId(itg);
                objIds[3] = InstIdentifier.INSTANCE.getId(hash);
                objIds[4] = InstIdentifier.INSTANCE.getId(mickey);

                return objIds;
            }
        });

        /* Check if all assigned identifiers are non-negative & unique.  */
        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(objIds[i] >= 1);
            for (int j = 0; j < i; j++) {
                Assert.assertFalse(objIds[i] == objIds[j]);
            }
        }
    }
}
