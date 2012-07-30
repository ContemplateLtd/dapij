/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import comms.AgentEventSrv;
import comms.CommsProto;
import transform.CreatEvent;
import transform.CreatEventLisnr;

/**
 * An creation event listener that converts events to a format suitable for
 * network transfer & sends them to external subscribers via the agent's event
 * server.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CreatEventNetSndr implements CreatEventLisnr {
        
    AgentEventSrv aes;
    
    public CreatEventNetSndr(AgentEventSrv aes) {
        this.aes = aes;
    }

    /* TODO: Implement, a dummy implementation for now. */
    @Override
    public void handleCreationEvent(CreatEvent e) {
        /* Compose & send a message over the network to other subscribers. */
        String msg = CommsProto.constructMsg(e.getStats().toString());
        
        /* If running, use server to send msg to client. */
        if (aes != null) {
            aes.sendEvent(msg);
        }
    }
}
