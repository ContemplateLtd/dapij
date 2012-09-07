package transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import agent.ArgumentStack;
import agent.InstanceIdentifier;
import agent.RuntimeEventSource;

/**
 * Provides common functionality to visitors that generate events on instance
 * accesses.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InstanceAccessVisitor extends MethodVisitor {

    private String methodName;
    private String className;

    public InstanceAccessVisitor(MethodVisitor mv, String methodName, String className) {
        super(Opcodes.ASM4, mv);
        this.methodName = methodName;
        this.className = className;
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
        if (!(methodName.equals("<init>") && (owner.equals(className)))) {
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

    /** Injects bytecode to generate access events. */
    protected void injectFireAccsEvent() {

        /* Get a reference to InstanceCreationTracker and put it on the bottom. */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(RuntimeEventSource.class),
                "INSTANCE", Type.getDescriptor(RuntimeEventSource.INSTANCE.getClass()));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(RuntimeEventSource.INSTANCE.getClass()), "getAccsEventSrc",
                Type.getMethodDescriptor(Type.getType(AccessEventSource.class)));

        /* Get Obj unique id & push. */
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(InstanceIdentifier.class),
                "INSTANCE", Type.getDescriptor(InstanceIdentifier.class));
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(InstanceIdentifier.class),
                "getId", Type.getMethodDescriptor(Type.LONG_TYPE, Type.getType(Object.class)));

        /* Get the thread ID. */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class),
                "currentThread", Type.getMethodDescriptor(Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Thread.class), "getId",
                Type.getMethodDescriptor(Type.getType(long.class)));

        /* Register object access. */
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(AccessEventSource.class),
                "fireEvent",
                Type.getMethodDescriptor(Type.getType(void.class), Type.getType(long.class),
                        Type.getType(long.class)));
    }
}
