import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConstantsTest {
    @Test
    void testConstantsExist() {
        assertEquals(1.5, Constants.VERSION_APP);
        assertNotNull(Constants.ERR);
    }
}
