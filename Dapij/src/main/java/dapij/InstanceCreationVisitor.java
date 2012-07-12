/*
 * TODO: enter meningful info
 * TODO: count instruction offset with a clever approach
 */
package dapij;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Stack;
import org.objectweb.asm.*;

/**
 *
 * @author emszy
 */
public class InstanceCreationVisitor extends MethodVisitor {

    /* Stats for the object creation currently detected */
    private int insnOfst = -1;
    private String creatorMethod;   /* name of method where creation occured */
    private String sourceFile;      /* source file */
    
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
 
    public InstanceCreationVisitor(MethodVisitor mv, String name,
            String sourceFile) {
        super(Opcodes.ASM4, mv);
        this.creatorMethod = name;
        this.sourceFile = sourceFile;
        objectCreationStack = new Stack<StackElement>();
    }
    
    @Override
    public void visitTypeInsn(int opcode, String type) {
        insnOfst++; /* keep instruction count up to date */
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
                creatorMethod, insnOfst));

        /* create reference of object being created */
        mv.visitTypeInsn(opcode, type);

        /* duplicate on stack to supply the map key for the put method call */
        mv.visitInsn(Opcodes.DUP);
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitMethodInsn(opcode, owner, name, desc);
        
        /* Don't transform if not a constructor or objectCreationStack empty */
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
         * the created instances info objects to mark this creation.
         * 
         * TODO: Could the following be a problem - what if the constructor
         * passes a reference to the created object to another thread and
         * that thread deletes the object? (leaked reference)
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
    public void visitFieldInsn(int opcode, String owner, String name,
            String desc) {
        insnOfst++; /* keep instruction count up to date */
 
        if (opcode == Opcodes.GETFIELD) {
  
            /* Duplicate the object reference to pass as an argument */
            mv.visitInsn(Opcodes.DUP);
            
            /* 
             * Get a reference to InstanceCreationTracker and put it on the
             * bottom
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
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(InstanceCreationTracker.class),
                    "registerAccess", Type.getMethodDescriptor(
                            Type.getType(void.class),
                            Type.getType(Object.class),
                            Type.getType(long.class)));
        }
        
        else if (opcode == Opcodes.PUTFIELD) {
            //TODO
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
    
    @Override
    public void visitLineNumber(int line, Label start) {
        /*
         * In the current version, in case the specified line is not valid, 
         * the code will be inserted before the next valid line.
         */
        HashMap<Integer, HashMap<String, Breakpoint>> bpts =
                Settings.INSTANCE.getBreakpts();
        if (!bpts.containsKey(line) ||
                !bpts.get(line).containsKey(sourceFile) ||
                bpts.get(line).get(sourceFile).isVisited()) {
            mv.visitLineNumber(line, start);
            return;
        }

        Breakpoint b = bpts.get(line).get(sourceFile);
        b.setVisited(true);
        
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(System.class),
                "out", Type.getDescriptor(PrintStream.class));
        mv.visitLdcInsn("State just before line " + line);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(PrintStream.class), "println",
                Type.getMethodDescriptor(Type.VOID_TYPE,
                        Type.getType(String.class)));

        mv.visitFieldInsn(Opcodes.GETSTATIC,
                Type.getInternalName(InstanceCreationTracker.class), "INSTANCE", 
                Type.getDescriptor(InstanceCreationTracker.INSTANCE.
                        getClass()));

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(InstanceCreationTracker.class),
                "displayInfo",
                Type.getMethodDescriptor(Type.getType(void.class)));

        if(b.isWriteToXML()) {
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                    Type.getInternalName(InstanceCreationTracker.class),
                    "INSTANCE", 
                    Type.getDescriptor(InstanceCreationTracker.INSTANCE
                            .getClass()));

            /* Load fullpath of xml output file on stack */
            mv.visitLdcInsn(Settings.INSTANCE.get(Settings.XML_OUT_SETT));
            
            /* create and load a Breakpoint object on stack */
            mv.visitTypeInsn(Opcodes.NEW,
                    Type.getInternalName(Breakpoint.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(b.getSourceFile());
            mv.visitLdcInsn(b.getLine());
            mv.visitLdcInsn(b.isVisited());
            mv.visitLdcInsn(b.isWriteToXML());
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    Type.getInternalName(Breakpoint.class), "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE,
                            Type.getType(String.class), Type.getType(int.class),
                            Type.getType(boolean.class),
                            Type.getType(boolean.class)));
            
            /* Export snapshot to XML file */
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(InstanceCreationTracker.class),
                    "writeXmlSnapshot",
                    Type.getMethodDescriptor(Type.VOID_TYPE,
                            Type.getType(String.class),
                            Type.getType(Breakpoint.class)));
        }
    }
    
    @Override
    public void visitIincInsn(int var, int increment) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitIincInsn(var, increment);
    }
    
    @Override
    public void visitInsn(int opcode) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitInsn(opcode);
    }
    
    @Override
    public void visitIntInsn(int opcode, int operand) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitIntInsn(opcode, operand);
    }
    
    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }
    
    @Override
    public void visitJumpInsn(int opcode, Label label) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitJumpInsn(opcode, label);
    }
    
    @Override
    public void visitLdcInsn(Object cst) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitLdcInsn(cst);
    }
    
    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }
    
    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitMultiANewArrayInsn(desc, dims);
    }
    
    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt,
            Label ... labels) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }
    
    @Override
    public void visitVarInsn(int opcode, int var) {
        insnOfst++; /* keep instruction count up to date */
        mv.visitVarInsn(opcode, var);
    }
}
