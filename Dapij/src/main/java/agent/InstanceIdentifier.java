package agent;

import com.google.common.collect.MapMaker;
import java.lang.reflect.Field;
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

    /** A long variable used to provide unique identifiers to objects. */
    private AtomicLong nextObjectID;

    /** A map storing IDs of objects whose classes could not be instrumented. */
    private ConcurrentMap<Object, Long> idMap;

    private InstanceIdentifier() {
        idMap = new MapMaker().weakKeys().makeMap();
        nextObjectID = new AtomicLong(1);
    }

    @Override
    public <T> long getId(T ref) {
        if (ref == null) {
         // TODO: IllegalArgumentException or NullPointerException instead?
            throw new RuntimeException("Null reference has no identifier.");
        }
        do {
            Field f = null;
            try {
                f = ref.getClass().getDeclaredField("__DAPIJ_ID");
                f.setAccessible(true);
            } catch (Exception e) {
                break; /* Proceed to using map if id field not injected. */
            }

            /* Handle objects with an injected identifier field. */
            synchronized (ref) {
                try {
                    long objId = f.getLong(ref);

                    /* If not previously set. */
                    if (objId == 0) {
                        objId = nextObjectID.getAndIncrement();
                        f.setLong(ref, objId);
                    }
                    return objId;
                } catch (Exception e) {
                    Settings.INSTANCE.println("Maybe looking for id of a null reference.");
                    throw new RuntimeException(e);
                }
            }
        } while (false);

        /* Handle objects without an injected identifier field. */
        synchronized (ref) {
            if (idMap.containsKey(ref)) {
                return idMap.get(ref).longValue();
            } else {
                long objId = nextObjectID.getAndIncrement();
                idMap.put(ref, new Long(objId)); /* Requires new Long obj. */
                return objId;
            }
        }
    }
}
