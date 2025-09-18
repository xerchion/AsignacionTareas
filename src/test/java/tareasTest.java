import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class tareasTest {
    @Test
    void testMainMethodExists() {
        // Test simple para verificar que la clase tareas existe y tiene el método main
        try {
            Class<?> tareasClass = tareas.class;
            assertNotNull(tareasClass, "La clase tareas debe existir");

            // Verificar que el método main existe
            java.lang.reflect.Method mainMethod = tareasClass.getMethod("main", String[].class);
            assertNotNull(mainMethod, "El método main debe existir");

            // Verificar que es estático
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                    "El método main debe ser estático");

            // Verificar que es público
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                    "El método main debe ser público");
        } catch (NoSuchMethodException e) {
            fail("El método main no fue encontrado: " + e.getMessage());
        }
    }

    @Test
    void testTareasClassExists() {
        // Test para verificar que la clase tareas existe y es instanciable
        Class<?> tareasClass = tareas.class;
        assertNotNull(tareasClass, "La clase tareas debe existir");
        assertTrue(tareasClass.getName().equals("tareas"), "La clase debe llamarse 'tareas'");
    }
}
