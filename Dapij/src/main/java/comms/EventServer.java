/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class EventServer extends Thread {
    
    private ServerSocket srvSock;
    private int port;
    private Socket conn;                /* connection to the (single) client */
    DataOutputStream outToClient;       /* stream to client */
    private static final String nm = "dapij-event-srv";   /* thread name */
    
    private int srvSoTimeout;           /* timeout for srvSock.accept() */
    private int connSoTimeout;          /* timeout for recv on client conn */
    
    private boolean allowedToRun;       /* true while main loop loops */
    private boolean stopped;            /* becomes true when main loop ends */
    private int connAttempts = 3;       /* number of attempts to bind to port */
    private int attpemtInterval = 5;    /* time btw attempts (in seconds) */

    public EventServer() {
        this.allowedToRun = true;
        this.srvSoTimeout = 5;
        this.connSoTimeout = 5;
        setName(nm);
    }
    
    public EventServer(int port) {
        this.allowedToRun = true;
        this.stopped = false;
        this.port = port;
    }
    
    public EventServer(ServerSocket srvSock, Socket conn) {
        this(srvSock.getLocalPort());
        this.srvSock = srvSock;
        this.conn = conn;
        
        try {
            outToClient = new DataOutputStream(conn.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setAttempts(int connAttempts) {
        this.connAttempts = connAttempts;
    }
    
    /**
     * Tries to bind the server to the specified port. If srvSock was previously
     * used, attempt to close it, set it to null and then try.
     */
    private void bind() {
        if (srvSock != null && srvSock.isBound() && !srvSock.isClosed()) {
            try {
                srvSock.setSoTimeout(srvSoTimeout);
                return;
            } catch (SocketException e) {
                try {
                    srvSock.close();
                    srvSock = null;
                } catch (IOException e2) {}    /* Ignore. */
            }
        }
        int i = 0;
        while (allowedToRun) {
            if (i >= connAttempts) {
                throw new RuntimeException(nm + ": Couldn't bind to port '" +
                port + "' ...");
            }
            try {
                System.out.println(nm + ": " + "[" + String.valueOf(i++) +
                        "] " + "Binding on port '" + port + "' ...");
                srvSock = new ServerSocket(port);
                srvSock.setSoTimeout(srvSoTimeout);
                if (srvSock.isBound()) {
                    System.out.println(nm + ": Done ...");
                    return;
                }
            } catch (IOException e) {
                /* Ignore. */
            }
            try {
                Thread.sleep(attpemtInterval * 1000); /* wait for 5 sec */
            } catch (InterruptedException ex) {
                /* Ignore. */
            }
            System.out.println(nm + ": Could not connect, trying again after " +
                    attpemtInterval + " seconds ...");
        }
        stopped = true;
    }
    
    /**
     * Reinitialises the single connection (to a client) this server maintains.
     */
    private void accept() {
        closeConn();    /* Reset conn */
        bind();         /* Re/bind srvSock if not usable. */
        
        /* Wait for a client connection with a blocking call. */
        while (allowedToRun) {
            try {
                
                /* reset only if not useable */
                if (conn == null || !conn.isConnected()) {
                    System.out.println(nm + ": Listening for clients ...");
                    conn = srvSock.accept();
                    System.out.println(nm + ": Client [" +
                            conn.getRemoteSocketAddress() + "] connected ...");
                }
                
                /* always try to reset this */
                outToClient = new DataOutputStream(conn.getOutputStream());
                return;
                
            /* could enter due to a timeout exception */
            } catch (IOException e) { 
                bind();         /* Rebind srvSock if not usable. */
                closeConn();    /* Reset conn & outToClient if not usable. */
            }
        }
        stopped = true;
    }
    
    /**
     * Main loop of Event Server
     */
    @Override
    public void run() {
        while (allowedToRun) {
            if (conn != null && conn.isConnected()) {
                // read (with timeout) from conn & do something with msg
                yield(); // TODO: remove when receiving implemented
                continue;
            }
            accept();   /* Wait for client if current conn dropped / not set */
        }
        stopped = true;
    }

    public void shutdown() {
        /* Stop main loop. */
        allowedToRun = false;
        for (int i = 0; !stopped; yield()); /* now wait until stopped. */
        
        /* Shutdown. */
        System.out.println(nm + ": Shutting down server on port " +
                srvSock.getLocalPort() + " ...");
        closeConn();
        if (srvSock != null && !srvSock.isClosed()) {
            try {
                srvSock.close();
            } catch (IOException e) { // TODO: Ignore maybe?
                System.out.println(nm + ": " + e.getMessage());
            }
        }
        srvSock = null;
        System.out.println(nm + ": " + "Done.");
    }
    
    /**
     * Closes connection and its output stream if either of them is not usable.
     */
    private void closeConn() {
        if (outToClient != null) {
            try {
                // TODO: what will happen if caller is shutdown() and client
                // has already shut down at this point? wait for reconnection?
                outToClient.flush();
                outToClient.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                outToClient = null;
            }
        }
        
        if (conn != null && !conn.isConnected()) {
            try {
                conn.close();
            } catch (IOException e) {
                /* Ignore. */
            } finally {
                conn = null;
            }
        }
    }
    
    public synchronized void sendEvent(String event) {
        try {
            outToClient.writeBytes(event);
        } catch (IOException e) {
            // TODO: improve this exception "handgling"
            System.out.println("Could not send event: '" + event + "'!");
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the srvSoTimeout
     */
    public int getSrvSoTimeout() {
        return srvSoTimeout;
    }

    /**
     * @param srvSoTimeout the srvSoTimeout to set
     */
    public void setSrvSoTimeout(int srvSoTimeout) {
        this.srvSoTimeout = srvSoTimeout;
    }

    /**
     * @return the connSoTimeout
     */
    public int getConnSoTimeout() {
        return connSoTimeout;
    }

    /**
     * @param connSoTimeout the connSoTimeout to set
     */
    public void setConnSoTimeout(int connSoTimeout) {
        this.connSoTimeout = connSoTimeout;
    }
}
