import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class GameDataTest {
    @Test
    void testConstructorAndGet() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        assertNotNull(gd);
        assertEquals(1, gd.get(1, 1));
        assertEquals(2, gd.get(1, 2));
        assertEquals(3, gd.get(2, 1));
        assertEquals(4, gd.get(2, 2));
    }

    @Test
    void testAgentsAndTasksVectors() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        assertNotNull(gd);
        assertNotNull(gd.getAgents());
        assertNotNull(gd.getTasks());
    }
}

class GameDataPropertyTest {
    @Property
    void testGameDataMatrixAccess(@ForAll("smallMatrices") int[][] datos) {
        int n = datos.length;
        if (n == 0)
            return;
        int m = datos[0].length;
        if (m == 0)
            return;

        GameData gd = new GameData(n, m, datos);
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                assertEquals(datos[i - 1][j - 1], gd.get(i, j));
            }
        }
        assertNotNull(gd.getAgents());
        assertNotNull(gd.getTasks());
    }

    @Provide
    Arbitrary<int[][]> smallMatrices() {
        return Arbitraries.integers().between(1, 3)
                .flatMap(size -> Arbitraries.integers().between(1, 50)
                        .array(int[].class).ofSize(size)
                        .array(int[][].class).ofSize(size));
    }
}
