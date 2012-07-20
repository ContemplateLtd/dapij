/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class EventClient extends Thread {

    private String host;
    private int port;
    private Socket conn;
    private boolean allowedToRun;
    private boolean stopped;
    //private int connAttempts = 3;
    private static final String nm = "dapij-event-cli";   /* thread name */
    
    private BufferedReader inFromServer;
    //private DataOutputStream outToServer;
    
    public EventClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.allowedToRun = true;
        this.stopped = false;
        setName(nm);
    }

    /**
     * Attempts to reconnect if forced and only resets inFromServer otherwise.
     * 
     * @param force A boolean value for enforce connection closure.
     */
    private void connect(boolean force) {
        closeConn(force);   /* close conn if forced or if not set */
        while (allowedToRun) {
            try {
                if (conn == null || !conn.isConnected()) {
                    System.out.println(nm + ": Connectiong to server [" +
                            host + ":" + port + "] ...");
                    conn = new Socket(host, port);
                }
                inFromServer = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
                System.out.println(nm + ": Done.");
                break;
            } catch (IOException e) {
                System.out.println(nm + ": Could not connect to '" + host +
                                    ":" + port + "' ...");
                throw new RuntimeException(e);
            }
        }
        stopped = true;
    }
    
    @Override
    public void run() {
        connect(true);
        String event = null;
        while (allowedToRun) {
            try {
                // TODO: remove this workaround and use a nonblocking socket
                if (inFromServer.ready()) {
                    event = inFromServer.readLine();
                } else {
                    try { Thread.sleep(5); } catch (InterruptedException ex) {}
                    continue;
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO: ignore?
            }
            if (event != null) {
                System.out.println(nm + ": Received event: " + event);
            }
            event = null;
        }
        stopped = true;
    }
    
    public void shutdown() {
        /* Stop main loop. */
        allowedToRun = false;
        for (int i = 0; !stopped; yield()); /* now wait until stopped. */
        
        /* Shutdown. */
        String event = null;
        System.out.println(nm + ": Shutting down, fetching last messages ...");

        while (true) {
            try {
                // TODO: remove this workaround and add nonblocking sockets
                if (inFromServer.ready()) {
                    event = inFromServer.readLine();
                    if (event != null) {
                        System.out.println(nm + ": Received event: " + event);
                        event = null;
                    }
                } else {
                    break;
                }
            } catch (IOException e) {
                System.out.println(nm + ": Could not read during shutdown ...");
                throw new RuntimeException(e); // TODO: ignore
            }
        }
        System.out.println(nm + ": Done.");
    }
    
    /**
     * Closes connection if not usable or forced. Guarantees input stream to
     * server is always closed after call.
     *
     * @param force A boolean value for enforce connection closure.
     */
    private void closeConn(boolean force) {
        if (conn != null && (!conn.isConnected() || force)) {
            try {
                conn.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                conn = null;
            }
        }
        
        /* Always close inFromServer */
        if (inFromServer != null) {
            try {
                // TODO: need to read remaining messages from server?
                inFromServer.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                inFromServer = null;
            }
        }
        
        /* Uncomment when sending msgs to server supported
        if (outToServer != null) {
            try {
                outToServer.flush();
                outToServer.close();
            } catch (IOException e) {
                // Ignore.
            } finally {
                outToServer = null;
            }
        }
        */
    }
}