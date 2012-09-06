package comms;

import static junit.framework.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;
import org.junit.Test;
import comms.CommsProtocol.AccsMsg;
import comms.CommsProtocol.CreatMsg;
import testutils.LoggingTestClient;
import testutils.TransformerTest;
import transform.InstanceAccessData;
import transform.InstanceCreationData;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AgentServerTest extends TransformerTest {

    /**
     * Test event client for detecting corrupt messages.
     *
     * @throws InterruptedException
     * TODO: Test needs to be completed.
     */
    @Test
    public void agentEventServerTest() throws InterruptedException {

        /* Start client first, as srv blocking start. */
        LoggingTestClient client = new LoggingTestClient(CommsProtocol.HOST, CommsProtocol.PORT);
        client.setDaemon(true);
        client.start();
        AgentServer server = AgentServer.blockingConnect(CommsProtocol.HOST, CommsProtocol.PORT,
                5000, 3000, 3);
        server.setDaemon(true);
        server.start();

        /* TODO: Send a correct message, check if correctly received. */
        server.blockSnd(AccsMsg.construct(new InstanceAccessData(5, 1)));

        server.blockSnd(ByteBuffer.wrap(new byte[]{5})); /* Construct & send corrupt msg. */

        /* TODO: Check if client recovered by sending another correct message. */

        server.shutdown();
        client.shutdown();
        try {
            server.join();
            client.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals("AgentEventSrv works: ", true, true); /* TODO: Write appropriate asserts. */
    }

    /**
     * Test if all sent messages are received. Test if msg reception order
     * matches order of generation.
     *
     * @throws InterruptedException
     */
    @Test
    public void eventOrderTest() throws InterruptedException {

        /* Start client first, as srv blocking start. */
        LoggingTestClient client = new LoggingTestClient(CommsProtocol.HOST, CommsProtocol.PORT);
        client.setDaemon(true);
        client.start();
        AgentServer server = AgentServer.blockingConnect(CommsProtocol.HOST, CommsProtocol.PORT,
                5000, 3000, 3);
        server.setDaemon(true);
        server.start();

        /* Send a 1000 different messages. */
        int nrMsgs = 1000;
        ByteBuffer[] testMessages = new ByteBuffer[nrMsgs];
        for(int i = 0; i < nrMsgs; i++) {
            if(i % 3 == 0) {
                testMessages[i] = CreatMsg.construct(
                        new InstanceCreationData(i, String.class.getName(), "Method" + i, 2*i,
                                3*i));
                server.blockSnd(testMessages[i]);
            }
            else if(i % 3 == 1) {
                testMessages[i] = CreatMsg.construct(
                        new InstanceCreationData(i, Integer.class.getName(), "Method" + i, 2*i,
                                3*i));
                server.blockSnd(testMessages[i]);
            }
            else if(i % 3 == 2) {
                testMessages[i] = AccsMsg.construct(new InstanceAccessData(i, 4*i));
                server.blockSnd(testMessages[i]);
            }
        }
        server.shutdown();
        client.shutdown();
        try {
            server.join();
            client.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<byte[]> receivedMessages = client.getEventLog();

        /* Check if all messages received. */
        assertEquals("Number msgs sent same as number of msgs received: ", testMessages.length,
                receivedMessages.size());

        /* Check if all messages received in correct order */
        int i = 0;
        while (i < nrMsgs) {
            assertEquals("Message order correct: ", true,
                    Arrays.equals(testMessages[i].array(), receivedMessages.get(i)));
            i++;
        }
    }
}
