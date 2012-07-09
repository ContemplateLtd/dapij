/*
 * TODO: enter meaningful info 
 */
package dapij;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        
        /* Do not instrument agent classes */
        //comment this!
        if (className.startsWith("dapij/") || className.startsWith("com/google/common/collect/") || className.startsWith("java/io/") ) {
            System.out.println("Did not instument " + className + "!");
            return classfileBuffer;
        }
        
        System.out.println("Instrumenting " + className + " ...");

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
    }

    public static void premain(String arglist, Instrumentation inst) throws IOException {
        System.out.println("CLASSPATH: " +
                System.getProperty("java.class.path"));
        inst.addTransformer(new Dapij());
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Hello! I'm Coco, your personal assistant!");
        System.out.println("In which file is the breakpoint?");
        InstanceCreationVisitor.targetFile = br.readLine();
        System.out.println("And what is its line number?");
        InstanceCreationVisitor.targetLine = Integer.parseInt(br.readLine());
        System.out.println("Do you want me to write it to an XML file as well? (y)");
        if(br.readLine().equalsIgnoreCase("y")) {
            InstanceCreationVisitor.writeToXML = true;
        }
        System.out.println("Cool! Let me set it then!");
    }
}