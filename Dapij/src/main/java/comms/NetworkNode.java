package comms;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * An abstract class that contains some common properties of network entities
 * such as servers and clients.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class NetworkNode extends Thread {

    private String host;            /* host on which node operates */
    private int port;               /* port on which node operates */
    private int attempts;           /* number of attempts to bind to port */
    private long soTimeout;         /* socket timeout */
    private long attemptInterval;   /* time btw attempts (in seconds) */
    private boolean running;        /* true while main loop loops */

    /* Bounded concurrent blocking queues for snd/recv messages. Order guaranteed. */
    private ArrayBlockingQueue<ByteBuffer> outMsgQ;
    private ArrayBlockingQueue<ByteBuffer> inMsgQ;

    public NetworkNode(String host, int port, long soTimeout, long attemptInterval, int attempts) {
        this.running = true;
        this.host = host;
        this.port = port;
        this.soTimeout = soTimeout;
        this.attemptInterval = attemptInterval;
        this.attempts = attempts;
        this.outMsgQ = new ArrayBlockingQueue<ByteBuffer>(15, true);    /* true - guarantee order */
        this.inMsgQ = new ArrayBlockingQueue<ByteBuffer>(15, true);     /* true - guarantee order */
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

    protected ArrayBlockingQueue<ByteBuffer> getOutMsgQ() {
        return outMsgQ;
    }

    protected ArrayBlockingQueue<ByteBuffer> getInMsgQ() {
        return inMsgQ;
    }

    public void blockSnd(ByteBuffer msg) throws InterruptedException {
        outMsgQ.put(msg);
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
        ByteBuffer bf = ByteBuffer.allocate(length);
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
