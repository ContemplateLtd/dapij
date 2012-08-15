package agent;

import comms.AgentSrv;
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

    private AgentSrv aes;

    public CreatEventNetSndr(AgentSrv aes) {
        this.aes = aes;
    }

    @Override
    public void handleCreationEvent(CreatEvent e) {

        /* Compose & send a message over to external subscribers. */
        aes.sendMsg(CommsProto.constructCreatMsg(e.getObjData()));
    }
}
