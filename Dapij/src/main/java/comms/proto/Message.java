package comms.proto;

import java.nio.ByteBuffer;
import transform.InstEventData;

/**
 * An abstract class that provides common functionality to classes that deal
 * with network messages' contents.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class Message {

    private ByteBuffer msgBody;

    public Message(ByteBuffer msgBody) {
        this.msgBody = msgBody;
    }

    public abstract Message deconstruct();
    public abstract ByteBuffer construct();
    public abstract InstEventData getMsg();

    public ByteBuffer getBody() {
        return msgBody;
    }

    protected void setBody(ByteBuffer msgBody) {
        this.msgBody = msgBody;
    }
}
