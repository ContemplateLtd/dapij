package transform;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Overrides the 13 ASM methods that visit the instructions of a method and
 * increments an instance variable (insnOfst) in each of them. This provides an
 * instruction offset value for each instruction as it is being visited. Values
 * range from 0 starting at the beginning of the method 0 to N, where N is the
 * number of instructions in that method.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InsnOfstProvdr extends MethodVisitor {

    private int insnOfst = -1;

    public InsnOfstProvdr(InsnOfstReader mvIof) {
        super(Opcodes.ASM4, mvIof);
        mvIof.setInsnOfsetProvider(this);
    }

    /**
     * Provides the method offset of the current instruction.
     *
     * @return An integer value representing the number of instructions in the
     *         visited method before the current instruction being visited.
     */
    public int getInsnOfst() {
        return insnOfst;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitInsn(int opcode) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitVarInsn(opcode, var);
    }
}
