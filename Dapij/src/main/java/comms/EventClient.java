package comms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import agent.Settings;
import comms.CommsProtocol.MsgBody;
import comms.CommsProtocol.MsgHeader;

/**
 * A network client for communication with the agent's network server.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class EventClient extends NetworkNode {

    private Selector selector;
    private SocketChannel sockChnl;

    public EventClient(String host, int port, long soTimeout, long attemptInterval, int attempts) {
        super(host, port, soTimeout, attemptInterval, attempts);
        setName("test-client");
    }

    /**
     * Attempts to re/connect to server.
     */
    private void connect() {
        while (isRunning()) {
            try {
                if (sockChnl == null || !sockChnl.isOpen()) {
                    sockChnl = SocketChannel.open();
                    sockChnl.configureBlocking(false);
                }
                if (!sockChnl.isConnected()) {
                    Settings.INSTANCE.println("Connecting to server [" + getHost() + ":"
                            + getPort() + "] ...");
                    sockChnl.connect(new InetSocketAddress(getHost(), getPort()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            /* Attempt to connect with attemptInterval sec intervals. */
            try {
                while (isRunning() && sockChnl.isConnectionPending()) {
                    if (sockChnl.finishConnect()) {
                        break;
                    }
                }
            } catch (Exception e) {
                try {
                    Thread.sleep(getAttemptInterval());
                } catch (Exception ex) {
                    e.printStackTrace(); /* Ignore. */
                }
                Settings.INSTANCE.println("Could not connect to [" + getHost() + ":" + getPort()
                        + "] ...");
                Settings.INSTANCE.println("Attempting again ...");
                continue;
            }
            try {
                if (selector == null || !selector.isOpen()) {
                    selector = Selector.open();
                }
                sockChnl.register(selector, SelectionKey.OP_READ);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Settings.INSTANCE.println("Done.");
            break;
        }
    }

    @Override
    public void run() {
        connect();
        while (isRunning()) {
            listenAndProcess(); /* Fetch remaining events. */
        }

        /* Shutdown gracefully. */
        Settings.INSTANCE.println("Shutting down ...");
        Settings.INSTANCE.println("Fetching remaining messages ...");
        listenAndProcess(); /* Fetch events remaining after shutdown(). */
        try {
            selector.close();
            sockChnl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.INSTANCE.println("Done.");
    }

    private void listenAndProcess() {
        try {
            selector.select(getSoTimeout());
        } catch (IOException e) {
            throw new RuntimeException(e); /* selector must be broken. */
        }
        Iterator<SelectionKey> i = selector.selectedKeys().iterator();

        /* If nothing ready for receive, check channel */
        if (!i.hasNext()) {
            if (isRunning() && (sockChnl == null || !sockChnl.isOpen() || !sockChnl.isConnected()
                    || !sockChnl.isRegistered())) {
                Settings.INSTANCE.println("Connection lost, reconnecting ...");
                connect(); /* Reconnect if connection died. */
            }

            return;
        }
        while (i.hasNext()) {
            SelectionKey k = i.next();
            i.remove();
            if (k.isReadable()) {
                SocketChannel srvChnl = (SocketChannel) k.channel();
                while (readEvent(srvChnl) != null); /* Fetch all events ready for reading. */
            }
        }
    }

    private MsgBody readEvent(SocketChannel srvChnl) {
        try {
            ByteBuffer hdrBuf = read(srvChnl, MsgHeader.SIZE);          /* Read header. */
            if (hdrBuf == null) {

                return null;                                            /* Nothing read. */
            }
            MsgHeader header = MsgHeader.deconstruct(hdrBuf);
            byte msgType = header.getMsgType();
            if (!CommsProtocol.MsgTypes.isSupported(msgType)) {
                throw new RuntimeException("Message type '" + msgType + "' is not supported.");
            }
            ByteBuffer bodyBuf = read(srvChnl, header.getBdySize());    /* Read body. */
            if (bodyBuf == null) {

                return null;                                            /* Nothing read. */
            }

            /* Accesses messages are more frequent, check for them first. */
            MsgBody body = CommsProtocol.deconstructMsg(bodyBuf, msgType);
            onMsgRecv(header, body);

            return body;
        } catch (IOException e) {       /* Channel does not work */

            return null;                /* Continue main loop. */
        } catch (RuntimeException e) {  /* Corrupted message detected. */
            throw new RuntimeException(e);
        }
    }

    protected abstract void onMsgRecv(MsgHeader header, MsgBody body);
}
