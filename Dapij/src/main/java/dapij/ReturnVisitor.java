/*
 * TODO: enter sensible info 
 */
package dapij;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author emszy
 */
public class ReturnVisitor extends MethodVisitor {

    private String name;

    public ReturnVisitor(MethodVisitor mv, String name) {
        super(Opcodes.ASM4, mv);
        this.name = name;
    }

    @Override
    public void visitInsn(int opcode) {
        if ((opcode == Opcodes.RETURN) || (opcode == Opcodes.IRETURN)
                || (opcode == Opcodes.ARETURN) || (opcode == Opcodes.DRETURN)
                || (opcode == Opcodes.FRETURN)) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Exiting method " + name);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                    "println", "(Ljava/lang/String;)V");
        }
        mv.visitInsn(opcode);
    }
}