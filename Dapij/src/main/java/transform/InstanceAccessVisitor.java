/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

import agent.ObjectStack;
import agent.RuntimeEventRegister;
import java.util.HashMap;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Injects code for generating events when instances are accessed across
 * threads.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InstanceAccessVisitor extends MethodVisitor {
    
    public InstanceAccessVisitor(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
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
}
