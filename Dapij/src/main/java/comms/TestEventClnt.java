/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A network client used for testing the agent's network server.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TestEventClnt extends Thread {

    private static final String nm = "TEC";
    private Socket conn;
    private String host;
    private int port;
    private boolean allowedToRun;
    private boolean stopped;
    private DataInputStream inFromServer;
    //private DataOutputStream outToServer;
    
    public TestEventClnt(String host, int port) {
        this.host = host;
        this.port = port;
        this.allowedToRun = true;
        this.stopped = false;
        setName("test-event-cli");
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
                    System.out.println(nm + ": Connecting to server [" +
                            host + ":" + port + "] ...");
                    conn = new Socket(host, port);
                }
                inFromServer = new DataInputStream(conn.getInputStream());
                System.out.println(nm + ": Done.");
                break;
            } catch (IOException e) {
                System.out.println(nm + ": Could not connect to '" + host +
                        ":" + port + "' ..." + "\n" + nm +
                        ": Attempting again ...");
                try { Thread.sleep(10); } catch (Exception ex) {} /* Ignore */
                continue;
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
                if (inFromServer.available() > 5) {
                    event = readEvent();
                } else {
                    try { Thread.sleep(5); } catch (InterruptedException ex) {}
                    continue;
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO: ignore?
            }
            if (event != null) {
                System.out.println(nm + ": RCV: " + event);
            }
            event = null;
        }
        stopped = true;
    }
    
    public void shutdown() {
        /* Stop main loop. */
        allowedToRun = false;
        for (; !stopped; yield());  /* now wait until stopped. */
        
        /* Shutdown. */
        String event = null;
        System.out.println(nm + ": Shutting down, fetching last messages ...");

        while (true) {
            try {
                // TODO: remove this workaround and add nonblocking sockets
                if (inFromServer.available() > 5) {
                    event = readEvent();
                    if (event != null) {
                        System.out.println(nm + ": RCV: " + event);
                        event = null;
                    }
                } else {
                    break;
                }
            } catch (IOException e) {
                System.out.println(nm + ": Could not read during shutdown ...");
                throw new RuntimeException(e);
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
                inFromServer.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                inFromServer = null;
            }
        }
        
        /* Uncomment when sending msgs to server needed/supported
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

    private String readEvent() {
        String event = null;
        try {
            byte type = inFromServer.readByte();
            int rest = inFromServer.readInt();
            byte [] msg = new byte[rest];
            inFromServer.readFully(msg);
            if (type == CommsProto.TYP_CRT) {
                event = CommsProto.deconstCreatMsg(msg);
            } else if (type == CommsProto.TYP_ACC) {
                event = CommsProto.deconstAccsMsg(msg);
            }
        } catch (Exception e) {
        } finally {
            return event;
        }
    }
}