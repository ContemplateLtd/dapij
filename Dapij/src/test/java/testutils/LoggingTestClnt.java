package testutils;

import java.util.ArrayList;
import comms.EventClnt;
import comms.CommsProto.MsgBody;
import comms.CommsProto.MsgHeader;
import utils.Helpers;

public class LoggingTestClnt extends EventClnt {

    private ArrayList<byte[]> msgLog;

    public LoggingTestClnt(String host, int port) {
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
