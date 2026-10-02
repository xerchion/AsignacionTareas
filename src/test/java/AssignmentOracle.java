/**
 * Utilidades de prueba para comprobar las asignaciones que devuelve
 * {@link Game} sin depender del propio algoritmo.
 */
final class AssignmentOracle {

    private AssignmentOracle() {
    }

    /**
     * Calcula el coste mínimo probando todas las asignaciones posibles.
     * Solo es viable para problemas pequeños.
     *
     * @param costs Matriz de costes [agente][tarea]
     * @return Coste de la mejor asignación
     */
    static int bruteForceCost(int[][] costs) {
        return bestFrom(costs, new boolean[costs.length], 0);
    }

    private static int bestFrom(int[][] costs, boolean[] usedAgents, int task) {
        if (task == costs.length) {
            return 0;
        }
        int best = Integer.MAX_VALUE;
        for (int agent = 0; agent < costs.length; agent++) {
            if (!usedAgents[agent]) {
                usedAgents[agent] = true;
                best = Math.min(best, costs[agent][task] + bestFrom(costs, usedAgents, task + 1));
                usedAgents[agent] = false;
            }
        }
        return best;
    }

    /**
     * Comprueba que la asignación usa cada agente exactamente una vez y
     * devuelve su coste.
     *
     * @param costs      Matriz de costes [agente][tarea]
     * @param assignment En la posición i (base 1), el agente de la tarea i
     * @return Coste total de la asignación
     * @throws AssertionError si la asignación no es una permutación de los
     *                        agentes
     */
    static int costOf(int[][] costs, Vector assignment) {
        int size = costs.length;
        if (assignment.size() != size) {
            throw new AssertionError("La asignación tiene " + assignment.size() + " tareas, se esperaban " + size);
        }

        boolean[] usedAgents = new boolean[size + 1];
        int total = 0;
        for (int task = 1; task <= size; task++) {
            int agent = assignment.get(task);
            if (agent < 1 || agent > size) {
                throw new AssertionError("Agente fuera de rango en la tarea " + task + ": " + agent);
            }
            if (usedAgents[agent]) {
                throw new AssertionError("El agente " + agent + " está asignado a más de una tarea");
            }
            usedAgents[agent] = true;
            total += costs[agent - 1][task - 1];
        }
        return total;
    }
}
