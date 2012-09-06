package transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import agent.InstanceIdentifier;
import agent.RuntimeEventSource;

/**
 * Provides common functionality to visitors that generate events on instance
 * accesses.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public abstract class AccessVisitor extends MethodVisitor {

    public AccessVisitor(int api, MethodVisitor mv) {
        super(api, mv);
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
