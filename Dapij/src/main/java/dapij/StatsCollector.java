/*
 * TODO: enter sensible info
 */
package dapij;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class StatsCollector extends ClassVisitor {
    
    private String sourceFile;

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
            mv = new ReturnVisitor(mv, name, sourceFile);
        }

        return mv;
    }
    
    @Override
    public void visitSource(String source, String debug) {
        sourceFile = source;
        cv.visitSource(source, debug);
    }
    
    @Override
    public void visitEnd() {
        /* add an instance of ObjectCreation */
        FieldNode fieldToAdd = new FieldNode(Opcodes.ACC_PUBLIC, "_info",
                Type.getDescriptor(ObjectCreationStats.class),
                "ObjectCreationStats", null);
        fieldToAdd.accept(cv);
        cv.visitEnd();
    }

    public StatsCollector(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }
}
