package agent;

import com.google.common.collect.MapMaker;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import transform.Identifier;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public final class InstIdentifier implements Identifier {

    public static final InstIdentifier INSTANCE = new InstIdentifier();

    /** A simple class used for assigning unique identifiers to objects. */
    private AtomicLong nextObjectID;

    /** A map storing IDs of objects whose classes could not be modified. */
    private ConcurrentMap<Object, Long> idMap;

    private InstIdentifier() {
        idMap = new MapMaker().weakKeys().makeMap();
        nextObjectID = new AtomicLong(1);
    }

    /* TODO: reduce/remove sync block to reduce impact on concurrency. */
    @Override
    public long getId(Object ref) {
        if (ref == null) {
            throw new RuntimeException("Null reference has no identifier.");
        }
        long objId = 0;

        do {
            Field f = null;
            try {
                /*
                 * TODO: What if only super class has the field? Then This getField
                 * will not return a field.
                 */
                f = ref.getClass().getDeclaredField("__DAPIJ_ID");
                f.setAccessible(true);
            } catch (Exception e) {
                break;
            }

            synchronized (ref) {
                try {
                    objId = f.getLong(ref); /* Must be 0 if not initialised. */
                } catch (Exception e) {
                    Settings.INSTANCE.println("Maybe looking for id of a null reference.");
                    throw new RuntimeException(e);
                }
                // compare&set on filed (value to be set is get&increment)
                if (objId == 0) {
                    objId = nextObjectID.getAndIncrement();
                    try {
                        f.setLong(ref, objId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return objId;
        } while (false);

        synchronized (ref) {
            /* Handle objects with no identifier field. */
            if (idMap.containsKey(ref)) {
                objId = idMap.get(ref).longValue();
            } else {
                objId = nextObjectID.getAndIncrement();
                idMap.put(ref, new Long(objId)); /* Requires new Long obj. */
            }
        }

        return objId;
    }
}
