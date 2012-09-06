package transform;

import agent.InstanceIdentifier;
import agent.RuntimeEventSource;
import java.util.HashMap;
import java.util.concurrent.Callable;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import testutils.TransformerTest;

/* TODO: Test InstructionOffsetVisitor. */

/**
 * A class containing tests for the {@code dapij.transform} package.
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

        /* Get map with ids for created instances. */
        HashMap<Long, InstanceCreationData> map = noInstrSetup(
                new Callable<HashMap<Long, InstanceCreationData>>() {

            @SuppressWarnings("unchecked")
            @Override
            public HashMap<Long, InstanceCreationData> call() throws Exception {

                /* Create a listener to register instance creations. */
                CreationEventListener l = new CreationEventListener() {

                    public HashMap<Long, InstanceCreationData> map =
                            new HashMap<Long, InstanceCreationData>();

                    @Override
                    public void handleCreationEvent(CreationEvent e) {

                        /* Collect creation data in a map on each creation event. */
                        map.put(e.getCreatData().getObjId(), e.getCreatData());
                    }
                };
                RuntimeEventSource.INSTANCE.getCreatEventSrc().addListener(l);

                return (HashMap<Long, InstanceCreationData>) l.getClass().getField("map").get(l);
            }
        });

        /* Create some instances, get their refs & test if the creations were registered. */
        Object[] refs = instrSetup(new Callable<Object[]>() {

            /* Returns a new Runnable object. */
            private Runnable anotherMethod() {
                final int i = 1; /* Use this to make Runnable's constructor non-default. */

                return new Runnable() {

                    @Override
                    public void run() {
                        Integer.valueOf(i);
                    }
                };
            }

            @Override
            public Object[] call() {
                String.valueOf(5);          /* Insert instruction to change offset. */
                Integer i = new Integer(5); /* Create an Integer object. */

                return new Object[] { i, anotherMethod() }; /* Return refs of created objects. */
            }
        });
        Identifier idfr = noInstrSetup(new Callable<Identifier>() {

            @Override
            public Identifier call() {
                return InstanceIdentifier.INSTANCE;
            }
        });

        /* Check if map contains info for the Integer object. */
        long i = idfr.getId(refs[0]);
        assertEquals("Intger map entry exists", true, map.containsKey(i));

        /* Check if info obj fields correct (one by one). */
        InstanceCreationData icsInt = (InstanceCreationData) map.get(i);
        assertEquals("Class corretly read & set", Integer.class.getName(), icsInt.getClassName());
        assertEquals("Method name correctly read & set", "call", icsInt.getMethod());
        assertEquals("Offset correctly read & set", true, icsInt.getOffset() == 3);
        assertEquals("Thread id correctly read & set", 1, icsInt.getThdId());

        /* Check if map contains info for the inner anonymous Runnable obj. */
        long r = idfr.getId(refs[1]);
        assertEquals("Rnbl map entry exists", true, map.containsKey(r));

        /* Check if info obj fields correct (one by one). */
        InstanceCreationData icsRnbl = (InstanceCreationData) map.get(r);
        assertEquals("Class corretly read & set", "transform.TransformationTest$2$1",
                icsRnbl.getClassName());
        assertEquals("Method name correctly read & set", "anotherMethod", icsRnbl.getMethod());
        assertEquals("Offset correctly read & set", 2, icsRnbl.getOffset());
        assertEquals("Thread id correctly read & set", 1, icsRnbl.getThdId());
    }
}
