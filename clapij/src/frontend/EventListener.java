package frontend;

import transform.InstanceAccessData;
import transform.InstanceCreationData;
import comms.CommsProtocol.AccsMsg;
import comms.CommsProtocol.CreatMsg;

/**
 * A class providing hooks for processing event messages. It allows
 * assembling listeners into an event processing chain.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class EventListener {

    private EventListener el; /* Previous  */
    
    public EventListener() {
        this.el = null;
    }

    EventListener(EventListener prevInChain) {
        this.el = prevInChain;
    }

    public EventListener nextListener() {
        return el;
    }

    protected void onCreatEvent(CreatMsg msg) {
        InstanceCreationData icd = msg.deconstruct().getMsg();
        onCreation(icd.getObjId(), icd.getClassName(), icd.getMethod(), icd.getOffset(),
                icd.getThdId());
    }

    protected void onAccsEvent(AccsMsg msg) {
        InstanceAccessData icd = msg.deconstruct().getMsg();
        onAccess(icd.getObjId(), icd.getThdId());
    }
    
    
    /**
     * A function called after a creation message is received.
     *
     * @param objId
     *            the identifier assigned to the created object.
     * @param className
     *            the name of the class of the object.
     * @param method
     *            the name of the method in which the object was created
     * @param offset
     *            the number of the line in the method where the object was created
     * @param threadId
     *            the identifier of the thread in which the object was created
     */
    public void onCreation(long objId, String className, String method, 
            int offset, long threadId) {

    }

    /**
     * A function called after an access message is received.
     *
     * @param objId
     *            the identifier of the accessed object.
     * @param thdId
     *            the identifier of the thread in which the object was accessed
     */
    public void onAccess(long objId, long thdId) {

    }
}
