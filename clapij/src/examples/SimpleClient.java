package examples;

import java.io.IOException;

import utils.Message;
import frontend.EventListener;
import frontend.EventStreamReader;

/*
 * This is a very simple client demonstrating the usage of the dapij client API.
 */
public class SimpleClient {

    public static void main(String[] args) throws IOException {

        /* Show the network messages */
        Message.turnOnNetworkMessages();

        /* Create a stream reader using the default host and port */
        EventStreamReader client = new EventStreamReader();

        /* 
         * Create a new event listener and define the actions taken on receiving creation and
         * access events.
         */
        EventListener el = new EventListener() {

            @Override
            public void onCreation(long objId, String className, String method, 
                    int offset, long threadId) {
                System.out.println("Creation: id:" + objId + " class:" + className + " method:"
                        + method + " offset:" + offset + " thread: " + threadId);
            }

            @Override
            public void onAccess(long objId, long thdId) {
                System.out.println("Access: id:" + objId + " thread:" + thdId);
            }

        };

        /* Attach the custom listener to the stream reader/ */
        client.setListener(el);
        client.setDaemon(true);

        /* Start the client. */
        client.start();

        while(client.isAlive()); /* Wait for the client to terminate. */

        try {
            client.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
