/*
 * TODO: enter meningful info
 */
package dapij;


import java.util.Stack;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author emszy
 */
public class InstanceCreationVisitor extends MethodVisitor {

    /* Stats for the object creation currently detected */
    private int line = -1;          /* line of instance creation */
    private String creatorMethod;   /* name of method where creation occured */
    private String sourceFile;      /* source file */
    //private int lastLine = -1;      /* last line visited */
    public static int targetLine = 23;
    public static String targetFile = "HelloAzura.java";
    private static boolean targetVisited = false;
    public static boolean writeToXML = false;   /* write output to an XML file /*
    
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
        this.creatorMethod = name;
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
        Type t = Type.getObjectType(type);
        objectCreationStack.push(new StackElement(t,
                creatorMethod, line));

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
        if (!top.method.equals(this.creatorMethod) ||
                !top.type.getInternalName().equals(owner)) {
            return;
        }
        
        StackElement currentElem = objectCreationStack.pop();
        
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
        
        mv.visitLdcInsn(currentElem.type);
        mv.visitLdcInsn(currentElem.method);
        mv.visitLdcInsn(currentElem.offset);
        
        /* get and push thread ID on stack */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(
                Thread.class), "currentThread", Type.getMethodDescriptor(
                Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(
                Thread.class), "getId", Type.getMethodDescriptor(
                Type.getType(long.class)));
        
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
                Type.getType(Class.class),
                Type.getType(String.class),
                Type.getType(int.class), Type.getType(long.class));

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(InstanceCreationTracker.class),
                "put", descriptor);
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.GETFIELD) {
            
            /* Duplicate the object reference to pass as an argument */
            
            mv.visitInsn(Opcodes.DUP);
            
            /* 
             * Get a reference to InstanceCreationTracker and put it on the
             * bottom
             * 
             */
            
            mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(
                InstanceCreationTracker.class), "INSTANCE", 
                Type.getDescriptor(InstanceCreationTracker.
                        INSTANCE.getClass()));
            
            mv.visitInsn(Opcodes.SWAP);
            
            /* get the thread ID */
            
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(
                Thread.class), "currentThread", Type.getMethodDescriptor(
                Type.getType(Thread.class)));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(
                Thread.class), "getId", Type.getMethodDescriptor(
                Type.getType(long.class)));
            
            
            /* register object access */
            
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(
                InstanceCreationTracker.class), "registerAccess", Type.getMethodDescriptor(
                Type.getType(void.class),Type.getType(Object.class),Type.getType(long.class)));
            
        }
        else if (opcode == Opcodes.PUTFIELD) {
            //TODO
        }
        
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
    
    @Override
    public void visitLineNumber(int line, Label start) {
        this.line = line;
        
        //if(sourceFile.equals(targetFile))
            //System.out.println("line: " + line);
        
        /*
         * In the current version, in case the specified line is not valid, 
         * the code will be inserted before the next valid line.
         * 
         * TODO: 
         */
        
        if((line >= targetLine) && (!targetVisited) && (sourceFile.equals(targetFile))) {
            
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("State for line " + line);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
            
            
            
            mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(
                InstanceCreationTracker.class), "INSTANCE", 
                Type.getDescriptor(InstanceCreationTracker.
                        INSTANCE.getClass()));
            
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(
                InstanceCreationTracker.class), "displayInfo", Type.getMethodDescriptor(
                Type.getType(void.class)));
            
            if(writeToXML) {
            
            mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(
                InstanceCreationTracker.class), "INSTANCE", 
                Type.getDescriptor(InstanceCreationTracker.
                        INSTANCE.getClass()));
            
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(
                InstanceCreationTracker.class), "writeInfoToXml", Type.getMethodDescriptor(
                Type.getType(void.class)));
            
            }
            
            targetVisited = true;
            
        }
        
        //lastLine = line;
        
        mv.visitLineNumber(line, start);
    }
}
