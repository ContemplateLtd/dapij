package transform;

import java.util.EventObject;

/**
 * An event object that represents events generated from creation of instances
 * during execution of instrumented client programs.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CreationEvent extends EventObject {

    private static final long serialVersionUID = 4207129732142980282L;
    private InstanceCreationData data;

    public CreationEvent(Object eventSrc, InstanceCreationData data) {
        super(eventSrc);
        this.data = data;
    }

    public InstanceCreationData getCreatData() {
        return data;
    }
}
