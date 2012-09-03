package agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.Test;
import testutils.TransformerTest;

/* TODO: test for garbage collected objects. */

public class InstanceIdentifierTest extends TransformerTest {

    /**
     * A class for testing purposes.
     *
     * @author Marcin Szymczak <mpszymczak@gmail.com>
     */
    public static class MickeyMaus {
        private int field;

        public MickeyMaus(int field) {
            this.field = field;
        }

        public int getTheField() {
            return field;
        }
    }

    @Test
    public void validityTest() throws Exception {
        Long[] objIds = instrSetup(new Callable<Long[]>() {

            @Override
            public Long[] call() {
                Long[] objIds = new Long[10000];  /* Stores generated ids. */

                /* Create references. */
                Object obj;
                String str;
                Integer itg;
                ConcurrentHashMap<Integer, String> hash;

                /* Create many objects & store their obj ids into an array. */
                for (int i = 0; i < 10000; i++) {
                    switch (i % 4) {
                    case 0:
                        obj = new Object();
                        objIds[i] = InstanceIdentifier.INSTANCE.getId(obj);
                        break;
                    case 1:
                        str = new String("A string");
                        objIds[i] = InstanceIdentifier.INSTANCE.getId(str);
                        break;
                    case 2:
                        itg = new Integer(1);
                        objIds[i] = InstanceIdentifier.INSTANCE.getId(itg);
                        break;
                    case 3:
                        hash = new ConcurrentHashMap<Integer, String>();
                        objIds[i] = InstanceIdentifier.INSTANCE.getId(hash);
                        break;
                    default:
                        break;
                    }
                }

                return objIds;
            }
        });

        /* Check if all assigned identifiers are non-negative & unique. */
        for (int i = 0; i < objIds.length; i++) {
            Assert.assertTrue(objIds[i] >= 1);
            for (int j = 0; j < i; j++) {
                Assert.assertFalse(objIds[i] == objIds[j]);
            }
        }
    }

    @Test
    public void concurrencyTest() throws Exception {

        /* Create & instrument a class that starts several equivalent generator threads. */
        ArrayList<Long> objIds = instrSetup(new Callable<ArrayList<Long>>() {

            /**
             * An internal class that defines a task for creating multiple
             * objects of instrumented types and returning their identifiers as
             * a {@link Long}{@code []}.
             *
             * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
             */
            class GeneratorOfNotInstrumentedObjects implements Callable<List<Long>> {

                private int nrItems = 0;

                public GeneratorOfNotInstrumentedObjects(int nrItems) {
                    this.nrItems = nrItems;
                }

                @Override
                public List<Long> call() {
                    List<Long> objIds = new ArrayList<Long>(nrItems);   /* Stores generated ids. */
                    Object obj = null;                                  /* a reusable reference */

                    /* Create many objects & store their obj ids into an array. */
                    for (int i = 0; i < nrItems; i++) {
                        switch (i % 4) {
                        case 0:
                            obj = new Object();
                            break;
                        case 1:
                            obj = new String("A string");
                            break;
                        case 2:
                            obj = new Integer(1);
                            break;
                        case 3:
                            obj = new ConcurrentHashMap<Integer, String>();
                            break;
                        default:
                            break;
                        }
                        objIds.add(new Long(InstanceIdentifier.INSTANCE.getId(obj)));
                    }

                    return objIds;
                }
            }

            /**
             * An internal class that defines a task for creating multiple
             * objects of uninstrumented types and returning their identifiers
             * as a {@link Long}{@code []}.
             *
             * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
             */
            class GeneratorOfInstrumentedObjects implements Callable<List<Long>> {

                private int nrItems = 0;

                public GeneratorOfInstrumentedObjects(int nrItems) {
                    this.nrItems = nrItems;
                }

                @Override
                public List<Long> call() {
                    List<Long> objIds = new ArrayList<Long>(nrItems);   /* Stores generated ids. */
                    Object obj = null;                                  /* a reusable reference */

                    /* Create many objects & store their obj ids into an array. */
                    for (int i = 0; i < nrItems; i++) {
                        obj = new MickeyMaus(5);
                        objIds.add(new Long(InstanceIdentifier.INSTANCE.getId(obj)));
                    }

                    return objIds;
                }
            }

            @Override
            public ArrayList<Long> call() {
                ExecutorService pool = Executors.newFixedThreadPool(4); /* Allow 4 threads. */
                ArrayList<Future<List<Long>>> futures = new ArrayList<Future<List<Long>>>();
                for (int i = 0; i < 100; i++) {
                    Callable<List<Long>> c = new GeneratorOfNotInstrumentedObjects(100);
                    Callable<List<Long>> c2 = new GeneratorOfInstrumentedObjects(100);
                    Future<List<Long>> f = pool.submit(c);
                    Future<List<Long>> f2 = pool.submit(c2);
                    futures.add(f);
                    futures.add(f2);
                }

                ArrayList<Long> result = new ArrayList<Long>();
                for (Future<List<Long>> future : futures) {
                    try {
                        result.addAll(future.get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                return result;
            }
        });

        /* Check if all assigned identifiers are non-negative & unique. */
        for (int i = 0; i < objIds.size(); i++) {
            Assert.assertEquals("ID greater than 0: ", true, objIds.get(i) > (long) 0);
            for (int j = i + 1; j < i; j++) {
                Assert.assertEquals("IDs different: ", true, objIds.get(i) != objIds.get(j));
            }
        }
    }
}
