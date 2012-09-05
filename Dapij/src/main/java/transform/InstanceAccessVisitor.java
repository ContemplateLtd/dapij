package transform;

import agent.ArgumentStack;
import agent.InstanceIdentifier;
import agent.RuntimeEventSource;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Injects code for generating events when instances are being accessed. The
 * events contain a thread identifier for later (concurrency) analysis that
 * allows identifying instances accessed from threads that did not create them.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InstanceAccessVisitor extends MethodVisitor {

    private String methodName;

    public InstanceAccessVisitor(MethodVisitor mv, String name) {
        super(Opcodes.ASM4, mv);
        methodName = name;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {

        /* Inject code to detect object accesses here. */
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE
                || (opcode == Opcodes.INVOKESPECIAL && !name.equals("<init>"))) {
            Type[] argTypes = Type.getArgumentTypes(desc); /* Get arg types */

            /* Pop args & store them temporarily in external stack. */
            for (int i = argTypes.length - 1; i >= 0; i--) {
                Type argType = argTypes[i];
                if (!ArgumentStack.TWO_SLOT_ARG_FUNC_MAP.containsKey(argType)
                        && !ArgumentStack.ONE_SLOT_ARG_FUNC_MAP.containsKey(argType)) {
                        argType = Type.getType(Object.class);
                }
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ArgumentStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, argType));
            }
            mv.visitInsn(Opcodes.DUP);
            injectFireAccsEvent(); /* Register this access. */

            /* Push the arguments back on the stack. */
            for (Type argType : argTypes) {
                boolean supportedType = true;
                String func;
                func = ArgumentStack.ONE_SLOT_ARG_FUNC_MAP.get(argType);
                if (func == null) {
                    func = ArgumentStack.TWO_SLOT_ARG_FUNC_MAP.get(argType);
                }
                if (func == null) {
                    func = "popObj";
                    supportedType = false;
                }

                mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ArgumentStack.class),
                        func, Type.getMethodDescriptor(
                                (supportedType) ? argType : Type.getType(Object.class)));

                if (!supportedType) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, argType.getInternalName());
                }
            }
        }
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        /*
         * Register field accesses.
         *
         * This "if" statement is used to make the agent ignore cases when
         * object is accessed from within its own constructor, as they caused
         * errors. See the doc folder for details.
         */
        if (!(methodName.equals("<init>") && (name.equals("this") || name.startsWith("this$")))) {
            if (opcode == Opcodes.GETFIELD) {
                mv.visitInsn(Opcodes.DUP); /* Dup if arg is an obj ref. */
                injectFireAccsEvent();
            } else if (opcode == Opcodes.PUTFIELD) {
                Type type = Type.getType(desc);

                /* If 2-slot value on stack, can't just swap. Do this instead. */
                if (type.equals(Type.LONG_TYPE) || type.equals(Type.DOUBLE_TYPE)) {
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

    private void injectFireAccsEvent() {

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
