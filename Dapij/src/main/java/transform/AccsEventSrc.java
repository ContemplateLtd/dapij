/*
 * TODO: doc comment
 */
package transform;

import java.util.ArrayList;

/**
 * An class that provides registering service for instance access event
 * listeners.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccsEventSrc {
    private ArrayList<AccsEventLisnr> listeners;

    public AccsEventSrc() {
        listeners = new ArrayList<AccsEventLisnr>();
    }

    public synchronized void addListener(AccsEventLisnr l) {
        listeners.add(l);
    }

    public synchronized boolean rmListener(AccsEventLisnr l) {
        return listeners.remove(l);
    }

    public void fireEvent(int objId, long threadId) {

        /* Create event */
        AccsEvent e = new AccsEvent(this, objId, threadId);
        
        for (AccsEventLisnr el : listeners) {
            el.handleAccessEvent(e);
        }
    }
}
