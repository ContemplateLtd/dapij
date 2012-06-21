/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.objectweb.asm.*;

/**
 *
 * @author Nikolay Pulev
 */
public class Dapij {

    /**
     * @param args the command line arguments
     */
    public static int a = 5;

    public static void main(String[] args) throws IOException {
        InputStream is = Dapij.class.getResourceAsStream("HelloAzura.class");
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES); //TODO: Read javadoc


        ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) {

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                System.err.println(superName);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public FieldVisitor visitField(int access, String name,
                    String desc,
                    String signature,
                    Object value) {
                System.out.println("Name: " + name);
                return super.visitField(access, name, desc, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                System.out.println("Method " + name);
                MethodVisitor mv;
                mv = super.visitMethod(access, name, desc, signature, exceptions);
                if(mv != null)
                    mv = new ReturnVisitor(mv, name);
                return mv;
            }
        };
        cr.accept(cv, 0);
        System.out.println("Class Length: " + cw.toByteArray().length);
        FileOutputStream fos = new FileOutputStream("HelloAzura.class");
        fos.write(cw.toByteArray());
        fos.close();
    }
}
