/**
 * Implementación de un vector de enteros con indexación basada en 1.
 *
 * Esta clase implementa un vector especializado que no utiliza el índice 0,
 * comenzando la numeración desde 1. Está diseñado específicamente para el
 * algoritmo de asignación de tareas donde los agentes y tareas se numeran
 * desde 1, proporcionando una interfaz más natural para el dominio del
 * problema.
 *
 * Características principales:
 * - Indexación base-1 (índices válidos: 1 a size)
 * - Operaciones básicas de acceso y modificación
 * - Copia profunda de vectores
 * - Representación textual para depuración
 *
 * @author Pasteleros
 * @version 1.5
 * @since 1.0
 */
public final class Vector {

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Array interno que almacena los datos del vector (índice 0 no utilizado) */
    private final int[] data;

    /** Tamaño lógico del vector (número de elementos válidos) */
    private final int size;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor que crea un vector de tamaño especificado.
     *
     * El vector resultante tendrá índices válidos desde 1 hasta size (inclusive).
     * El índice 0 existe en el array interno pero no se utiliza.
     *
     * @param size Número de elementos del vector. Debe ser mayor que 0.
     * @throws IllegalArgumentException si size es menor o igual a 0
     */
    public Vector(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño del vector debe ser mayor que 0");
        }

        // El array interno es size+1 ya que no usamos el índice 0
        this.data = new int[size + 1];
        this.size = size;
    }

    // ========== MÉTODOS PÚBLICOS DE ACCESO ==========

    /**
     * Obtiene el valor almacenado en la posición especificada.
     *
     * @param index Índice del elemento a obtener (base-1: 1 a size)
     * @return Valor almacenado en la posición especificada
     * @throws IndexOutOfBoundsException si el índice está fuera del rango válido
     */
    public int get(int index) {
        validateIndex(index);
        return data[index];
    }

    /**
     * Establece un valor en la posición especificada.
     *
     * @param index Índice donde almacenar el valor (base-1: 1 a size)
     * @param value Valor a almacenar
     * @throws IndexOutOfBoundsException si el índice está fuera del rango válido
     */
    public void set(int index, int value) {
        validateIndex(index);
        data[index] = value;
    }

    /**
     * Obtiene el tamaño lógico del vector.
     *
     * @return Número de elementos válidos en el vector
     */
    public int size() {
        return this.size;
    }

    // ========== MÉTODOS DE MANIPULACIÓN ==========

    /**
     * Copia los elementos de otro vector a este vector.
     *
     * Ambos vectores deben tener el mismo tamaño para que la operación sea válida.
     * Esta operación realiza una copia profunda de los valores.
     *
     * @param source Vector fuente del cual copiar los elementos
     * @throws IllegalArgumentException si source es null o tiene un tamaño
     *                                  diferente
     */
    public void copy(Vector source) {
        if (source == null) {
            throw new IllegalArgumentException("El vector fuente no puede ser null");
        }
        if (source.size() != this.size) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener el mismo tamaño. Actual: " + this.size +
                            ", Fuente: " + source.size());
        }

        for (int i = 1; i <= this.size; i++) {
            this.data[i] = source.get(i);
        }
    }

    /**
     * Crea una copia independiente de este vector.
     *
     * @return Nuevo vector con los mismos valores que este vector
     */
    public Vector clone() {
        Vector copy = new Vector(this.size);
        copy.copy(this);
        return copy;
    }

    /**
     * Llena el vector con un valor específico.
     *
     * @param value Valor con el que llenar todas las posiciones del vector
     */
    public void fill(int value) {
        for (int i = 1; i <= this.size; i++) {
            this.data[i] = value;
        }
    }

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Busca la primera ocurrencia de un valor en el vector.
     *
     * @param value Valor a buscar
     * @return Índice de la primera ocurrencia (base-1), o -1 si no se encuentra
     */
    public int indexOf(int value) {
        for (int i = 1; i <= this.size; i++) {
            if (this.data[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Verifica si el vector contiene un valor específico.
     *
     * @param value Valor a buscar
     * @return true si el vector contiene el valor, false en caso contrario
     */
    public boolean contains(int value) {
        return indexOf(value) != -1;
    }

    /**
     * Encuentra el valor máximo en el vector.
     *
     * @return Valor máximo, o Integer.MIN_VALUE si el vector está vacío lógicamente
     */
    public int max() {
        if (this.size == 0) {
            return Integer.MIN_VALUE;
        }

        int maximum = this.data[1];
        for (int i = 2; i <= this.size; i++) {
            if (this.data[i] > maximum) {
                maximum = this.data[i];
            }
        }
        return maximum;
    }

    /**
     * Encuentra el valor mínimo en el vector.
     *
     * @return Valor mínimo, o Integer.MAX_VALUE si el vector está vacío lógicamente
     */
    public int min() {
        if (this.size == 0) {
            return Integer.MAX_VALUE;
        }

        int minimum = this.data[1];
        for (int i = 2; i <= this.size; i++) {
            if (this.data[i] < minimum) {
                minimum = this.data[i];
            }
        }
        return minimum;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Valida que un índice esté dentro del rango válido del vector.
     *
     * @param index Índice a validar
     * @throws IndexOutOfBoundsException si el índice está fuera del rango válido
     */
    private void validateIndex(int index) {
        if (index < 1 || index > this.size) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de rango: " + index + " (rango válido: 1-" + this.size + ")");
        }
    }

    /**
     * Proporciona una representación en cadena del vector.
     * Los elementos se muestran separados por espacios, comenzando desde el índice
     * 1.
     *
     * @return Representación textual del vector
     */
    @Override
    public String toString() {
        if (this.size == 0) {
            return "[]";
        }

        StringBuilder result = new StringBuilder();
        result.append("[");
        for (int i = 1; i <= this.size; i++) {
            if (i > 1) {
                result.append(", ");
            }
            result.append(this.data[i]);
        }
        result.append("]");
        return result.toString();
    }

    /**
     * Compara este vector con otro objeto para determinar igualdad.
     *
     * @param obj Objeto a comparar
     * @return true si los objetos son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Vector other = (Vector) obj;
        if (this.size != other.size) {
            return false;
        }

        for (int i = 1; i <= this.size; i++) {
            if (this.data[i] != other.data[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calcula el código hash para este vector.
     *
     * @return Código hash basado en el tamaño y contenido del vector
     */
    @Override
    public int hashCode() {
        int hash = this.size;
        for (int i = 1; i <= this.size; i++) {
            hash = 31 * hash + this.data[i];
        }
        return hash;
    }
}
