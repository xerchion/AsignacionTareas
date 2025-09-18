/**
 * Representa un nodo en el árbol de búsqueda del algoritmo de asignación de
 * tareas.
 *
 * Esta clase encapsula el estado de una solución parcial en el algoritmo de
 * ramificación
 * y poda (branch and bound) para el problema de asignación de tareas. Cada nodo
 * representa
 * un estado donde algunas tareas han sido asignadas a agentes y contiene
 * información sobre
 * el coste actual y una estimación optimista del coste final.
 *
 * El nodo mantiene:
 * - Asignaciones actuales de agentes a tareas
 * - Estado de qué tareas han sido asignadas
 * - Coste acumulado de las asignaciones realizadas
 * - Estimación optimista del coste total de la solución
 * - Información sobre la última tarea asignada
 *
 * Características principales:
 * - Implementa Comparable para uso en estructuras de heap/cola de prioridad
 * - Indexación base-1 consistente con Vector y VectorB
 * - Operaciones de copia profunda para ramificación del algoritmo
 * - Validación de parámetros en operaciones críticas
 *
 * @author Pasteleros
 * @version 2.0
 * @since 1.0
 */
public final class Node implements Comparable<Node> {

    // ========== ATRIBUTOS PRIVADOS ==========

    /**
     * Almacena en la posición i el agente asignado a la tarea i.
     * Utiliza indexación base-1 para consistencia con el dominio del problema.
     */
    private final Vector agents;

    /**
     * Indica si la tarea en la posición i ha sido asignada a algún agente.
     * Utiliza indexación base-1 para consistencia con el dominio del problema.
     */
    private final VectorB assigned;

    /** Índice de la última tarea asignada en el proceso de construcción del nodo */
    private int lastOrder;

    /** Coste total acumulado de las asignaciones ya realizadas */
    private int cost;

    /**
     * Estimación optimista del coste total que se puede alcanzar desde este nodo
     */
    private int optimalEstimate;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor que crea un nuevo nodo para un problema de tamaño especificado.
     *
     * Inicializa un nodo vacío donde no se han realizado asignaciones, con coste
     * cero
     * y estimación optimista cero. Los vectores internos se crean con el tamaño
     * especificado
     * usando indexación base-1.
     *
     * @param size Número de tareas/agentes en el problema. Debe ser mayor que 0.
     * @throws IllegalArgumentException si size es menor o igual a 0
     */
    public Node(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño del nodo debe ser mayor que 0");
        }

        this.agents = new Vector(size);
        this.assigned = new VectorB(size);
        this.lastOrder = 0;
        this.cost = 0;
        this.optimalEstimate = 0;
    }

    // ========== MÉTODOS DE ACCESO A AGENTES ==========

    /**
     * Obtiene el vector de asignaciones de agentes a tareas.
     *
     * @return Vector que contiene las asignaciones actuales (indexación base-1)
     */
    public Vector getAgents() {
        return agents;
    }

    /**
     * Copia las asignaciones de agentes desde otro vector.
     *
     * Realiza una copia profunda de todas las asignaciones del vector fuente.
     * Ambos vectores deben tener el mismo tamaño.
     *
     * @param source Vector fuente con las asignaciones a copiar
     * @throws IllegalArgumentException si source es null o tiene tamaño diferente
     */
    public void setAgents(Vector source) {
        if (source == null) {
            throw new IllegalArgumentException("El vector de agentes no puede ser null");
        }
        if (source.size() != this.agents.size()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener el mismo tamaño. Actual: " + this.agents.size() +
                            ", Fuente: " + source.size());
        }

        for (int i = 1; i <= source.size(); i++) {
            this.agents.set(i, source.get(i));
        }
    }

    /**
     * Asigna un agente específico a una tarea específica.
     *
     * @param taskIndex Índice de la tarea (base-1: 1 a size)
     * @param agentId   Identificador del agente asignado a la tarea
     * @throws IndexOutOfBoundsException si taskIndex está fuera del rango válido
     */
    public void setAgent(int taskIndex, int agentId) {
        this.agents.set(taskIndex, agentId);
    }

    /**
     * Obtiene el agente asignado a una tarea específica.
     *
     * @param taskIndex Índice de la tarea (base-1: 1 a size)
     * @return Identificador del agente asignado a la tarea
     * @throws IndexOutOfBoundsException si taskIndex está fuera del rango válido
     */
    public int getAgent(int taskIndex) {
        return this.agents.get(taskIndex);
    }

    // ========== MÉTODOS DE ACCESO A ASIGNACIONES ==========

    /**
     * Verifica si una tarea específica ha sido asignada.
     *
     * @param taskIndex Índice de la tarea a verificar (base-1: 1 a size)
     * @return true si la tarea ha sido asignada, false en caso contrario
     * @throws IndexOutOfBoundsException si taskIndex está fuera del rango válido
     */
    public boolean getAssigned(int taskIndex) {
        return assigned.get(taskIndex);
    }

    /**
     * Obtiene el vector completo de estados de asignación.
     *
     * @return Vector booleano que indica qué tareas han sido asignadas
     */
    public VectorB getAllAssigned() {
        return assigned;
    }

    /**
     * Marca una tarea específica como asignada o no asignada.
     *
     * @param taskIndex  Índice de la tarea (base-1: 1 a size)
     * @param isAssigned true para marcar como asignada, false para marcar como no
     *                   asignada
     * @throws IndexOutOfBoundsException si taskIndex está fuera del rango válido
     */
    public void setAssigned(int taskIndex, boolean isAssigned) {
        this.assigned.set(taskIndex, isAssigned);
    }

    /**
     * Copia los estados de asignación desde otro vector booleano.
     *
     * Realiza una copia profunda de todos los estados de asignación.
     * Ambos vectores deben tener el mismo tamaño.
     *
     * @param source Vector fuente con los estados a copiar
     * @throws IllegalArgumentException si source es null o tiene tamaño diferente
     */
    public void setAssigned(VectorB source) {
        if (source == null) {
            throw new IllegalArgumentException("El vector de asignaciones no puede ser null");
        }
        if (source.size() != this.assigned.size()) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener el mismo tamaño. Actual: " + this.assigned.size() +
                            ", Fuente: " + source.size());
        }

        for (int i = 1; i <= source.size(); i++) {
            this.assigned.set(i, source.get(i));
        }
    }

    // ========== MÉTODOS DE ACCESO A INFORMACIÓN DEL ALGORITMO ==========

    /**
     * Obtiene el índice de la última tarea asignada.
     *
     * @return Índice de la última tarea asignada (0 si no se ha asignado ninguna)
     */
    public int getLastOrder() {
        return lastOrder;
    }

    /**
     * Establece el índice de la última tarea asignada.
     *
     * @param taskIndex Índice de la última tarea asignada
     * @throws IllegalArgumentException si taskIndex es negativo
     */
    public void setLastOrder(int taskIndex) {
        if (taskIndex < 0) {
            throw new IllegalArgumentException("El índice de tarea no puede ser negativo");
        }
        this.lastOrder = taskIndex;
    }

    /**
     * Obtiene el coste total acumulado de las asignaciones realizadas.
     *
     * @return Coste total actual
     */
    public int getCost() {
        return cost;
    }

    /**
     * Establece el coste total acumulado.
     *
     * @param totalCost Nuevo coste total
     * @throws IllegalArgumentException si totalCost es negativo
     */
    public void setCost(int totalCost) {
        if (totalCost < 0) {
            throw new IllegalArgumentException("El coste no puede ser negativo");
        }
        this.cost = totalCost;
    }

    /**
     * Obtiene la estimación optimista del coste total de la solución.
     *
     * @return Estimación optimista del coste final
     */
    public int getOptEst() {
        return optimalEstimate;
    }

    /**
     * Establece la estimación optimista del coste total.
     *
     * @param estimate Nueva estimación optimista
     * @throws IllegalArgumentException si estimate es negativo
     */
    public void setOptEst(int estimate) {
        if (estimate < 0) {
            throw new IllegalArgumentException("La estimación no puede ser negativa");
        }
        this.optimalEstimate = estimate;
    }

    // ========== MÉTODOS DE MANIPULACIÓN ==========

    /**
     * Realiza una copia profunda de este nodo a otro nodo existente.
     *
     * Copia todos los atributos del nodo actual al nodo destino, incluyendo
     * las asignaciones de agentes, estados de asignación, y métricas del algoritmo.
     *
     * @param targetNode Nodo destino donde copiar la información
     * @throws IllegalArgumentException si targetNode es null o tiene tamaño
     *                                  diferente
     */
    public void copy(Node targetNode) {
        if (targetNode == null) {
            throw new IllegalArgumentException("El nodo destino no puede ser null");
        }
        if (targetNode.agents.size() != this.agents.size()) {
            throw new IllegalArgumentException(
                    "Los nodos deben tener el mismo tamaño. Actual: " + this.agents.size() +
                            ", Destino: " + targetNode.agents.size());
        }

        targetNode.setOptEst(this.optimalEstimate);
        targetNode.setAssigned(this.assigned);
        targetNode.setCost(this.cost);
        targetNode.setLastOrder(this.lastOrder);
        targetNode.agents.copy(this.agents);
        targetNode.assigned.copy(this.assigned);
    }

    /**
     * Crea una copia independiente de este nodo.
     *
     * @return Nuevo nodo con los mismos valores que este nodo
     */
    public Node clone() {
        Node copy = new Node(this.agents.size());
        this.copy(copy);
        return copy;
    }

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Cuenta el número de tareas que han sido asignadas.
     *
     * @return Número de tareas asignadas
     */
    public int getAssignedCount() {
        return this.assigned.countTrue();
    }

    /**
     * Cuenta el número de tareas que no han sido asignadas.
     *
     * @return Número de tareas sin asignar
     */
    public int getUnassignedCount() {
        return this.assigned.countFalse();
    }

    /**
     * Verifica si el nodo representa una solución completa.
     *
     * @return true si todas las tareas han sido asignadas, false en caso contrario
     */
    public boolean isComplete() {
        return this.assigned.allTrue();
    }

    /**
     * Verifica si el nodo está vacío (sin asignaciones).
     *
     * @return true si no se ha asignado ninguna tarea, false en caso contrario
     */
    public boolean isEmpty() {
        return this.assigned.allFalse();
    }

    /**
     * Obtiene el tamaño del problema (número de tareas/agentes).
     *
     * @return Tamaño del problema
     */
    public int getSize() {
        return this.agents.size();
    }

    // ========== IMPLEMENTACIÓN DE COMPARABLE ==========

    /**
     * Compara este nodo con otro nodo basándose en la estimación optimista.
     *
     * Los nodos se ordenan por estimación optimista ascendente, lo que permite
     * que los nodos más prometedores (menor coste estimado) tengan prioridad
     * en algoritmos de búsqueda con cola de prioridad.
     *
     * @param other Nodo a comparar
     * @return valor negativo si este nodo es "mejor" (menor estimación),
     *         cero si son equivalentes, valor positivo si es "peor"
     * @throws NullPointerException si other es null
     */
    @Override
    public int compareTo(Node other) {
        if (other == null) {
            throw new NullPointerException("No se puede comparar con un nodo null");
        }

        // Comparación por estimación optimista (menor es mejor)
        return Integer.compare(this.optimalEstimate, other.optimalEstimate);
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Proporciona una representación textual detallada del nodo.
     *
     * Incluye información sobre las asignaciones realizadas, estados de asignación,
     * métricas del algoritmo y estadísticas del nodo.
     *
     * @return Representación textual completa del nodo
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("=== Nodo ===\n");
        result.append("Tamaño: ").append(getSize()).append("\n");
        result.append("Agentes asignados a tareas: ").append(agents).append("\n");
        result.append("Tareas asignadas: ").append(assigned).append("\n");
        result.append("Última tarea asignada: ").append(lastOrder).append("\n");
        result.append("Coste total: ").append(cost).append("\n");
        result.append("Estimación óptima: ").append(optimalEstimate).append("\n");
        result.append("Tareas asignadas/total: ").append(getAssignedCount())
                .append("/").append(getSize()).append("\n");
        result.append("Estado: ").append(isComplete() ? "Completo" : isEmpty() ? "Vacío" : "Parcial").append("\n");
        result.append("================\n");
        return result.toString();
    }

    /**
     * Compara este nodo con otro objeto para determinar igualdad.
     *
     * Dos nodos son iguales si tienen el mismo tamaño, las mismas asignaciones,
     * los mismos estados de asignación y las mismas métricas del algoritmo.
     *
     * @param obj Objeto a comparar
     * @return true si los nodos son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Node other = (Node) obj;
        return this.lastOrder == other.lastOrder &&
                this.cost == other.cost &&
                this.optimalEstimate == other.optimalEstimate &&
                this.agents.equals(other.agents) &&
                this.assigned.equals(other.assigned);
    }

    /**
     * Calcula el código hash para este nodo.
     *
     * @return Código hash basado en todos los atributos del nodo
     */
    @Override
    public int hashCode() {
        int hash = this.lastOrder;
        hash = 31 * hash + this.cost;
        hash = 31 * hash + this.optimalEstimate;
        hash = 31 * hash + this.agents.hashCode();
        hash = 31 * hash + this.assigned.hashCode();
        return hash;
    }
}