/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dapij;

import java.util.Stack;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public class ObjectStack {
    
    public final static ObjectStack INSTANCE = new ObjectStack();
    private Stack stack;
    
    private ObjectStack() {
        stack = new Stack();
    }
    
    public void push(Object obj) {
        stack.push(obj);
    }
    
    public void push(boolean i) {
        stack.push(i);
    }
    
    public void push(byte i) {
        stack.push(new Byte(i));
    }
    
    public void push(char i) {
        stack.push(new Character(i));
    }
    
    public void push(double i) {
        stack.push(new Double(i));
    }
    
    public void push(float i) {
        stack.push(new Float(i));
    }
    
    public void push(int i) {
        stack.push(new Integer(i));
    }
    
    public void push(long i) {
        stack.push(new Long(i));
    }
    
    public void push(short i) {
        stack.push(new Short(i));
    }
    
    public Object popObj() {
        return stack.pop();
    }
    
    public boolean popBoolean() {
        return ((Boolean)stack.pop()).booleanValue();
    }
    
    public byte popByte() {
        return ((Byte)stack.pop()).byteValue();
    }
    
    public char popChar() {
        return ((Character)stack.pop()).charValue();
    }
    
    public double popDouble() {
        return ((Double)stack.pop()).doubleValue();
    }
    
    public float popFloat() {
        return ((Float)stack.pop()).floatValue();
    }
    
    public int popInt() {
        return ((Integer)stack.pop()).intValue();
    }
    
    public long popLong() {
        return ((Long)stack.pop()).longValue();
    }
    
    public short popShort() {
        return ((Short)stack.pop()).shortValue();
    }
}
