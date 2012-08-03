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
    private int nextObjectID = 1;

    /** A map storing IDs of objects whose classes could not be modified. */
    private ConcurrentMap<Object, Integer> idMap;

    private InstIdentifier() {
        idMap = new MapMaker().weakKeys().makeMap();
    }

    private int nextId() {
        return nextObjectID++;
    }

    // TODO: what if null is passed as a ref
    // TODO: reducing synchronisation block size
    @Override
    public synchronized int getId(Object ref) {
        int objId = 0;
        try {
            Field f = ref.getClass().getField("__DAPIJ_ID");
            objId = f.getInt(ref); /* Must be 0 if not initialised. */
            if (objId == 0) {
                objId = nextId();
                f.setInt(ref, objId);
            }

            return objId;
        } catch (Exception e) {
            if (idMap.containsKey(ref)) {
                objId = idMap.get(ref).intValue();
            } else {
                objId = nextId();
                idMap.put(ref, Integer.valueOf(objId));
            }

            return objId;
        }
    }
}
