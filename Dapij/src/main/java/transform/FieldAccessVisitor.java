package transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import agent.ArgumentStack;

/**
 * Injects code that generates events when instance fields are being accessed.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class FieldAccessVisitor extends AccessVisitor {

    private String methodName;

    public FieldAccessVisitor(MethodVisitor mv, String name) {
        super(Opcodes.ASM4, mv);
        methodName = name;
    }

    /** Registers field accesses. */
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        /*
         * This "if" statement is used to make the agent ignore cases when
         * object is accessed from within its own constructor, as they cause
         * errors/inconsistencies. See the doc folder for details.
         *
         * TODO: does info exist in doc folder?
         * TODO: review if statement. Constructors not instrumented. Is this desirable?
         * TODO: what is "this$"?
         */
        if (!(methodName.equals("<init>") && (name.equals("this") || name.startsWith("this$")))) {
            if (opcode == Opcodes.GETFIELD) {
                mv.visitInsn(Opcodes.DUP); /* If argument is a reference, duplicate it. */
                injectFireAccsEvent();
            } else if (opcode == Opcodes.PUTFIELD) {
                Type type = Type.getType(desc);

                /* If type takes 2 slots on stack, can't just swap, do this instead. */
                if (ArgumentStack.isComputationalType2(type)) {
                    mv.visitInsn(Opcodes.DUP2_X1);
                    mv.visitInsn(Opcodes.POP2);
                    mv.visitInsn(Opcodes.DUP_X2);
                    injectFireAccsEvent();
                } else {

                    /* Put a copy of object reference on top. */
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(Opcodes.POP);
                    injectFireAccsEvent();
                }
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
}
