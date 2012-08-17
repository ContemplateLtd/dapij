package comms.proto;

import java.nio.ByteBuffer;

/**
 * A class for containing and manipulating message headers.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class MsgHeader {

    public static final int HDR_SIZE = 5;   /* 5 bytes in total */
    private byte msgType;                   /* 1 byte */
    private int bdySize;                    /* 4 bytes */
    private ByteBuffer header;                  /* buffer representation */

    public MsgHeader(byte type, int bdySize) {
        this.msgType = type;
        this.bdySize = bdySize;
    }

    public MsgHeader(ByteBuffer hdrBuf) {
        if (hdrBuf.remaining() < HDR_SIZE) {
            throw new RuntimeException("Invalid message header.");
        }
        header = hdrBuf;
    }

    /**
     * Constructs and return the byte array representation of this header
     * object.
     * @return the byte array header.
     */
    public ByteBuffer construct() {
        if (header == null) {
            header = construct(msgType, bdySize);
        }

        return header;
    }

    public static ByteBuffer construct(byte msgType, int bdySize) {
        ByteBuffer bf = ByteBuffer.allocate(HDR_SIZE);
        bf.put(msgType);
        bf.putInt(bdySize);
        bf.flip();

        return bf;
    }

    public MsgHeader deconstruct() {
        if (header != null) {
            msgType = header.get();
            bdySize = header.getInt();
        }

        return this; /* Do nothing if header not set. */
    }

    public byte getMsgType() {
        return msgType;
    }

    public int getBdySize() {
        return bdySize;
    }

    public ByteBuffer getHeader() {
        return header;
    }
}
