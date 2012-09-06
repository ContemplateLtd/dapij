package comms;

import java.nio.ByteBuffer;
import junit.framework.Assert;
import org.junit.Test;
import testutils.TransformerTest;
import transform.InstanceAccessData;
import transform.InstanceCreationData;
import comms.CommsProtocol.AccsMsg;
import comms.CommsProtocol.CreatMsg;
import comms.CommsProtocol.MsgBody;
import comms.CommsProtocol.MsgHeader;
import comms.CommsProtocol.MsgTypes;

/**
 * A class containing tests for the {@link CommsProtocol} communications
 * protocol class.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class CommsProtocolTest extends TransformerTest {

    private InstanceCreationData creationData = new InstanceCreationData((long) 1, "TestClass",
            "TestMethod", 5, (long) 1);
    private  InstanceAccessData accessData = new InstanceAccessData((long) 2, (long) 2);

    /**
     * Tests the correctness of the general {@link CommsProto}
     * {@code .deconstructMsg()} method.
     */
    public void commsProtoDeconstructionTest() {

        /* Test method with creation message body. */
        ByteBuffer creationMsgBuf = CreatMsg.construct(creationData);
        CommsProtocol.deconstructMsg(creationMsgBuf, MsgTypes.TYP_CRT);
        MsgBody creationBody = CommsProtocol.deconstructMsg(creationMsgBuf, MsgTypes.TYP_ACC);
        Assert.assertEquals("CommsProtocol.deconstructMsg() maps to correct handler's "
                + "deconstruct() method for deconstructiong creation message bodies: ",
                true , creationData.equals(creationBody.getMsg()));

        /* Test method with access message body. */
        ByteBuffer accessMsgBuf = AccsMsg.construct(accessData);
        MsgBody accessBody = CommsProtocol.deconstructMsg(accessMsgBuf, MsgTypes.TYP_ACC);
        Assert.assertEquals("CommsProtocol.deconstructMsg() maps to correct handler's "
                + "deconstruct() method for deconstructiong access message bodies: ",
                true , accessData.equals(accessBody.getMsg()));
    }

    /**
     * Tests {@link MsgHeader} de/construction methods. Test tests for header
     * buffer length as it is fixed.
     */
    @Test
    public void msgHeaderContructionTest() {
        int bodySize = 30;

        /* Construct a ByteBuffer header. */
        ByteBuffer hdrBuf = MsgHeader.construct(MsgTypes.TYP_ACC, bodySize);

        Assert.assertEquals("Header size property adhered to: ", true,
                MsgHeader.SIZE == hdrBuf.capacity());
        MsgHeader header = new MsgHeader(hdrBuf);
        header.deconstruct(); /* Extract data from buffer & set internal fields. */
        Assert.assertEquals("Msg header properly de/constructs message type property: ", true,
                header.getMsgType() == MsgTypes.TYP_ACC);
        Assert.assertEquals("Msg header properly de/constructs body size property: ", true,
                header.getBdySize() == bodySize);
    }

    /**
     * Tests the de/construction methods of all concrete implementations of
     * {@link MsgBody}. Test does not test for message body buffer length as
     * length varies between messages.
     */
    public void msgBodyConstructionTest() {

        /* Test creation message de/construction. */
        ByteBuffer creationMsgBuf = CreatMsg.construct(creationData);
        CreatMsg creationMsg = new CreatMsg(creationMsgBuf);
        creationMsg.deconstruct();
        Assert.assertEquals("CreatMsg correctly de/constructs: ", true,
                creationData.equals(creationMsg.getMsg()));

        /* Test access message de/construction. */
        ByteBuffer accessMsgBuf = AccsMsg.construct(accessData);
        AccsMsg accessMsg = new AccsMsg(accessMsgBuf);
        accessMsg.deconstruct();
        Assert.assertEquals("AccsMsg correctly de/constructs: ", true,
                accessData.equals(accessMsg.getMsg()));
    }

    public void msgTypesTest() {

        /*
         * Check if each message type's type constant differs from every other
         * type's type constant.
         */
        Assert.assertEquals("Message type constants differ from each other: ", true,
                MsgTypes.TYP_CRT != MsgTypes.TYP_ACC);

        /* Check if isSupported() method returns correct values. */
        Assert.assertEquals("Creation events are supported: ", true,
                MsgTypes.isSupported(MsgTypes.TYP_CRT));
        Assert.assertEquals("Access events are supported: ", true,
                MsgTypes.isSupported(MsgTypes.TYP_CRT));

        /* Check if each message type has a message body handler class. */
        Assert.assertEquals("Creation events have a class for messge body handling: ", true,
                MsgTypes.msgClassForType(MsgTypes.TYP_CRT) != null);
        Assert.assertEquals("Access events have a class for messge body handling: ", true,
                MsgTypes.msgClassForType(MsgTypes.TYP_ACC) != null);

        /*
         * Check if handler class for each type differs from every other type's
         * class.
         */
        Assert.assertEquals("Access events have a class for messge body handling: ", true,
                MsgTypes.msgClassForType(MsgTypes.TYP_ACC)
                        != MsgTypes.msgClassForType(MsgTypes.TYP_CRT));
    }
}
