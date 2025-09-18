import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GamePropertyTest {
    @Test
    void testGameRunWithValidData() {
        // Test básico con datos válidos conocidos
        int[][] datos = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
        GameData gd = new GameData(3, 3, datos);
        Game g = new Game(gd, false);
        String result = g.run();
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void testGameWithTraceEnabled() {
        // Test con traza habilitada
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        Game g = new Game(gd, true);
        g.run();
        String trace = g.getTrace();
        assertNotNull(trace);
        assertTrue(trace.contains("Traza"));
    }

    @Test
    void testGameConstructorValidation() {
        // Test de validación del constructor
        int[][] datos = { { 1, 2 }, { 3, 4 } };
        GameData gd = new GameData(2, 2, datos);
        Game g1 = new Game(gd, false);
        Game g2 = new Game(gd, true);
        assertNotNull(g1);
        assertNotNull(g2);
    }
}