/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

/**
 * A class taking care of the communication protocol between the event server
 * and the event hub/client running in the agent.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CommsProto {
    /* Network configuration */ // TODO: load from config file?
    public static final int port = 7890;
    public static final String host = "localhost";
    
    /* Event types */
    public static final int TYP_OBJ = 1;    /* msg is object creation event */
    public static final int TYP_ACC = 2;    /* msg is object access event */
    // add more here if needed ...
    
    public static void constrObjEventMsg() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }
    
    public static void constrAccEventMsg() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
