/*
 * TODO: enter sensible info 
 */
package dapij;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 *
 * @author Nikolay Pulev
 */
public class Dapij implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("Processing class " + className);

        try {
            
            /* scan class binary format to find fields for toString() method */
            ClassReader creader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor sc_visitor = new StatsCollector(writer);
            creader.accept(sc_visitor, 0);

            return writer.toByteArray();

        } catch (IllegalStateException e) {
            throw new IllegalClassFormatException("Error: " + e.getMessage()
                    + " on class " + classfileBuffer);
        }
    }

    public static void premain(String arglist, Instrumentation inst) {
        inst.addTransformer(new Dapij());
    }
}