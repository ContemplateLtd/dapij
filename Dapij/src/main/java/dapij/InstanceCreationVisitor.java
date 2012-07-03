/*
 * TODO: enter meningful info
 */
package dapij;


import java.util.Stack;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 *
 * @author emszy
 */
public class InstanceCreationVisitor extends MethodVisitor {

    /* Stats for the object creation currently detected */
    private int line = -1;      /* line of instance creation */
    private String creator;     /* name of method where creation occured */
    private String type;        /* type of object created */
    private String sourceFile;  /* source file */
    
    /*
     * A stack for handling nested NEW-INVOKEVIRTUAL instruction patterns met
     * while visiting methods.
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

    public InstanceCreationVisitor(MethodVisitor mv, String name,
            String sourceFile) {
        super(Opcodes.ASM4, mv);
        this.creator = name;
        this.sourceFile = sourceFile;
        
        objectCreationStack = new Stack<StackElement>();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode != Opcodes.NEW) {
            mv.visitTypeInsn(opcode, type);
            return;
        }
        
        /* 
         * push the object creation data onto the stack and leave it there
         * until object initialization (construction) has completed
         */
        Type t = Type.getType(type);
        objectCreationStack.push(new StackElement(t,
                creator, line));

        /* create reference of object being created */
        mv.visitTypeInsn(opcode, type);

        /* duplicate on stack to supply the map key for the put method call */
        mv.visitInsn(Opcodes.DUP);
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
        mv.visitMethodInsn(opcode, owner, name, desc);
        
        /* Do not transform if not a constructor or if stack empty */
        if(!name.equals("<init>") || objectCreationStack.empty()) {
            return;
        }
        
        /*
         * Pop if this method same as creator and type of object being
         * created equals type of stack entry to be popped.
         */
        StackElement top = objectCreationStack.lastElement();
        if (!top.method.equals(this.creator) ||
                !top.type.getInternalName().equals(owner)) {
            return;
        }
                
        StackElement currentElem = objectCreationStack.pop();
        
//        int newLocal = this.newLocal(top.type);
//        mv.visitVarInsn(Opcodes.ASTORE, newLocal);  /* Store new instance */
//        mv.VisitVar

        /* 
         * Push a reference to the InstanceCreationTracker singleton object
         * to allow for calling it's put instance method (just after visiting
         * the INVOKESPECIAL that corresponds to this NEW).
         */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(
                InstanceCreationTracker.class), "INSTANCE", 
                Type.getDescriptor(InstanceCreationTracker.
                        INSTANCE.getClass()));
        
        mv.visitInsn(Opcodes.SWAP);
        
        //mv.visitLdcInsn(currentElem.type);
        mv.visitLdcInsn(currentElem.method);
        mv.visitLdcInsn(this.line);
        // TODO: Insert bytecode to obtain threadId dynamically.
        mv.visitLdcInsn((long) 73110);
        
        /*
         * Put and entry into the (singleton) identity map containing
         * the created instances to record the instance created.
         * 
         * TODO: Could the following be a problem - what if the constructor
         * passes a reference to the created object to another thread and
         * that thread deletes the object?
         */
        String descriptor = Type.getMethodDescriptor(
                Type.getType(void.class), Type.getType(Object.class),
                //Type.getType(Class.class),
                Type.getType(String.class),
                Type.getType(int.class), Type.getType(long.class));

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(InstanceCreationTracker.class),
                "put", descriptor);

    }
    
    @Override
    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);
        this.line = line;
    }
}
