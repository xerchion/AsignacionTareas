import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NodeTest {
    @Test
    void testConstructor() {
        Node n = new Node(3);
        assertNotNull(n);
    }

    @Test
    void testOrderedByOptimisticEstimate() {
        Node cheap = new Node(3);
        cheap.setOptEst(10);
        Node expensive = new Node(3);
        expensive.setOptEst(20);

        assertTrue(cheap.compareTo(expensive) < 0);
        assertTrue(expensive.compareTo(cheap) > 0);
    }

    @Test
    void testTiesGoToTheDeepestNode() {
        Node shallow = new Node(3);
        shallow.setOptEst(10);
        shallow.setLastOrder(1);
        Node deep = new Node(3);
        deep.setOptEst(10);
        deep.setLastOrder(2);

        assertTrue(deep.compareTo(shallow) < 0);
        assertTrue(shallow.compareTo(deep) > 0);
        assertEquals(0, deep.compareTo(deep));
    }

    @Test
    void testCloneIsIndependent() {
        Node original = new Node(3);
        original.setAgent(1, 2);
        original.setAssigned(2, true);
        original.setLastOrder(1);
        original.setCost(5);

        Node copy = original.clone();
        copy.setAgent(2, 3);
        copy.setAssigned(3, true);
        copy.setCost(9);

        assertEquals(2, copy.getAgent(1));
        assertTrue(copy.getAssigned(2));
        assertEquals(0, original.getAgent(2));
        assertFalse(original.getAssigned(3));
        assertEquals(5, original.getCost());
    }
}
