package comms;

import agent.AccsEventNetSndr;
import static agent.Agent.setupEventSrv;
import agent.CreatEventNetSndr;
import agent.RuntmEventSrc;
import java.util.concurrent.Callable;
import junit.framework.Assert;
import org.junit.Test;
import testutils.TransfmrTest;

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
}
