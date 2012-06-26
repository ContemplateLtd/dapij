/*
 * TODO: enter sensible info 
 */
package dapij;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author emszy
 */
public class ObjectCreationVisitor extends MethodVisitor {

    /* Stats for the object creation currently detected */
    private String creator;     /* name of creator method */
    private String type;        /* type of object created */
    private int line = -1;      /* line where created */
    private String sourceFile;  /* source file where created */
    private int opcode = 1-Opcodes.NEW;

    public ObjectCreationVisitor(MethodVisitor mv, String name,
            String sourceFile) {
        super(Opcodes.ASM4, mv);
        this.creator = name;
        this.sourceFile = sourceFile;
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        this.opcode = 1-Opcodes.NEW;
        if (opcode == Opcodes.NEW) {
            this.opcode = opcode;
            this.type = type;
            
            
            visitVarInsn(Opcodes.ALOAD, 0);
            visitTypeInsn(Opcodes.NEW, Type.getInternalName(ObjectCreationStats.class));
            visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(this.getClass());      /* object type */ // TODO:
            mv.visitLdcInsn(creator);   /* creator method */
            mv.visitLdcInsn(line);      /* TODO: offset */
            mv.visitLdcInsn(5);         /* TODO: threadId */
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ObjectCreationStats.class),
                    "ObjectCreationStats",
                    "(Ljava/lang/Class;Ljava/lang/String;IJ)V");

            mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(ObjectCreationStats.class),
                    "_info", "Ldapij/ObjectCreationStats;");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ObjectCreationStats.class), "toString", "()Ljava/lang/Sting;");
            /*
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Created '" + type + "' in " + creator + ", "
                    + sourceFile + ":"+ line);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                    "println", "(Ljava/lang/String;)V");
                    */
        }
        mv.visitTypeInsn(opcode, type);
    }
/*
    @Override
    public void visitEnd() {
        if (this.opcode == Opcodes.NEW) {
  
        }
        mv.visitEnd();
    }
*/    
    @Override
    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);
        this.line = line;
    }
}