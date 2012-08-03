package transform;

import agent.InstIdentifier;
import agent.RuntmEventSrc;
import java.util.Stack;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A class for instrumenting client programs that allows the agent to detect
 * newly created instances during execution of these programs.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstCreatVistr extends InsnOfstReader {

    /* Stats for the object creation currently detected */
    private String creatorMethod; /* name of method where creation occured */

    /**
     * A stack for handling nested NEW-INVOKEVIRTUAL instruction patterns met
     * while visiting methods. Needed for inserting bytecode for recording info
     * about created objects.
     */
    private Stack<StackElement> objectCreationStack;

    /**
     * A wrapper of object creation information. Used to compose the entries of
     * instanceCreationStack.
     */
    private final class StackElement {
        private Type type;
        private String method;
        private int offset;

        public StackElement(Type type, String method, int offset) {
            this.type = type;
            this.method = method;
            this.offset = offset;
        }
    }

    public InstCreatVistr(MethodVisitor mv, String name) {
        super(mv);
        this.creatorMethod = name;
        objectCreationStack = new Stack<StackElement>();
    }

    /**
     * Collects information, pushes it on a objectCreationStack & loads the
     * necessary refs on the stack. This provides the necessary data for an
     * object creation detection when the matching INVOKEVIRTUAL instruction is
     * visited (i.e. when the created instance has been initialised).
     *
     * @param opcode
     * @param type
     */
    @Override
    public void visitTypeInsn(int opcode, String type) {

        /* Only intereseted in NEW instructions */
        if (opcode != Opcodes.NEW) {
            mv.visitTypeInsn(opcode, type);
            return;
        }

        /*
         * Push the object creation data onto the stack and leave it there until
         * object initialization (construction) has completed.
         */
        Type t = Type.getObjectType(type);
        objectCreationStack.push(new StackElement(t, creatorMethod, getInsnOfst()));

        mv.visitTypeInsn(opcode, type); /* create ref of object being created */
        mv.visitInsn(Opcodes.DUP); /* supply map key to fireEvent call */
    }

    /**
     * Pops information from objectCreationStack & injects code to create an
     * event that registers the a newly created & instantiated instance.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {

        mv.visitMethodInsn(opcode, owner, name, desc);

        /* Inject code to detect object creations here. */
        /* Don't transform if not a constructor or objectCreationStack empty */
        if (!name.equals("<init>") || objectCreationStack.empty()) {
            return;
        }

        /*
         * Pop if this method same as creator method and type of object being
         * created equals type of stack entry to be popped.
         */
        StackElement top = objectCreationStack.lastElement();
        if (!top.method.equals(this.creatorMethod) || !top.type.getInternalName().equals(owner)) {
            return;
        }

        /*
         * Push a reference to the CreationEventGenerator object to allow for
         * calling it's fireEvent instance method (just after visiting the
         * INVOKESPECIAL insn corresponding to the previously detected NEW).
         */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(RuntmEventSrc.class), "INSTANCE",
                Type.getDescriptor(RuntmEventSrc.INSTANCE.getClass()));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(RuntmEventSrc.INSTANCE.getClass()), "getCreatEventSrc",
                Type.getMethodDescriptor(Type.getType(CreatEventSrc.class)));

        /* Get Obj unique id & push. */
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(InstIdentifier.class),
                "INSTANCE", Type.getDescriptor(InstIdentifier.class));
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(InstIdentifier.class),
                "getId", Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(Object.class)));

        /* Push remaining arguments on stack. */
        StackElement currentElem = objectCreationStack.pop();
        mv.visitLdcInsn(currentElem.type);
        mv.visitLdcInsn(currentElem.method);
        mv.visitLdcInsn(currentElem.offset);

        /* Get thread ID & push. */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class),
                "currentThread", Type.getMethodDescriptor(Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Thread.class), "getId",
                Type.getMethodDescriptor(Type.getType(long.class)));

        /*
         * Put an entry into the concurrent map to record this creation.
         *
         * TODO: Could the following be a problem - what if the constructor
         * passes a reference to the created object to another thread and that
         * thread deletes the object (leaked reference)?
         */
        String descriptor = Type.getMethodDescriptor(Type.getType(void.class),
                Type.getType(int.class), Type.getType(Class.class), Type.getType(String.class),
                Type.getType(int.class), Type.getType(long.class));

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(CreatEventSrc.class),
                "fireEvent", descriptor);
    }
}
