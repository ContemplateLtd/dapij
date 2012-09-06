package transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import agent.ArgumentStack;

/**
 * Injects code that generates events when instance methods are being accessed.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class MethodAccessVisitor extends AccessVisitor {

    public MethodAccessVisitor(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {

        /* TODO: Handle INVOKESTATICs. */
        /* TODO: Does not instrument constructors? */

        /* Inject code to detect object accesses here. */
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE
                || (opcode == Opcodes.INVOKESPECIAL && !name.equals("<init>"))) {
            Type[] argTypes = Type.getArgumentTypes(desc); /* Get types of method arguments. */

            /* Pop args (from rightmost to leftmost) & push them temporarily in external stack. */
            for (int i = argTypes.length - 1; i >= 0; i--) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ArgumentStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE,
                                (ArgumentStack.isPrimitive(argTypes[i]))
                                     ? argTypes[i] : ArgumentStack.OBJECT_TYPE));
            }
            mv.visitInsn(Opcodes.DUP);
            injectFireAccsEvent(); /* Register this access. */

            /* Push args back on stack (from leftmost to rightmost). */
            for (Type argType : argTypes) {
                String popMethodName = ArgumentStack.getPopMethodNameFor(argType);

                /*
                 * Probably faster then calling ArgumentStack.isPrimitive()
                 * (i.e. querying internal map) at this point. The general pop
                 * method that casts elements to java.lang.Object has the name
                 * "pop".
                 */
                boolean isNotPrimitive = popMethodName.equals("pop");

                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ArgumentStack.class),
                        popMethodName, Type.getMethodDescriptor(
                                (isNotPrimitive) ? Type.getType(Object.class) : argType));

                if (isNotPrimitive) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, argType.getInternalName());
                }
            }
        }
        mv.visitMethodInsn(opcode, owner, name, desc);
    }
}
