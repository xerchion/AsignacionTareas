/**
 * Clase encargada de manejar y validar los argumentos de línea de comandos
 * para configurar la ejecución del programa de gestión de tareas.
 *
 * Esta clase procesa automáticamente los argumentos del programa y determina:
 * - Si se debe mostrar la ayuda (-h)
 * - Si se debe generar traza del algoritmo (-t)
 * - Los archivos de entrada y salida especificados
 *
 * Sintaxis soportada: tareas [-t] [-h] [fichero_entrada] [fichero_salida]
 *
 * @author Pasteleros
 * @version 1.5
 * @since 1.0
 */
public final class ArgsHandler {

    // ========== CONSTANTES PRIVADAS ==========

    /** Argumento para solicitar ayuda */
    private static final String HELP_ARG = "-h";

    /** Argumento para habilitar la traza del algoritmo */
    private static final String TRACE_ARG = "-t";

    // ========== ATRIBUTOS DE INSTANCIA ==========

    /** Número total de argumentos pasados al programa */
    private final int nArgs;

    /** Array inmutable con los argumentos de línea de comandos originales */
    private final String[] args;

    /** Mensaje descriptivo sobre el estado actual de los argumentos procesados */
    private String message = "";

    /** Indica si el usuario ha solicitado generar la traza del algoritmo */
    private boolean trace = false;

    /** Ruta del archivo de entrada de datos, null si no se especifica */
    private String fileIn;

    /** Ruta del archivo de salida para los resultados, null si no se especifica */
    private String fileOut;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor principal que procesa automáticamente los argumentos de línea de
     * comandos.
     *
     * Al crear la instancia, se analizan todos los argumentos proporcionados para:
     * - Detectar opciones de configuración (-t, -h)
     * - Identificar archivos de entrada y salida
     * - Validar la sintaxis de los argumentos
     *
     * @param args Array de argumentos de línea de comandos proporcionados por el
     *             usuario.
     *             No puede ser null, pero puede estar vacío.
     * @throws IllegalArgumentException si args es null
     */
    public ArgsHandler(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("El array de argumentos no puede ser null");
        }

        this.nArgs = args.length;
        this.args = args.clone(); // Clonamos para inmutabilidad

        if (this.hasArgs()) {
            this.findTrace();
            this.solveFiles();
        }
    }

    // ========== MÉTODOS PÚBLICOS DE CONSULTA ==========

    /**
     * Verifica si se han proporcionado argumentos al programa.
     *
     * @return true si hay al menos un argumento, false en caso contrario
     */
    public boolean hasArgs() {
        if (this.nArgs > 0) {
            return true;
        } else {
            this.message = Constants.NO_ARGS;
            return false;
        }
    }

    /**
     * Determina si el usuario ha solicitado mostrar la ayuda del programa.
     *
     * La ayuda se solicita con el argumento "-h" como primer parámetro.
     *
     * @return true si se debe mostrar la ayuda, false en caso contrario
     */
    public boolean needHelp() {
        return this.hasArgs() && HELP_ARG.equals(this.args[0]);
    }

    /**
     * Obtiene el mensaje descriptivo actual sobre el estado de los argumentos.
     *
     * Este mensaje puede contener información sobre errores de validación
     * o el estado actual del procesamiento de argumentos.
     *
     * @return Mensaje de estado actual, cadena vacía si no hay mensajes
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Verifica si se ha especificado un archivo de salida.
     *
     * @return true si hay un archivo de salida configurado, false en caso contrario
     */
    public boolean hasFileOut() {
        return this.fileOut != null && !this.fileOut.trim().isEmpty();
    }

    /**
     * Verifica si se ha especificado un archivo de entrada.
     *
     * @return true si hay un archivo de entrada configurado, false en caso
     *         contrario
     */
    public boolean hasFileIn() {
        return this.fileIn != null && !this.fileIn.trim().isEmpty();
    }

    /**
     * Obtiene la ruta del archivo de entrada especificado.
     *
     * @return Ruta del archivo de entrada, o null si no se especificó
     */
    public String getFileInName() {
        return this.fileIn;
    }

    /**
     * Obtiene la ruta del archivo de salida especificado.
     *
     * @return Ruta del archivo de salida, o null si no se especificó
     */
    public String getFileOutName() {
        return this.fileOut;
    }

    /**
     * Indica si se ha solicitado generar la traza del algoritmo.
     *
     * La traza se habilita con el argumento "-t" como primer parámetro.
     *
     * @return true si se debe generar traza, false en caso contrario
     */
    public boolean wantTrace() {
        return this.trace;
    }

    // ========== MÉTODOS PRIVADOS DE PROCESAMIENTO ==========

    /**
     * Analiza los argumentos para determinar si se ha solicitado la traza del
     * algoritmo.
     *
     * La opción de traza (-t) debe ser el primer argumento si está presente.
     * Actualiza el estado interno de la instancia.
     */
    private void findTrace() {
        if (this.nArgs > 0 && TRACE_ARG.equals(this.args[0])) {
            this.trace = true;
        }
    }

    /**
     * Procesa y asigna los archivos de entrada y salida según los argumentos
     * restantes.
     *
     * El orden de procesamiento es:
     * 1. Si hay traza (-t), los archivos empiezan en el índice 1
     * 2. El primer archivo disponible es el de entrada
     * 3. El segundo archivo disponible es el de salida
     *
     * Este método actualiza los campos fileIn y fileOut según corresponda.
     */
    private void solveFiles() {
        int index = this.trace ? 1 : 0; // Si hay traza, los archivos empiezan en el índice 1

        // Procesar archivo de entrada
        if (this.nArgs > index) {
            String potentialFileIn = this.args[index].trim();
            if (!potentialFileIn.isEmpty()) {
                this.fileIn = potentialFileIn;
                index++;
            }
        }

        // Procesar archivo de salida
        if (this.nArgs > index) {
            String potentialFileOut = this.args[index].trim();
            if (!potentialFileOut.isEmpty()) {
                this.fileOut = potentialFileOut;
            }
        }
    }
}
