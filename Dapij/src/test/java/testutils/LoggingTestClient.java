package testutils;

import java.util.ArrayList;
import comms.EventClient;
import comms.CommsProtocol.MsgBody;
import comms.CommsProtocol.MsgHeader;
import utils.Helpers;

public class LoggingTestClient extends EventClient {

    private ArrayList<byte[]> msgLog;

    public LoggingTestClient(String host, int port) {
        super(host, port);
        msgLog = new ArrayList<byte[]>();
    }

    public ArrayList<byte[]> getEventLog() {
        return msgLog;
    }

    @Override
    protected void onMsgRecv(MsgHeader header, MsgBody body) {

        /* Log event. */
        msgLog.add(Helpers.arrCat(header.getHeader().array(), body.getBody().array()));
    }
}
