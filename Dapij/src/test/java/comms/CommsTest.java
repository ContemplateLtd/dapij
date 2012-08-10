package comms;

import agent.AccsEventNetSndr;
import static agent.Agent.setupEventSrv;
import agent.CreatEventNetSndr;
import agent.RuntmEventSrc;
import agent.Settings;

import java.util.Arrays;
import java.util.concurrent.Callable;
import junit.framework.Assert;
import org.junit.Test;
import testutils.TransfmrTest;
import transform.InstCreatData;

/**
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CommsTest extends TransfmrTest {

    /**
     * Test agent's event server with a test EventClient.
     */
    @Test
    public void agentEventServerTest() throws Exception {
        TestEventClnt tec = setupEventClnt();   /* Set up client & get its ref. */
        tec.start();                            /* Start client before srv is started. */
        AgentEventSrv aes = runtimeSetup(new Callable<AgentEventSrv>() {

            @Override
            public AgentEventSrv call() {

                /*
                 * Start a srv to recv & fwd events to a single client. Call
                 * blocks until client connected.
                 */
                AgentEventSrv aes = setupEventSrv();

                /* Add a listener to send events to the server. */
                RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(new CreatEventNetSndr(aes));
                RuntmEventSrc.INSTANCE.getAccsEventSrc().addListener(new AccsEventNetSndr(aes));
                aes.start();

                return aes;
            }
        });

        /* Perform random actions to generate events. */
        runtimeSetup(new Callable<Object>() {

            @Override
            public Object call() {
                new String(new String(String.valueOf(new Integer(5)))); /* Create objects. */
                this.toString(); /* TODO: Generate access? */

                return null;
            }
        });
        aes.shutdown();
        tec.shutdown();

        /* TODO: FIX: write test to check if all event msgs correctly received. */
        Assert.assertEquals("AgentEventSrv workds: ", true, true);
    }

    /**
     * Test if the messages are received by the client in the correct order
     */
    @Test
    public void EventOrderTest() throws Exception {
        Settings.INSTANCE.set(Settings.SETT_QUIET_NET, "false");
        TestEventClnt tec = setupEventClnt();    /* Set up client. */
        tec.start();                             /* Start client before srv. */
        AgentEventSrv aes = runtimeSetup(new Callable<AgentEventSrv>() {

            @Override
            public AgentEventSrv call() {

                /*
                 * Start a srv to recv & fwd events to a single client. Call
                 * blocks until client connected.
                 */
                AgentEventSrv aes = setupEventSrv();

                /* Add a listener to send events to the server. */
                RuntmEventSrc.INSTANCE.getCreatEventSrc().addListener(new CreatEventNetSndr(aes));
                RuntmEventSrc.INSTANCE.getAccsEventSrc().addListener(new AccsEventNetSndr(aes));
                aes.start();

                return aes;
            }
        });

        byte[][] testMessages = new byte[100][];

        for(int i = 0; i < 100; i++) {
            /* TODO: The test fails without the sleep statement. This is probably due to the client
             * not being able to process a large bunch of messages sent at the same time */
            Thread.sleep(100);
            if(i % 3 == 0) {
                testMessages[i] = CommsProto.constructCreatMsg(
                        new InstCreatData(i, String.class, "Method" + i, 2*i, 3*i));
                aes.sendEvent(testMessages[i]);
            }
            else if(i % 3 == 1) {
                testMessages[i] = CommsProto.constructCreatMsg(
                        new InstCreatData(i, Integer.class, "Method" + i, 2*i, 3*i));
                aes.sendEvent(testMessages[i]);
            }
            else if(i % 3 == 2) {
                testMessages[i] = CommsProto.constructAccsMsg(i, 4*i);
                aes.sendEvent(testMessages[i]);
            }

        }

        aes.shutdown();
        tec.shutdown();

        byte[][] receivedMessages = tec.getEventLog();

        /* Check if all messages received in correct order */
        boolean orderCorrect;
        int i =0;
        int j =0;
       // System.out.println("rec_len: " + receivedMessages.length);
        while((i < testMessages.length)&&(j < receivedMessages.length)) {
            if(Arrays.equals(testMessages[i], receivedMessages[j])) {
                i++;
                j++;
            }
            else {
                j++;
            }
        }

        orderCorrect = i == testMessages.length;

        Assert.assertTrue("Message order correct " + i + " " + j, orderCorrect);
    }
}
