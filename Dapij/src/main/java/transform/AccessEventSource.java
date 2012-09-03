package transform;

import java.util.ArrayList;

/**
 * An class that provides registering service for instance access event
 * listeners.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccessEventSource {
    private ArrayList<AccessEventListener> listeners;

    public AccessEventSource() {
        listeners = new ArrayList<AccessEventListener>();
    }

    public synchronized void addListener(AccessEventListener l) {
        listeners.add(l);
    }

    public synchronized boolean rmListener(AccessEventListener l) {
        return listeners.remove(l);
    }

    public void fireEvent(long objId, long threadId) {

        /* Create event. */
        AccessEvent e = new AccessEvent(this, new InstanceAccessData(objId, threadId));

        /* Notify subscribers. */
        for (AccessEventListener el : listeners) {
            el.handleAccessEvent(e);
        }
    }
}
