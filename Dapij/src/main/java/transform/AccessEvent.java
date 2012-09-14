package transform;

import java.util.EventObject;

/**
 * An event object that represents events generated from instance accesses
 * during execution of instrumented client programs.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class AccessEvent extends EventObject {

    private static final long serialVersionUID = -8427249131514728600L;
    private InstanceAccessData data;

    public AccessEvent(Object src, InstanceAccessData data) {
        super(src);
        this.data = data;
    }

    public InstanceAccessData getAccsData() {
        return data;
    }
}
