package transform;

import java.util.ArrayList;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A class visitor for instrumenting client programs that allows the agent to
 * collect various data during execution of these programs for the purpose of
 * dynamic analysis.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class StatsCollector extends ClassVisitor {

    public StatsCollector(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
            String[] interfaces) {

        /*
         * Change version of loaded java class to 1.5 if less than 1.4. Needed
         * because the loading of class constants in the constant pool (as
         * "mv.visitLdcInsn(currentElem.type)" does in
         * InstanceCreationVisitor::visitMethodInsn()) is not supported by some
         * older Java versions. Becase the version of the classes being
         * instrumented is unknown, this may sometimes cause validation errors.
         *
         * Cause of validation error suggested by:
         * http://mail-archive.ow2.org/asm/2009-06/msg00011.html
         *
         * Ezample outputs (with and without the validation error) of the
         * command "mvn -e test &> <output*>", the generated bytecode and a diff
         * file are available in ${basedir}/doc/class-constants-issue directory.
         *
         * TODO: This fix might be the case of additional unforeseen errors.
         */
        ArrayList<Integer> javaVersions = new ArrayList<Integer>();
        javaVersions.add(Integer.valueOf(Opcodes.V1_1));
        javaVersions.add(Integer.valueOf(Opcodes.V1_2));
        javaVersions.add(Integer.valueOf(Opcodes.V1_3));
        javaVersions.add(Integer.valueOf(Opcodes.V1_4));
        cv.visit((javaVersions.contains(version)) ? Opcodes.V1_5 : version, access, name,
                signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
            String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        /* Insert bytecode to track created objectects */
        if (mv == null) {
            return mv;
        }

        /*
         * CHAIN: InsnOfstVistr -> InstCreatVistr -> InstAccsVistr
         */
        InstAccsVistr iav = new InstAccsVistr(mv, name);
        InstCreatVistr icv = new InstCreatVistr(iav, name);
        mv = new InsnOfstProvdr(icv); /* create the provider icv requires */

        return mv;
    }

    @Override
    public void visitEnd() {
        // TODO: check for existing field
        cv.visitField(Opcodes.ACC_PUBLIC, "__DAPIJ_ID", Type.INT_TYPE.getDescriptor(), null, null);
        cv.visitEnd();
    }
}
