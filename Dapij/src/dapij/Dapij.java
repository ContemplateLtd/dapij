/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

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
       InputStream is = Dapij.class.getResourceAsStream("NewClass.class");
       ClassReader cr = new ClassReader(is);
       ClassWriter cw = new ClassWriter(0); //TODO: Read javadoc
       
       
       ClassVisitor cv = new ClassVisitor(Opcodes.ASM4, cw) {

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                System.err.println(superName);
                super.visit(version, access, name, signature, superName, interfaces);
            }
           
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
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }

        };
       cr.accept(cv, 0);
       System.out.println("Class Length: " + cw.toByteArray().length);
    }
}
