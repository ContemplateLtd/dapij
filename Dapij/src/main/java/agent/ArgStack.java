package agent;

import java.util.Stack;

/**
 * A stack that temporarily stores stack elements during instrumentation. Used
 * to provide access to stack entries that are difficult to access.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public final class ArgStack {

    public static final ArgStack INSTANCE = new ArgStack();

    @SuppressWarnings("rawtypes")
    private Stack stack;

    @SuppressWarnings("rawtypes")
    private ArgStack() {
        stack = new Stack();
    }

    @SuppressWarnings("unchecked")
    public void push(Object obj) {
        stack.push(obj);
    }

    @SuppressWarnings("unchecked")
    public void push(boolean i) {
        stack.push(i);
    }

    @SuppressWarnings("unchecked")
    public void push(byte i) {
        stack.push(Byte.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public void push(char i) {
        stack.push(Character.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public void push(double i) {
        stack.push(Double.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public void push(float i) {
        stack.push(Float.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public void push(int i) {
        stack.push(Integer.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public void push(long i) {
        stack.push(Long.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public void push(short i) {
        stack.push(Short.valueOf(i));
    }

    public Object popObj() {
        return stack.pop();
    }

    public boolean popBoolean() {
        return ((Boolean) stack.pop()).booleanValue();
    }

    public byte popByte() {
        return ((Byte) stack.pop()).byteValue();
    }

    public char popChar() {
        return ((Character) stack.pop()).charValue();
    }

    public double popDouble() {
        return ((Double) stack.pop()).doubleValue();
    }

    public float popFloat() {
        return ((Float) stack.pop()).floatValue();
    }

    public int popInt() {
        return ((Integer) stack.pop()).intValue();
    }

    public long popLong() {
        return ((Long) stack.pop()).longValue();
    }

    public short popShort() {
        return ((Short) stack.pop()).shortValue();
    }
}
