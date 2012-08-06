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

import agent.Settings;

/**
 * A server that allows the agent to communicate runtime information to one
 * external client on the network.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AgentEventSrv extends Thread {

    public static final String NM = "AES";
    private static boolean quiet = shouldBeQuiet();
    private DataOutputStream outToClient;   /* output stream to client */
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
        this.stopped = false;
        this.srvSoTimeout = 5;
        this.connSoTimeout = 5;
    }

    public AgentEventSrv(int port) {
        this();
        setName("agent-event-srv");
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
                stdoutPrintln(NM + ": " + "[" + String.valueOf(i++) + "] Binding on port '"
                        + port + "' ...");
                srvSock = new ServerSocket(port);
                srvSock.setSoTimeout(srvSoTimeout);
                if (srvSock.isBound()) {
                    stdoutPrintln(NM + ": Done ...");

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
            stdoutPrintln(NM + ": Could not connect, trying again after " + attpemtInterval
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
                    stdoutPrintln(NM + ": Listening for clients ...");
                    conn = srvSock.accept();
                    stdoutPrintln(NM + ": Client [" + conn.getRemoteSocketAddress()
                            + "] connected ...");
                }

                /* always try to reset this */
                outToClient = new DataOutputStream(conn.getOutputStream());

                return;
            } catch (IOException e) {   /* could enter due to a timeout exception */
                bind();                 /* Rebind srvSock if not usable. */
                closeConn();            /* Reset conn & outToClient if not usable. */
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
        stdoutPrintln(NM + ": Shutting down server on port " + srvSock.getLocalPort() + " ...");
        closeConn();
        if (srvSock != null && !srvSock.isClosed()) {
            try {
                srvSock.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        srvSock = null;
        stdoutPrintln(NM + ": " + "Done.");
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
            stdoutPrintln("Could not send message!");
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

    /**
     * Attempts to bind and accept (in a blocking manner with a timeout) a
     * client connection.
     *
     * @param srvSock
     *            An initialised ServerSocket object.
     * @param port
     *            Port to bind the server socket to.
     * @param attempts
     *            Number of times to wait for a client with timeout soTimeout.
     * @param soTimeout
     *            The timeout to wait for a client.
     * @return The a Socket of a successful client connection.
     */
    public static Socket blockingConnect(ServerSocket srvSock, int attempts,
            int soTimeout) {
        Socket sock = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                stdoutPrintln(NM + ": [" + i + "] AgentEventServer: Binding on port '"
                        + CommsProto.PORT + "'.");
                srvSock.setSoTimeout(soTimeout);
                stdoutPrintln(NM + ": AgentEventServer: Done.");
                stdoutPrintln(NM + ": AgentEventServer: Listening for clients ...");
                sock = srvSock.accept();
                stdoutPrintln(NM + ": AgentEventServer: Client ["
                        + sock.getRemoteSocketAddress() + "] connected ...");
                break;
            } catch (IOException ex) {
                stdoutPrintln(NM + ": AgentEventServer: Could not connect, trying again ...");
                continue;
            }
        }
        return sock;
    }

    private static boolean shouldBeQuiet() {
        String q = Settings.INSTANCE.get(Settings.SETT_QUIET_NET);

        /* By default no messages are sent to stdout. */
        return (q == null || (q != null && !q.equals("false"))) ? true : false;
    }

    private static void stdoutPrintln(String msg) {
        if (!quiet) {
            System.out.println(msg);
        }
    }
}
