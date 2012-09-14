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

    /*
     * Bounded concurrent blocking queues for snd/recv messages. Order
     * guaranteed.
     */
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

    /**
     * Writes the remaining bytes in a buffer to the given socket channel.
     *
     * @param chnl
     *            the {@link SocketChannel} to write to.
     * @param bf
     *            the {@link ByteBuffer} contents that are going to be written.
     * @return true if message successfully sent and false if nothing sent.
     */
    protected static boolean write(SocketChannel chnl, ByteBuffer bf) {
        if (!bf.hasRemaining()) {

            return false;
        }
        try {
            while (bf.hasRemaining()) {
                chnl.write(bf);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);  /* TODO: channel may have been closed, return false? */
        }

        return true;
    }

    /**
     * Read length characters from a nonblocking {@link SocketChannel} into a
     * {@link ByteBuffer} in a nonblocking manner.
     *
     * @param chnl
     *            The nonblocking channel to read from.
     * @param length
     *            The number of bytes to read.
     * @return A {@link ByteBuffer} of length capacity (maybe partially or
     *         fully) filled with the bytes read or null if no bytes were read.
     * @throws IOException
     *             if there is a problem with the {@link SocketChannel} while
     *             reading.
     * @throws IllegalArgumentException
     *             if length {@code < 0} or channel is blocking.
     */
    protected static ByteBuffer read(SocketChannel chnl, int length) throws IOException {
        if (chnl.isBlocking()) {
            throw new IllegalArgumentException("SocketChannel chnl cannot be blocking.");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("Illegal lenght: expected length > 0, but got "
                    + "length = " + length);
        }
        int justRead = 0; /* Number of bytes just read. */
        ByteBuffer bf = ByteBuffer.allocate(length);
        while (bf.hasRemaining()) {
            justRead = chnl.read(bf);
            if (justRead > 0) {
                continue;
            } else if (justRead == 0) {
                break;
            } else if (justRead == -1) {
                chnl.close(); /* Channel not usable anymore, close. */
            }
        }
        if (bf.remaining() == length) {
            return null; /* Return null if no bytes read & buffer empty. */
        } else if (bf.remaining() > 0) {
            throw new RuntimeException("Corrupted message detected.");
        }
        bf.flip();

        return bf;
    }
}
