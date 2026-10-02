import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class HeapTest {
    @Test
    void testInsertAndExtractMin() {
        Heap<Integer> heap = new Heap<>();
        heap.insert(5);
        heap.insert(3);
        heap.insert(8);
        heap.insert(1);
        assertFalse(heap.isEmpty());
        assertEquals(1, heap.getTop());
        assertEquals(3, heap.getTop());
        assertEquals(5, heap.getTop());
        assertEquals(8, heap.getTop());
        assertTrue(heap.isEmpty());
    }

    @Test
    void testHeapFromArray() {
        Integer[] arr = { 4, 2, 7, 1 };
        Heap<Integer> heap = new Heap<>(arr);
        assertEquals(1, heap.getTop());
        assertEquals(2, heap.getTop());
        assertEquals(4, heap.getTop());
        assertEquals(7, heap.getTop());
    }

    @Test
    void testGrowsBeyondInitialCapacity() {
        Heap<Integer> heap = new Heap<>(4);
        for (int value = 1000; value >= 1; value--) {
            heap.insert(value);
        }
        assertEquals(1000, heap.size());
        assertTrue(heap.isValidHeap());
        for (int expected = 1; expected <= 1000; expected++) {
            assertEquals(expected, heap.extractMin());
        }
        assertTrue(heap.isEmpty());
    }
}

class HeapPropertyTest {
    @Property
    void testHeapMaintainsOrder(@ForAll("randomIntArrays") int[] arr) {
        Heap<Integer> heap = new Heap<>();
        for (int n : arr) {
            heap.insert(n);
        }
        int prev = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int current = heap.getTop();
            assertTrue(current >= prev);
            prev = current;
        }
    }

    @Provide
    Arbitrary<int[]> randomIntArrays() {
        return Arbitraries.integers().between(-1000, 1000).array(int[].class).ofMinSize(1).ofMaxSize(50);
    }

    @Property
    void testHeapFromArrayProperty(@ForAll("randomIntArrays") int[] arr) {
        Integer[] boxed = java.util.Arrays.stream(arr).boxed().toArray(Integer[]::new);
        Heap<Integer> heap = new Heap<>(boxed);
        int[] sorted = arr.clone();
        java.util.Arrays.sort(sorted);
        for (int value : sorted) {
            assertEquals(value, heap.getTop());
        }
        assertTrue(heap.isEmpty());
    }

    @Property
    void testGetFirstDoesNotRemove(@ForAll("randomIntArrays") int[] arr) {
        Integer[] boxed = java.util.Arrays.stream(arr).boxed().toArray(Integer[]::new);
        Heap<Integer> heap = new Heap<>(boxed);
        if (arr.length > 0) {
            int first = heap.getFirst();
            int top = heap.getTop();
            assertEquals(first, top);
        }
    }
}
