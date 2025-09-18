import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class MatrixTest {
    @Test
    void testConstructor() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        Matrix m = new Matrix(2, 2, datos);
        assertNotNull(m);
        assertEquals(1, m.get(1, 1));
        assertEquals(4, m.get(2, 2));
    }
}
