package comms.proto;

import java.nio.ByteBuffer;
import transform.InstAccsData;

/**
 * A class for manipulating and containing access messages.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public final class AccsMsg extends Message {

    public static final byte TYP_ACC = 1;
    public static final int BDY_SIZE = 12;
    private InstAccsData accsData;

    public AccsMsg(ByteBuffer body) {
        super(body);
    }

    public AccsMsg(InstAccsData accsData) {
        super(null);
        this.accsData = accsData;
    }

    @Override
    public ByteBuffer construct() {
        if (getBody() == null && accsData != null) {
            setBody(construct(accsData));
        }

        return getBody(); /* Set only once. */
    }

    public static ByteBuffer construct(InstAccsData accsData) {
        ByteBuffer bf = ByteBuffer.allocate(MsgHeader.HDR_SIZE + BDY_SIZE);
        bf.put(TYP_ACC); /* faster not to use MsgHeader.construct() here. */
        bf.putInt(BDY_SIZE);
        bf.putInt(accsData.getObjId());
        bf.putLong(accsData.getThdId());
        bf.flip();

        return bf; /* Set only once. */
    }

    @Override
    public AccsMsg deconstruct() {
        if (getBody() != null && accsData == null) {
            accsData = new InstAccsData(getBody().getInt(), getBody().getLong());
        }

        return this;
    }

    @Override
    public InstAccsData getMsg() {
        return accsData;
    }
}
