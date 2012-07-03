/*
 * TODO: enter sensible info
 */
package dapij;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class StatsCollector extends ClassVisitor {
    
    private String sourceFile;

    public StatsCollector(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public void visitSource(String source, String debug) {
        sourceFile = source;    /* Obtain name of source file */
        cv.visitSource(source, debug);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
                exceptions);
        
        /* Insert bytecode to track created objectects */
        if (mv != null) {
            mv = new InstanceCreationVisitor(mv, name, sourceFile);
        }
        
        return mv;
    }
}
