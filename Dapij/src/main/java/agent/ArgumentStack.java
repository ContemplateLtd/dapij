package agent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.Type;

/**
 * A stack that temporarily stores stack elements during instrumentation. Used
 * to provide access to stack entries that are difficult to access.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public final class ArgumentStack {

    public static final Type OBJECT_TYPE = Type.getType(Object.class);

    /**
     * Maps computational type 1 primitive types (types taking 1 stack
     * slot when loaded on stack) to corresponding pop method names.
     */
    private static final Map<Type, String> TYPE_ONE_POP_MTHDS;
    static {
        Map<Type, String> initialValue = new HashMap<Type, String>();
        initialValue.put(Type.BOOLEAN_TYPE, "popBoolean");
        initialValue.put(Type.BYTE_TYPE, "popByte");
        initialValue.put(Type.CHAR_TYPE, "popChar");
        initialValue.put(Type.FLOAT_TYPE, "popFloat");
        initialValue.put(Type.INT_TYPE, "popInt");
        initialValue.put(Type.SHORT_TYPE, "popShort");
        TYPE_ONE_POP_MTHDS = Collections.unmodifiableMap(initialValue);
    }

    /**
     * Maps computational type 2 primitive types (types taking 2 stack
     * slot when loaded on stack) to corresponding pop method names.
     */
    private static final Map<Type, String> TYPE_TWO_POP_MTHDS;
    static {
        Map<Type, String> initialValue = new HashMap<Type, String>();
        initialValue.put(Type.DOUBLE_TYPE, "popDouble");
        initialValue.put(Type.LONG_TYPE, "popLong");
        TYPE_TWO_POP_MTHDS = Collections.unmodifiableMap(initialValue);
    }

    /** A datastructure created solely for optimising getPopMethodName(). */
    private static final Map<Type, String> PRIMITIVE_TYPE_POP_MTHDS;
    static {
        Map<Type, String> initialValue = new HashMap<Type, String>();
        initialValue.putAll(TYPE_ONE_POP_MTHDS);
        initialValue.putAll(TYPE_TWO_POP_MTHDS);
        PRIMITIVE_TYPE_POP_MTHDS = Collections.unmodifiableMap(initialValue);
    }

    /**
     * A {@link ThreadLocal} that provides each thread with a separate stack
     * instance.
     */
    @SuppressWarnings("rawtypes")
    private static ThreadLocal<Stack> localStack = new ThreadLocal<Stack>() {

        @Override
        protected Stack initialValue() {
            return new Stack();
        }
    };

    private ArgumentStack() {}

    /**
     * Checks whether the given type is primitive or not.
     *
     * @param type
     *            the type to be checked.
     * @return true if primitive and false otherwise.
     */
    public static boolean isPrimitive(Type type) {
        return (PRIMITIVE_TYPE_POP_MTHDS.containsKey(type));
    }

    /**
     * Determines whether the given type takes 2 slots when loaded on stack.
     *
     * @param type
     *            the type to be checked.
     * @return {@code true} if type takes 2 slots and {@code false} if 1.
     */
    public static boolean isComputationalType2(Type type) {
        return TYPE_TWO_POP_MTHDS.containsKey(type);
    }

    /**
     * Returns the appropriate pop method name for the given type.
     *
     * @param type the type
     * @return a {@link String} containing the correct pop method name.
     */
    public static String getPopMethodNameFor(Type type) {
        String methodName = PRIMITIVE_TYPE_POP_MTHDS.get(type);
        return (methodName != null) ? methodName : "pop";
    }

    @SuppressWarnings("unchecked")
    public static void push(Object obj) {
        localStack.get().push(obj);
    }

    @SuppressWarnings("unchecked")
    public static void push(boolean i) {
        localStack.get().push(i);
    }

    @SuppressWarnings("unchecked")
    public static void push(byte i) {
        localStack.get().push(Byte.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public static void push(char i) {
        localStack.get().push(Character.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public static void push(double i) {
        localStack.get().push(Double.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public static void push(float i) {
        localStack.get().push(Float.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public static void push(int i) {
        localStack.get().push(Integer.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public static void push(long i) {
        localStack.get().push(Long.valueOf(i));
    }

    @SuppressWarnings("unchecked")
    public static void push(short i) {
        localStack.get().push(Short.valueOf(i));
    }

    /**
     * Removes and returns the top stack element. This method is used to pop
     * elements of non-primitive types.
     *
     * @return the element as an Object.
     */
    public static Object pop() {
        return localStack.get().pop();
    }

    public static boolean popBoolean() {
        return ((Boolean) localStack.get().pop()).booleanValue();
    }

    public static byte popByte() {
        return ((Byte) localStack.get().pop()).byteValue();
    }

    public static char popChar() {
        return ((Character) localStack.get().pop()).charValue();
    }

    public static double popDouble() {
        return ((Double) localStack.get().pop()).doubleValue();
    }

    public static float popFloat() {
        return ((Float) localStack.get().pop()).floatValue();
    }

    public static int popInt() {
        return ((Integer) localStack.get().pop()).intValue();
    }

    public static long popLong() {
        return ((Long) localStack.get().pop()).longValue();
    }

    public static short popShort() {
        return ((Short) localStack.get().pop()).shortValue();
    }
}
