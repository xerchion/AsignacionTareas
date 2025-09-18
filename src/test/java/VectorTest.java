import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class VectorTest {
    @Test
    void testConstructor() {
        Vector v = new Vector(3);
        assertNotNull(v);
        v.set(1, 10);
        assertEquals(10, v.get(1));
    }
}
