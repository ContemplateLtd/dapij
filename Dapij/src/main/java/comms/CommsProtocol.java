package comms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import transform.InstanceAccessData;
import transform.InstanceCreationData;
import transform.InstanceEventData;
import agent.Settings;

/**
 * A class that defines the communications protocol between the agent server and
 * one external client.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class CommsProtocol {

    /* Network configuration */
    public static final int PORT = Integer.valueOf(Settings.INSTANCE.get(Settings.SETT_NET_PORT));
    public static final String HOST = Settings.INSTANCE.get(Settings.SETT_NET_HOST);

    private CommsProtocol() {}

    /**
     * A class defining the message types supported by the communications
     * protocol.
     *
     * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
     */
    public static final class MsgTypes {

        public static final byte TYP_CRT = 0;
        public static final byte TYP_ACC = 1;

        /**
         * An unmodifiable map from supported msg types to classes handling
         * these types.
         */
        private static final Map<Byte, Class<? extends MsgBody>> SUPPORTED_TYPES;
        static {
            Map<Byte, Class<? extends MsgBody>> types =
                    new HashMap<Byte, Class<? extends MsgBody>>();
            try {
                types.put(MsgTypes.TYP_ACC, AccsMsg.class);
                types.put(MsgTypes.TYP_CRT, CreatMsg.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            SUPPORTED_TYPES = Collections.unmodifiableMap(types); /* Initialise map here. */
        }

        /**
         * Checks whether a certain message type is supported by the
         * communications protocol.
         *
         * @param type
         *            A byte value representing the type of the message.
         * @return {@code true} if message type supported and {@code false}
         *         otherwise.
         */
        public static boolean isSupported(byte type) {
            return SUPPORTED_TYPES.containsKey(type);
        }

        /**
         * Gets the class constant of the class responsible for handling a
         * message type given its type.
         *
         * @param msgType
         *            a {@code byte} value representing the type of the message. Can
         *            be any {@link MsgTypes}{@code .TYP_*} constant.
         * @return the class constant for the message type or null if type not
         *         supported.
         */
        public static Class<? extends MsgBody> msgClassForType(byte msgType) {
            return SUPPORTED_TYPES.get(msgType);
        }
    }

    /**
     * Deconstructs the {@link ByteBuffer} body of a message into a
     * {@link MsgBody} object.
     *
     * @param bodyBuf
     *            the contents of the message body
     * @param msgType
     *            the type of the message.
     * @return a fully initialised concrete MsgBody object.
     * @throws RuntimeException
     *             if message type not supported.
     */
    public static MsgBody deconstructMsg(ByteBuffer bodyBuf, byte msgType) {
        try {
            Constructor<? extends MsgBody> c = MsgTypes.msgClassForType(msgType)
                    .getConstructor(ByteBuffer.class);

            return c.newInstance(bodyBuf).deconstruct();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A container class for containing and manipulating message headers.
     *
     * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
     */
    public static final class MsgHeader {

        public static final int SIZE = 5;   /* 5 bytes in total */
        private byte msgType;               /* 1 byte */
        private int bdySize;                /* 4 bytes */
        private ByteBuffer header;          /* header contents */

        public MsgHeader(byte type, int bdySize) {
            this.msgType = type;
            this.bdySize = bdySize;
        }

        /**
         * Creates a {@link MsgHeader} object from the contents of a header.
         *
         * @param hdrBuf
         *            a {@link ByteBuffer} with the header contents.
         * @throws RuntimeException
         *             if {@code hdrBuf} does not have at least
         *             {@link MsgHeader}{@code .SIZE} bytes remaining.
         */
        public MsgHeader(ByteBuffer hdrBuf) {
            if (hdrBuf.remaining() < SIZE) {
                throw new RuntimeException("Invalid message header.");
            }
            header = hdrBuf;
        }

        /**
         * Constructs and return the {@link ByteBuffer} representation of this
         * header object.
         *
         * @return the {@link ByteBuffer} header object.
         */
        public ByteBuffer construct() {
            if (header == null) {
                header = construct(msgType, bdySize);
            }

            return header;
        }

        /**
         * Constructs and return the {@link ByteBuffer} representation of a
         * header, described by these input parameters.
         *
         * @param msgType
         *            the type constant of this message.
         * @param bdySize
         *            the length of the body of this message.
         * @return the {@link ByteBuffer} header object.
         */
        public static ByteBuffer construct(byte msgType, int bdySize) {
            ByteBuffer bf = ByteBuffer.allocate(SIZE);
            bf.put(msgType);
            bf.putInt(bdySize);
            bf.flip();

            return bf;
        }

        /**
         * Deconstructs the {@link ByteBuffer} header contents according to the
         * communications protocol and sets the fields of this header object to
         * the extracted values.
         *
         * @return the instantiated {@link MsgHeader} object.
         */
        public MsgHeader deconstruct() {
            if (header != null) {
                msgType = header.get();
                bdySize = header.getInt();
            }

            return this; /* Do nothing if header not set. */
        }

        public static MsgHeader deconstruct(ByteBuffer header) {
            return new MsgHeader(header).deconstruct();
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

    /**
     * An abstract class that provides common functionality to classes dealing
     * with contents of network messages.
     *
     * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
     */
    public abstract static class MsgBody {

        /** The type of an object creation event message. */
        private final byte msgType;

        /** The contents of an event message. */
        private ByteBuffer msgBody;

        public MsgBody(ByteBuffer msgBody, byte msgType) {
            this.msgBody = msgBody;
            this.msgType = msgType;
        }

        public abstract ByteBuffer construct();
        public abstract MsgBody deconstruct();
        public abstract InstanceEventData getMsg();

        public byte getMsgType() {
            return msgType;
        }

        public ByteBuffer getBody() {
            return msgBody;
        }

        protected void setBody(ByteBuffer msgBody) {
            this.msgBody = msgBody;
        }
    }

    /**
     * A class for manipulating and containing creation messages.
     *
     * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
     */
    public static final class CreatMsg extends MsgBody {

        private InstanceCreationData creatData;

        public CreatMsg(ByteBuffer body) {
            super(body, MsgTypes.TYP_CRT);
        }

        public CreatMsg(InstanceCreationData accsData) {
            super(null, MsgTypes.TYP_CRT);
            this.creatData = accsData;
        }

        @Override
        public ByteBuffer construct() {
            if (getBody() == null && creatData != null) {
                setBody(construct(creatData)); /* Set value only once. */
            }

            return getBody();
        }

        public static ByteBuffer construct(InstanceCreationData creatData) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream dos = null;
            try {
                dos = new ObjectOutputStream(bos);

                /* Create body first - have to know body length before creating header. */
                dos.writeLong(creatData.getObjId());
                dos.writeObject(creatData.getClassName());
                dos.writeObject(creatData.getMethod());
                dos.writeLong(creatData.getThdId());
                dos.writeInt(creatData.getOffset());
                dos.flush();
                byte[] body = bos.toByteArray();
                bos.close();
                dos.close();

                /* Construct message. */
                ByteBuffer msg = ByteBuffer.allocate(MsgHeader.SIZE + body.length);
                msg.put(MsgHeader.construct(MsgTypes.TYP_CRT, body.length));    /* Put header. */
                msg.put(body);                                                  /* Append body. */
                msg.flip();

                return msg;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public CreatMsg deconstruct() {
            if (getBody() != null && creatData == null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(getBody().array());
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(bis);
                    long objId = ois.readLong();
                    String className = (String) ois.readObject();
                    String method = (String) ois.readObject();
                    long thdId = ois.readLong();
                    int ofst = ois.readInt();
                    bis.close();
                    ois.close();

                    /* Set value only once. */
                    creatData = new InstanceCreationData(objId, className, method, ofst, thdId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return this;
        }

        public static CreatMsg deconstruct(ByteBuffer body) {
            return new CreatMsg(body).deconstruct();
        }

        @Override
        public InstanceCreationData getMsg() {
            return creatData;
        }
    }

    /**
     * A class for manipulating and containing access messages.
     *
     * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
     */
    public static final class AccsMsg extends MsgBody {

        private InstanceAccessData accsData;

        public AccsMsg(ByteBuffer body) {
            super(body, MsgTypes.TYP_ACC);
        }

        public AccsMsg(InstanceAccessData accsData) {
            super(null, MsgTypes.TYP_ACC);
            this.accsData = accsData;
        }

        @Override
        public ByteBuffer construct() {
            if (getBody() == null && accsData != null) {
                setBody(construct(accsData)); /* Set value only once. */
            }

            return getBody();
        }

        public static ByteBuffer construct(InstanceAccessData accsData) {
            ByteBuffer bf = ByteBuffer.allocate(MsgHeader.SIZE + 16);

            /* Construct header, faster not to use MsgHeader.construct() here. */
            bf.put(MsgTypes.TYP_ACC);           /* Set message type. */
            bf.putInt(16);                      /* Set body Length. */

            /* Append body. */
            bf.putLong(accsData.getObjId());    /* 8 bytes */
            bf.putLong(accsData.getThdId());    /* 8 bytes */
            bf.flip();

            return bf;
        }

        @Override
        public AccsMsg deconstruct() {
            if (getBody() != null && accsData == null) {
                accsData = new InstanceAccessData(getBody().getLong(), getBody().getLong());
            }

            return this;
        }

        public static AccsMsg deconstruct(ByteBuffer body) {
            return new AccsMsg(body).deconstruct();
        }

        @Override
        public InstanceAccessData getMsg() {
            return accsData;
        }
    }
}
