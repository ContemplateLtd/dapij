package transform;

/**
 * An interface defining the methods of listeners of instance access events
 * generated during execution of instrumented client program.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public interface AccessEventListener {

    void handleAccessEvent(AccessEvent e);
}
