/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import dapij.Settings;

/**
 * A class taking care of the communication protocol between the event server
 * and the event hub/client running in the agent.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CommsProto {
    /* Network configuration */
    public static final int port = 
            Integer.valueOf(Settings.INSTANCE.getSett(Settings.SETT_EVS_PORT));
    public static final String host =
            Settings.INSTANCE.getSett(Settings.SETT_CLI_HOST);
    
    /* Event types */
    public static final int TYP_OBJ = 1;    /* msg is object creation event */
    public static final int TYP_ACC = 2;    /* msg is object access event */
    // add more here if needed ...
    
    public static String constrObjEventMsg(String event) {
        // TODO: reimplement
        return event + "\n";
    }
    
    public static String constrAccEventMsg(String event) {
        // TODO: reimplement
        return event + "\n";
    }
}
