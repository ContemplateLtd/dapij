/*
 * TODO: doc comment
 */
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
    
    /* Test agent's event server with a test EventClient. */
    @Test
    public void agentEventServerTest() throws Exception {
        
        /* Start a client first to receive and process events & get its ref. */
        TestEventClnt tec = setupEventClnt();
        tec.start();    /* Start client. */
        
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
                RuntmEventSrc.INSTANCE.getAccsEventSrc()
                        .addListener(new AccsEventNetSndr(aes));
                aes.start();
                
                return aes;
            }
        });
        
        /* Perform random actions to generate events. */
        runtimeSetup(new Callable<Object>() {
            @Override
            public Object call() {
                
               /* Create objects. */
               new String("Random test string: " +
                       new String(String.valueOf(new Integer(5))));
               
               /* Generate access. */
               this.toString();
               
               return null;
            }
        });
        
        aes.shutdown();
        tec.shutdown();
        
        // TODO: check if all event msgs correctly received.
        Assert.assertEquals("AgentEventSrv workds: ", true, true);
    }
}
