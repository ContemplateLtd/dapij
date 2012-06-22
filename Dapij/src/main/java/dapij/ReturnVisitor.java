/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    /*
    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN
                || opcode == Opcodes.IRETURN
                || opcode == Opcodes.ARETURN
                || opcode == Opcodes.DRETURN
                || opcode == Opcodes.FRETURN) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Exiting method " + name);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
        }
        mv.visitInsn(opcode);
    }
    * 
    */
}