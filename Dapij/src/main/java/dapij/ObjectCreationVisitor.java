/*
 * TODO: enter meningful info 
 */
package dapij;


import java.util.Stack;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author emszy
 */
public class ObjectCreationVisitor extends MethodVisitor {

    /* Stats for the object creation currently detected */
    private String creator;     /* name of creator method */
    private String type;        /* type of object created */
    private int line = -1;      /* line where created */
    private String sourceFile;  /* source file where created */
    private Stack<StackElement> objectCreationStack;
 
    /* A simple data structure used for storing object creation iformation
         on the stack */
    private class StackElement {
        public Type type;
        public String method;
        public int offset;
        public long threadId;
        
        
        public StackElement(Type type, String method, int offset, long threadId) {
            this.type = type;
            this.method = method;
            this.offset = offset;
            this.threadId = threadId;
        }
        
    }

    public ObjectCreationVisitor(MethodVisitor mv, String name,
            String sourceFile) {
        super(Opcodes.ASM4, mv);
        this.creator = name;
        this.sourceFile = sourceFile;
        objectCreationStack = new Stack<StackElement>();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        //mv.visitTypeInsn(opcode, type);
        if (opcode == Opcodes.NEW) {
            this.type = type;
            /* push the object creation data onto the stack and leave it there
             * until object initialization has completed
             */
            objectCreationStack.push(new StackElement(Type.getType(type), creator, line, 73110));
            
            
           /* 
            mv.visitLdcInsn(Type.getType(LineNumTracker.class));
                    
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "dapij/LineNumTracker",
                    "getLineNum", Type.getMethodDescriptor(Type.getType(Integer.class)));
            mv.visitInsn(Opcodes.POP2);
            * 
            */
            //mv.visitLdcInsn(Type.getType(LineNumTracker.class));
            //mv.visitLdcInsn(new Integer(line));
            
            //mv.visitLdcInsn(line);
            //mv.visitMethodInsn(Opcodes.INVOKESTATIC, "dapij/LineNumTracker",
            //        "push", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE));
            
            
            
            /*
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.NEW,
                    Type.getInternalName(ObjectCreationStats.class));
    
            // Create The object
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(Type.getObjectType(type));  // object class
            mv.visitLdcInsn(creator);                   // creator method
            mv.visitLdcInsn(new Integer(line));         // TODO: offset
            mv.visitLdcInsn(new Long(5));               // TODO: threadId
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    Type.getInternalName(ObjectCreationStats.class), "<init>",
                    "(Ljava/lang/Class;Ljava/lang/String;IJ)V");
            */
            
            /*
             * set the _info field with the newly created ObjectCreationStats
             * object that's put on the stack by the above code
             */
            /*
            mv.visitFieldInsn(Opcodes.PUTFIELD,
                    Type.getInternalName(ObjectCreationStats.class), "_info",
                    "Ldapij/ObjectCreationStats;");
            */
            
          /*
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");
            mv.visitLdcInsn("Created '" + type + "' in " + creator + ", "
                    + sourceFile + ":"+ line);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                    "println", "(Ljava/lang/String;)V");
          */
        }
        mv.visitTypeInsn(opcode, type);
    }
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        mv.visitMethodInsn(opcode, owner, name, desc);
        if(name.equals("<init>")) {
            StackElement currentElem = objectCreationStack.pop();
            /* do something sensible with it! */
        }
    }
    
    @Override
    public void visitLineNumber(int line, Label start) {
        mv.visitLineNumber(line, start);
        this.line = line;
    }
}
