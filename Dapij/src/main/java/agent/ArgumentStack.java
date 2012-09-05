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

    /**
     * Type to function name map. Useful for obtaining correct method. Contains
     * functions for computational type 1 types (one-stack-slot types).
     */
    public static final Map<Type, String> ONE_SLOT_ARG_FUNC_MAP;
    static {
        Map<Type, String> initialValue = new HashMap<Type, String>();
        initialValue.put(Type.getType(Object.class), "popObj");
        initialValue.put(Type.BOOLEAN_TYPE, "popBoolean");
        initialValue.put(Type.BYTE_TYPE, "popByte");
        initialValue.put(Type.CHAR_TYPE, "popChar");
        initialValue.put(Type.FLOAT_TYPE, "popFloat");
        initialValue.put(Type.INT_TYPE, "popInt");
        initialValue.put(Type.SHORT_TYPE, "popShort");
        ONE_SLOT_ARG_FUNC_MAP = Collections.unmodifiableMap(initialValue);
    }

    /**
     * Type to function name map. Useful for obtaining correct method. Contains
     * functions for computational type 2 types (two-stack-slot types).
     */
    public static final Map<Type, String> TWO_SLOT_ARG_FUNC_MAP;
    static {
        Map<Type, String> initialValue = new HashMap<Type, String>();
        initialValue.put(Type.getType(Object.class), "popDouble");
        initialValue.put(Type.BOOLEAN_TYPE, "popLong");
        TWO_SLOT_ARG_FUNC_MAP = Collections.unmodifiableMap(initialValue);
    }

    @SuppressWarnings("rawtypes")
    private static ThreadLocal<Stack> localStack = new ThreadLocal<Stack>() {

        @Override
        protected Stack initialValue() {
            return new Stack();
        }
    };

    private ArgumentStack() {}

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

    public static Object popObj() {
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
