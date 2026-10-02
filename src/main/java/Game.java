
/**
 * Motor del algoritmo de asignación de tareas mediante ramificación y poda.
 *
 * Resuelve el problema de asignación: hay n agentes y n tareas, cada agente
 * tarda un tiempo distinto en cada tarea, y hay que asignar una tarea a cada
 * agente de forma que el coste total sea mínimo.
 *
 * Esquema del algoritmo:
 * - Cada nodo es una solución parcial: las k primeras tareas ya tienen agente.
 * - Los nodos vivos se guardan en un montículo de mínimos ordenado por su
 * estimación optimista, de modo que siempre se expande el más prometedor.
 * - La estimación optimista (cota inferior) de un nodo es su coste más, para
 * cada tarea pendiente, el menor tiempo de esa tarea entre todos los agentes.
 * - La estimación pesimista (cota superior) es su coste más, para cada tarea
 * pendiente, el mayor tiempo de esa tarea. Cualquier forma de completar el
 * nodo cuesta como mucho eso, así que sirve como cota de poda antes de tener
 * una solución completa.
 * - Se poda todo nodo cuya estimación optimista supere la cota.
 *
 * El resultado es siempre óptimo. El coste en tiempo es factorial en el peor
 * caso; la poda lo reduce mucho en la práctica.
 *
 * @author Pasteleros
 * @version 2.1
 * @since 1.0
 */
public final class Game {

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Número de agentes y de tareas (iguales en este problema) */
    private final int problemSize;

    /** Datos del problema (matriz de costes, tareas y agentes) */
    private final GameData gameData;

    /** Indica si se debe generar la traza del algoritmo */
    private final boolean traceEnabled;

    /** Traza acumulada durante la ejecución */
    private StringBuilder trace;

    /** Resultado final en forma de texto */
    private String result;

    /** Mejor asignación encontrada: en la posición i, el agente de la tarea i */
    private Vector bestAssignment;

    /** Coste de la mejor asignación encontrada */
    private int bestCost;

    /** Indica si run() ya ha dejado una solución disponible */
    private boolean solved;

    // ========== ESTADÍSTICAS DEL ALGORITMO ==========

    /** Nodos creados (incluida la raíz) */
    private int nodesGenerated;

    /** Nodos extraídos del montículo y expandidos */
    private int nodesExplored;

    /** Nodos descartados por superar la cota */
    private int nodesPruned;

    /** Veces que una solución completa ha mejorado a la anterior */
    private int solutionsFound;

    // ========== CONSTRUCTOR ==========

    /**
     * Prepara el algoritmo con los datos del problema.
     *
     * @param gameData    Datos del problema: matriz de costes, tareas y agentes
     * @param enableTrace Si se debe generar la traza del proceso
     * @throws IllegalArgumentException si gameData es null, no es un problema
     *                                  cuadrado o sus costes son tan grandes
     *                                  que su suma no cabe en un entero
     */
    public Game(GameData gameData, boolean enableTrace) {
        if (gameData == null) {
            throw new IllegalArgumentException("Los datos del juego no pueden ser null");
        }

        this.problemSize = gameData.getAgentsNumber();
        this.gameData = gameData;
        this.traceEnabled = enableTrace;
        this.result = "";

        validateGameData();
        resetState();
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Ejecuta el algoritmo y devuelve la asignación óptima en forma de texto.
     *
     * @return Resultado: una línea "agente tarea" por cada tarea y el coste total
     */
    public String run() {
        resetState();

        solveBranchAndBound(this.gameData.getTimes(), this.gameData.getTasks());
        this.solved = true;

        this.result = "\nSolución: \n" + composeResult();
        return this.result;
    }

    /**
     * Devuelve la traza acumulada durante el algoritmo.
     *
     * @return La traza, o cadena vacía si no está habilitada
     */
    public String getTrace() {
        if (this.traceEnabled && this.trace != null) {
            return this.trace.toString();
        }
        return "";
    }

    /**
     * Devuelve el resultado del último {@link #run()}.
     *
     * @return El resultado, o cadena vacía si aún no se ha ejecutado
     */
    public String getResult() {
        return this.result;
    }

    /**
     * Devuelve el coste de la asignación óptima encontrada por {@link #run()}.
     *
     * @return Coste total mínimo
     * @throws IllegalStateException si aún no se ha ejecutado el algoritmo
     */
    public int getOptimalCost() {
        ensureSolved();
        return this.bestCost;
    }

    /**
     * Devuelve la asignación óptima encontrada por {@link #run()}.
     *
     * @return Copia del vector de asignación: en la posición i (base 1) está el
     *         agente que realiza la tarea i
     * @throws IllegalStateException si aún no se ha ejecutado el algoritmo
     */
    public Vector getAssignment() {
        ensureSolved();
        return this.bestAssignment.clone();
    }

    // ========== ALGORITMO DE RAMIFICACIÓN Y PODA ==========

    /**
     * Recorre el árbol de soluciones parciales en orden de menor estimación
     * optimista, podando las ramas que no pueden mejorar la cota.
     *
     * Deja la mejor asignación en {@link #bestAssignment} y su coste en
     * {@link #bestCost}.
     *
     * @param costMatrix Matriz de costes [agente][tarea]
     * @param tasks      Vector de tareas, en el orden en que se asignan
     */
    private void solveBranchAndBound(Matrix costMatrix, Vector tasks) {
        Heap<Node> liveNodes = new Heap<>();

        // Raíz: ninguna tarea asignada
        Node root = new Node(this.problemSize);
        root.setOptEst(optimisticEstimate(costMatrix, tasks, 0, 0));
        liveNodes.insert(root);
        this.nodesGenerated++;

        // Cota inicial: lo que costaría la peor asignación imaginable
        int bound = pessimisticEstimate(costMatrix, tasks, 0, 0);
        addTraceEntry("Nodo raíz", root, bound);

        while (!liveNodes.isEmpty()) {
            Node current = liveNodes.extractMin();

            // El montículo entrega los nodos de menor a mayor estimación: si
            // este ya no puede mejorar lo encontrado, ninguno de los que quedan
            // puede.
            if (current.getOptEst() > bound || current.getOptEst() >= this.bestCost) {
                this.nodesPruned += 1 + liveNodes.size();
                addTraceEntry("Nodo podado al extraerlo; fin de la búsqueda", current, bound);
                break;
            }

            this.nodesExplored++;
            int taskOrder = current.getLastOrder() + 1;

            // Un hijo por cada agente que sigue libre
            for (int agent = 1; agent <= this.problemSize; agent++) {
                if (current.getAssigned(agent)) {
                    continue;
                }

                Node child = current.clone();
                child.setLastOrder(taskOrder);
                child.setAgent(taskOrder, agent);
                child.setAssigned(agent, true);
                child.setCost(current.getCost() + costMatrix.get(agent, tasks.get(taskOrder)));
                this.nodesGenerated++;

                if (taskOrder == this.problemSize) {
                    // Solución completa
                    child.setOptEst(child.getCost());
                    if (child.getCost() < this.bestCost) {
                        this.bestCost = child.getCost();
                        this.bestAssignment.copy(child.getAgents());
                        this.solutionsFound++;
                        bound = Math.min(bound, this.bestCost);
                        addTraceEntry("Solución que mejora la anterior", child, bound);
                    } else {
                        this.nodesPruned++;
                        addTraceEntry("Solución descartada", child, bound);
                    }
                } else {
                    // Solución parcial
                    child.setOptEst(optimisticEstimate(costMatrix, tasks, taskOrder, child.getCost()));

                    if (child.getOptEst() <= bound && child.getOptEst() < this.bestCost) {
                        liveNodes.insert(child);

                        // Completar este nodo cuesta como mucho su estimación
                        // pesimista: la cota puede bajar hasta ahí
                        bound = Math.min(bound,
                                pessimisticEstimate(costMatrix, tasks, taskOrder, child.getCost()));
                        addTraceEntry("Nodo añadido", child, bound);
                    } else {
                        this.nodesPruned++;
                        addTraceEntry("Nodo podado", child, bound);
                    }
                }
            }
        }

        addTraceSummary();
    }

    // ========== ESTIMACIONES ==========

    /**
     * Estimación optimista (cota inferior) del coste total de un nodo.
     *
     * Suma al coste acumulado, para cada tarea pendiente, el menor tiempo de esa
     * tarea entre todos los agentes. Nunca sobreestima el coste real, porque
     * ningún agente puede hacer la tarea en menos tiempo.
     *
     * @param costMatrix  Matriz de costes [agente][tarea]
     * @param tasks       Vector de tareas
     * @param lastOrder   Última tarea ya asignada
     * @param currentCost Coste acumulado
     * @return Estimación optimista del coste total
     */
    private int optimisticEstimate(Matrix costMatrix, Vector tasks, int lastOrder, int currentCost) {
        int estimate = currentCost;

        for (int taskIndex = lastOrder + 1; taskIndex <= this.problemSize; taskIndex++) {
            int minCostForTask = Integer.MAX_VALUE;
            for (int agent = 1; agent <= this.problemSize; agent++) {
                minCostForTask = Math.min(minCostForTask, costMatrix.get(agent, tasks.get(taskIndex)));
            }
            estimate += minCostForTask;
        }

        return estimate;
    }

    /**
     * Estimación pesimista (cota superior) del coste total de un nodo.
     *
     * Suma al coste acumulado, para cada tarea pendiente, el mayor tiempo de esa
     * tarea entre todos los agentes. Cualquier forma de completar el nodo cuesta
     * como mucho este valor.
     *
     * @param costMatrix  Matriz de costes [agente][tarea]
     * @param tasks       Vector de tareas
     * @param lastOrder   Última tarea ya asignada
     * @param currentCost Coste acumulado
     * @return Estimación pesimista del coste total
     */
    private int pessimisticEstimate(Matrix costMatrix, Vector tasks, int lastOrder, int currentCost) {
        int estimate = currentCost;

        for (int taskIndex = lastOrder + 1; taskIndex <= this.problemSize; taskIndex++) {
            int maxCostForTask = Integer.MIN_VALUE;
            for (int agent = 1; agent <= this.problemSize; agent++) {
                maxCostForTask = Math.max(maxCostForTask, costMatrix.get(agent, tasks.get(taskIndex)));
            }
            estimate += maxCostForTask;
        }

        return estimate;
    }

    // ========== RESULTADO Y TRAZA ==========

    /**
     * Compone el texto del resultado: una línea "agente tarea" por cada tarea y,
     * al final, el coste total.
     *
     * @return Resultado formateado
     */
    private String composeResult() {
        Vector tasks = this.gameData.getTasks();
        StringBuilder text = new StringBuilder("\nResultado Óptimo \n (agente/tarea)\n");

        for (int i = 1; i <= this.problemSize; i++) {
            text.append(this.bestAssignment.get(i)).append(" ").append(tasks.get(i)).append("\n");
        }
        text.append("Coste total: ").append(this.bestCost).append("\n");

        return text.toString();
    }

    /**
     * Añade una línea a la traza, si está habilitada.
     *
     * @param message Qué ha ocurrido con el nodo
     * @param node    Nodo afectado
     * @param bound   Cota vigente tras el suceso
     */
    private void addTraceEntry(String message, Node node, int bound) {
        if (!this.traceEnabled) {
            return;
        }

        this.trace.append("\n").append(message).append(": agentes [");
        for (int i = 1; i <= this.problemSize; i++) {
            if (i > 1) {
                this.trace.append(" ");
            }
            if (i <= node.getLastOrder()) {
                this.trace.append(node.getAgent(i));
            } else {
                this.trace.append("-");
            }
        }
        this.trace.append("], coste ").append(node.getCost())
                .append(", estimación optimista ").append(node.getOptEst())
                .append(", cota ").append(bound);
    }

    /**
     * Añade a la traza el resumen de la búsqueda, si está habilitada.
     */
    private void addTraceSummary() {
        if (!this.traceEnabled) {
            return;
        }

        this.trace.append("\n\nResumen de la búsqueda:")
                .append("\n  Nodos generados: ").append(this.nodesGenerated)
                .append("\n  Nodos expandidos: ").append(this.nodesExplored)
                .append("\n  Nodos podados: ").append(this.nodesPruned)
                .append("\n  Soluciones que mejoraron la anterior: ").append(this.solutionsFound)
                .append("\n  Coste óptimo: ").append(this.bestCost)
                .append("\n");
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Deja el algoritmo listo para una nueva ejecución.
     */
    private void resetState() {
        this.bestAssignment = new Vector(this.problemSize);
        this.bestCost = Integer.MAX_VALUE;
        this.solved = false;
        this.nodesGenerated = 0;
        this.nodesExplored = 0;
        this.nodesPruned = 0;
        this.solutionsFound = 0;
        this.trace = this.traceEnabled ? new StringBuilder(Constants.TRACE_HEADER) : null;
    }

    /**
     * Comprueba que el algoritmo ya se ha ejecutado.
     *
     * @throws IllegalStateException si aún no hay solución
     */
    private void ensureSolved() {
        if (!this.solved) {
            throw new IllegalStateException("El algoritmo aún no se ha ejecutado. Llame a run() primero.");
        }
    }

    /**
     * Valida la consistencia de los datos del problema.
     *
     * @throws IllegalArgumentException si los datos son inconsistentes
     */
    private void validateGameData() {
        if (this.gameData.getAgentsNumber() != this.gameData.getTasksNumber()) {
            throw new IllegalArgumentException(
                    "El número de agentes debe ser igual al número de tareas");
        }

        if (this.problemSize <= 0) {
            throw new IllegalArgumentException(
                    "El tamaño del problema debe ser positivo");
        }

        if (this.gameData.getTimes() == null) {
            throw new IllegalArgumentException(
                    "La matriz de costes no puede ser null");
        }

        if (this.gameData.getTasks() == null) {
            throw new IllegalArgumentException(
                    "El vector de tareas no puede ser null");
        }

        // La cota inicial suma el mayor tiempo de cada tarea: debe caber en un int
        Matrix costMatrix = this.gameData.getTimes();
        long worstCase = 0;
        for (int task = 1; task <= this.problemSize; task++) {
            int maxCostForTask = 0;
            for (int agent = 1; agent <= this.problemSize; agent++) {
                maxCostForTask = Math.max(maxCostForTask, costMatrix.get(agent, task));
            }
            worstCase += maxCostForTask;
        }
        if (worstCase >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Los costes son demasiado grandes: su suma no cabe en un entero");
        }
    }
}
