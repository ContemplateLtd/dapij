package transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A method visitor that provides functionality to other method visitors (by
 * subclassing) wishing to utilise instruction offsets provided by
 * InstructionOffsetProvider (a method visitor).
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InstructionOffsetReader extends MethodVisitor {

    private InstanceOffsetProvider iop;

    public InstructionOffsetReader(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    /**
     * A reference to a InstructionOffsetProvider has to be passed to each
     * object of this type before any other of it's methods are called so that
     * it's getInsnOfst() method can work properly. This creates backward link
     * in the chain of method visitors.
     *
     * @param iop
     *            The method visitor that calculates the correct instruction
     *            offsets prior to delegating to an instance of this class.
     */
    public final void setInsnOfsetProvider(InstanceOffsetProvider iop) {
        this.iop = iop;
    }

    protected final int getInsnOfst() {
        if (iop == null) {
            throw new RuntimeException("A " + InstanceOffsetProvider.class.getName()
                    + " has to be attached to the chain of method visitors before this "
                    + InstructionOffsetReader.class.getName() + " to allow its proper operation.");
        }

        return iop.getInsnOfst();
    }
}
