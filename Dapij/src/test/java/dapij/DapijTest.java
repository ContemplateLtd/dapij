package dapij;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

public class DapijTest {

    @Test
    public void constructorIsInstrumented() throws Exception {
        ClassLoader cl = new TestClassLoader();
        Class<?> clazz = cl.loadClass(ObjectCreator.class.getName());
        clazz.getMethod("create").invoke(null);
        Map<Object, InstanceCreationStats> m = getCreationMap(cl);
        assertEquals(1, m.size());
    }

    private Map<Object, InstanceCreationStats> getCreationMap(ClassLoader cl) {
        try {
            Class<?> clazz = cl.loadClass(InstanceCreationTracker.class.getName());
            Field field = clazz.getField("INSTANCE");
            field.setAccessible(true);
            Object instance = field.get(null);
            Field mapField = instance.getClass().getDeclaredField("instanceMap");
            mapField.setAccessible(true);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Map<Object, InstanceCreationStats> map = (Map) mapField.get(instance);
            return map;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class ObjectCreator {
        public static ObjectInstance create() {
            return new ObjectInstance();
        }
    }

    static class ObjectInstance {

    }

}
