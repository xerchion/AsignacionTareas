/**
 * Implementación de un montículo binario de mínimos (min-heap) genérico
 * optimizado para algoritmos de búsqueda.
 *
 * Esta clase implementa una cola de prioridad basada en un montículo binario de
 * mínimos, donde el elemento
 * con menor valor (según su comparación natural) siempre está en la raíz. Está
 * especialmente optimizada
 * para su uso en algoritmos de ramificación y poda (branch and bound) donde se
 * necesita acceso eficiente
 * al elemento de menor coste.
 *
 * Características principales:
 * - Estructura de datos completa binaria representada como array
 * - Operaciones de inserción y extracción en O(log n)
 * - Indexación base-1 para simplificar cálculos de navegación padre/hijo
 * - Capacidad fija configurable con validación de límites
 * - Construcción eficiente desde array existente en O(n)
 *
 * Invariantes del montículo:
 * - Propiedad de orden: Para todo nodo i, vector[i] ≤ vector[2*i] y vector[i] ≤
 * vector[2*i+1]
 * - Estructura completa: Todos los niveles están llenos excepto posiblemente el
 * último
 * - Indexación: Padre de i está en i/2, hijos de i están en 2*i y 2*i+1
 *
 * @param <T> Tipo de elementos almacenados en el montículo. Debe implementar
 *            Comparable<T>
 *
 * @author Pasteleros
 * @version 2.0
 * @since 1.0
 */
public final class Heap<T extends Comparable<T>> {

    // ========== CONSTANTES ==========

    /** Capacidad máxima por defecto del montículo */
    private static final int DEFAULT_CAPACITY = 100;

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Número de elementos actualmente almacenados en el montículo */
    private int size;

    /** Capacidad máxima del montículo */
    private final int maxCapacity;

    /**
     * Array genérico que almacena los elementos del montículo (índice 0 no
     * utilizado)
     */
    private final T[] vector;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor que crea un montículo vacío con capacidad por defecto.
     */
    public Heap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructor que crea un montículo vacío con capacidad específica.
     *
     * @param capacity Capacidad máxima del montículo. Debe ser mayor que 0.
     * @throws IllegalArgumentException si capacity es menor o igual a 0
     */
    @SuppressWarnings("unchecked")
    public Heap(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que 0");
        }

        this.maxCapacity = capacity;
        this.vector = (T[]) new Comparable[capacity + 1]; // +1 porque no usamos índice 0
        this.size = 0;
    }

    /**
     * Constructor que crea un montículo a partir de un array existente.
     *
     * Construye el montículo usando el algoritmo de "heapify" en tiempo O(n),
     * que es más eficiente que insertar elementos uno por uno.
     *
     * @param sourceArray Array inicial con los elementos para construir el
     *                    montículo
     * @throws IllegalArgumentException si sourceArray es null o excede la capacidad
     *                                  máxima
     */
    @SuppressWarnings("unchecked")
    public Heap(T[] sourceArray) {
        if (sourceArray == null) {
            throw new IllegalArgumentException("El array fuente no puede ser null");
        }
        if (sourceArray.length > DEFAULT_CAPACITY) {
            throw new IllegalArgumentException(
                    "El array supera la capacidad máxima: " + sourceArray.length + " > " + DEFAULT_CAPACITY);
        }

        this.maxCapacity = Math.max(DEFAULT_CAPACITY, sourceArray.length);
        this.vector = (T[]) new Comparable[this.maxCapacity + 1];
        this.vector[0] = null; // Índice 0 no utilizado

        // Copiar elementos del array fuente al montículo
        System.arraycopy(sourceArray, 0, this.vector, 1, sourceArray.length);
        this.size = sourceArray.length;

        // Construir el montículo desde las hojas hacia la raíz
        buildHeap();
    }

    /**
     * Constructor que crea un montículo a partir de un array con capacidad
     * específica.
     *
     * @param sourceArray Array inicial con los elementos
     * @param capacity    Capacidad máxima del montículo
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    @SuppressWarnings("unchecked")
    public Heap(T[] sourceArray, int capacity) {
        if (sourceArray == null) {
            throw new IllegalArgumentException("El array fuente no puede ser null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que 0");
        }
        if (sourceArray.length > capacity) {
            throw new IllegalArgumentException(
                    "El array supera la capacidad especificada: " + sourceArray.length + " > " + capacity);
        }

        this.maxCapacity = capacity;
        this.vector = (T[]) new Comparable[capacity + 1];
        this.vector[0] = null;

        System.arraycopy(sourceArray, 0, this.vector, 1, sourceArray.length);
        this.size = sourceArray.length;

        buildHeap();
    }

    // ========== MÉTODOS PÚBLICOS PRINCIPALES ==========

    /**
     * Inserta un nuevo elemento en el montículo manteniendo las propiedades del
     * heap.
     *
     * El elemento se añade al final del montículo y luego se hace "flotar" hacia
     * arriba
     * hasta encontrar su posición correcta. Complejidad: O(log n).
     *
     * @param element Elemento a insertar
     * @throws IllegalArgumentException si element es null
     * @throws IllegalStateException    si el montículo está lleno
     */
    public void insert(T element) {
        if (element == null) {
            throw new IllegalArgumentException("No se puede insertar un elemento null");
        }
        if (size >= maxCapacity) {
            throw new IllegalStateException(
                    "El montículo está lleno. Capacidad máxima: " + maxCapacity);
        }

        size++;
        vector[size] = element;
        heapifyUp(size);
    }

    /**
     * Obtiene y elimina el elemento mínimo del montículo (la raíz).
     *
     * El elemento en la raíz se reemplaza con el último elemento y luego se
     * hace "hundir" para restaurar las propiedades del montículo. Complejidad:
     * O(log n).
     *
     * @return Elemento mínimo del montículo
     * @throws IllegalStateException si el montículo está vacío
     */
    public T extractMin() {
        if (isEmpty()) {
            throw new IllegalStateException("No se puede extraer de un montículo vacío");
        }

        T min = vector[1];
        vector[1] = vector[size];
        vector[size] = null; // Limpiar referencia para GC
        size--;

        if (size > 0) {
            heapifyDown(1);
        }

        return min;
    }

    /**
     * Obtiene el elemento mínimo sin eliminarlo del montículo.
     *
     * @return Elemento mínimo del montículo (raíz)
     * @throws IllegalStateException si el montículo está vacío
     */
    public T peekMin() {
        if (isEmpty()) {
            throw new IllegalStateException("No se puede consultar un montículo vacío");
        }
        return vector[1];
    }

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Verifica si el montículo está vacío.
     *
     * @return true si no hay elementos, false en caso contrario
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Verifica si el montículo está lleno.
     *
     * @return true si ha alcanzado la capacidad máxima, false en caso contrario
     */
    public boolean isFull() {
        return size >= maxCapacity;
    }

    /**
     * Obtiene el número de elementos actualmente en el montículo.
     *
     * @return Tamaño actual del montículo
     */
    public int size() {
        return size;
    }

    /**
     * Obtiene la capacidad máxima del montículo.
     *
     * @return Capacidad máxima
     */
    public int capacity() {
        return maxCapacity;
    }

    /**
     * Obtiene el número de espacios libres en el montículo.
     *
     * @return Número de elementos que se pueden insertar aún
     */
    public int remainingCapacity() {
        return maxCapacity - size;
    }

    // ========== MÉTODOS DE MANIPULACIÓN AVANZADA ==========

    /**
     * Limpia todos los elementos del montículo.
     *
     * Establece el tamaño a 0 y limpia las referencias para ayudar al garbage
     * collector.
     */
    public void clear() {
        for (int i = 1; i <= size; i++) {
            vector[i] = null;
        }
        size = 0;
    }

    /**
     * Crea una copia independiente de este montículo.
     *
     * @return Nuevo montículo con los mismos elementos y capacidad
     */
    public Heap<T> clone() {
        Heap<T> copy = new Heap<>(this.maxCapacity);
        for (int i = 1; i <= this.size; i++) {
            copy.vector[i] = this.vector[i];
        }
        copy.size = this.size;
        return copy;
    }

    /**
     * Convierte el montículo a un array ordenado.
     *
     * @return Array con los elementos del montículo en orden ascendente
     */
    @SuppressWarnings("unchecked")
    public T[] toSortedArray() {
        if (size == 0) {
            return (T[]) new Comparable[0];
        }

        // Crear una copia para no modificar el montículo original
        Heap<T> tempHeap = this.clone();
        T[] result = (T[]) new Comparable[size];

        for (int i = 0; i < result.length; i++) {
            result[i] = tempHeap.extractMin();
        }

        return result;
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    /**
     * Construye el montículo desde un array desordenado usando el algoritmo de
     * Floyd.
     *
     * Procesa todos los nodos internos desde el último hacia la raíz,
     * aplicando heapifyDown a cada uno. Complejidad: O(n).
     */
    private void buildHeap() {
        // Comenzar desde el último nodo interno (size/2) hacia la raíz
        for (int i = size / 2; i >= 1; i--) {
            heapifyDown(i);
        }
    }

    /**
     * Hace flotar un elemento hacia arriba para restaurar la propiedad del
     * montículo.
     *
     * Compara el elemento con su padre y los intercambia si es necesario,
     * continuando hasta que se restaure la propiedad del heap o se alcance la raíz.
     *
     * @param index Índice del elemento a hacer flotar
     */
    private void heapifyUp(int index) {
        while (index > 1) {
            int parentIndex = index / 2;

            // Si el padre es menor o igual, hemos terminado
            if (vector[parentIndex].compareTo(vector[index]) <= 0) {
                break;
            }

            // Intercambiar con el padre y continuar
            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    /**
     * Hace hundir un elemento hacia abajo para restaurar la propiedad del
     * montículo.
     *
     * Compara el elemento con sus hijos y lo intercambia con el menor de ellos si
     * es necesario,
     * continuando hasta que se restaure la propiedad del heap o se alcance una
     * hoja.
     *
     * @param index Índice del elemento a hacer hundir
     */
    private void heapifyDown(int index) {
        while (true) {
            int leftChild = 2 * index;
            int rightChild = 2 * index + 1;
            int smallest = index;

            // Verificar hijo izquierdo
            if (leftChild <= size && vector[leftChild].compareTo(vector[smallest]) < 0) {
                smallest = leftChild;
            }

            // Verificar hijo derecho
            if (rightChild <= size && vector[rightChild].compareTo(vector[smallest]) < 0) {
                smallest = rightChild;
            }

            // Si no hay cambios necesarios, hemos terminado
            if (smallest == index) {
                break;
            }

            // Intercambiar y continuar con el hijo
            swap(index, smallest);
            index = smallest;
        }
    }

    /**
     * Intercambia dos elementos en el array del montículo.
     *
     * @param i Índice del primer elemento
     * @param j Índice del segundo elemento
     */
    private void swap(int i, int j) {
        T temp = vector[i];
        vector[i] = vector[j];
        vector[j] = temp;
    }

    // ========== MÉTODOS DE UTILIDAD Y DEPURACIÓN ==========

    /**
     * Verifica si el montículo mantiene sus propiedades estructurales.
     *
     * @return true si el montículo es válido, false en caso contrario
     */
    public boolean isValidHeap() {
        for (int i = 1; i <= size / 2; i++) {
            int leftChild = 2 * i;
            int rightChild = 2 * i + 1;

            // Verificar hijo izquierdo
            if (leftChild <= size && vector[i].compareTo(vector[leftChild]) > 0) {
                return false;
            }

            // Verificar hijo derecho
            if (rightChild <= size && vector[i].compareTo(vector[rightChild]) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calcula la altura del montículo.
     *
     * @return Altura del montículo (número de niveles - 1)
     */
    public int height() {
        if (size == 0)
            return -1;
        return (int) Math.floor(Math.log(size) / Math.log(2));
    }

    /**
     * Proporciona una representación textual del montículo.
     *
     * @return Representación en cadena con información del estado y elementos
     */
    @Override
    public String toString() {
        if (size == 0) {
            return "Heap[vacío, capacidad=" + maxCapacity + "]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Heap[tamaño=").append(size)
                .append(", capacidad=").append(maxCapacity)
                .append(", altura=").append(height())
                .append("]\n");

        sb.append("Elementos: [");
        for (int i = 1; i <= size; i++) {
            if (i > 1)
                sb.append(", ");
            sb.append(vector[i]);
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * Compara este montículo con otro objeto para determinar igualdad.
     *
     * @param obj Objeto a comparar
     * @return true si los montículos son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Heap<?> other = (Heap<?>) obj;
        if (this.size != other.size || this.maxCapacity != other.maxCapacity) {
            return false;
        }

        for (int i = 1; i <= size; i++) {
            if (!this.vector[i].equals(other.vector[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calcula el código hash para este montículo.
     *
     * @return Código hash basado en el tamaño, capacidad y elementos
     */
    @Override
    public int hashCode() {
        int hash = size;
        hash = 31 * hash + maxCapacity;
        for (int i = 1; i <= size; i++) {
            hash = 31 * hash + (vector[i] != null ? vector[i].hashCode() : 0);
        }
        return hash;
    }

    // ========== MÉTODOS LEGACY (COMPATIBILIDAD) ==========

    /**
     * @deprecated Usar {@link #size()} en su lugar
     * @return Tamaño actual del montículo
     */
    @Deprecated
    public int length() {
        return size();
    }

    /**
     * @deprecated Usar {@link #peekMin()} en su lugar
     * @return Elemento mínimo sin eliminarlo
     */
    @Deprecated
    public T getFirst() {
        return isEmpty() ? null : peekMin();
    }

    /**
     * @deprecated Usar {@link #extractMin()} en su lugar
     * @return Elemento mínimo eliminándolo del montículo
     */
    @Deprecated
    public T getTop() {
        return isEmpty() ? null : extractMin();
    }
}
