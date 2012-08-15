package comms;

import static junit.framework.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.ByteBuffer;
import org.junit.Test;
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
     */
    @Test
    public void agentEventServerTest() {

        /* Start client first, as srv blocking start. */
        TestClnt client = new TestClnt(CommsProto.HOST, CommsProto.PORT);
        client.setDaemon(true);
        client.start();
        AgentSrv server = AgentSrv.blockingConnect(CommsProto.HOST, CommsProto.PORT, 3, 5000);
        server.setDaemon(true);
        server.start();

        /* TODO: Send a correct message, check if correctly received. */
        server.sendMsg(CommsProto.constructAccsMsg(new InstAccsData(5, 1)));

        server.sendMsg(ByteBuffer.wrap(new byte[]{5})); /* Construct & send corrupt msg. */

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
     */
    @Test
    public void EventOrderTest() {

        /* Start client first, as srv blocking start. */
        TestClnt client = new TestClnt(CommsProto.HOST, CommsProto.PORT).withMsgLog();
        client.setDaemon(true);
        client.start();
        AgentSrv server = AgentSrv.blockingConnect(CommsProto.HOST, CommsProto.PORT, 3, 5000);
        server.setDaemon(true);
        server.start();

        ByteBuffer[] testMessages = new ByteBuffer[100];
        for(int i = 0; i < 100; i++) {
            if(i % 3 == 0) {
                testMessages[i] = CommsProto.constructCreatMsg(
                        new InstCreatData(i, String.class, "Method" + i, 2*i, 3*i));
                server.sendMsg(testMessages[i]);
            }
            else if(i % 3 == 1) {
                testMessages[i] = CommsProto.constructCreatMsg(
                        new InstCreatData(i, Integer.class, "Method" + i, 2*i, 3*i));
                server.sendMsg(testMessages[i]);
            }
            else if(i % 3 == 2) {
                testMessages[i] = CommsProto.constructAccsMsg(new InstAccsData(i, 4*i));
                server.sendMsg(testMessages[i]);
            }
        }

        server.shutdown();
        client.shutdown();

        try {
            server.join();
            client.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ArrayList<byte[]> receivedMessages = client.getEventLog();

        /* Check if all messages received. */
        assertEquals("Number msgs sent same as number of msgs received: ", testMessages.length,
                receivedMessages.size());

        /* Check if all messages received in correct order */
        int i = 0;
        while (i < testMessages.length) {
            assertEquals("Message order correct: ", true,
                    Arrays.equals(testMessages[i].array(), receivedMessages.get(i)));
            i++;
        }
    }
}
