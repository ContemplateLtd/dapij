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
 * A server that allows the agent to communicate runtime information to one
 * external client on the network.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AgentEventSrv extends Thread {

    public static final String NM = "AES";
    private DataOutputStream outToClient;   /* stream to client */
    private ServerSocket srvSock;
    private Socket conn;                    /* connection to the (single) client */
    private int port;
    private int srvSoTimeout;               /* timeout for srvSock.accept() */
    private int connSoTimeout;              /* timeout for recv on client conn */
    private int connAttempts = 3;           /* number of attempts to bind to port */
    private int attpemtInterval = 5;        /* time btw attempts (in seconds) */
    private boolean allowedToRun;           /* true while main loop loops */
    private boolean stopped;                /* becomes true when main loop ends */

    public AgentEventSrv() {
        this.allowedToRun = true;
        this.srvSoTimeout = 5;
        this.connSoTimeout = 5;
        setName("agent-event-srv");
    }

    public AgentEventSrv(int port) {
        this.allowedToRun = true;
        this.stopped = false;
        this.port = port;
    }

    public AgentEventSrv(ServerSocket srvSock, Socket conn) {
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
                } catch (IOException e2) {
                    /* Ignore. */
                }
            }
        }
        int i = 0;
        while (allowedToRun) {
            if (i >= connAttempts) {
                throw new RuntimeException(NM + ": Couldn't bind to port '" + port + "' ...");
            }
            try {
                System.out.println(NM + ": " + "[" + String.valueOf(i++) + "] Binding on port '"
                        + port + "' ...");
                srvSock = new ServerSocket(port);
                srvSock.setSoTimeout(srvSoTimeout);
                if (srvSock.isBound()) {
                    System.out.println(NM + ": Done ...");

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
            System.out.println(NM + ": Could not connect, trying again after " + attpemtInterval
                    + " seconds ...");
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
                    System.out.println(NM + ": Listening for clients ...");
                    conn = srvSock.accept();
                    System.out.println(NM + ": Client [" + conn.getRemoteSocketAddress()
                            + "] connected ...");
                }

                /* always try to reset this */
                outToClient = new DataOutputStream(conn.getOutputStream());

                return;

                /* could enter due to a timeout exception */
            } catch (IOException e) {
                bind(); /* Rebind srvSock if not usable. */
                closeConn(); /* Reset conn & outToClient if not usable. */
            }
        }
        stopped = true;
    }

    /**
     * The Event Server's main loop.
     */
    @Override
    public void run() {
        while (allowedToRun) {
            if (conn != null && conn.isConnected()) {
                // TODO: read (with timeout) from conn & do something with msg
                yield();
                continue;
            }
            accept(); /* Wait for client if current conn dropped / not set */
        }
        stopped = true;
    }

    public void shutdown() {
        allowedToRun = false; /* Stop main loop. */

        /* Now wait until stopped. */
        while (!stopped) {
            yield();
        }

        /* Shutdown. */
        System.out.println(NM + ": Shutting down server on port " + srvSock.getLocalPort()
                + " ...");
        closeConn();
        if (srvSock != null && !srvSock.isClosed()) {
            try {
                srvSock.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        srvSock = null;
        System.out.println(NM + ": " + "Done.");
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

    public synchronized void sendEvent(byte[] msg) {
        try {
            outToClient.write(msg);
        } catch (IOException e) {
            // TODO: improve this exception handler
            System.out.println("Could not send message!");
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
     * @param srvSoTimeout
     *            the srvSoTimeout to set
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
     * @param connSoTimeout
     *            the connSoTimeout to set
     */
    public void setConnSoTimeout(int connSoTimeout) {
        this.connSoTimeout = connSoTimeout;
    }
}
