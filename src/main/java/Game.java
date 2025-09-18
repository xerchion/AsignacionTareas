
/**
 * Motor principal del algoritmo de asignación de tareas usando ramificación y
 * poda (Branch and Bound).
 *
 * Esta clase implementa la solución completa al problema de asignación de
 * tareas a agentes,
 * donde cada agente debe ser asignado a exactamente una tarea y viceversa,
 * minimizando el
 * coste total de las asignaciones.
 *
 * El algoritmo utiliza:
 * - Ramificación y poda para exploración eficiente del espacio de búsqueda
 * - Montículo de mínimos para gestionar nodos por prioridad (mejor estimación
 * primero)
 * - Estimaciones optimistas y pesimistas para podar ramas no prometedoras
 * - Generación opcional de traza detallada para análisis del proceso
 *
 * Complejidad temporal: O(n! * n²) en el peor caso, pero significativamente
 * mejorada
 * por la poda. En la práctica, mucho mejor que fuerza bruta.
 *
 * Características principales:
 * - Garantía de encontrar la solución óptima
 * - Poda inteligente basada en cotas
 * - Traza detallada opcional para propósitos educativos
 * - Gestión eficiente de memoria mediante reutilización de nodos
 *
 * @author Pasteleros
 * @version 2.0
 * @since 1.0
 */
public final class Game {

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Número de agentes y tareas (deben ser iguales en este problema) */
    private final int problemSize;

    /** Datos del problema (matriz de costes, tareas y agentes) */
    private final GameData gameData;

    /** Indica si se debe generar traza detallada del algoritmo */
    private final boolean traceEnabled;

    /** Traza acumulada durante la ejecución del algoritmo */
    private StringBuilder trace;

    /** Resultado final del algoritmo */
    private String result;

    // ========== ESTADÍSTICAS DEL ALGORITMO ==========

    /** Contador de nodos explorados durante la búsqueda */
    private int nodesExplored = 0;

    /** Contador de nodos generados durante la búsqueda */
    private int nodesGenerated = 0;

    /** Contador de nodos podados por la cota */
    private int nodesPruned = 0;

    /** Contador de soluciones completas encontradas */
    private int solutionsFound = 0;

    /** Contador de actualizaciones de la cota */
    private int boundUpdates = 0;

    /** Tiempo de ejecución del algoritmo en milisegundos */
    private long executionTime = 0;

    /** Cota final alcanzada */
    private int finalBound = 0;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor que inicializa el motor del algoritmo con los datos del problema.
     *
     * @param gameData    Datos del problema incluyendo matriz de costes, tareas y
     *                    agentes
     * @param enableTrace Si se debe generar traza detallada del proceso
     * @throws IllegalArgumentException si gameData es null o tiene datos
     *                                  inconsistentes
     */
    public Game(GameData gameData, boolean enableTrace) {
        if (gameData == null) {
            throw new IllegalArgumentException("Los datos del juego no pueden ser null");
        }

        this.problemSize = gameData.getAgentsNumber();
        this.gameData = gameData;
        this.traceEnabled = enableTrace;

        // Inicializar traza si está habilitada
        if (this.traceEnabled) {
            this.trace = new StringBuilder(Constants.TRACE_HEADER);
        }

        this.result = "";

        // Validar consistencia de datos
        validateGameData();
    }

    // ========== ALGORITMO PRINCIPAL BRANCH AND BOUND ==========

    /**
     * Implementación del algoritmo de ramificación y poda para el problema de
     * asignación.
     *
     * Este método implementa el núcleo del algoritmo Branch and Bound:
     * 1. Inicializa el nodo raíz y la cola de prioridad
     * 2. Calcula cotas iniciales (pesimista) para la poda
     * 3. Explora nodos en orden de promesa (mejor estimación primero)
     * 4. Genera nodos hijo para cada posible asignación
     * 5. Aplica poda basada en cotas para eliminar ramas no prometedoras
     * 6. Actualiza la mejor solución encontrada
     *
     * @param costMatrix Matriz de costes [agente][tarea]
     * @param tasks      Vector de tareas disponibles
     * @param agents     Vector donde se almacenará la asignación óptima
     * @return Resultado formateado de la asignación óptima
     */
    private String solveBranchAndBound(Matrix costMatrix, Vector tasks, Vector agents) {
        // Inicializar estructuras de datos del algoritmo
        Heap<Node> priorityQueue = new Heap<>();
        Node currentNode = new Node(this.problemSize);
        Node childNode = new Node(this.problemSize);
        Node auxiliaryNode = new Node(this.problemSize);

        // Variables de control del algoritmo
        int bestCostSoFar = Integer.MAX_VALUE;
        int currentBound;
        int pessimisticEstimate;

        // Inicializar estadísticas
        resetStatistics();

        // === FASE 1: INICIALIZACIÓN ===

        // Configurar nodo raíz (sin asignaciones)
        initializeRootNode(currentNode);
        priorityQueue.insert(currentNode);

        // Calcular cota inicial usando estimación pesimista
        currentBound = calculatePessimisticEstimate(costMatrix, tasks, 0, 0);
        this.boundUpdates++;

        addTraceEntry("Inicialización del algoritmo", currentNode, 0, currentBound, false);

        // === FASE 2: BÚSQUEDA PRINCIPAL ===

        while (!priorityQueue.isEmpty()) {
            Node nextNode = priorityQueue.extractMin();
            this.nodesExplored++;

            // Verificar si el nodo sigue siendo prometedor
            if (nextNode.getOptEst() > currentBound) {
                this.nodesPruned++;
                addTraceEntry("Nodo podado por cota", nextNode, nextNode.getCost(), currentBound, false);
                continue;
            }

            // Copiar nodo actual para exploración
            nextNode.copy(currentNode);

            // Preparar nodo hijo base
            prepareChildNode(childNode, currentNode);

            // === FASE 3: GENERACIÓN DE NODOS HIJO ===

            for (int agentIndex = 1; agentIndex <= this.problemSize; agentIndex++) {
                if (!childNode.getAssigned(agentIndex)) {

                    // Asignar agente a la siguiente tarea
                    makeAssignment(childNode, agentIndex, costMatrix, tasks);

                    if (childNode.getLastOrder() == this.problemSize) {
                        // === SOLUCIÓN COMPLETA ENCONTRADA ===
                        processSolutionNode(childNode, agents, tasks, costMatrix, currentBound);
                        if (childNode.getCost() < bestCostSoFar) {
                            bestCostSoFar = childNode.getCost();
                            currentBound = bestCostSoFar;
                            this.boundUpdates++;
                            this.solutionsFound++;
                            this.finalBound = bestCostSoFar;
                        }

                    } else {
                        // === SOLUCIÓN PARCIAL - CONTINUAR BÚSQUEDA ===

                        // Calcular estimación optimista para este nodo
                        int optimisticEst = calculateOptimisticEstimate(
                                costMatrix, tasks, childNode.getLastOrder(), childNode.getCost());
                        childNode.setOptEst(optimisticEst);

                        // Solo añadir a la cola si es prometedor
                        if (optimisticEst <= currentBound) {
                            childNode.copy(auxiliaryNode);
                            priorityQueue.insert(auxiliaryNode);
                            this.nodesGenerated++;

                            addTraceEntry("Nodo parcial añadido", childNode, childNode.getCost(), optimisticEst, false);
                        } else {
                            this.nodesPruned++;
                        }

                        // Actualizar cota con estimación pesimista si es mejor
                        pessimisticEstimate = calculatePessimisticEstimate(
                                costMatrix, tasks, childNode.getLastOrder(), childNode.getCost());

                        if (currentBound > pessimisticEstimate) {
                            currentBound = pessimisticEstimate;
                            this.boundUpdates++;
                        }
                    }

                    // Deshacer asignación para probar siguiente agente
                    undoAssignment(childNode, agentIndex);
                }
            }
        }

        // === FASE 4: FINALIZACIÓN ===

        this.finalBound = currentBound;
        addTraceEntry("Algoritmo finalizado", currentNode, bestCostSoFar, currentBound, true);

        // Generar resultado de las asignaciones finales
        return composeResult(agents);
    }

    // ========== MÉTODOS AUXILIARES DEL ALGORITMO ==========

    /**
     * Inicializa el nodo raíz sin asignaciones.
     *
     * @param rootNode Nodo a inicializar como raíz
     */
    private void initializeRootNode(Node rootNode) {
        for (int i = 1; i <= this.problemSize; i++) {
            rootNode.setAssigned(i, false);
        }
        rootNode.setLastOrder(0);
        rootNode.setCost(0);
        rootNode.setOptEst(0);
    }

    /**
     * Prepara un nodo hijo basado en el nodo actual.
     *
     * @param childNode   Nodo hijo a preparar
     * @param currentNode Nodo padre actual
     */
    private void prepareChildNode(Node childNode, Node currentNode) {
        childNode.setLastOrder(currentNode.getLastOrder() + 1);
        childNode.setAgents(currentNode.getAgents());
        childNode.setAssigned(currentNode.getAllAssigned());
    }

    /**
     * Realiza una asignación de agente a tarea en el nodo hijo.
     *
     * @param childNode  Nodo donde hacer la asignación
     * @param agentIndex Índice del agente a asignar
     * @param costMatrix Matriz de costes
     * @param tasks      Vector de tareas
     */
    private void makeAssignment(Node childNode, int agentIndex, Matrix costMatrix, Vector tasks) {
        childNode.setAgent(childNode.getLastOrder(), agentIndex);
        childNode.setAssigned(agentIndex, true);

        int assignmentCost = costMatrix.get(agentIndex, tasks.get(childNode.getLastOrder()));
        childNode.setCost(childNode.getCost() + assignmentCost);
    }

    /**
     * Deshace una asignación de agente.
     *
     * @param childNode  Nodo donde deshacer la asignación
     * @param agentIndex Índice del agente a desasignar
     */
    private void undoAssignment(Node childNode, int agentIndex) {
        childNode.setAssigned(agentIndex, false);
    }

    /**
     * Procesa un nodo que representa una solución completa.
     *
     * @param solutionNode Nodo con solución completa
     * @param agents       Vector donde copiar la solución
     * @param tasks        Vector de tareas
     * @param costMatrix   Matriz de costes
     * @param currentBound Cota actual
     */
    private void processSolutionNode(Node solutionNode, Vector agents, Vector tasks,
            Matrix costMatrix, int currentBound) {
        if (solutionNode.getCost() <= currentBound) {
            agents.copy(solutionNode.getAgents());
            this.solutionsFound++;

            addTraceEntry("Nueva solución óptima encontrada", solutionNode,
                    solutionNode.getCost(), solutionNode.getCost(), true);
        }
    }

    // ========== MÉTODOS DE ESTIMACIÓN ==========

    /**
     * Calcula la estimación optimista del coste restante.
     *
     * Para cada tarea no asignada, toma el mínimo coste posible entre todos los
     * agentes
     * disponibles. Esta estimación nunca sobreestima el coste real.
     *
     * @param costMatrix  Matriz de costes [agente][tarea]
     * @param tasks       Vector de tareas
     * @param lastOrder   Última tarea asignada
     * @param currentCost Coste acumulado hasta ahora
     * @return Estimación optimista del coste total
     */
    private int calculateOptimisticEstimate(Matrix costMatrix, Vector tasks, int lastOrder, int currentCost) {
        int estimate = currentCost;

        // Para cada tarea restante, encontrar el mínimo coste
        for (int taskIndex = lastOrder + 1; taskIndex <= this.problemSize; taskIndex++) {
            int minCostForTask = Integer.MAX_VALUE;

            // Buscar el agente con menor coste para esta tarea
            for (int agentIndex = 1; agentIndex <= this.problemSize; agentIndex++) {
                int cost = costMatrix.get(agentIndex, tasks.get(taskIndex));
                if (cost < minCostForTask) {
                    minCostForTask = cost;
                }
            }

            estimate += minCostForTask;
        }

        return estimate;
    }

    /**
     * Calcula la estimación pesimista del coste restante.
     *
     * Para cada tarea no asignada, toma el máximo coste posible entre todos los
     * agentes.
     * Esta estimación nunca subestima el coste real y se usa como cota superior.
     *
     * @param costMatrix  Matriz de costes [agente][tarea]
     * @param tasks       Vector de tareas
     * @param lastOrder   Última tarea asignada
     * @param currentCost Coste acumulado hasta ahora
     * @return Estimación pesimista del coste total
     */
    private int calculatePessimisticEstimate(Matrix costMatrix, Vector tasks, int lastOrder, int currentCost) {
        int estimate = currentCost;

        // Para cada tarea restante, encontrar el máximo coste
        for (int taskIndex = lastOrder + 1; taskIndex <= this.problemSize; taskIndex++) {
            int maxCostForTask = Integer.MIN_VALUE;

            // Buscar el agente con mayor coste para esta tarea
            for (int agentIndex = 1; agentIndex <= this.problemSize; agentIndex++) {
                int cost = costMatrix.get(agentIndex, tasks.get(taskIndex));
                if (cost > maxCostForTask) {
                    maxCostForTask = cost;
                }
            }

            estimate += maxCostForTask;
        }

        return estimate;
    }

    // ========== MÉTODOS DE TRAZA Y FORMATEO ==========

    /**
     * Añade una entrada a la traza del algoritmo si está habilitada.
     *
     * @param message    Mensaje descriptivo de la operación
     * @param node       Nodo actual
     * @param cost       Coste actual
     * @param bound      Cota o estimación
     * @param isSolution Si es una solución completa
     */
    private void addTraceEntry(String message, Node node, int cost, int bound, boolean isSolution) {
        if (!this.traceEnabled) {
            return;
        }

        if (this.trace == null) {
            this.trace = new StringBuilder(Constants.TRACE_HEADER);
        }

        this.trace.append(String.format("\n%s:\n", message));
        this.trace.append(String.format("  Coste: %d\n", cost));
        this.trace.append(String.format("  Cota/Estimación: %d\n", bound));

        if (isSolution) {
            this.trace.append("  *** SOLUCIÓN ÓPTIMA ***\n");
        }

        this.trace.append("  Asignaciones: ");
        for (int i = 1; i <= this.problemSize; i++) {
            this.trace.append(node.getAgent(i));
            if (i < this.problemSize) {
                this.trace.append(", ");
            }
        }
        this.trace.append("\n");
    }

    /**
     * Compone el resultado completo incluyendo estadísticas y traza.
     *
     * @param solution Cadena de la solución óptima
     * @return Resultado completo formateado
     */
    private String composeCompleteResult(String solution) {
        StringBuilder result = new StringBuilder();

        // Añadir solución principal
        result.append(solution);

        // Añadir estadísticas
        result.append("\n\n=== ESTADÍSTICAS DEL ALGORITMO ===\n");
        result.append(String.format("Nodos explorados: %d\n", this.nodesExplored));
        result.append(String.format("Nodos generados: %d\n", this.nodesGenerated));
        result.append(String.format("Nodos podados: %d\n", this.nodesPruned));
        result.append(String.format("Soluciones encontradas: %d\n", this.solutionsFound));
        result.append(String.format("Actualizaciones de cota: %d\n", this.boundUpdates));
        result.append(String.format("Tiempo de ejecución: %d ms\n", this.executionTime));
        result.append(String.format("Coste óptimo final: %d\n", this.finalBound));

        // Añadir traza si está habilitada
        if (this.traceEnabled && this.trace != null) {
            result.append("\n\n=== TRAZA DEL ALGORITMO ===\n");
            result.append(this.trace.toString());
        }

        return result.toString();
    }

    /**
     * Formatea las asignaciones óptimas finales.
     *
     * @param agents Vector de agentes con las asignaciones óptimas
     * @return Resultado formateado de las asignaciones
     */
    private String formatOptimalAssignments(Vector agents) {
        StringBuilder result = new StringBuilder();

        result.append("SOLUCIÓN ÓPTIMA ENCONTRADA:\n");
        result.append("==========================\n");

        Vector tasks = this.gameData.getTasks();
        Matrix costMatrix = this.gameData.getTimes();
        int totalCost = 0;

        for (int taskOrder = 1; taskOrder <= this.problemSize; taskOrder++) {
            int agentId = agents.get(taskOrder);
            int taskId = tasks.get(taskOrder);
            int assignmentCost = costMatrix.get(agentId, taskId);

            result.append(String.format("Tarea %d → Agente %d (Coste: %d)\n",
                    taskId, agentId, assignmentCost));
            totalCost += assignmentCost;
        }

        result.append("==========================\n");
        result.append(String.format("COSTE TOTAL: %d\n", totalCost));

        return result.toString();
    }

    /**
     * Valida la consistencia de los datos del juego.
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
    }

    // ========== MÉTODOS AUXILIARES DE ESTADÍSTICAS ==========

    /**
     * Reinicia todas las estadísticas del algoritmo.
     */
    private void resetStatistics() {
        this.nodesExplored = 0;
        this.nodesGenerated = 0;
        this.nodesPruned = 0;
        this.solutionsFound = 0;
        this.boundUpdates = 0;
        this.executionTime = 0;
        this.finalBound = 0;
    }

    // ========== MÉTODOS HEREDADOS MANTENIDOS PARA COMPATIBILIDAD ==========

    /**
     * Compone el resultado a partir del vector de agentes (método heredado para
     * compatibilidad).
     * Mantiene el formato original esperado por los tests.
     *
     * @param agents Vector de agentes asignados
     * @return Resultado formateado en el estilo original
     */
    private String composeResult(Vector agents) {
        String result = "\n" + "Resultado Óptimo \n (agente/tarea)\n";
        for (int i = 1; i <= this.problemSize; i++) {
            result = result + agents.get(i) + " " + i + "\n";
        }
        return result;
    }

    /**
     * Método principal que ejecuta el algoritmo de resolución (único método público
     * run).
     * Mantiene la compatibilidad con la implementación existente.
     *
     * @return El resultado final del algoritmo
     */
    public String run() {
        long startTime = System.currentTimeMillis();

        try {
            // Reiniciar estadísticas
            resetStatistics();

            // Preparar datos (mantiene estilo original)
            Matrix times = this.gameData.getTimes();
            Vector tasks = this.gameData.getTasks();
            Vector agents = this.gameData.getAgents();

            // Ejecutar algoritmo refactorizado
            String solution = solveBranchAndBound(times, tasks, agents);

            // Calcular estadísticas finales
            this.executionTime = System.currentTimeMillis() - startTime;

            // Componer resultado en formato original
            this.result = "\nSolución: \n" + solution;

            return this.result;

        } catch (Exception e) {
            throw new RuntimeException("Error durante la ejecución del algoritmo: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve la traza acumulada durante el algoritmo.
     *
     * @return La traza del algoritmo o cadena vacía si no está habilitada
     */
    public String getTrace() {
        if (this.traceEnabled && this.trace != null) {
            return this.trace.toString();
        }
        return "";
    }

    /**
     * Devuelve el resultado final del algoritmo.
     *
     * @return El resultado del algoritmo
     */
    public String getResult() {
        return this.result;
    }

    /**
     * Añade información a la traza (método heredado para compatibilidad con tests).
     *
     * @param data  Vector que contiene las asignaciones de agentes a tareas
     * @param tasks Vector que representa las tareas
     * @param cost  Coste acumulado actual
     * @param opt   Estimación optimista
     * @param msg   Mensaje descriptivo de la acción
     * @param alt   Indica si se trata de una alternativa mínima
     */
    public void addToTrace(Vector data, Vector tasks, int cost, int opt, String msg, boolean alt) {
        if (!this.traceEnabled) {
            return;
        }

        if (this.trace == null) {
            this.trace = new StringBuilder(Constants.TRACE_HEADER);
        }

        // Mantener formato original para compatibilidad
        StringBuilder traceBuilder = new StringBuilder(this.trace.toString());
        traceBuilder.append("\n").append(msg).append(": \n");
        traceBuilder.append("Coste: ").append(cost).append("\n");

        // Incluir estimación optimista solo si no es una alternativa mínima
        if (!alt) {
            traceBuilder.append("Estimación optimista: ").append(opt).append("\n");
        }

        // Agregar encabezado de asignaciones y detalles (agente/tarea)
        traceBuilder.append("(agente/tarea)\n");
        for (int i = 1; i <= this.problemSize; i++) {
            traceBuilder.append(data.get(i)).append(" ").append(tasks.get(i)).append("\n");
        }

        // Actualizar la traza acumulada
        this.trace = new StringBuilder(traceBuilder.toString());
    }

    /**
     * Método auxiliar para calcular la estimación optimista del coste (método
     * original refactorizado).
     *
     * Para cada tarea no asignada, encuentra el mínimo coste posible entre todos
     * los agentes.
     * Esta estimación nunca sobreestima el coste real, siendo así admisible para el
     * algoritmo.
     *
     * @param times     Matriz de tiempos/costes [agente][tarea]
     * @param tasks     Vector de tareas
     * @param lastOrder Última tarea asignada
     * @param cost      Coste acumulado actual
     * @return Estimación optimista del coste total
     */
    private int optimisticEst(Matrix times, Vector tasks, int lastOrder, int cost) {
        int estimate = cost;

        // Para cada tarea restante, encontrar el mínimo coste
        for (int taskIndex = lastOrder + 1; taskIndex <= this.problemSize; taskIndex++) {
            int minCostForTask = times.get(1, tasks.get(taskIndex)); // Empezar con el primer agente

            // Buscar el agente con menor coste para esta tarea
            for (int agentIndex = 2; agentIndex <= this.problemSize; agentIndex++) {
                int currentCost = times.get(agentIndex, tasks.get(taskIndex));
                if (minCostForTask > currentCost) {
                    minCostForTask = currentCost;
                }
            }

            estimate += minCostForTask;
        }

        return estimate;
    }

    /**
     * Método auxiliar para calcular la estimación pesimista del coste (método
     * original refactorizado).
     *
     * Para cada tarea no asignada, encuentra el máximo coste posible entre todos
     * los agentes.
     * Esta estimación se usa como cota superior para el algoritmo de poda.
     *
     * @param times     Matriz de tiempos/costes [agente][tarea]
     * @param tasks     Vector de tareas
     * @param lastOrder Última tarea asignada
     * @param cost      Coste acumulado actual
     * @return Estimación pesimista del coste total
     */
    private int pessimistEst(Matrix times, Vector tasks, int lastOrder, int cost) {
        int estimate = cost;

        // Para cada tarea restante, encontrar el máximo coste
        for (int taskIndex = lastOrder + 1; taskIndex <= this.problemSize; taskIndex++) {
            int maxCostForTask = times.get(1, tasks.get(taskIndex)); // Empezar con el primer agente

            // Buscar el agente con mayor coste para esta tarea
            for (int agentIndex = 2; agentIndex <= this.problemSize; agentIndex++) {
                int currentCost = times.get(agentIndex, tasks.get(taskIndex));
                if (maxCostForTask < currentCost) {
                    maxCostForTask = currentCost;
                }
            }

            estimate += maxCostForTask;
        }

        return estimate;
    }

    /**
     * Método que resuelve el problema utilizando el algoritmo de ramificación y
     * poda (método original).
     * Mantiene la lógica algorítmica original pero con mejoras en la documentación
     * y estructura.
     *
     * @param times  Matriz de tiempos de agentes y tareas
     * @param tasks  Vector de tareas
     * @param agents Vector de agentes a asignar
     * @return La solución del problema como una cadena de texto
     */
    private String solveRyP(Matrix times, Vector tasks, Vector agents) {
        String result = ""; // Resultado del algoritmo
        Heap<Node> heap = new Heap<>(); // Montículo para almacenar los nodos

        // Inicialización de nodos
        Node node = new Node(this.problemSize); // Nodo inicial
        Node son = new Node(this.problemSize); // Nodo hijo
        Node save = new Node(this.problemSize); // Nodo auxiliar para evitar sobrescritura

        int bound; // Cota inicial
        int pessEst; // Estimación pesimista

        // Inicializaciones
        heap = new Heap<>();
        int cost = 0;

        // Configuración inicial del nodo raíz
        for (int i = 1; i <= this.problemSize; i++) {
            node.setAssigned(i, false);
        }
        node.setLastOrder(0);
        node.setCost(cost);
        heap.insert(node);

        // Calcular la cota inicial usando estimación pesimista
        bound = pessimistEst(times, tasks, node.getLastOrder(), node.getCost());
        this.boundUpdates++;

        while (!heap.isEmpty()) {
            Node nextNode = heap.extractMin(); // Usar método no deprecated
            this.nodesExplored++;

            // Verificar si el nodo sigue siendo prometedor
            if (nextNode.getOptEst() > bound) {
                this.nodesPruned++;
                continue;
            }

            nextNode.copy(node); // Extraer nodo más prometedor
            this.nodesExplored++;

            son = new Node(this.problemSize);
            son.setLastOrder(node.getLastOrder() + 1);
            son.setAgents(node.getAgents());
            son.setAssigned(node.getAllAssigned());

            for (int j = 1; j <= this.problemSize; j++) {
                if (!son.getAssigned(j)) {
                    son.setAgent(son.getLastOrder(), j);
                    son.setAssigned(j, true);
                    son.setCost(node.getCost() + times.get(j, tasks.get(son.getLastOrder())));

                    if (son.getLastOrder() == this.problemSize) { // Si la solución está completa
                        if (bound >= son.getCost()) {
                            agents.copy(son.getAgents()); // Actualizar mejor solución
                            cost = son.getCost();
                            bound = cost;
                            this.boundUpdates++;
                            this.solutionsFound++;
                            this.finalBound = bound;

                            // Gestión de la traza
                            if (this.traceEnabled) {
                                String msg = "Encontrada una Alternativa mínima";
                                boolean alt = true;
                                addToTrace(agents, tasks, cost, son.getOptEst(), msg, alt);
                            }
                        }
                    } else { // La solución no está completa
                        son.setOptEst(optimisticEst(times, tasks, son.getLastOrder(), son.getCost()));

                        // Solo añadir si es prometedor
                        if (son.getOptEst() <= bound) {
                            // Usar un nodo auxiliar para evitar sobrescritura
                            save = new Node(this.problemSize);
                            son.copy(save);
                            heap.insert(save);
                            this.nodesGenerated++;
                        } else {
                            this.nodesPruned++;
                        }

                        pessEst = pessimistEst(times, tasks, son.getLastOrder(), son.getCost());
                        if (bound > pessEst) {
                            bound = pessEst;
                            this.boundUpdates++;
                        }

                        // Gestión de la traza
                        if (this.traceEnabled) {
                            String msg = "Añadido un nodo (Solución incompleta)";
                            addToTrace(agents, tasks, cost, son.getOptEst(), msg, false);
                        }
                    }
                    son.setAssigned(j, false); // Desmarcar
                }
            }
        }

        // Generar resultado como string a partir de agentes
        result = composeResult(agents);

        return result;
    }
}
