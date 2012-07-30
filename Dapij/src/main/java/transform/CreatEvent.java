/*
 * TODO: doc comment
 */
package transform;

import java.util.EventObject;

/**
 * An event object that represents events generated from creation of instances
 * during execution of instrumented client programs.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CreatEvent extends EventObject {
    
    private InstCreatStats stats;
    private Object ref;
    
    public CreatEvent(Object eventSrc, Object ref,
            InstCreatStats stats) {
        super(eventSrc);
        this.ref = ref;
        this.stats = stats;
    }

    public InstCreatStats getStats() {
        return stats;
    }
    
    public Object getRef() {
        return ref;
    }


}
