package agent;

import com.google.common.collect.MapMaker;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;
import transform.Identifier;

/**
 *
 * @author Marcin Szymczak <mpszymczak@gmail.com>
 */
public final class InstIdentifier implements Identifier {

    public static final InstIdentifier INSTANCE = new InstIdentifier();

    /** A simple class used for assigning unique identifiers to objects. */
    private long nextObjectID = 1;

    /** A map storing IDs of objects whose classes could not be modified. */
    private ConcurrentMap<Object, Long> idMap;

    private InstIdentifier() {
        idMap = new MapMaker().weakKeys().makeMap();
    }

    private long nextId() {
        return nextObjectID++;
    }

    /* TODO: reduce/remove sync block to reduce impact on concurrency. */
    @Override
    public synchronized long getId(Object ref) {
        if (ref == null) {
            throw new RuntimeException("Null reference has no identifier.");
        }
        long objId = 0;
        try {

            /*
             * TODO: What if only super class has the field? Then This getField
             * will not return a field.
             */
            Field f = ref.getClass().getDeclaredField("__DAPIJ_ID");
            objId = f.getLong(ref); /* Must be 0 if not initialised. */
            if (objId == 0) {
                objId = nextId();
                f.setLong(ref, objId);
            }

            return objId;
        } catch (Exception e) {
            if (idMap.containsKey(ref)) {
                objId = idMap.get(ref).longValue();
            } else {
                objId = nextId();
                idMap.put(ref, new Long(objId)); /* Requires new Long obj. */
            }

            return objId;
        }
    }
}
