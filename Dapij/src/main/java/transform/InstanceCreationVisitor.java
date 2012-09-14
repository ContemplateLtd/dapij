package transform;

import agent.InstanceIdentifier;
import agent.RuntimeEventSource;
import java.util.Stack;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A class for instrumenting client programs that allows the agent to detect
 * newly created instances at runtime.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceCreationVisitor extends InstructionOffsetReader {

    /* Data for the object creation currently detected. */
    private String creatorMethod; /* name of method where creation occurred */

    /**
     * A {@link Stack} for handling nested NEW-INVOKEVIRTUAL instruction
     * patterns during method visiting. Used for loading correct data on stack
     * when object creations are detected (immediately after INVOKESPECIAL).
     */
    private Stack<StackElement> objectCreationStack;

    /**
     * A wrapper of object creation information. Used to compose the entries of
     * {@code objectCreationStack}.
     */
    private static final class StackElement {

        private String name;
        private String method;
        private int offset;

        public StackElement(String name, String method, int offset) {
            this.name = name;
            this.method = method;
            this.offset = offset;
        }

        public String getName() {
            return name;
        }

        public String getMethod() {
            return method;
        }

        public int getOffset() {
            return offset;
        }
    }

    public InstanceCreationVisitor(MethodVisitor mv, String name) {
        super(mv);
        this.creatorMethod = name;
        objectCreationStack = new Stack<StackElement>();
    }

    /**
     * Collects information, pushes it on a {@code objectCreationStack} & loads the
     * necessary refs on the stack. This provides the necessary data for an
     * object creation detection when the matching INVOKEVIRTUAL instruction is
     * visited (i.e. when the created instance has been initialised).
     *
     * @param opcode
     * @param type
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {
        mv.visitTypeInsn(opcode, type);

        /* Only interested in NEW instructions */
        if (opcode != Opcodes.NEW) {
            return;
        }

        mv.visitInsn(Opcodes.DUP); /* Dup ref for consumption by the fireEvent call. */

        /* Push creation data on stack to pop it when construction ends. */
        objectCreationStack.push(
                new StackElement(type, creatorMethod, getInsnOfst()));
    }

    /**
     * Pops information from {@code objectCreationStack} & injects code to
     * create an event that registers the a newly created & instantiated
     * instance.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        mv.visitMethodInsn(opcode, owner, name, desc);

        /* Inject code to detect object creations here. */
        /* Do not transform if not a constructor call or objectCreationStack empty. */
        if (!name.equals("<init>") || objectCreationStack.empty()) {
            return;
        }

        /*
         * Pop if this method same as creator method and type of object being
         * created equals type of stack entry to be popped.
         */
        StackElement top = objectCreationStack.lastElement();
        if (!top.method.equals(this.creatorMethod) || !top.getName().equals(owner)) {
            return;
        }

        /*
         * Push a reference to the getCreatEventSrc instance to allow for
         * calling it's fireEvent instance method (just after visiting the
         * INVOKESPECIAL insn corresponding to the previously detected NEW).
         */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(RuntimeEventSource.class),
                "INSTANCE", Type.getDescriptor(RuntimeEventSource.INSTANCE.getClass()));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(RuntimeEventSource.INSTANCE.getClass()), "getCreatEventSrc",
                Type.getMethodDescriptor(Type.getType(CreationEventSource.class)));

        /* Get Obj unique id & push. */
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(InstanceIdentifier.class),
                "INSTANCE", Type.getDescriptor(InstanceIdentifier.class));
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(InstanceIdentifier.class),
                "getId", Type.getMethodDescriptor(Type.LONG_TYPE, Type.getType(Object.class)));

        /* Push remaining arguments on stack. */
        StackElement currentElem = objectCreationStack.pop();
        mv.visitLdcInsn(currentElem.getName().replace('/', '.')); /* Convert to binary name. */
        mv.visitLdcInsn(currentElem.getMethod());
        mv.visitLdcInsn(currentElem.getOffset());

        /* Get thread ID & push. */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class),
                "currentThread", Type.getMethodDescriptor(Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Thread.class), "getId",
                Type.getMethodDescriptor(Type.getType(long.class)));

        /* Generate an event to record creation. */
        String descriptor = Type.getMethodDescriptor(Type.getType(void.class),
                Type.getType(long.class), Type.getType(String.class), Type.getType(String.class),
                Type.getType(int.class), Type.getType(long.class));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(CreationEventSource.class),
                "fireEvent", descriptor);
    }
}
