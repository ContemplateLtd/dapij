/*
 * TODO: enter sensible info
 */
package dapij;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.FieldNode;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class StatsCollector extends ClassVisitor {
    
    private String sourceFile;
    private boolean isFieldPresent;
    private static final String insrtFldName = "_info";

    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        MethodVisitor mv;
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new ObjectCreationVisitor(mv, name, sourceFile);
        }

        return mv;
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        if (name.equals(insrtFldName)) {
            isFieldPresent = true;
        }
        return cv.visitField(access, name, desc, signature, value);
    }
    
    @Override
    public void visitSource(String source, String debug) {
        sourceFile = source;    /* Obtain name of source file */
        cv.visitSource(source, debug);
    }
    
    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            /* add a field of type ObjectCreation to current class */
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE, insrtFldName,
                    Type.getDescriptor(ObjectCreationStats.class), null, null);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        cv.visitEnd();
    }

    public StatsCollector(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }
}
