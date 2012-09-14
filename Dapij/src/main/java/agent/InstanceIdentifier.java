package agent;

import com.google.common.collect.MapMaker;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import transform.Identifier;

/**
 * A singleton class that provides unique object identification using a hybrid
 * approach. Objects are assigned a unique long identifier stored as a field if
 * they are instrumented or in a concurrent map if instrumentation was not
 * possible.
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public final class InstanceIdentifier implements Identifier {

    public static final InstanceIdentifier INSTANCE = new InstanceIdentifier();
    public static final String ID_NAME = "__DAPIJ_ID";

    /** A void identifier value. Initilised to {@cide (long) 0}. */
    public static final long NO_ID = 0;

    /* For atomic access to the injected ID_NAME field in instances of instrumented classes. */
    private static final Object UNSAFE;             /* An instance of sun.misc.Unsafe */
    private static final Method COMPARE_AND_SET;    /* Unsafe.compareAndSwapLong() */
    private static final Method GET_FIELD_OFFSET;   /* Unsafe.objectFieldOffset() */
    static {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = unsafeField.get(null); /* Initialised as sun.misc.Unsafe already loaded. */
            COMPARE_AND_SET = unsafeClass.getMethod("compareAndSwapLong",
                    Object.class, long.class, long.class, long.class);
            GET_FIELD_OFFSET = unsafeClass.getMethod("objectFieldOffset", Field.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** A {@link AtomicLong} variable used to provide unique identifiers to objects. */
    private AtomicLong nextObjectID;

    /** A map storing IDs of objects whose classes could not be instrumented. */
    private ConcurrentMap<Object, Long> idMap;

    private InstanceIdentifier() {
        idMap = new MapMaker().weakKeys().makeMap();
        nextObjectID = new AtomicLong(1);
    }

    /**
     * Gets (and sets if not set) the unique identifier of the object referenced
     * by ref.
     *
     * @param ref
     *            the reference for which an id is returned.
     * @return a {@code long} representing the identifier or
     *         {@link InstanceIdentifier}{@code .NO_ID} if ref {@code == null}.
     */
    @Override
    public <T> long getId(T ref) {
        if (ref == null) {

            return NO_ID;
        }

        Field f = null;
            try {
                f = ref.getClass().getDeclaredField(ID_NAME);
                f.setAccessible(true);
            } catch (NoSuchFieldException e) {} /* Ignore. */

        /* Handle objects with no identifier field. */
        if (f == null) {
            Long objId = idMap.get(ref);
            if (objId == null) {
                Long newId = new Long(nextObjectID.getAndIncrement()); /* A new object needed. */
                objId = idMap.putIfAbsent(ref, newId);
                if (objId == null) {
                   objId = newId;
                }
            }

            return objId.longValue();
        }

        /* Handle objects with injected identifier field. */
        long objId = NO_ID;
        try {
            objId = f.getLong(ref);
        } catch (IllegalAccessException e) {} /* Ignore. */
        if (objId == NO_ID) {
            long newId = nextObjectID.getAndIncrement();
            try {
                long idFieldOffset = (Long) GET_FIELD_OFFSET.invoke(UNSAFE, f); /* Get offset. */

                /* Atomic compare & set -> if (id == NO_ID) { id = newId; }. */
                boolean hasUpdateOccured = (Boolean) COMPARE_AND_SET.invoke(UNSAFE, ref,
                        idFieldOffset, NO_ID, newId);

                /* If no update, read again to get possible updates since last getLong() above. */
                objId = (hasUpdateOccured ? newId : f.getLong(ref));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return objId;
    }
}
