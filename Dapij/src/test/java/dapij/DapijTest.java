package dapij;

import java.lang.reflect.Constructor;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

public class DapijTest {

    @Test
    public void constructorIsInstrumented() throws Exception {
        ClassLoader cl = new TestClassLoader();
        Class<?> clazz = cl.loadClass(ObjectInstance.class.getName());
        
        Map<Object, InstanceCreationStats> m = getCreationMap(cl);
        
        /* Creat an object  & test wheter recorded */
        Integer mapEntry = (Integer) clazz.getMethod("create").invoke(
                clazz.newInstance());
        
        assertEquals("Creation recorded: ", true, m.containsKey(mapEntry));
    }

    private Map<Object, InstanceCreationStats> getCreationMap(ClassLoader cl) {
        try {
            Class<?> clazz = cl.loadClass(
                    InstanceCreationTracker.class.getName());
            Field field = clazz.getField("INSTANCE");
            field.setAccessible(true);
            Object instance = field.get(null);
            Field mapField = instance.getClass().
                    getDeclaredField("instanceMap");
            mapField.setAccessible(true);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Map<Object, InstanceCreationStats> map = (Map) mapField.get(
                    instance);
            
            return map;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
   public static class ObjectInstance {
        public ObjectInstance() {}
        
        public Integer create() {
            return new Integer(5);
        }
    }
}
