/**
 * Implementación de una matriz bidimensional de enteros con indexación
 * consistente basada en 1.
 *
 * Esta clase representa una matriz de enteros optimizada para el algoritmo de
 * asignación
 * de tareas. Utiliza indexación base-1 para mantener consistencia con el
 * dominio del
 * problema donde tanto agentes como tareas se numeran desde 1.
 *
 * Características principales:
 * - Indexación base-1 consistente (filas y columnas: 1 a size)
 * - Inmutabilidad en dimensiones tras construcción
 * - Validación robusta de parámetros
 * - Operaciones optimizadas para matrices de costos
 * - Representación textual para depuración
 *
 * @author Pasteleros
 * @version 2.0
 * @since 1.0
 */
public final class Matrix {

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Número de filas de la matriz */
    private final int rows;

    /** Número de columnas de la matriz */
    private final int cols;

    /** Datos internos de la matriz (índices 0 no utilizados) */
    private final int[][] data;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor que crea una matriz con dimensiones específicas y datos
     * proporcionados.
     *
     * La matriz resultante tendrá índices válidos desde (1,1) hasta (rows,cols).
     * Los datos deben estar en formato base-0 pero serán accedidos con índices
     * base-1.
     *
     * @param rows       Número de filas. Debe ser mayor que 0.
     * @param cols       Número de columnas. Debe ser mayor que 0.
     * @param sourceData Datos fuente en formato bidimensional [rows][cols]
     * @throws IllegalArgumentException si las dimensiones son inválidas o los datos
     *                                  son null
     */
    public Matrix(int rows, int cols, int[][] sourceData) {
        validateDimensions(rows, cols);
        validateSourceData(sourceData, rows, cols);

        this.rows = rows;
        this.cols = cols;

        // Creamos la matriz interna con espacio extra para evitar índice 0
        this.data = new int[rows + 1][cols + 1];

        // Copiamos los datos fuente a posiciones base-1
        copySourceData(sourceData);
    }

    /**
     * Constructor que crea una matriz vacía con dimensiones específicas.
     * Todos los elementos se inicializan a 0.
     *
     * @param rows Número de filas. Debe ser mayor que 0.
     * @param cols Número de columnas. Debe ser mayor que 0.
     * @throws IllegalArgumentException si las dimensiones son inválidas
     */
    public Matrix(int rows, int cols) {
        validateDimensions(rows, cols);

        this.rows = rows;
        this.cols = cols;
        this.data = new int[rows + 1][cols + 1];
    }

    // ========== MÉTODOS PÚBLICOS DE ACCESO ==========

    /**
     * Obtiene el valor en la posición especificada usando indexación base-1.
     *
     * @param row Fila del elemento (base-1: 1 a rows)
     * @param col Columna del elemento (base-1: 1 a cols)
     * @return Valor almacenado en la posición especificada
     * @throws IndexOutOfBoundsException si los índices están fuera del rango válido
     */
    public int get(int row, int col) {
        validateIndices(row, col);
        return data[row][col];
    }

    /**
     * Establece un valor en la posición especificada usando indexación base-1.
     *
     * @param row   Fila donde almacenar el valor (base-1: 1 a rows)
     * @param col   Columna donde almacenar el valor (base-1: 1 a cols)
     * @param value Valor a almacenar
     * @throws IndexOutOfBoundsException si los índices están fuera del rango válido
     */
    public void set(int row, int col, int value) {
        validateIndices(row, col);
        data[row][col] = value;
    }

    /**
     * Obtiene el número de filas de la matriz.
     *
     * @return Número de filas
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Obtiene el número de columnas de la matriz.
     *
     * @return Número de columnas
     */
    public int getCols() {
        return this.cols;
    }

    /**
     * Verifica si la matriz es cuadrada.
     *
     * @return true si rows == cols, false en caso contrario
     */
    public boolean isSquare() {
        return this.rows == this.cols;
    }

    // ========== MÉTODOS DE MANIPULACIÓN ==========

    /**
     * Crea una copia profunda de esta matriz.
     *
     * @return Nueva matriz con los mismos valores que esta matriz
     */
    public Matrix clone() {
        Matrix copy = new Matrix(this.rows, this.cols);
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                copy.set(i, j, this.get(i, j));
            }
        }
        return copy;
    }

    /**
     * Llena toda la matriz con un valor específico.
     *
     * @param value Valor con el que llenar la matriz
     */
    public void fill(int value) {
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                this.data[i][j] = value;
            }
        }
    }

    /**
     * Llena una fila específica con un valor.
     *
     * @param row   Fila a llenar (base-1: 1 a rows)
     * @param value Valor con el que llenar la fila
     * @throws IndexOutOfBoundsException si row está fuera del rango válido
     */
    public void fillRow(int row, int value) {
        validateRowIndex(row);
        for (int j = 1; j <= this.cols; j++) {
            this.data[row][j] = value;
        }
    }

    /**
     * Llena una columna específica con un valor.
     *
     * @param col   Columna a llenar (base-1: 1 a cols)
     * @param value Valor con el que llenar la columna
     * @throws IndexOutOfBoundsException si col está fuera del rango válido
     */
    public void fillCol(int col, int value) {
        validateColIndex(col);
        for (int i = 1; i <= this.rows; i++) {
            this.data[i][col] = value;
        }
    }

    // ========== MÉTODOS DE CONSULTA Y ANÁLISIS ==========

    /**
     * Encuentra el valor mínimo en toda la matriz.
     *
     * @return Valor mínimo encontrado
     */
    public int getMin() {
        int min = this.data[1][1];
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                if (this.data[i][j] < min) {
                    min = this.data[i][j];
                }
            }
        }
        return min;
    }

    /**
     * Encuentra el valor máximo en toda la matriz.
     *
     * @return Valor máximo encontrado
     */
    public int getMax() {
        int max = this.data[1][1];
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                if (this.data[i][j] > max) {
                    max = this.data[i][j];
                }
            }
        }
        return max;
    }

    /**
     * Encuentra el valor mínimo en una fila específica.
     *
     * @param row Fila a examinar (base-1: 1 a rows)
     * @return Valor mínimo en la fila
     * @throws IndexOutOfBoundsException si row está fuera del rango válido
     */
    public int getRowMin(int row) {
        validateRowIndex(row);
        int min = this.data[row][1];
        for (int j = 2; j <= this.cols; j++) {
            if (this.data[row][j] < min) {
                min = this.data[row][j];
            }
        }
        return min;
    }

    /**
     * Encuentra el valor mínimo en una columna específica.
     *
     * @param col Columna a examinar (base-1: 1 a cols)
     * @return Valor mínimo en la columna
     * @throws IndexOutOfBoundsException si col está fuera del rango válido
     */
    public int getColMin(int col) {
        validateColIndex(col);
        int min = this.data[1][col];
        for (int i = 2; i <= this.rows; i++) {
            if (this.data[i][col] < min) {
                min = this.data[i][col];
            }
        }
        return min;
    }

    /**
     * Calcula la suma de todos los elementos de la matriz.
     *
     * @return Suma total de todos los elementos
     */
    public long getSum() {
        long sum = 0;
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                sum += this.data[i][j];
            }
        }
        return sum;
    }

    // ========== MÉTODOS DE EXPORTACIÓN ==========

    /**
     * Obtiene una copia de los datos de la matriz en formato base-0 estándar.
     *
     * @return Array bidimensional con los datos de la matriz [0..rows-1][0..cols-1]
     */
    public int[][] toArray() {
        int[][] result = new int[this.rows][this.cols];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                result[i][j] = this.data[i + 1][j + 1];
            }
        }
        return result;
    }

    /**
     * Obtiene una fila específica como array.
     *
     * @param row Fila a extraer (base-1: 1 a rows)
     * @return Array con los valores de la fila [0..cols-1]
     * @throws IndexOutOfBoundsException si row está fuera del rango válido
     */
    public int[] getRowAsArray(int row) {
        validateRowIndex(row);
        int[] result = new int[this.cols];
        for (int j = 0; j < this.cols; j++) {
            result[j] = this.data[row][j + 1];
        }
        return result;
    }

    /**
     * Obtiene una columna específica como array.
     *
     * @param col Columna a extraer (base-1: 1 a cols)
     * @return Array con los valores de la columna [0..rows-1]
     * @throws IndexOutOfBoundsException si col está fuera del rango válido
     */
    public int[] getColAsArray(int col) {
        validateColIndex(col);
        int[] result = new int[this.rows];
        for (int i = 0; i < this.rows; i++) {
            result[i] = this.data[i + 1][col];
        }
        return result;
    }

    // ========== MÉTODOS PRIVADOS DE VALIDACIÓN ==========

    /**
     * Valida que las dimensiones sean válidas.
     */
    private void validateDimensions(int rows, int cols) {
        if (rows <= 0) {
            throw new IllegalArgumentException("El número de filas debe ser mayor que 0");
        }
        if (cols <= 0) {
            throw new IllegalArgumentException("El número de columnas debe ser mayor que 0");
        }
    }

    /**
     * Valida que los datos fuente sean válidos.
     */
    private void validateSourceData(int[][] sourceData, int expectedRows, int expectedCols) {
        if (sourceData == null) {
            throw new IllegalArgumentException("Los datos fuente no pueden ser null");
        }
        if (sourceData.length != expectedRows) {
            throw new IllegalArgumentException(
                    "El número de filas no coincide. Esperado: " + expectedRows +
                            ", Actual: " + sourceData.length);
        }
        for (int i = 0; i < expectedRows; i++) {
            if (sourceData[i] == null || sourceData[i].length != expectedCols) {
                throw new IllegalArgumentException(
                        "Fila " + i + " tiene longitud incorrecta. Esperado: " + expectedCols +
                                ", Actual: " + (sourceData[i] == null ? "null" : sourceData[i].length));
            }
        }
    }

    /**
     * Copia los datos fuente a la matriz interna.
     */
    private void copySourceData(int[][] sourceData) {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                this.data[i + 1][j + 1] = sourceData[i][j];
            }
        }
    }

    /**
     * Valida que los índices estén dentro del rango válido.
     */
    private void validateIndices(int row, int col) {
        validateRowIndex(row);
        validateColIndex(col);
    }

    /**
     * Valida que el índice de fila esté dentro del rango válido.
     */
    private void validateRowIndex(int row) {
        if (row < 1 || row > this.rows) {
            throw new IndexOutOfBoundsException(
                    "Índice de fila fuera de rango: " + row + " (rango válido: 1-" + this.rows + ")");
        }
    }

    /**
     * Valida que el índice de columna esté dentro del rango válido.
     */
    private void validateColIndex(int col) {
        if (col < 1 || col > this.cols) {
            throw new IndexOutOfBoundsException(
                    "Índice de columna fuera de rango: " + col + " (rango válido: 1-" + this.cols + ")");
        }
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Proporciona una representación en cadena de la matriz.
     *
     * @return Representación textual de la matriz con formato tabular
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Matrix[").append(this.rows).append("x").append(this.cols).append("]:\n");

        for (int i = 1; i <= this.rows; i++) {
            result.append("[");
            for (int j = 1; j <= this.cols; j++) {
                if (j > 1) {
                    result.append(", ");
                }
                result.append(String.format("%4d", this.data[i][j]));
            }
            result.append("]\n");
        }

        return result.toString();
    }

    /**
     * Compara esta matriz con otro objeto para determinar igualdad.
     *
     * @param obj Objeto a comparar
     * @return true si las matrices son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Matrix other = (Matrix) obj;
        if (this.rows != other.rows || this.cols != other.cols) {
            return false;
        }

        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                if (this.data[i][j] != other.data[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calcula el código hash para esta matriz.
     *
     * @return Código hash basado en dimensiones y contenido
     */
    @Override
    public int hashCode() {
        int hash = 31 * this.rows + this.cols;
        for (int i = 1; i <= this.rows; i++) {
            for (int j = 1; j <= this.cols; j++) {
                hash = 31 * hash + this.data[i][j];
            }
        }
        return hash;
    }
}
