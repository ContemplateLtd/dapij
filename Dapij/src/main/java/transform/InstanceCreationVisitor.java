/*
 * TODO: doc comment
 */
package transform;

import agent.RuntimeEventRegister;
import java.util.Stack;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class InstanceCreationVisitor extends MethodVisitor {

    /* Stats for the object creation currently detected */
    private String creatorMethod;   /* name of method where creation occured */
    private InsnOffsetVisitor offsetCounter;
    
    /**
     * A stack for handling nested NEW-INVOKEVIRTUAL instruction patterns met
     * while visiting methods. Needed for inserting bytecode for recording
     * info about created objects.
     */
    private Stack<StackElement> objectCreationStack;

    /**
     * A wrapper of object creation information. Used to compose the
     * entries of objectCreationStack.
     */
    private class StackElement {
        public Type type;
        public String method;
        public int offset;
        
        public StackElement(Type type, String method, int offset) {
            this.type = type;
            this.method = method;
            this.offset = offset;
        }
    }
 
    public InstanceCreationVisitor(MethodVisitor mv, String name) {
        super(Opcodes.ASM4, mv);
        this.creatorMethod = name;
        objectCreationStack = new Stack<StackElement>();
    }
    
    /**
     * A reference to a InsnOffsetVisitor has to be passed to each object of
     * this type before any other of it's methods are called. This creates
     * backward access in the chain of visitors.
     * 
     * @offstCntr The method visitor that calculates the correct instruction
     * offsets prior to delegating to an instance of this class.
     */
    public void setInsnOffsetCounter(InsnOffsetVisitor offsetCounter) {
        this.offsetCounter = offsetCounter;
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        
        /* Only intereseted in NEW instructions */
        if (opcode != Opcodes.NEW) {
            mv.visitTypeInsn(opcode, type);
            return;
        }

        /*
         * Push the object creation data onto the stack and leave it there
         * until object initialization (construction) has completed.
         */
        Type t = Type.getObjectType(type);
        objectCreationStack.push(new StackElement(t, creatorMethod,
                offsetCounter.getInsnOffset()));

        /* create reference of object being created */
        mv.visitTypeInsn(opcode, type);

        /* duplicate to supply the map key for the regCreation method call */
        mv.visitInsn(Opcodes.DUP);
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
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
        if (!top.method.equals(this.creatorMethod) ||
                !top.type.getInternalName().equals(owner)) {
            return;
        }
        StackElement currentElem = objectCreationStack.pop();
        
        /*
         * Push a reference to the InstanceCreationTracker singleton object
         * to allow for calling it's put instance method (just after visiting
         * the INVOKESPECIAL that corresponds to the detected NEW).
         */
        mv.visitFieldInsn(Opcodes.GETSTATIC,
                Type.getInternalName(RuntimeEventRegister.class), "INSTANCE", 
                Type.getDescriptor(RuntimeEventRegister.INSTANCE.getClass()));
        mv.visitInsn(Opcodes.SWAP);
        
        /* push remaining arguments on stack */
        mv.visitLdcInsn(currentElem.type);
        mv.visitLdcInsn(currentElem.method);
        mv.visitLdcInsn(currentElem.offset);
        
        /* get thread ID & push it */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(Thread.class), "currentThread",
                Type.getMethodDescriptor(Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(Thread.class), "getId",
                Type.getMethodDescriptor(Type.getType(long.class)));
        
        /*
         * Put an entry into the (singleton) identity map containing
         * the created instances info objects to mark this creation.
         * 
         * TODO: Could the following be a problem - what if the constructor
         * passes a reference to the created object to another thread and
         * that thread deletes the object (leaked reference)?
         */
        String descriptor = Type.getMethodDescriptor(
                Type.getType(void.class), Type.getType(Object.class),
                Type.getType(Class.class), Type.getType(String.class),
                Type.getType(int.class), Type.getType(long.class));

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(RuntimeEventRegister.class),
                "regCreation", descriptor);
    }
}
