package transform;

import java.util.ArrayList;

/**
 * An class that provides registering service for instance creation event
 * listeners.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CreationEventSource {

    private ArrayList<CreationEventListener> listeners;

    public CreationEventSource() {
        listeners = new ArrayList<CreationEventListener>();
    }

    public synchronized void addListener(CreationEventListener l) {
        listeners.add(l);
    }

    public synchronized boolean rmListener(CreationEventListener l) {
        return listeners.remove(l);
    }

    public void fireEvent(long objId, String className, String method, int offset, long threadId) {

        /* Create event. */
        CreationEvent e = new CreationEvent(
                this, new InstanceCreationData(objId, className, method, offset, threadId));

        /* Notify subscribers. */
        for (CreationEventListener el : listeners) {
            el.handleCreationEvent(e);
        }
    }
}
