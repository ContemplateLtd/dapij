/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;
import dapij.StatsCollector;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.*;

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
			System.out.println("Obtaining Stats from " + classfileBuffer);
			// scan class binary format to find fields for toString() method
			ClassReader creader = new ClassReader(classfileBuffer);
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor sc_visitor = new StatsCollector(writer);
			creader.accept(sc_visitor, ClassReader.SKIP_DEBUG);
			
			return writer.toByteArray();
			
		} catch (IllegalStateException e) {
			throw new IllegalClassFormatException("Error: " + e.getMessage() +
				" on class " + classfileBuffer);
		}  
	}
	
	public static void premain(String arglist, Instrumentation inst) {
		inst.addTransformer(new Dapij());
	}
}