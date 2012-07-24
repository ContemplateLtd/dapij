/*
 * TODO: doc comment
 */
package transform;

import agent.ObjectStack;
import agent.Breakpoint;
import agent.RuntimeEventRegister;
import agent.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import org.objectweb.asm.Label;
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
    private String sourceFile;      /* source file */
    private InsnOffsetVisitor offsetCounter;
    private int line; // TODO: Left for testing, but should perhaps be removed
    
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
        
        /* Inject code to detect object accesses here. */
        if (    opcode == Opcodes.INVOKEVIRTUAL ||
                opcode == Opcodes.INVOKEINTERFACE ||
                (opcode == Opcodes.INVOKESPECIAL && !name.equals("<init>"))) {
            
            /* 
             * To access the object reference, all the arguments have to be
             * removed first.
             */
            /* Create useful data structures */
            HashMap<Type, String> oneSlotArgFuncMap =
                    new HashMap<Type, String>();
            oneSlotArgFuncMap.put(Type.getType(Object.class), "popObj");
            oneSlotArgFuncMap.put(Type.BOOLEAN_TYPE, "popBoolean");
            oneSlotArgFuncMap.put(Type.BYTE_TYPE, "popByte");
            oneSlotArgFuncMap.put(Type.CHAR_TYPE, "popChar");
            oneSlotArgFuncMap.put(Type.FLOAT_TYPE, "popFloat");
            oneSlotArgFuncMap.put(Type.INT_TYPE, "popInt");
            oneSlotArgFuncMap.put(Type.SHORT_TYPE, "popShort");
            
            HashMap<Type, String> twoSlotArgFuncMap =
                    new HashMap<Type, String>();
            twoSlotArgFuncMap.put(Type.DOUBLE_TYPE, "popDouble");
            twoSlotArgFuncMap.put(Type.LONG_TYPE, "popLong");
            
            Type[] argTypes = Type.getArgumentTypes(desc);  /* Get arg types */
            
            /* Pop args & store them temporarily in external stack. */
            for (int i = argTypes.length - 1; i >= 0; i--) {
                Type argType = argTypes[i];
                mv.visitFieldInsn(Opcodes.GETSTATIC,
                        Type.getInternalName(ObjectStack.class), "INSTANCE",
                        Type.getDescriptor(ObjectStack.INSTANCE.getClass()));
                
                if (twoSlotArgFuncMap.containsKey(argType)) {
                    mv.visitInsn(Opcodes.DUP_X2);   /* fancy swap if 2 slots */
                    mv.visitInsn(Opcodes.POP);      /* fancy swap if 2 slots */
                } else {
                    
                    /* Use Object type if argType not supported */
                    if (!oneSlotArgFuncMap.containsKey(argType)) {
                        argType = Type.getType(Object.class);   
                    }
                    mv.visitInsn(Opcodes.SWAP); /* regular swap if 1 slot */
                }
                
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class), "push",
                        Type.getMethodDescriptor(Type.VOID_TYPE, argType));
            }
            mv.visitInsn(Opcodes.DUP);
            
            injectRegAccessCall();  /* Register this access. */
            
            /* Push the arguments back on the stack */
            for (Type argType : argTypes) {
                mv.visitFieldInsn(Opcodes.GETSTATIC,
                        Type.getInternalName(ObjectStack.class), "INSTANCE",
                        Type.getDescriptor(ObjectStack.INSTANCE.getClass()));
                
                boolean supportedType = true;
                String func = "popObj";
                if (oneSlotArgFuncMap.containsKey(argType)) {
                    func = oneSlotArgFuncMap.get(argType);
                } else if (twoSlotArgFuncMap.containsKey(argType)) {
                    func = twoSlotArgFuncMap.get(argType);
                } else {
                    supportedType = false;
                }

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(ObjectStack.class), func,
                        Type.getMethodDescriptor((supportedType) ?
                                argType : Type.getType(Object.class)));
                
                if (!supportedType) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST,
                            argType.getInternalName());
                }
            }
        }
        
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
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name,
            String desc) {
        if (opcode == Opcodes.GETFIELD) {
            mv.visitInsn(Opcodes.DUP);      /* Dup if arg is an obj ref. */
            injectRegAccessCall();

        } else if (opcode == Opcodes.PUTFIELD) {
            Type type = Type.getType(desc);
            
            /* If 2-slot value on stack, can't just swap. Do this instad. */
            if (type.equals(Type.LONG_TYPE) ||
                    type.equals(Type.DOUBLE_TYPE)) {
                mv.visitInsn(Opcodes.DUP2_X1);
                mv.visitInsn(Opcodes.POP2);
                mv.visitInsn(Opcodes.DUP_X2);
                injectRegAccessCall();
            } else {
                mv.visitInsn(Opcodes.SWAP); /* Swap value and obj ref. */
                mv.visitInsn(Opcodes.DUP);  /* Dup obj ref to pass as arg. */
                injectRegAccessCall();
                mv.visitInsn(Opcodes.SWAP); /* swap back */
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
    
    private void injectRegAccessCall() {
        /* 
         * Get a reference to InstanceCreationTracker and put it on the
         * bottom
         */
        mv.visitFieldInsn(Opcodes.GETSTATIC,
                Type.getInternalName(RuntimeEventRegister.class), "INSTANCE",
                Type.getDescriptor(RuntimeEventRegister.INSTANCE.getClass()));
        mv.visitInsn(Opcodes.SWAP);

        /* get the thread ID */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(Thread.class), "currentThread",
                Type.getMethodDescriptor(Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(Thread.class), "getId",
                Type.getMethodDescriptor(Type.getType(long.class)));

        /* register object access */
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(RuntimeEventRegister.class), "regAccess",
                Type.getMethodDescriptor(Type.getType(void.class),
                        Type.getType(Object.class),
                        Type.getType(long.class)));
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
        
        if (b.isWriteToXML()) {
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                    Type.getInternalName(RuntimeEventRegister.class),
                    "INSTANCE",
                    Type.getDescriptor(RuntimeEventRegister.INSTANCE
                            .getClass()));

            /* Load fullpath of xml output file on stack */
            mv.visitLdcInsn(Settings.INSTANCE.getSett(Settings.SETT_XML_OUT));
            
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
            
            /* Output snapshot to an XML file */
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(RuntimeEventRegister.class),
                    "writeXmlSnapshot",
                    Type.getMethodDescriptor(Type.VOID_TYPE,
                            Type.getType(String.class),
                            Type.getType(Breakpoint.class)));
        }
    }
}
