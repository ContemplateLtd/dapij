package agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import junit.framework.Assert;
import org.junit.Test;

import utils.Helpers;

public class ArgumentStackTest {

    /**
     * A {@link Callable}{@code <}{@link Boolean}{@code >} task that pushes and
     * pops elements into and from the {@link ArgumenStack} {@link ThreadLocal}
     * to test if for consistency and correct behaviour in a multithreaded
     * environment.
     *
     * @author Nikolay Pulev <N.Pulev@sms.ed.ac.uk>
     */
    @SuppressWarnings("unchecked")
    public static class StackUser implements Callable<Boolean> {

        private static final List<Class<?>> STACK_TYPES;
        static {
            List<Class<?>> initialValue = new ArrayList<Class<?>>();
            initialValue.add(int.class);
            initialValue.add(short.class);
            initialValue.add(long.class);
            initialValue.add(double.class);
            initialValue.add(char.class);
            initialValue.add(byte.class);
            initialValue.add(float.class);
            initialValue.add(boolean.class);
            initialValue.add(byte[][].class);                       /* Add an array. */
            initialValue.add(new ArrayList<String>().getClass());   /* Add a template object. */
            STACK_TYPES = Collections.unmodifiableList(initialValue);
        }

        private static final int NR_TYPES = STACK_TYPES.size();

        private static final List<Class<?>> STACK_TEST_ENTRIES;
        static {
            @SuppressWarnings("rawtypes")
            List initialValue = new ArrayList();
            initialValue.add((int) 1);
            initialValue.add((short) 2);
            initialValue.add((long) 3);
            initialValue.add((double) 4);
            initialValue.add((char) '5');
            initialValue.add((byte) 6);
            initialValue.add((float) 7.8);
            initialValue.add((boolean) true);
            initialValue.add(new byte[][] {new byte[]{1}, new byte[]{}, new byte[]{2, 3}});
            initialValue.add(new ArrayList<String>());
            STACK_TEST_ENTRIES = Collections.unmodifiableList(initialValue);
        }

        /**
         * A reference {@link Stack} against which the {@link ArgumentStack} is
         * being tested.
         */
        @SuppressWarnings("rawtypes")
        private Stack reference;

        /**
         * A {@link Stack} that holds class constants of {@code reference}'s
         * entries.
         */
        private Stack<Class<?>> referenceEntryTypes;

        /**
         * A random {@code long} providing a bit pattern to pseudo randomise
         * popping & pushing.
         */
        private long bitPattern;

        @SuppressWarnings("rawtypes")
        public StackUser(long bitPattern) {
            this.bitPattern = bitPattern;
            reference = new Stack();
            referenceEntryTypes = new Stack<Class<?>>();
        }

        private void pushElem(int index) {
            reference.push(STACK_TEST_ENTRIES.get(index));      /* Push entry on reference stack. */
            ArgumentStack.push(STACK_TEST_ENTRIES.get(index));  /* Push entry on tested stack. */
            referenceEntryTypes.push(STACK_TYPES.get(index));   /* Push type on type stack. */
        }

        /**
         * Pops elements both from the reference and tested stacks and checks
         * whether the elements are equal.
         *
         * @return {@code true} if the elements are the same and {@code false}
         *         otherwise.
         */
        private boolean popAndCheckIfEqual() {
            Class<?> poppedType = referenceEntryTypes.pop();

            return (poppedType == int.class && popInt() == ArgumentStack.popInt()
                    || poppedType == short.class && popShort() == ArgumentStack.popShort()
                    || poppedType == long.class && popLong() == ArgumentStack.popLong()
                    || poppedType == double.class && popDouble() == ArgumentStack.popDouble()
                    || poppedType == char.class && popChar() == ArgumentStack.popChar()
                    || poppedType == byte.class && popByte() == ArgumentStack.popByte()
                    || poppedType == float.class && popFloat() == ArgumentStack.popFloat()
                    || poppedType == boolean.class && popBoolean() == ArgumentStack.popBoolean()
                    || popObj().equals(ArgumentStack.pop()));
        }

        public boolean popBoolean() {
            return ((Boolean) reference.pop()).booleanValue();
        }

        public byte popByte() {
            return ((Byte) reference.pop()).byteValue();
        }

        public char popChar() {
            return ((Character) reference.pop()).charValue();
        }

        public double popDouble() {
            return ((Double) reference.pop()).doubleValue();
        }

        public float popFloat() {
            return ((Float) reference.pop()).floatValue();
        }

        public int popInt() {
            return ((Integer) reference.pop()).intValue();
        }

        public long popLong() {
            return ((Long) reference.pop()).longValue();
        }

        public short popShort() {
            return ((Short) reference.pop()).shortValue();
        }

        public Object popObj() {
            return reference.pop();
        }

        /** Given a random bit pattern (a 64-bit long), push on occurrences of 0 and pop on 1. */
        @Override
        public Boolean call() {
            int bitPatternSize = 64; /* Size of Long - 8 bytes * 8 bits = 64 bits. */
            for (int i = 0; i < bitPatternSize; i++) {

                /* Push random entry from STACK_ENTRIES on 0, and pop on 1 if stack not empty. */
                if (((bitPattern >>> i) & 0x0000000000000001) == 0) {
                    pushElem(i % NR_TYPES);
                } else if(!reference.empty()) {
                    if (!popAndCheckIfEqual()) {

                        return false; /* If inconsistency found, return false. */
                    }
                }
            }

            /* Pop remaining elements if any left. */
            while (!reference.empty()) {
                if (!popAndCheckIfEqual()) {

                    return false; /* If inconsistency found, return false. */
                }
            }

            return true;
        }
    }

    @Test
    public void concurrencyTest() {
        ExecutorService pool = Executors.newFixedThreadPool(4); /* Allow 4 threads. */
        ArrayList<Future<Boolean>> isCorrect = new ArrayList<Future<Boolean>>();

        /* Submit tasks by providing a random bit mask. */
        for (int i = 0; i < 1000; i++) {
            isCorrect.add(pool.submit(new StackUser(Helpers.randomLong())));
        }

        String testedClass = ArgumentStack.class.getName();

        /* Check stack consistency. */
        for (Future<Boolean> correct : isCorrect) {
            try {
                Assert.assertEquals(testedClass + " is concistent in "
                        + "multithreaded enviroment: ", true, correct.get().booleanValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
