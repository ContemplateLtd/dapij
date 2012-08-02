/*
 * TODO: doc ocmment
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

    @Override
    public void handleCreationEvent(CreatEvent e) {
        
        /* Compose & send a message over to external subscribers. */
        aes.sendEvent(CommsProto.constructCreatMsg(e.getObjData()));
    }
}
