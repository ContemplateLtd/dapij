package transform;

import agent.ArgStack;
import agent.InstIdentifier;
import agent.RuntmEventSrc;
import java.util.HashMap;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Injects code for generating events when instances are being accessed. The
 * events contain a thread identifier for later (concurrency) analysis that
 * allows identifying instances accessed from threads that did not create them.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InstAccsVistr extends MethodVisitor {

    private String methodName;

    public InstAccsVistr(MethodVisitor mv, String name) {
        super(Opcodes.ASM4, mv);
        methodName = name;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {

        /* Inject code to detect object accesses here. */
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE
                || (opcode == Opcodes.INVOKESPECIAL && !name.equals("<init>"))) {

            /*
             * To access the object reference, all the arguments have to be
             * removed first. Create useful data structures:
             */
            HashMap<Type, String> oneSlotArgFuncMap = new HashMap<Type, String>();
            oneSlotArgFuncMap.put(Type.getType(Object.class), "popObj");
            oneSlotArgFuncMap.put(Type.BOOLEAN_TYPE, "popBoolean");
            oneSlotArgFuncMap.put(Type.BYTE_TYPE, "popByte");
            oneSlotArgFuncMap.put(Type.CHAR_TYPE, "popChar");
            oneSlotArgFuncMap.put(Type.FLOAT_TYPE, "popFloat");
            oneSlotArgFuncMap.put(Type.INT_TYPE, "popInt");
            oneSlotArgFuncMap.put(Type.SHORT_TYPE, "popShort");

            HashMap<Type, String> twoSlotArgFuncMap = new HashMap<Type, String>();
            twoSlotArgFuncMap.put(Type.DOUBLE_TYPE, "popDouble");
            twoSlotArgFuncMap.put(Type.LONG_TYPE, "popLong");

            Type[] argTypes = Type.getArgumentTypes(desc); /* Get arg types */

            /* Pop args & store them temporarily in external stack. */
            for (int i = argTypes.length - 1; i >= 0; i--) {
                Type argType = argTypes[i];
                mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(ArgStack.class),
                        "INSTANCE", Type.getDescriptor(ArgStack.INSTANCE.getClass()));

                if (twoSlotArgFuncMap.containsKey(argType)) {
                    mv.visitInsn(Opcodes.DUP_X2);   /* fancy swap if arg takes 2 slots */
                    mv.visitInsn(Opcodes.POP);
                } else {

                    /* Use Object type if argType not supported */
                    if (!oneSlotArgFuncMap.containsKey(argType)) {
                        argType = Type.getType(Object.class);
                    }
                    mv.visitInsn(Opcodes.SWAP);     /* regular swap if 1 slot */
                }
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ArgStack.class),
                        "push", Type.getMethodDescriptor(Type.VOID_TYPE, argType));
            }
            mv.visitInsn(Opcodes.DUP);
            injectFireAccsEvent(); /* Register this access. */

            /* Push the arguments back on the stack. */
            for (Type argType : argTypes) {
                mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(ArgStack.class),
                        "INSTANCE", Type.getDescriptor(ArgStack.INSTANCE.getClass()));

                boolean supportedType = true;
                String func = "popObj";
                if (oneSlotArgFuncMap.containsKey(argType)) {
                    func = oneSlotArgFuncMap.get(argType);
                } else if (twoSlotArgFuncMap.containsKey(argType)) {
                    func = twoSlotArgFuncMap.get(argType);
                } else {
                    supportedType = false;
                }

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ArgStack.class),
                        func, Type.getMethodDescriptor(
                                (supportedType) ? argType : Type.getType(Object.class)));

                if (!supportedType) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, argType.getInternalName());
                }
            }
        }
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        /*
         * Register field accesses.
         *
         * This "if" statement is used to make the agent ignore cases when
         * object is accessed from within its own constructor, as they caused
         * errors. See the doc folder for details.
         */
        if (!(methodName.equals("<init>") && (name.equals("this") || name.startsWith("this$")))) {
            if (opcode == Opcodes.GETFIELD) {
                mv.visitInsn(Opcodes.DUP); /* Dup if arg is an obj ref. */
                injectFireAccsEvent();
            } else if (opcode == Opcodes.PUTFIELD) {
                Type type = Type.getType(desc);

                /* If 2-slot value on stack, can't just swap. Do this instad. */
                if (type.equals(Type.LONG_TYPE) || type.equals(Type.DOUBLE_TYPE)) {
                    mv.visitInsn(Opcodes.DUP2_X1);
                    mv.visitInsn(Opcodes.POP2);
                    mv.visitInsn(Opcodes.DUP_X2);
                    injectFireAccsEvent();
                } else {

                    /* Put a copy of object reference on top. */
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(Opcodes.POP);
                    injectFireAccsEvent();
                }
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    private void injectFireAccsEvent() {

        /* Get a reference to InstanceCreationTracker and put it on the bottom. */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(RuntmEventSrc.class), "INSTANCE",
                Type.getDescriptor(RuntmEventSrc.INSTANCE.getClass()));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(RuntmEventSrc.INSTANCE.getClass()), "getAccsEventSrc",
                Type.getMethodDescriptor(Type.getType(AccsEventSrc.class)));

        /* Get Obj unique id & push. */
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(InstIdentifier.class),
                "INSTANCE", Type.getDescriptor(InstIdentifier.class));
        mv.visitInsn(Opcodes.SWAP); /* Swap to keep obj ref on top. */

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(InstIdentifier.class),
                "getId", Type.getMethodDescriptor(Type.LONG_TYPE, Type.getType(Object.class)));

        /* Gget the thread ID. */
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class),
                "currentThread", Type.getMethodDescriptor(Type.getType(Thread.class)));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Thread.class), "getId",
                Type.getMethodDescriptor(Type.getType(long.class)));

        /* Register object access. */
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(AccsEventSrc.class),
                "fireEvent",
                Type.getMethodDescriptor(Type.getType(void.class), Type.getType(long.class),
                        Type.getType(long.class)));
    }
}
