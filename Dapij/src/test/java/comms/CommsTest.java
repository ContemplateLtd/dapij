package comms;

import static junit.framework.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;
import org.junit.Test;
import comms.proto.AccsMsg;
import comms.proto.CommsProto;
import comms.proto.CreatMsg;
import testutils.TransfmrTest;
import transform.InstAccsData;
import transform.InstCreatData;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CommsTest extends TransfmrTest {

    /* TODO: Before test - generate random port within some predetermined range? */

    /**
     * Test event client for detecting corrupt messages.
     * @throws InterruptedException
     * TODO: Complete this test.
     */
    @Test
    public void agentEventServerTest() throws InterruptedException {

        /* Start client first, as srv blocking start. */
        TestClnt client = new TestClnt(CommsProto.HOST, CommsProto.PORT);
        client.setDaemon(true);
        client.start();
        AgentSrv server = AgentSrv.blockingConnect(CommsProto.HOST, CommsProto.PORT, 5000, 3000, 3);
        server.setDaemon(true);
        server.start();

        /* TODO: Send a correct message, check if correctly received. */
        server.blockSnd(AccsMsg.construct(new InstAccsData(5, 1)));

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
     * @throws InterruptedException
     */
    @Test
    public void EventOrderTest() throws InterruptedException {

        /* Start client first, as srv blocking start. */
        TestClnt client = new TestClnt(CommsProto.HOST, CommsProto.PORT).withMsgLog();
        client.setDaemon(true);
        client.start();
        AgentSrv server = AgentSrv.blockingConnect(CommsProto.HOST, CommsProto.PORT, 5000, 3000, 3);
        server.setDaemon(true);
        server.start();

        /* Send a 1000 different messages. */
        int nrMsgs = 1000;
        ByteBuffer[] testMessages = new ByteBuffer[nrMsgs];
        for(int i = 0; i < nrMsgs; i++) {
            if(i % 3 == 0) {
                testMessages[i] = CreatMsg.construct(
                        new InstCreatData(i, String.class, "Method" + i, 2*i, 3*i));
                server.blockSnd(testMessages[i]);
            }
            else if(i % 3 == 1) {
                testMessages[i] = CreatMsg.construct(
                        new InstCreatData(i, Integer.class, "Method" + i, 2*i, 3*i));
                server.blockSnd(testMessages[i]);
            }
            else if(i % 3 == 2) {
                testMessages[i] = AccsMsg.construct(new InstAccsData(i, 4*i));
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
