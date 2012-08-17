package comms.proto;

import agent.Settings;
import java.nio.ByteBuffer;

/* TODO: Revise how types are defined & handled. Move them here? */

/**
 * A class taking care of the communication protocol between the server (an
 * agent subcomponent) and one external client.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class CommsProto {

    /* Network configuration */
    public static final int PORT = Integer.valueOf(Settings.INSTANCE.get(Settings.SETT_EVS_PORT));
    public static final String HOST = Settings.INSTANCE.get(Settings.SETT_CLI_HOST);

    private CommsProto() {}

    /**
     * Checks whether a certain message type is supported by the message
     * protocol.
     *
     * @param type
     *            A byte value representing the type of the message.
     * @return true if message type supported and false otherwise.
     */
    /* When adding new types, make sure to update isSupported(). */
    public static boolean isSupported(byte type) {
        return (type == CreatMsg.TYP_CRT || type == AccsMsg.TYP_ACC);
    }

    public static Message deconstMsgBdy(ByteBuffer body, byte msgType) {
        /* optimisation - check if access first - accesses messages are more frequent. */
        if (msgType == AccsMsg.TYP_ACC) {
            return new AccsMsg(body).deconstruct();
        }
        if (msgType == CreatMsg.TYP_CRT) {
            return new CreatMsg(body).deconstruct();
        }
        return null;
    }
}
