package frontend;

import comms.CommsProto.AccsMsg;
import comms.CommsProto.CreatMsg;

/**
 * An abstract class providing hooks for processing event messages. It allows
 * creating a chain of listeners each processing the result of the one before
 * it.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class EventListener {

    protected EventListener el; /* Previous  */

    EventListener(EventListener prevInChain) {
        this.el = prevInChain;
    }

    public EventListener() {}

    public abstract void onCreatEvent(CreatMsg msg);

    public abstract void onAccsEvent(AccsMsg msg);
}
