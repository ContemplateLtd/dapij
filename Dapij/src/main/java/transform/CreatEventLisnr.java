/*
 * TODO: doc comment
 */
package transform;

/**
 * An interface defining the methods of listeners of instance creation events
 * generated during execution of instrumented client program.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public interface CreatEventLisnr {

    public void handleCreationEvent(CreatEvent e);
}
