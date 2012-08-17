package comms;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import agent.Settings;

/**
 * An abstract class that contains some common properties of network entities
 * such as servers and clients.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class NetworkNode extends Thread {

    private static boolean quiet = shouldBeQuiet();
    private String host;            /* host on which node operates */
    private int port;               /* port on which node operates */
    private int attempts;           /* number of attempts to bind to port */
    private long soTimeout;         /* socket timeout */
    private long attemptInterval;   /* time btw attempts (in seconds) */
    private boolean running;        /* true while main loop loops */
    private Queue<ByteBuffer> outMsgQ;
    private Queue<ByteBuffer> inMsgQ;

    public NetworkNode(String host, int port, long soTimeout, long attemptInterval, int attempts) {
        this.running = true;
        this.host = host;
        this.port = port;
        this.soTimeout = soTimeout;
        this.attemptInterval = attemptInterval;
        this.attempts = attempts;
        this.outMsgQ = new LinkedList<ByteBuffer>();
        this.inMsgQ = new LinkedList<ByteBuffer>();
    }

    protected boolean isRunning() {
        return running;
    }

    public synchronized void shutdown() {
        this.running = false;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public long getSoTimeout() {
        return soTimeout;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public long getAttemptInterval() {
        return this.attemptInterval;
    }

    /* TODO: use logger & remove this from class */
    private static boolean shouldBeQuiet() {
        String q = Settings.INSTANCE.get(Settings.SETT_QUIET_NET);

        /* By default no messages are sent to stdout. */
        return (q == null || (q != null && !q.equals("false"))) ? true : false;
    }

    /* TODO: use logger & remove this from class */
    protected static void stdoutPrintln(String msg) {
        if (!quiet) {
            System.out.println(Thread.currentThread().getName() + ": " + msg);
        }
    }

    protected Queue<ByteBuffer> getOutMsgQ() {
        return outMsgQ;
    }

    protected Queue<ByteBuffer> getInMsgQ() {
        return inMsgQ;
    }

    public synchronized void sendMsg(ByteBuffer msg) {
        while (!getOutMsgQ().offer(msg));
    }

    protected static boolean write(SocketChannel chnl, ByteBuffer bf) throws IOException {
        if (!bf.hasRemaining()) {
            return false;
        }
        while (bf.hasRemaining()) {
            chnl.write(bf);
        }

        return true;
    }

    /**
     * Attempts to read length characters from a nonblocking SocketChannel into
     * a ByteBuffer.
     *
     * @param chnl
     *            The nonblocking channel to read from.
     * @param length
     *            The number of bytes to read.
     * @return A ByteBuffer of length capacity (maybe partially or fully) filled
     *         with the bytes read or null if no bytes were read.
     * @throws IOException
     *             if there is a problem with the SocketChannel while reading.
     * @throws RuntimeException
     *             if length < 0 or channel is blocking.
     */
    protected static ByteBuffer read(SocketChannel chnl, int length) throws IOException {
        if (chnl.isBlocking()) {
            throw new RuntimeException("SocketChannel cannot be blocking.");
        }
        if (length <= 0) {
            throw new RuntimeException("Illegal lenght: expected: length > 0, got: length = "
                    + length);
        }
        int justRead = 0;       /* Number of bytes just read. */
        ByteBuffer bf = ByteBuffer.wrap(new byte[length]);  // TODO: switch to .allocate(length);
        while (bf.hasRemaining()) {
            justRead = chnl.read(bf);
            if (justRead > 0) {
                continue;
            } else if (justRead == 0) {
                break;
            } else if (justRead == -1) {
                chnl.close();   /* Not usable anymore, close. */
            }
        }
        if (bf.remaining() == length) {
            return null;        /* Return null if no bytes read & buffer empty. */
        } else if (bf.remaining() > 0) {
            throw new RuntimeException("Corrupted message detected.");
        }
        bf.flip();

        return bf;
    }
}
