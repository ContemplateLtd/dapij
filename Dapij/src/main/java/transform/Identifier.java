package transform;

/**
 * An interface for classes providing entity identification.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public interface Identifier {

    <T> long getId(T ref);
}
