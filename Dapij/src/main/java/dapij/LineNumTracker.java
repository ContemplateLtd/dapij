/*
 * TODO: write a meaningful explanation
 */
package dapij;

import java.util.Stack;

/**
 *
 * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
 */
public class LineNumTracker {
    
    private static ThreadLocal < Stack < Integer > > lineNumStack = 
            new ThreadLocal() {
        @Override
        protected Stack < Integer > initialValue() {
            return new Stack < Integer >();
        }
     };
 
    public static void push(int lineNum) {
        lineNumStack.get().push(new Integer(lineNum));
    }

    public static int pop() {
        return lineNumStack.get().pop().intValue();
    }
     
    public static int size() {
        return lineNumStack.get().size();
    }
    
    /**
     * TODO: EXPLAIN - inserted as bytecode
     * @return Integer The line number of caller of 'new'
     */
    public static Integer getLineNum() {
        Integer i = new Integer(Thread.currentThread().getStackTrace()[2]
                .getLineNumber());
        System.out.println("The line is: " + i);
        return i ;// new Integer(
              //  Thread.currentThread().getStackTrace()[2].getLineNumber());
    }
}
