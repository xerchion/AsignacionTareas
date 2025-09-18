import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VectorBTest {
    @Test
    void testConstructor() {
        VectorB vb = new VectorB(3);
        assertNotNull(vb);
        vb.set(1, true);
        assertTrue(vb.get(1));
    }
}
