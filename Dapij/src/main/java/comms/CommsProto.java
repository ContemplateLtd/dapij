/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import agent.Settings;
import java.io.*;
import java.nio.ByteBuffer;
import transform.InstCreatData;

/**
 * A class taking care of the communication protocol between the server (a part
 * of the agent) and external clients.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CommsProto {
    
    /* Network configuration */
    public static final int port = 
            Integer.valueOf(Settings.INSTANCE.get(Settings.SETT_EVS_PORT));
    public static final String host = 
            Settings.INSTANCE.get(Settings.SETT_CLI_HOST);
    
    /* Event types */
    public static final byte TYP_CRT = 0;   /* msg is object creation event */
    public static final byte TYP_ACC = 1;   /* msg is object access event */
    // TODO: add more here if needed ...
    
    public static byte[] constructAccsMsg(int objId, long thdId) {
            ByteBuffer bf = ByteBuffer.allocate(17);
            bf.put(TYP_ACC);
            bf.putInt(12);
            bf.putInt(objId);
            bf.putLong(thdId);
            return bf.array();
    }
    
    public static String deconstAccsMsg(byte[] b) {
        ByteBuffer bf = ByteBuffer.wrap(b);
        int objId = bf.getInt();
        long thdId = bf.getLong();

        /* TODO: fix function. */
        return "[ACC: id:" + String.valueOf(objId)+ ", thd:" +
                String.valueOf(thdId) + "]";
    }
    
        public static byte[] constructCreatMsg(InstCreatData stats) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream dos = null;
        try {
            dos = new ObjectOutputStream(bos);

            dos.writeInt(stats.getObjId());
            dos.writeObject(stats.getClazz());
            dos.writeObject(stats.getMethod());
            dos.writeLong(stats.getThreadId());
            dos.writeInt(stats.getOffset());
            dos.flush();
            byte[] data = bos.toByteArray();
            bos.close();
            dos.close();
            
            ByteBuffer bf = ByteBuffer.allocate(5);
            bf.put(TYP_CRT);
            bf.putInt(data.length);
            return concat(bf.array(), data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String deconstCreatMsg(byte[] b) {
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        ObjectInputStream ois = null;
        InstCreatData ics = null;
        byte[] msg = null;
        try {
            ois = new ObjectInputStream(bis);
            int objId = ois.readInt();
            Class objCls = (Class) ois.readObject();
            String method = (String) ois.readObject();
            long thdId = ois.readLong();
            int ofst = ois.readInt();
            bis.close();
            ois.close();
            
            return new InstCreatData(objId, objCls, method, ofst, thdId)
                    .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    } 
    
    private static byte[] concat(byte[]... arrays) {
        int catLen = 0;
        for (int i = 0; i < arrays.length; catLen += arrays[i++].length);
        
        byte[] cat = new byte[catLen];
        int currLen = 0;
        
        for (byte[] a : arrays) {
           System.arraycopy(a, 0, cat, currLen, a.length);
           currLen += a.length;
        }
        return cat;
    }
}
