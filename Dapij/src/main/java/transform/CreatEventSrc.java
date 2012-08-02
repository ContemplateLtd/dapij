/*
 * TODO: doc comment
 */
package transform;

import java.util.ArrayList;

/**
 * An class that provides registering service for instance creation event
 * listeners.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CreatEventSrc {

    private ArrayList<CreatEventLisnr> listeners;

    public CreatEventSrc() {
        listeners = new ArrayList<CreatEventLisnr>();
    }

    public synchronized void addListener(CreatEventLisnr l) {
        listeners.add(l);
    }

    public synchronized boolean rmListener(CreatEventLisnr l) {
        return listeners.remove(l);
    }

    public void fireEvent(Object ref, Class clazz, String method, int offset,
            long threadId) {
        
        /* Create event */
        CreatEvent e = new CreatEvent(this, ref,
                new InstCreatStats(clazz, method, offset, threadId));
        
        /* Notify subscribers. */
        for (CreatEventLisnr el : listeners) {
            el.handleCreationEvent(e);
        }
    }
}