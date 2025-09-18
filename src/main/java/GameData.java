/**
 * Clase que gestiona los datos de configuración del problema de asignación de
 * tareas.
 *
 * Esta clase encapsula toda la información necesaria para resolver el problema
 * de
 * asignación óptima de tareas a agentes, incluyendo:
 * - Número de agentes y tareas
 * - Matriz de tiempos de ejecución para cada combinación agente-tarea
 * - Vectores de identificadores de agentes y tareas
 *
 * Los datos se almacenan de forma inmutable una vez inicializados, garantizando
 * la integridad durante todo el proceso de resolución del algoritmo.
 *
 * @author Pasteleros
 * @version 1.5
 * @since 1.0
 */
public final class GameData {

    // ========== ATRIBUTOS PRIVADOS ==========

    /**
     * Matriz que contiene los tiempos de ejecución de cada agente para cada tarea
     */
    private final Matrix data;

    /** Número total de agentes disponibles */
    private final int nAgents;

    /** Número total de tareas a asignar */
    private final int nTasks;

    /** Vector con los identificadores de los agentes (1-indexed) */
    private final Vector agents;

    /** Vector con los identificadores de las tareas (1-indexed) */
    private final Vector tasks;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor que inicializa los datos del problema de asignación.
     *
     * @param nAgents    Número de agentes disponibles. Debe ser mayor que 0.
     * @param nTasks     Número de tareas a asignar. Debe ser mayor que 0.
     * @param timeMatrix Matriz bidimensional con los tiempos de ejecución.
     *                   timeMatrix[i][j] representa el tiempo que el agente i
     *                   necesita para completar la tarea j.
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    public GameData(int nAgents, int nTasks, int[][] timeMatrix) {
        validateConstructorParameters(nAgents, nTasks, timeMatrix);

        this.nAgents = nAgents;
        this.nTasks = nTasks;
        this.data = new Matrix(nAgents, nTasks, timeMatrix);
        this.agents = createAgentsVector();
        this.tasks = createTasksVector();
    }

    // ========== MÉTODOS PÚBLICOS DE ACCESO ==========

    /**
     * Obtiene el tiempo de ejecución para una combinación específica agente-tarea.
     *
     * @param agentIndex Índice del agente (0-based)
     * @param taskIndex  Índice de la tarea (0-based)
     * @return Tiempo de ejecución requerido
     * @throws IndexOutOfBoundsException si los índices están fuera de rango
     */
    public int getExecutionTime(int agentIndex, int taskIndex) {
        validateIndices(agentIndex, taskIndex);
        return data.get(agentIndex, taskIndex);
    }

    /**
     * Método de compatibilidad que mantiene la interfaz original con índices
     * base-1.
     *
     * @deprecated Usar {@link #getExecutionTime(int, int)} para mayor claridad
     * @param row Índice de fila (agente, base-1: 1 a nAgents)
     * @param col Índice de columna (tarea, base-1: 1 a nTasks)
     * @return Tiempo de ejecución
     * @throws IndexOutOfBoundsException si los índices están fuera de rango
     */
    @Deprecated
    public int get(int row, int col) {
        // Matrix ya maneja índices base-1 internamente
        validateIndicesBase1(row, col);
        return data.get(row, col);
    }

    /**
     * Obtiene el número total de agentes disponibles.
     *
     * @return Número de agentes
     */
    public int getAgentsNumber() {
        return this.nAgents;
    }

    /**
     * Obtiene el número total de tareas a asignar.
     *
     * @return Número de tareas
     */
    public int getTasksNumber() {
        return this.nTasks;
    }

    /**
     * Obtiene una copia del vector de agentes.
     * Los agentes están numerados desde 1 hasta nAgents.
     *
     * @return Vector inmutable con los identificadores de agentes
     */
    public Vector getAgents() {
        return this.agents; // Vector es inmutable por diseño
    }

    /**
     * Obtiene una copia del vector de tareas.
     * Las tareas están numeradas desde 1 hasta nTasks.
     *
     * @return Vector inmutable con los identificadores de tareas
     */
    public Vector getTasks() {
        return this.tasks; // Vector es inmutable por diseño
    }

    /**
     * Obtiene la matriz completa de tiempos de ejecución.
     *
     * @return Matriz inmutable con todos los tiempos de ejecución
     */
    public Matrix getTimes() {
        return this.data; // Matrix es inmutable por diseño
    }

    // ========== MÉTODOS PRIVADOS DE INICIALIZACIÓN ==========

    /**
     * Valida los parámetros del constructor para asegurar que son válidos.
     *
     * @param nAgents    Número de agentes
     * @param nTasks     Número de tareas
     * @param timeMatrix Matriz de tiempos
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    private void validateConstructorParameters(int nAgents, int nTasks, int[][] timeMatrix) {
        if (nAgents <= 0) {
            throw new IllegalArgumentException("El número de agentes debe ser mayor que 0");
        }
        if (nTasks <= 0) {
            throw new IllegalArgumentException("El número de tareas debe ser mayor que 0");
        }
        if (timeMatrix == null) {
            throw new IllegalArgumentException("La matriz de tiempos no puede ser null");
        }
        if (timeMatrix.length != nAgents) {
            throw new IllegalArgumentException(
                    "La matriz debe tener exactamente " + nAgents + " filas (agentes)");
        }

        // Validar que todas las filas tengan el número correcto de columnas
        for (int i = 0; i < timeMatrix.length; i++) {
            if (timeMatrix[i] == null) {
                throw new IllegalArgumentException("La fila " + i + " de la matriz no puede ser null");
            }
            if (timeMatrix[i].length != nTasks) {
                throw new IllegalArgumentException(
                        "La fila " + i + " debe tener exactamente " + nTasks + " columnas (tareas)");
            }

            // Validar que no haya tiempos negativos
            for (int j = 0; j < timeMatrix[i].length; j++) {
                if (timeMatrix[i][j] < 0) {
                    throw new IllegalArgumentException(
                            "El tiempo de ejecución para agente " + i + ", tarea " + j + " no puede ser negativo");
                }
            }
        }
    }

    /**
     * Valida que los índices proporcionados estén dentro de los rangos válidos
     * (base-0).
     *
     * @param agentIndex Índice del agente (0-based)
     * @param taskIndex  Índice de la tarea (0-based)
     * @throws IndexOutOfBoundsException si algún índice está fuera de rango
     */
    private void validateIndices(int agentIndex, int taskIndex) {
        if (agentIndex < 0 || agentIndex >= nAgents) {
            throw new IndexOutOfBoundsException(
                    "Índice de agente fuera de rango: " + agentIndex + " (rango válido: 0-" + (nAgents - 1) + ")");
        }
        if (taskIndex < 0 || taskIndex >= nTasks) {
            throw new IndexOutOfBoundsException(
                    "Índice de tarea fuera de rango: " + taskIndex + " (rango válido: 0-" + (nTasks - 1) + ")");
        }
    }

    /**
     * Valida que los índices proporcionados estén dentro de los rangos válidos
     * (base-1).
     * Para compatibilidad con el método get() legacy.
     *
     * @param agentIndex Índice del agente (1-based)
     * @param taskIndex  Índice de la tarea (1-based)
     * @throws IndexOutOfBoundsException si algún índice está fuera de rango
     */
    private void validateIndicesBase1(int agentIndex, int taskIndex) {
        if (agentIndex < 1 || agentIndex > nAgents) {
            throw new IndexOutOfBoundsException(
                    "Índice de agente fuera de rango: " + agentIndex + " (rango válido: 1-" + nAgents + ")");
        }
        if (taskIndex < 1 || taskIndex > nTasks) {
            throw new IndexOutOfBoundsException(
                    "Índice de tarea fuera de rango: " + taskIndex + " (rango válido: 1-" + nTasks + ")");
        }
    }

    /**
     * Crea e inicializa el vector de agentes con identificadores secuenciales.
     * Los agentes se numeran desde 1 hasta nAgents.
     *
     * @return Vector inicializado con los identificadores de agentes
     */
    private Vector createAgentsVector() {
        Vector agentsVector = new Vector(nAgents);
        for (int i = 1; i <= nAgents; i++) {
            agentsVector.set(i, i);
        }
        return agentsVector;
    }

    /**
     * Crea e inicializa el vector de tareas con identificadores secuenciales.
     * Las tareas se numeran desde 1 hasta nTasks.
     *
     * @return Vector inicializado con los identificadores de tareas
     */
    private Vector createTasksVector() {
        Vector tasksVector = new Vector(nTasks);
        for (int i = 1; i <= nTasks; i++) {
            tasksVector.set(i, i);
        }
        return tasksVector;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Proporciona una representación en cadena de los datos del juego.
     * Útil para depuración y logging.
     *
     * @return Representación textual de los datos
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GameData{");
        sb.append("agentes=").append(nAgents);
        sb.append(", tareas=").append(nTasks);
        sb.append(", matriz=").append(data != null ? "inicializada" : "null");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Verifica si los datos representan un problema válido (matriz cuadrada).
     *
     * @return true si el número de agentes es igual al número de tareas
     */
    public boolean isSquareProblem() {
        return nAgents == nTasks;
    }

    /**
     * Calcula el tiempo total mínimo teórico si cada agente realizara su tarea más
     * rápida.
     * Este valor puede usarse como heurística o para análisis.
     *
     * @return Suma de los tiempos mínimos por agente
     */
    public int getMinimumTheoreticalTime() {
        int totalMin = 0;
        for (int agent = 0; agent < nAgents; agent++) {
            int minTimeForAgent = Integer.MAX_VALUE;
            for (int task = 0; task < nTasks; task++) {
                minTimeForAgent = Math.min(minTimeForAgent, getExecutionTime(agent, task));
            }
            totalMin += minTimeForAgent;
        }
        return totalMin;
    }
}
