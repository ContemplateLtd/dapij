/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An interface for all method visitors that can utilise instruction offsets
 * provided by InsnOffsetProvider.
 * 
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class InsnOfstReader extends MethodVisitor {
    
    private InsnOfstProvider iop;
    
    public InsnOfstReader(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }
    
    /**
     * A reference to a InsnOfstProvider has to be passed to each object of
     * this type before any other of it's methods are called so that it's
     * getInsnOfst() method can work properly. This creates backward link in
     * the chain of method visitors.
     * 
     * @offstCntr The method visitor that calculates the correct instruction
     * offsets prior to delegating to an instance of this class.
     */
    public final void setInsnOfsetProvider(InsnOfstProvider iop) {
        this.iop = iop;
    }
    
    protected final int getInsnOfst() {
        if (iop == null) {
            throw new RuntimeException("A " + InsnOfstProvider.class.getName() +
                    " has to be attached to the chain of method visitors " +
                    "before this " + InsnOfstReader.class.getName() + " so " +
                    "that it can be instantiated properly .");
        }
        return iop.getInsnOfst();
    }
}
