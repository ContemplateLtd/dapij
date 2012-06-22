/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import org.objectweb.asm.ClassVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class StatsCollector extends ClassVisitor {
	
	public StatsCollector(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		System.err.println(superName);
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		System.out.println("Method " + name);
		MethodVisitor mv;
		mv = super.visitMethod(access, name, desc, signature, exceptions);
		if(mv != null) mv = new ReturnVisitor(mv, name);
		return mv;
	}
}
