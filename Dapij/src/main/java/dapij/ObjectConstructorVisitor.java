/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class ObjectConstructorVisitor extends MethodVisitor {
    String className;       /* Name of class - should be java.lang.Object */
    String insrtFldName;    /* Name of field to be inserted */
    
    public ObjectConstructorVisitor(MethodVisitor mv, String className,
            String insrtFldName) {
        super(Opcodes.ASM4, mv);
        this.className = className;
    }
    
    @Override
    public void visitCode() {
        mv.visitCode();
        
        // load reference to 'this' for PUTFIELD
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        // create and push a reference to ObjectCreationStats for PUTFIELD
        mv.visitTypeInsn(Opcodes.NEW,
                Type.getInternalName(InstanceCreationStats.class));
        
        /* Initialise the InstanceCreation */
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,  // pop line number dynamically
                Type.getInternalName(LineNumTracker.class), "pop",
                "()Ljava/lang/Integer;");
        // TODO: Should something be done here?
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,   // initialise _init field 
                Type.getInternalName(InstanceCreationStats.class), "<init>",
                "(I)V");
        
        /*
         * Set the _info field with the newly created InstanceCreationStats
         * object that's put on the stack by the above code
         */
        mv.visitFieldInsn(Opcodes.PUTFIELD,
                Type.getObjectType(this.className).getInternalName(), "_info",
                Type.getDescriptor(InstanceCreationStats.class));
    }
}
