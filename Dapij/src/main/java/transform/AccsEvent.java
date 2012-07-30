/*
 * TODO: doc comment
 */
package transform;

import java.util.EventObject;

/**
 * An event object that represents events generated from instance accesses
 * during execution of instrumented client programs.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccsEvent extends EventObject {
    
    private Object ref;
    private long threadId;
    
    public AccsEvent(Object src, Object ref, long threadId) {
        super(src);
        this.ref = ref;
        this.threadId = threadId;
    }

    public Object getRef() {
        return ref;
    }

    public long getThreadId() {
        return threadId;
    }
}
