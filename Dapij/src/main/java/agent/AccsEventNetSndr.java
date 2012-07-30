/*
 * TODO: doc comment
 */
package agent;

import comms.AgentEventSrv;
import comms.CommsProto;
import transform.AccsEvent;
import transform.AccsEventLisnr;

/**
 * An access event listener that converts events to a format suitable for
 * network transfer & sends them to external subscribers via the agent's event
 * server.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccsEventNetSndr implements AccsEventLisnr {
    
    AgentEventSrv aes;
    
    public AccsEventNetSndr(AgentEventSrv aes) {
        this.aes = aes;
    }

    /* TODO: Implement, a dummy implementation for now. */
    @Override
    public void handleAccessEvent(AccsEvent e) {
        String msg = CommsProto.constructMsg("Object " + e.getRef() +
                " accessed from thread " + e.getThreadId());
        
        /* Send message to client if event server started */
        if (aes != null) {
            aes.sendEvent(msg);
        }
    }
}
