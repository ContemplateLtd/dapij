/*
 * TODO: enter meningful info
 * TODO: count instruction offset with a clever approach if possible
 */
package dapij;

import java.io.PrintStream;
import java.util.HashMap;
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
    private String creatorMethod;   /* name of method where creation occured */
    private String sourceFile;      /* source file */
    private InsnOffsetVisitor offstCntr;
    private int line;
    
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
    
    /**
     * A reference to a InsnOffsetVisitor has to be passed to each object of
     * this type before any other of it's methods are called. This creates
     * backward access in the chain of visitors.
     * 
     * @offstCntr The method visitor that calculates the correct instruction
     * offsets prior to delegating to an instance of this class.
     */
    public void setInsnOffsetCounter(InsnOffsetVisitor offstCntr) {
        this.offstCntr = offstCntr;
    }
    
    @Override
    public void visitInsn(int opcode) {
        mv.visitInsn(opcode);
        System.out.println("insn opc:" + Integer.toHexString(opcode));
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
                offstCntr.getInsnOffset()));

        /* create reference of object being created */
        mv.visitTypeInsn(opcode, type);

        /* duplicate on stack to supply the map key for the put method call */
        mv.visitInsn(Opcodes.DUP);
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
        
        System.out.println("opc:" + Integer.toHexString(opcode));
        /* Register object access */
        /* Do not register object creation here */
        if((opcode == Opcodes.INVOKEVIRTUAL)||
                (opcode == Opcodes.INVOKEINTERFACE)||
                ((opcode == Opcodes.INVOKEINTERFACE)&&
                (!name.equals("<init>")))){
            
            /* 
             * To access the object reference, all the arguments have to be
             * removed first
             */
            
            /* Get the argument type list */
            Type[] argumentTypes = Type.getArgumentTypes(desc);
            //System.out.println(argumentTypes.length);
            /* 
             * Pop the arguments from the stack and store them temporarily on 
             * an external stack
             */
            
            /* Create the stack */
            
            
            for(int i = argumentTypes.length - 1; i >= 0; i--) {
                Type argType = argumentTypes[i];
                mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(
                    ObjectStack.class), "INSTANCE", Type.getDescriptor(
                    ObjectStack.INSTANCE.getClass()));
                
                if(argType.equals(Type.getType(Object.class))) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.getType(Object.class)));
                    System.out.println("popping obj");
                }
                else if(argType.equals(Type.BOOLEAN_TYPE)) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.BOOLEAN_TYPE));
                    System.out.println("popping bool");
                }
                else if(argType.equals(Type.BYTE_TYPE)) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.BYTE_TYPE));
                    System.out.println("popping byte");
                }
                else if(argType.equals(Type.CHAR_TYPE)) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.CHAR_TYPE));
                    System.out.println("popping char");
                }
                else if(argType.equals(Type.DOUBLE_TYPE)) {
                    
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.POP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.DOUBLE_TYPE));
                    System.out.println("popping double");
                }
                else if(argType.equals(Type.FLOAT_TYPE)) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.FLOAT_TYPE));
                    System.out.println("popping float");
                }
                else if(argType.equals(Type.INT_TYPE)) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.INT_TYPE));
                    System.out.println("popping int");
                }
                else if(argType.equals(Type.LONG_TYPE)) {
                    
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.POP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.LONG_TYPE));
                    System.out.println("popping long");
                }
                else if(argType.equals(Type.SHORT_TYPE)) {
                    
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.SHORT_TYPE));
                    System.out.println("popping short");
                }
                else {
                    mv.visitInsn(Opcodes.SWAP);
                    
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, 
                        Type.getType(Object.class)));
                    System.out.println("popping obj");
                }
            }
            
            mv.visitInsn(Opcodes.DUP);
            
            /* Now the object reference is on top of the stack */
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
                
            
            /* Push the arguments back on the stack */
            for(Type argType : argumentTypes) {
                
                mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(
                    ObjectStack.class), "INSTANCE", Type.getDescriptor(
                    ObjectStack.INSTANCE.getClass()));
                
                if(argType.equals(Type.getType(Object.class))) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popObj", Type.getMethodDescriptor(
                        Type.getType(Object.class)));
                    System.out.println("pushing obj");
                }
                else if(argType.equals(Type.BOOLEAN_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popBoolean", Type.getMethodDescriptor(
                        Type.BOOLEAN_TYPE));
                    System.out.println("pushing bool");
                }
                else if(argType.equals(Type.BYTE_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popByte", Type.getMethodDescriptor(
                        Type.BYTE_TYPE));
                    System.out.println("pushing byte");
                }
                else if(argType.equals(Type.CHAR_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popChar", Type.getMethodDescriptor(
                        Type.CHAR_TYPE));
                    System.out.println("pushing char");
                }
                else if(argType.equals(Type.DOUBLE_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popDouble", Type.getMethodDescriptor(
                        Type.DOUBLE_TYPE));
                    System.out.println("pushing double");
                }
                else if(argType.equals(Type.FLOAT_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popFloat", Type.getMethodDescriptor(
                        Type.FLOAT_TYPE));
                    System.out.println("pushing float");
                }
                else if(argType.equals(Type.INT_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popInt", Type.getMethodDescriptor(
                        Type.INT_TYPE));
                    System.out.println("pushing int");
                }
                else if(argType.equals(Type.LONG_TYPE)) {
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class),
                        "popLong", Type.getMethodDescriptor(
                        Type.LONG_TYPE));
                    System.out.println("pushing long");
                }
                else if(argType.equals(Type.SHORT_TYPE)) {mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                   Type.getInternalName(ObjectStack.class),
                        "popShort", Type.getMethodDescriptor(
                        Type.SHORT_TYPE));
                    System.out.println("pushing short");
                } else {mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(ObjectStack.class),
                        "popObj", Type.getMethodDescriptor(
                        Type.getType(Object.class)));
                    mv.visitTypeInsn(Opcodes.CHECKCAST, argType.getInternalName());
                    System.out.println("pushing obj");
                }
            }
            
            
            //mv.visitInsn(Opcodes.POP);
            
            
        }
        
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
            
            Type type = Type.getType(desc);
            
            if(type.equals(Type.LONG_TYPE) || type.equals(Type.DOUBLE_TYPE)) {
                
                /* 
                 * If the value to be stored is of computational type 2, then 
                 * we cannot swap the top 2 values on the stack. This case has 
                 * to be treated separately
                 */
                
                /* Copy the  */
                mv.visitInsn(Opcodes.DUP2_X1);
                mv.visitInsn(Opcodes.POP2);
                mv.visitInsn(Opcodes.DUP_X2);
                
                
                /* 
                * Get a reference to InstanceCreationTracker and put it on the
                * bottomd
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
            else {
                
                /* swap the value and the object reference */
                mv.visitInsn(Opcodes.SWAP);
                
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
                
                /* swap back */
                mv.visitInsn(Opcodes.SWAP);

            }
            
            //TODO
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
    
    @Override
    public void visitLineNumber(int line, Label start) {
        this.line = line;// TODO: remove
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
}
