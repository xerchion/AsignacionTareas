/**
 * Implementación de un vector de valores booleanos con indexación basada en 1.
 *
 * Esta clase implementa un vector especializado de booleanos que no utiliza el
 * índice 0,
 * comenzando la numeración desde 1. Está diseñado específicamente para el
 * algoritmo de
 * asignación de tareas donde se necesita trackear estados booleanos de agentes
 * y tareas
 * numerados desde 1, proporcionando una interfaz más natural para el dominio
 * del problema.
 *
 * Características principales:
 * - Indexación base-1 (índices válidos: 1 a size)
 * - Operaciones básicas de acceso y modificación para valores booleanos
 * - Copia profunda de vectores booleanos
 * - Representación textual para depuración
 * - Operaciones específicas para lógica booleana
 *
 * @author Pasteleros
 * @version 1.5
 * @since 1.0
 */
public final class VectorB {

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Array interno que almacena los datos del vector (índice 0 no utilizado) */
    private final boolean[] data;

    /** Tamaño lógico del vector (número de elementos válidos) */
    private final int size;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor que crea un vector booleano de tamaño especificado.
     *
     * El vector resultante tendrá índices válidos desde 1 hasta size (inclusive).
     * El índice 0 existe en el array interno pero no se utiliza.
     * Todos los valores se inicializan a false por defecto.
     *
     * @param size Número de elementos del vector. Debe ser mayor que 0.
     * @throws IllegalArgumentException si size es menor o igual a 0
     */
    public VectorB(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("El tamaño del vector debe ser mayor que 0");
        }

        // El array interno es size+1 ya que no usamos el índice 0
        this.data = new boolean[size + 1];
        this.size = size;
    }

    // ========== MÉTODOS PÚBLICOS DE ACCESO ==========

    /**
     * Obtiene el valor booleano almacenado en la posición especificada.
     *
     * @param index Índice del elemento a obtener (base-1: 1 a size)
     * @return Valor booleano almacenado en la posición especificada
     * @throws IndexOutOfBoundsException si el índice está fuera del rango válido
     */
    public boolean get(int index) {
        validateIndex(index);
        return data[index];
    }

    /**
     * Establece un valor booleano en la posición especificada.
     *
     * @param index Índice donde almacenar el valor (base-1: 1 a size)
     * @param value Valor booleano a almacenar
     * @throws IndexOutOfBoundsException si el índice está fuera del rango válido
     */
    public void set(int index, boolean value) {
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
     * Copia los elementos de otro vector booleano a este vector.
     *
     * Ambos vectores deben tener el mismo tamaño para que la operación sea válida.
     * Esta operación realiza una copia profunda de los valores.
     *
     * @param source Vector fuente del cual copiar los elementos
     * @throws IllegalArgumentException si source es null o tiene un tamaño
     *                                  diferente
     */
    public void copy(VectorB source) {
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
     * Crea una copia independiente de este vector booleano.
     *
     * @return Nuevo vector booleano con los mismos valores que este vector
     */
    public VectorB clone() {
        VectorB copy = new VectorB(this.size);
        copy.copy(this);
        return copy;
    }

    /**
     * Llena el vector con un valor booleano específico.
     *
     * @param value Valor booleano con el que llenar todas las posiciones del vector
     */
    public void fill(boolean value) {
        for (int i = 1; i <= this.size; i++) {
            this.data[i] = value;
        }
    }

    /**
     * Establece todos los valores del vector a false.
     */
    public void setAllFalse() {
        fill(false);
    }

    /**
     * Establece todos los valores del vector a true.
     */
    public void setAllTrue() {
        fill(true);
    }

    // ========== MÉTODOS DE CONSULTA ==========

    /**
     * Busca la primera ocurrencia de un valor booleano en el vector.
     *
     * @param value Valor booleano a buscar
     * @return Índice de la primera ocurrencia (base-1), o -1 si no se encuentra
     */
    public int indexOf(boolean value) {
        for (int i = 1; i <= this.size; i++) {
            if (this.data[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Verifica si el vector contiene al menos un valor true.
     *
     * @return true si hay al menos un elemento true, false en caso contrario
     */
    public boolean containsTrue() {
        return indexOf(true) != -1;
    }

    /**
     * Verifica si el vector contiene al menos un valor false.
     *
     * @return true si hay al menos un elemento false, false en caso contrario
     */
    public boolean containsFalse() {
        return indexOf(false) != -1;
    }

    /**
     * Verifica si todos los elementos del vector son true.
     *
     * @return true si todos los elementos son true, false en caso contrario
     */
    public boolean allTrue() {
        return !containsFalse();
    }

    /**
     * Verifica si todos los elementos del vector son false.
     *
     * @return true si todos los elementos son false, false en caso contrario
     */
    public boolean allFalse() {
        return !containsTrue();
    }

    /**
     * Cuenta el número de elementos con valor true.
     *
     * @return Número de elementos true en el vector
     */
    public int countTrue() {
        int count = 0;
        for (int i = 1; i <= this.size; i++) {
            if (this.data[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Cuenta el número de elementos con valor false.
     *
     * @return Número de elementos false en el vector
     */
    public int countFalse() {
        return this.size - countTrue();
    }

    // ========== OPERACIONES LÓGICAS ==========

    /**
     * Realiza una operación AND lógica elemento por elemento con otro vector.
     * Los resultados se almacenan en este vector.
     *
     * @param other Vector con el cual realizar la operación AND
     * @throws IllegalArgumentException si other es null o tiene tamaño diferente
     */
    public void and(VectorB other) {
        if (other == null) {
            throw new IllegalArgumentException("El vector no puede ser null");
        }
        if (other.size() != this.size) {
            throw new IllegalArgumentException("Los vectores deben tener el mismo tamaño");
        }

        for (int i = 1; i <= this.size; i++) {
            this.data[i] = this.data[i] && other.get(i);
        }
    }

    /**
     * Realiza una operación OR lógica elemento por elemento con otro vector.
     * Los resultados se almacenan en este vector.
     *
     * @param other Vector con el cual realizar la operación OR
     * @throws IllegalArgumentException si other es null o tiene tamaño diferente
     */
    public void or(VectorB other) {
        if (other == null) {
            throw new IllegalArgumentException("El vector no puede ser null");
        }
        if (other.size() != this.size) {
            throw new IllegalArgumentException("Los vectores deben tener el mismo tamaño");
        }

        for (int i = 1; i <= this.size; i++) {
            this.data[i] = this.data[i] || other.get(i);
        }
    }

    /**
     * Realiza una operación NOT lógica en todos los elementos del vector.
     */
    public void not() {
        for (int i = 1; i <= this.size; i++) {
            this.data[i] = !this.data[i];
        }
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

        VectorB other = (VectorB) obj;
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
     * Calcula el código hash para este vector booleano.
     *
     * @return Código hash basado en el tamaño y contenido del vector
     */
    @Override
    public int hashCode() {
        int hash = this.size;
        for (int i = 1; i <= this.size; i++) {
            hash = 31 * hash + (this.data[i] ? 1 : 0);
        }
        return hash;
    }
}
