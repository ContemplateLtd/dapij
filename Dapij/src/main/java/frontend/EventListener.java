package frontend;

import comms.CommsProtocol.AccsMsg;
import comms.CommsProtocol.CreatMsg;

/**
 * An abstract class providing hooks for processing event messages. It allows
 * assembling listeners into an event processing chain.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class EventListener {

    private EventListener el; /* Previous  */

    EventListener(EventListener prevInChain) {
        this.el = prevInChain;
    }

    public EventListener() {}

    public EventListener nextListener() {
        return el;
    }

    public abstract void onCreatEvent(CreatMsg msg);

    public abstract void onAccsEvent(AccsMsg msg);
}
