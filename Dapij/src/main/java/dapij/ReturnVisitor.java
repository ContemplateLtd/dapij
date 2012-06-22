/*
 * TODO: enter sensible info 
 */
package dapij;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author emszy
 */
public class ReturnVisitor extends MethodVisitor {

    private String name;
    private int currentLine = -1;

    public ReturnVisitor(MethodVisitor mv, String name) {
        super(Opcodes.ASM4, mv);
        this.name = name;
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Creating new " + type + " in method " + name + " at line " + currentLine);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
        }
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);
        currentLine = line;
    }
}