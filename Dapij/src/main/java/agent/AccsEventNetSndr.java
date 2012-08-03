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

    private AgentEventSrv aes;

    public AccsEventNetSndr(AgentEventSrv aes) {
        this.aes = aes;
    }

    @Override
    public void handleAccessEvent(AccsEvent e) {

        /* Send message to client if event server started. */
        aes.sendEvent(CommsProto.constructAccsMsg(e.getObjId(), e.getThreadId()));
    }
}
