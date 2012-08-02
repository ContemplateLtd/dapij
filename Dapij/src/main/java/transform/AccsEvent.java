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
    
    private int objId;
    private long threadId;
    
    public AccsEvent(Object src, int objId, long threadId) {
        super(src);
        this.objId = objId;
        this.threadId = threadId;
    }

    public int getObjId() {
        return objId;
    }

    public long getThreadId() {
        return threadId;
    }
}
