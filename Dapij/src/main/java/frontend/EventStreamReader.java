package frontend;

import comms.CommsProto;
import comms.EventClnt;
import comms.CommsProto.MsgBody;
import comms.CommsProto.MsgHeader;
import comms.CommsProto.MsgTypes;

/**
 * A network client that listens for events and generates callbacks to a chain
 * of listeners.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class EventStreamReader extends EventClnt {

    private EventListener ev;

    public EventStreamReader(String host, int port) {
        super(host, port);
    }

    public void setListener(EventListener ev) {
        this.ev = ev;
    }

    @Override
    protected void onMsgRecv(MsgHeader header, MsgBody body) {

        /* Accesses messages are more frequent, check for them first. */
        if (header.getMsgType() == MsgTypes.TYP_ACC) {
            ev.onAccsEvent((CommsProto.AccsMsg) body);
        } else if (header.getMsgType() == MsgTypes.TYP_CRT) {
            ev.onCreatEvent((CommsProto.CreatMsg) body);
        }
    }
}
