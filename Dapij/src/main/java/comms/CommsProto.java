/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package comms;

import agent.Settings;

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
    public static final int TYP_OBJ = 1;    /* msg is object creation event */
    public static final int TYP_ACC = 2;    /* msg is object access event */
    // TODO: add more here if needed ...
    
    // TODO: reimplement
    public static String constructMsg(String event) {
        return event + "\n";
    }
}
