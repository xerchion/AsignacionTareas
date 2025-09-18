import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameTest {
    @Test
    void testConstructor() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        Game g = new Game(gd, true);
        assertNotNull(g);
    }

    @Test
    void testRun() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        Game g = new Game(gd, false);
        String result = g.run();
        assertNotNull(result);
        assertTrue(result.contains("Resultado"));
    }

    @Test
    void testGetTrace() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        Game g = new Game(gd, true);
        g.run();
        String trace = g.getTrace();
        assertNotNull(trace);
    }

    @Test
    void testGetResult() {
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        Game g = new Game(gd, false);
        g.run();
        String result = g.getResult();
        assertNotNull(result);
    }
}
