package comms.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import transform.InstCreatData;

/**
 * A class for manipulating and containing creation messages.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class CreatMsg extends Message {

    public static final byte TYP_CRT = 0; /* an object creation event message */
    private InstCreatData creatData;

    public CreatMsg(ByteBuffer body) {
        super(body);
    }

    public CreatMsg(InstCreatData accsData) {
        super(null);
        this.creatData = accsData;
    }

    @Override
    public ByteBuffer construct() {
        if (getBody() == null && creatData != null) {
            setBody(construct(creatData));
        }

        return getBody();  /* Set only once. */
    }

    public static ByteBuffer construct(InstCreatData creatData) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream dos = null;
        try {
            dos = new ObjectOutputStream(bos);

            /* Create body. */
            dos.writeLong(creatData.getObjId());
            dos.writeObject(creatData.getClazz());
            dos.writeObject(creatData.getMethod());
            dos.writeLong(creatData.getThdId());
            dos.writeInt(creatData.getOffset());
            dos.flush();
            byte[] body = bos.toByteArray(); /* Body length varies because of method name. */
            bos.close();
            dos.close();

            ByteBuffer msg = ByteBuffer.allocate(MsgHeader.HDR_SIZE + body.length);
            msg.put(new MsgHeader(TYP_CRT, body.length).construct());   /* Prepend header. */
            msg.put(body);                                              /* Append body. */
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
                Class<?> objCls = (Class<?>) ois.readObject();
                String method = (String) ois.readObject();
                long thdId = ois.readLong();
                int ofst = ois.readInt();
                bis.close();
                ois.close();

                creatData = new InstCreatData(objId, objCls, method, ofst, thdId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this;  /* Set only once. */
    }

    @Override
    public InstCreatData getMsg() {
        return creatData;
    }
}

