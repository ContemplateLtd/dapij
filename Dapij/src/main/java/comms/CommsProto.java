/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import agent.Settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import transform.InstAccsData;
import transform.InstCreatData;

/**
 * A class taking care of the communication protocol between the server (a part
 * of the agent) and external clients.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class CommsProto {

    /* Network configuration */
    public static final int PORT =
            Integer.valueOf(Settings.INSTANCE.get(Settings.SETT_EVS_PORT));
    public static final String HOST = Settings.INSTANCE.get(Settings.SETT_CLI_HOST);

    /* Event types */
    /* TODO: add more msg types here if needed ... */
    public static final byte TYP_CRT = 0; /* msg is object creation event */
    public static final byte TYP_ACC = 1; /* msg is object access event */

    private CommsProto() {}

    public static ByteBuffer constructAccsMsg(InstAccsData data) {
        ByteBuffer bf = ByteBuffer.allocate(17);
        bf.put(TYP_ACC);
        bf.putInt(12);
        bf.putInt(data.getObjId());
        bf.putLong(data.getThdId());
        bf.flip();

        return bf;
    }

    public static InstAccsData deconstAccsMsg(byte[] body) {
        ByteBuffer bf = ByteBuffer.wrap(body);
        int objId = bf.getInt();
        long thdId = bf.getLong();

        return new InstAccsData(objId, thdId);
    }

    public static ByteBuffer constructCreatMsg(InstCreatData data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream dos = null;
        try {
            dos = new ObjectOutputStream(bos);

            /* Create msg body. */
            dos.writeInt(data.getObjId());
            dos.writeObject(data.getClazz());
            dos.writeObject(data.getMethod());
            dos.writeLong(data.getThreadId());
            dos.writeInt(data.getOffset());
            dos.flush();
            byte[] body = bos.toByteArray();
            bos.close();
            dos.close();

            /* Create msg header. */
            ByteBuffer bf = ByteBuffer.allocate(5);
            bf.put(TYP_CRT);
            bf.putInt(body.length);

            return ByteBuffer.wrap(concat(bf.array(), body));   /* Stick to body & return. */
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InstCreatData deconstCreatMsg(byte[] body) {
        ByteArrayInputStream bis = new ByteArrayInputStream(body);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            int objId = ois.readInt();
            Class<?> objCls = (Class<?>) ois.readObject();
            String method = (String) ois.readObject();
            long thdId = ois.readLong();
            int ofst = ois.readInt();
            bis.close();
            ois.close();

            return new InstCreatData(objId, objCls, method, ofst, thdId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] concat(byte[]... arrays) {
        int catLen = 0;
        for (int i = 0; i < arrays.length; i++) {
            catLen += arrays[i].length;
        }

        byte[] cat = new byte[catLen];
        int currLen = 0;

        for (byte[] a : arrays) {
            System.arraycopy(a, 0, cat, currLen, a.length);
            currLen += a.length;
        }

        return cat;
    }

    public static Object deconstMsg(byte[] body, byte msgType) {
        if (msgType == CommsProto.TYP_CRT) {
            return CommsProto.deconstCreatMsg(body);
        } else if (msgType == CommsProto.TYP_ACC) {
            return CommsProto.deconstAccsMsg(body);
        } else {
            throw new RuntimeException("Message Type not recognised.");
        }
    }
}
