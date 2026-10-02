import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

/**
 * Comprueba que el algoritmo devuelve de verdad la asignación de coste mínimo.
 */
class GameOptimalityTest {

    private static Game solve(int[][] costs) {
        Game game = new Game(new GameData(costs.length, costs.length, costs), false);
        game.run();
        return game;
    }

    @Test
    void testExample4x4() {
        // ejemplos/ejemplo-4x4.txt: la diagonal cuesta 73 y el óptimo es 61
        int[][] costs = {
                { 11, 12, 18, 40 },
                { 14, 15, 13, 22 },
                { 11, 17, 19, 23 },
                { 17, 14, 20, 28 } };
        Game game = solve(costs);

        assertEquals(61, game.getOptimalCost());
        assertEquals(61, AssignmentOracle.costOf(costs, game.getAssignment()));
    }

    @Test
    void testExample3x3() {
        int[][] costs = {
                { 10, 15, 20 },
                { 12, 10, 18 },
                { 15, 18, 12 } };
        Game game = solve(costs);

        assertEquals(32, game.getOptimalCost());
        assertEquals(32, AssignmentOracle.costOf(costs, game.getAssignment()));
    }

    @Test
    void testOptimumOffTheDiagonal() {
        // La única asignación de coste 3 es la antidiagonal
        int[][] costs = {
                { 9, 9, 1 },
                { 9, 1, 9 },
                { 1, 9, 9 } };
        Game game = solve(costs);
        Vector assignment = game.getAssignment();

        assertEquals(3, game.getOptimalCost());
        assertEquals(3, assignment.get(1));
        assertEquals(2, assignment.get(2));
        assertEquals(1, assignment.get(3));
    }

    @Test
    void testSingleAgent() {
        int[][] costs = { { 7 } };
        Game game = solve(costs);

        assertEquals(7, game.getOptimalCost());
        assertEquals(1, game.getAssignment().get(1));
    }

    @Test
    void testAllCostsEqual() {
        // Con todos los costes iguales todas las estimaciones empatan: no debe
        // dispararse el número de nodos ni desbordarse el montículo
        int size = 12;
        int[][] costs = new int[size][size];
        for (int[] row : costs) {
            java.util.Arrays.fill(row, 5);
        }
        Game game = solve(costs);

        assertEquals(5 * size, game.getOptimalCost());
        assertEquals(5 * size, AssignmentOracle.costOf(costs, game.getAssignment()));
    }

    @Test
    void testZeroCosts() {
        int[][] costs = new int[4][4];
        Game game = solve(costs);

        assertEquals(0, game.getOptimalCost());
        assertEquals(0, AssignmentOracle.costOf(costs, game.getAssignment()));
    }

    @Test
    void testResultTextMatchesAssignment() {
        int[][] costs = {
                { 11, 12, 18, 40 },
                { 14, 15, 13, 22 },
                { 11, 17, 19, 23 },
                { 17, 14, 20, 28 } };
        Game game = new Game(new GameData(4, 4, costs), false);
        String result = game.run();
        Vector assignment = game.getAssignment();

        StringBuilder expectedLines = new StringBuilder();
        for (int task = 1; task <= 4; task++) {
            expectedLines.append(assignment.get(task)).append(" ").append(task).append("\n");
        }
        assertTrue(result.contains(expectedLines.toString()), result);
        assertTrue(result.contains("Coste total: 61"), result);
        assertEquals(result, game.getResult());
    }

    @Test
    void testTraceReportsOptimalCost() {
        int[][] costs = {
                { 10, 15, 20 },
                { 12, 10, 18 },
                { 15, 18, 12 } };
        Game game = new Game(new GameData(3, 3, costs), true);
        game.run();
        String trace = game.getTrace();

        assertTrue(trace.contains("Nodo raíz"), trace);
        assertTrue(trace.contains("Coste óptimo: 32"), trace);
    }

    @Test
    void testRunTwiceGivesSameResult() {
        int[][] costs = {
                { 4, 2, 8 },
                { 4, 3, 7 },
                { 3, 1, 6 } };
        Game game = new Game(new GameData(3, 3, costs), true);
        String first = game.run();
        String firstTrace = game.getTrace();
        String second = game.run();

        assertEquals(first, second);
        assertEquals(firstTrace, game.getTrace());
    }

    @Test
    void testRunDoesNotModifyGameData() {
        int[][] costs = {
                { 9, 9, 1 },
                { 9, 1, 9 },
                { 1, 9, 9 } };
        GameData data = new GameData(3, 3, costs);
        new Game(data, false).run();

        for (int i = 1; i <= 3; i++) {
            assertEquals(i, data.getAgents().get(i));
            assertEquals(i, data.getTasks().get(i));
        }
    }

    @Test
    void testSolutionNotAvailableBeforeRun() {
        int[][] costs = { { 1, 2 }, { 3, 4 } };
        Game game = new Game(new GameData(2, 2, costs), false);

        assertThrows(IllegalStateException.class, game::getOptimalCost);
        assertThrows(IllegalStateException.class, game::getAssignment);
    }

    @Test
    void testRejectsNonSquareProblem() {
        int[][] costs = { { 1, 2, 3 }, { 4, 5, 6 } };
        GameData data = new GameData(2, 3, costs);

        assertThrows(IllegalArgumentException.class, () -> new Game(data, false));
    }

    @Test
    void testRejectsCostsThatOverflow() {
        int[][] costs = {
                { Integer.MAX_VALUE, 1 },
                { 1, Integer.MAX_VALUE } };
        GameData data = new GameData(2, 2, costs);

        assertThrows(IllegalArgumentException.class, () -> new Game(data, false));
    }
}

class GameOptimalityPropertyTest {

    @Property(tries = 500)
    void testCostMatchesBruteForce(@ForAll("squareMatrices") int[][] costs) {
        Game game = new Game(new GameData(costs.length, costs.length, costs), false);
        game.run();

        int expected = AssignmentOracle.bruteForceCost(costs);
        assertEquals(expected, game.getOptimalCost());
        assertEquals(expected, AssignmentOracle.costOf(costs, game.getAssignment()));
    }

    @Property(tries = 500)
    void testCostMatchesBruteForceWithManyTies(@ForAll("matricesWithTies") int[][] costs) {
        Game game = new Game(new GameData(costs.length, costs.length, costs), true);
        game.run();

        int expected = AssignmentOracle.bruteForceCost(costs);
        assertEquals(expected, game.getOptimalCost());
        assertEquals(expected, AssignmentOracle.costOf(costs, game.getAssignment()));
    }

    @Provide
    Arbitrary<int[][]> squareMatrices() {
        return squareMatricesWithCostsUpTo(100);
    }

    @Provide
    Arbitrary<int[][]> matricesWithTies() {
        return squareMatricesWithCostsUpTo(2);
    }

    private Arbitrary<int[][]> squareMatricesWithCostsUpTo(int maxCost) {
        return Arbitraries.integers().between(1, 7)
                .flatMap(size -> Arbitraries.integers().between(0, maxCost)
                        .array(int[].class).ofSize(size)
                        .array(int[][].class).ofSize(size));
    }
}
