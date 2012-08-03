package transform;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * A ClassFileTransformer used by the agent to instrument user program classes
 * upon loading. The classes of this project are filtered and not transformed.
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class Transfmr implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

        /*
         * Do not instrument project's classes at runtime.
         *
         * TODO: should remove all additional classes from this blacklist! Code
         * currently breaks if any of these is removed.
         */
        if (className.startsWith("agent/") || className.startsWith("comms/")
                || className.startsWith("plugin/") || className.startsWith("transform/")
                || className.startsWith("com/google/common/collect/")
                || className.startsWith("java/io/") || className.startsWith("sun/net/")
                || className.startsWith("java/util/")) {
            System.out.println("Did not instument " + className + "!");

            return classfileBuffer;
        }
        System.out.println("Instrumenting " + className + " ...");

        return transformClass(classfileBuffer);
    }

    /**
     * Reads and instruments class bytecode using a StatsCollector visitor.
     */
    public static byte[] transformClass(byte[] classfileBuffer) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor sc = new StatsCollector(writer);
        new ClassReader(classfileBuffer).accept(sc, 0);

        return writer.toByteArray();
    }
}
