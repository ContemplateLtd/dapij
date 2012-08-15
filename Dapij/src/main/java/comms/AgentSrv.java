package comms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * A server that allows the agent to communicate runtime information to one
 * external client on the network.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AgentSrv extends NetworkNode {

    private ServerSocketChannel srvSockChnl;
    private Selector selector;

    public AgentSrv(String host, int port, long soTimeout, long attemptInerval, int attempts) {
        super(host, port, 1000, 5000, 3);
        setName("agnt-server");
    }

    public AgentSrv(ServerSocketChannel srvSockChnl, Selector selector) {
        this(srvSockChnl.socket().getInetAddress().getHostAddress(),
                srvSockChnl.socket().getLocalPort(), 1000, 5000, 3);
        this.srvSockChnl = srvSockChnl;
        this.selector = selector;
    }

    @Override
    public void run() {
        if (srvSockChnl == null || !srvSockChnl.isOpen() || !srvSockChnl.isRegistered()) {
            bind(); /* Re/bind server not initialised or dead. */
        }
        while (isRunning()) {
            try {
                selector.select();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Iterator<SelectionKey> i = selector.selectedKeys().iterator();
            while (i.hasNext()) {
                SelectionKey k = i.next();
                i.remove();
                if (k.isAcceptable()) {
                    accept(k);
                }
                if (k.isWritable()) {
                    flushOutMsgQ(k);
                }
            }
        }

        /* Shutdown gracefully. */
        stdoutPrintln("Shutting down server on [" + getHost() + ":" + getPort() + "] ...");
        if (selector.isOpen()) {
            try {
                for (SelectionKey k : selector.keys()) {
                    k.channel().close();
                }
                selector.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (srvSockChnl != null && srvSockChnl.isOpen()) {
            try {
                srvSockChnl.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        selector = null;
        srvSockChnl = null;
        stdoutPrintln("Done.");
    }

    /* Waits for a client connections && resets the server if dead. */
    private boolean accept(SelectionKey k) {
        SocketChannel client = null;
        try {
            client = srvSockChnl.accept();
            if (client == null) {
                return false;
            }
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            return false; /* Ignore & continue. */
        }
        stdoutPrintln("Client [" + client.socket().getRemoteSocketAddress() + "] connected ...");
        return true;
    }

    private synchronized void flushOutMsgQ(SelectionKey k) {
        SocketChannel client = (SocketChannel) k.channel();
        boolean failed = false;
        while (!failed && !getOutMsgQ().isEmpty()) {
            ByteBuffer msg = getOutMsgQ().peek();
            try {
                if (write(client, msg)) {
                    getOutMsgQ().poll();
                } else {
                    failed = true;
                }
            } catch (IOException e) {
                /* TODO: Check channel - may have been closed. */
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Tries to bind the server to the specified port. If srvSockChnl was
     * previously used, attempt to close it, set it to null and then try.
     */
    private void bind() {
        int i = 1;
        while (isRunning()) {
            if (i >= getAttempts()) {
                throw new RuntimeException("Could not bind to [" + getHost() + ":" + getPort()
                        + "] ...");
            }
            try {
                stdoutPrintln("(" + String.valueOf(i++) + ") Binding to [" + getHost() + ":"
                        + getPort() + "] ...");
                srvSockChnl = ServerSocketChannel.open();
                srvSockChnl.configureBlocking(false);
                srvSockChnl.socket().bind(new InetSocketAddress(getHost(), getPort()));
                if (selector == null || !selector.isOpen()) {
                    selector = Selector.open();
                }
                srvSockChnl.register(selector, SelectionKey.OP_ACCEPT);
                return;
            } catch (IOException e) {
                stdoutPrintln("Could not bind, trying again after " + getAttemptInterval()
                        + " seconds ...");
                try {
                    Thread.sleep(getAttemptInterval() * 1000);
                } catch (InterruptedException ex) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }

    /**
     * Attempts to bind and accept (with a timeout) one client connection.
     *
     * @param host
     *            The host address to bind to.
     * @param port
     *            The host port to bind to.
     * @param attempts
     *            Number of times to wait for a client with timeout soTimeout.
     * @param soTimeout
     *            The timeout to wait for a client.
     * @return An initialised (bounded, with one client connection) and not yet
     *         started AgentSrv server object.
     */
    public static AgentSrv blockingConnect(String host, int port, int attempts, int soTimeout) {
        ServerSocketChannel srvSockChnl = null;
        SocketChannel cliSockChnl = null;
        Selector selector = null;
        try {
            srvSockChnl = ServerSocketChannel.open();
            srvSockChnl.configureBlocking(false);
            selector = Selector.open();
            srvSockChnl.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (int i = 1; i <= attempts; i++) {
            try {
                stdoutPrintln("(" + i + ") Binding to [" + host + ":" + port + "] ...");
                srvSockChnl.socket().bind(new InetSocketAddress(host, port));
                stdoutPrintln("Done.");
            } catch (IOException ex) {
                stdoutPrintln("Could not bind, attempting again ...");
                // TODO: timeout?
                continue;
            }
            while (i <= attempts) {
                try {
                    stdoutPrintln("(" + i + ") Listening ...");
                    selector.select(soTimeout);
                    Set<SelectionKey> ks = selector.keys();
                    if (ks.size() < 1) {
                        i++;
                        continue;   /* Wait for a client for another soTimeout seconds. */
                    }
                    for (SelectionKey k : ks) {
                        if (k.isAcceptable()) {
                            cliSockChnl = srvSockChnl.accept();
                            cliSockChnl.configureBlocking(false);
                            cliSockChnl.register(selector, SelectionKey.OP_WRITE);
                            stdoutPrintln("Client [" + cliSockChnl.socket().getRemoteSocketAddress()
                                    + "] connected ...");
                            return new AgentSrv(srvSockChnl, selector);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); /* TODO: remove after debugging. */
                } finally {
                    i++;
                }
            }
        }
        throw new RuntimeException("No client connected ...");
    }
}
