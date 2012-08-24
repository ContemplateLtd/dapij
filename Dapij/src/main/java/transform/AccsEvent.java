package transform;

import java.util.EventObject;

/**
 * An event object that represents events generated from instance accesses
 * during execution of instrumented client programs.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccsEvent extends EventObject {

    private static final long serialVersionUID = 1L; /* default value */
    private InstAccsData data;

    public AccsEvent(Object src, InstAccsData data) {
        super(src);
        this.data = data;
    }

    public InstAccsData getAccsData() {
        return data;
    }
}
