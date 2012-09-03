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
import agent.Settings;

/**
 * A server that allows the agent to communicate runtime information to one
 * external client on the network.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AgentServer extends NetworkNode {

    private ServerSocketChannel srvChnl;
    private SocketChannel cliChnl;
    private Selector selector;

    public AgentServer(String host, int port, long soTimeout, long attemptInerval, int attempts) {
        super(host, port, 1000, 5000, 3);
        setName("agnt-server");
    }

    public AgentServer(ServerSocketChannel srvSockChnl, SocketChannel cliChnl, Selector selector) {
        this(srvSockChnl.socket().getInetAddress().getHostAddress(),
                srvSockChnl.socket().getLocalPort(), 1000, 5000, 3);
        this.srvChnl = srvSockChnl;
        this.cliChnl = cliChnl;
        this.selector = selector;
    }

    @Override
    public void run() {
        if (srvChnl == null || !srvChnl.isOpen() || !srvChnl.isRegistered()) {
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

                /* Accept only if no client connected */
                if (cliChnl == null && k.isAcceptable()) {
                    accept(k);
                }
                if (k.isWritable()) {
                    flushOutMsgQ(); /* Flush messages to the single client. */
                }
            }
        }

        /* Shutdown gracefully. */
        Settings.INSTANCE.println("Shutting down server on [" + getHost() + ":" + getPort()
                + "] ...");
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
        if (srvChnl != null && srvChnl.isOpen()) {
            try {
                srvChnl.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        selector = null;
        srvChnl = null;
        Settings.INSTANCE.println("Done.");
    }

    /* Waits for a client connections && resets the server if dead. */
    private boolean accept(SelectionKey k) {
        try {
            cliChnl = srvChnl.accept();
            if (cliChnl == null) {

                return false;
            }
            cliChnl.configureBlocking(false);
            cliChnl.register(selector, SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();

            return false; /* Ignore & continue. */
        }
        Settings.INSTANCE.println("Client [" + cliChnl.socket().getRemoteSocketAddress()
                + "] connected ...");

        return true;
    }

    private synchronized void flushOutMsgQ() {
        ByteBuffer msg;
        while ((msg = getOutMsgQ().peek()) != null) {
            if (write(cliChnl, msg)) {
                getOutMsgQ().poll();    /* Rm msg from queue only if successfully sent. */
            } else {
                break;
            }
        }
    }

    /**
     * Tries to bind the server to the specified port. If {@code srvSockChnl} was
     * previously used, attempt to close it, set it to {@code null} and then try.
     */
    private void bind() {
        int i = 1;
        while (isRunning()) {
            if (i >= getAttempts()) {
                throw new RuntimeException("Could not bind to [" + getHost() + ":" + getPort()
                        + "] ...");
            }
            try {
                Settings.INSTANCE.println("(" + String.valueOf(i++) + ") Binding to [" + getHost()
                        + ":" + getPort() + "] ...");
                srvChnl = ServerSocketChannel.open();
                srvChnl.configureBlocking(false);
                srvChnl.socket().bind(new InetSocketAddress(getHost(), getPort()));
                if (selector == null || !selector.isOpen()) {
                    selector = Selector.open();
                }
                srvChnl.register(selector, SelectionKey.OP_ACCEPT);

                return;
            } catch (IOException e) {
                Settings.INSTANCE.println("Could not bind, trying again after "
                        + getAttemptInterval() + " seconds ...");
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
     *            Number of times to wait for a client with timeout
     *            {@code soTimeout}.
     * @param soTimeout
     *            The timeout to wait for a client.
     * @return An initialised (bounded, with one client connection) and not yet
     *         started {@link AgentServer} server object.
     */
    public static AgentServer blockingConnect(String host, int port, long soTimeout,
            long attemptInterval, int attempts) {
        ServerSocketChannel srvChnl = null;
        SocketChannel cliChnl = null;
        Selector selector = null;
        try {
            srvChnl = ServerSocketChannel.open();
            srvChnl.configureBlocking(false);
            selector = Selector.open();
            srvChnl.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (int i = 1; i <= attempts; i++) {
            try {
                Settings.INSTANCE.println("(" + i + ") Binding to [" + host + ":" + port + "] ...");
                srvChnl.socket().bind(new InetSocketAddress(host, port));
                Settings.INSTANCE.println("Done.");
            } catch (IOException ex) {
                Settings.INSTANCE.println("Could not bind, attempting again ...");
                try {
                    Thread.sleep(attemptInterval);  /* Wait before next bind attempt */
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            while (i <= attempts) {
                try {
                    Settings.INSTANCE.println("(" + i + ") Listening ...");
                    selector.select(soTimeout * attempts);
                    Set<SelectionKey> ks = selector.keys();
                    if (ks.size() < 1) {
                        i++;
                        continue;
                    }
                    for (SelectionKey k : ks) {
                        if (k.isAcceptable()) {
                            cliChnl = srvChnl.accept();
                            cliChnl.configureBlocking(false);
                            cliChnl.register(selector, SelectionKey.OP_WRITE);
                            Settings.INSTANCE.println("Client ["
                                    + cliChnl.socket().getRemoteSocketAddress()
                                    + "] connected ...");

                            return new AgentServer(srvChnl, cliChnl, selector);
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
