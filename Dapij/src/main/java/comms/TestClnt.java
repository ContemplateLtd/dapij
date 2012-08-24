package comms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import agent.Settings;

import comms.proto.CommsProto;
import comms.proto.Message;
import comms.proto.MsgHeader;

import utils.Helpers;

/**
 * A network client used for testing the agent's network server.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class TestClnt extends NetworkNode {

    private Selector selector;
    private SocketChannel sockChnl;
    private ArrayList<byte[]> msgLog;

    public TestClnt(String host, int port) {

        /*
         * Manual tests suggest a soTimout value of 500 results in discarding
         * late messages (during fetching of last messages by client).
         */
        super(host, port, 100, 2000, 5);
        setName("test-client");
    }

    /**
     * Attempts to reconnect if forced and only resets inFromServer otherwise.
     *
     * @param force
     *            A boolean value for enforce connection closure.
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
            listenAndProcess(false); /* Fetch remaining events. */
        }

        /* Shutdown gracefully. */
        Settings.INSTANCE.println("Shutting down ...");
        Settings.INSTANCE.println("Fetching remaining messages ...");
        listenAndProcess(true); /* Fetch remaining events. */
        try {
            selector.close();
            sockChnl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Settings.INSTANCE.println("Done.");
    }

    private void listenAndProcess(boolean finalise) {
        try {
            selector.select(getSoTimeout());
        } catch (IOException e) {
            throw new RuntimeException(e); /* selector must be broken. */
        }
        Iterator<SelectionKey> i = selector.selectedKeys().iterator();

        /* If nothing ready for receive, check channel */
        if (!i.hasNext()) {
            if (!finalise && (sockChnl == null || !sockChnl.isOpen() || !sockChnl.isConnected()
                    || !sockChnl.isRegistered())) {
                Settings.INSTANCE.println("Connection lost, reconnecting ...");
                connect();  /* Reconnect if connection died. */
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

    private Object readEvent(SocketChannel srvChnl) {
        try {
            ByteBuffer hdrBuf = read(srvChnl, MsgHeader.HDR_SIZE);      /* Read header. */
            if (hdrBuf == null) {

                return null;                                            /* Nothing read. */
            }
            MsgHeader header = new MsgHeader(hdrBuf).deconstruct();
            if (!CommsProto.isSupported(header.getMsgType())) {
                throw new RuntimeException("Message type '" + header.getMsgType()
                        + "' is not supported.");
            }
            ByteBuffer bodyBuf = read(srvChnl, header.getBdySize());    /* Read body. */
            if (bodyBuf == null) {

                return null;                                            /* Nothing read. */
            }
            if (msgLog != null) {
                msgLog.add(Helpers.arrCat(hdrBuf.array(), bodyBuf.array()));    /* Log event. */
            }
            Message event = CommsProto.deconstMsgBdy(bodyBuf, header.getMsgType());
            Settings.INSTANCE.println("RCV: " + event.getMsg().toString());

            return event;
        } catch (IOException e) {       /* Channel does not work */
            return null;                /* Continue main loop. */
        } catch (RuntimeException e) {  /* Corrupted message detected. */
            throw new RuntimeException(e);
        }
    }

    public ArrayList<byte[]> getEventLog() {
        if (msgLog != null) {

            return msgLog;
        } else {
            throw new RuntimeException("Instance does not use message logging.");
        }
    }

    public TestClnt withMsgLog() {
        msgLog = new ArrayList<byte[]>();

        return this;
    }
}
