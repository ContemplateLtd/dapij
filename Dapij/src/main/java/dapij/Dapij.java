/*
 * TODO: enter sensible info 
 */
package dapij;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 *
 * @author Nikolay Pulev
 */
public class Dapij implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("Instrumenting " + className + " ...");

        try {
            /* read and instrument class bytecode */
            ClassReader creader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            
            /*
             * Uncomment the lines below and pass tcv to the constructor of
             * sc_visitor to print the instrumented bytecode on System.out.
             */
            //TraceClassVisitor tcv = new TraceClassVisitor(writer,
            //        new PrintWriter(System.out));
            
            ClassVisitor sc_visitor = new StatsCollector(writer);
            creader.accept(sc_visitor, 0);
            byte[] bts = writer.toByteArray();
            
            return bts;
        } catch (IllegalStateException e) {
            throw new IllegalClassFormatException("Error: " + e.getMessage()
                    + " on class " + classfileBuffer);
        }
    }

    public static void premain(String arglist, Instrumentation inst) {
        inst.addTransformer(new Dapij());
    }
}