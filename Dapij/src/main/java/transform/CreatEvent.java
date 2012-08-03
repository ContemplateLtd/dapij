package transform;

import java.util.EventObject;

/**
 * An event object that represents events generated from creation of instances
 * during execution of instrumented client programs.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CreatEvent extends EventObject {

    private static final long serialVersionUID = 1L; /* default value */
    private InstCreatData stats;

    public CreatEvent(Object eventSrc, InstCreatData stats) {
        super(eventSrc);
        this.stats = stats;
    }

    public InstCreatData getObjData() {
        return stats;
    }
}
