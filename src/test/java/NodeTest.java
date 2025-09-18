import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class NodeTest {
    @Test
    void testConstructor() {
        Node n = new Node(3);
        assertNotNull(n);
    }
}
