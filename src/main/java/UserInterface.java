import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Interfaz de usuario para el sistema de asignación de tareas usando
 * ramificación y poda.
 *
 * Esta clase gestiona toda la interacción con el usuario, incluyendo:
 * - Procesamiento de argumentos de línea de comandos
 * - Entrada manual de datos del problema
 * - Validación robusta de entradas
 * - Manejo de archivos de entrada y salida
 * - Presentación de resultados y trazas
 *
 * Responsabilidades principales:
 * - Orchestrar el flujo de entrada de datos (archivo vs. manual)
 * - Validar y sanitizar todas las entradas del usuario
 * - Formatear y presentar resultados de manera clara
 * - Manejar errores de entrada/salida de forma elegante
 *
 * Patrones implementados:
 * - Facade: Simplifica la interacción con múltiples subsistemas
 * - Strategy: Diferentes estrategias de entrada (archivo vs. manual)
 * - Template Method: Flujo estándar de ejecución con pasos personalizables
 *
 * @author Pasteleros
 * @version 2.0
 * @since 1.0
 */
public final class UserInterface {

    // ========== ATRIBUTOS PRIVADOS ==========

    /** Manejador especializado de argumentos de línea de comandos */
    private final ArgsHandler argsHandler;

    /** Datos del problema validados y listos para el algoritmo */
    private GameData gameData;

    /** Controlador para operaciones de lectura de archivos */
    private FileController inputFileController;

    /** Controlador para operaciones de escritura de archivos */
    private FileController outputFileController;

    /** Scanner para entrada de datos del usuario con configuración optimizada */
    private final Scanner userInputScanner;

    /** Indica si se debe generar y mostrar la traza del algoritmo */
    private final boolean traceEnabled;

    /** Contador de errores de validación para estadísticas */
    private int validationErrorCount = 0;

    /** Marcador de estado de inicialización exitosa */
    private boolean properlyInitialized = false;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor principal que inicializa la interfaz de usuario con validación
     * completa.
     *
     * @param args Argumentos de línea de comandos proporcionados por el usuario
     * @throws IllegalArgumentException si los argumentos son null
     * @throws RuntimeException         si ocurre un error durante la inicialización
     */
    public UserInterface(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("Los argumentos no pueden ser null");
        }

        try {
            // Inicializar manejador de argumentos
            this.argsHandler = new ArgsHandler(args);
            this.traceEnabled = this.argsHandler.wantTrace();

            // Configurar scanner con configuraciones robustas
            this.userInputScanner = new Scanner(System.in);
            configureScanner();

            // Validar argumentos de línea de comandos
            validateCommandLineArguments();

            this.properlyInitialized = true;

        } catch (Exception e) {
            throw new RuntimeException("Error durante la inicialización de la interfaz: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODO PRINCIPAL PÚBLICO ==========

    /**
     * Ejecuta el flujo principal completo de la interfaz de usuario.
     *
     * Este método orquesta todo el proceso:
     * 1. Verifica si el usuario solicita ayuda
     * 2. Determina la fuente de datos (archivo vs. entrada manual)
     * 3. Carga y valida los datos del problema
     * 4. Prepara la configuración para el algoritmo
     *
     * @throws RuntimeException si ocurre un error durante la ejecución
     */
    public void run() {
        try {
            validateInitialization();

            // === FASE 1: VERIFICAR SOLICITUD DE AYUDA ===
            if (this.argsHandler.needHelp()) {
                displayHelp();
                return; // Salir después de mostrar ayuda
            }

            // === FASE 2: DETERMINAR Y EJECUTAR ESTRATEGIA DE ENTRADA ===
            if (this.argsHandler.hasFileIn()) {
                loadDataFromFile();
            } else {
                loadDataFromUserInput();
            }

            // === FASE 3: VALIDACIÓN FINAL ===
            validateGameData();

        } catch (Exception e) {
            handleExecutionError(e);
        }
    }

    // ========== MÉTODOS DE CARGA DE DATOS ==========

    /**
     * Carga los datos del problema desde un archivo especificado.
     *
     * @throws RuntimeException si ocurre un error al leer el archivo
     */
    private void loadDataFromFile() {
        try {
            String fileName = this.argsHandler.getFileInName();
            validateFileName(fileName, "entrada");

            this.inputFileController = new FileController(fileName);
            this.gameData = this.inputFileController.getData();

            displayMessage(String.format("Datos cargados exitosamente desde: %s", fileName));

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar datos desde archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Solicita y recopila los datos del problema mediante entrada manual del
     * usuario.
     * Incluye validación completa y manejo de errores.
     *
     * @throws RuntimeException si ocurre un error durante la entrada de datos
     */
    private void loadDataFromUserInput() {
        try {
            displayMessage(Constants.DATA_INTRO_HEADER);

            // === ENTRADA DE DIMENSIONES ===
            int numAgents = promptForPositiveInteger(Constants.N_AGENTS_IN, "número de agentes");
            int numTasks = promptForPositiveInteger(Constants.N_TASKS_IN, "número de tareas");

            // === VALIDACIÓN DE DIMENSIONES ===
            validateProblemDimensions(numAgents, numTasks);

            // === ENTRADA DE MATRIZ DE COSTES ===
            int[][] costMatrix = collectCostMatrix(numAgents, numTasks);

            // === CREACIÓN Y VALIDACIÓN DE GAMEDATA ===
            this.gameData = new GameData(numAgents, numTasks, costMatrix);

            displayMessage("Datos ingresados exitosamente.");

        } catch (Exception e) {
            throw new RuntimeException("Error durante la entrada manual de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Recopila la matriz de costes mediante entrada del usuario con validación.
     *
     * @param numAgents Número de agentes
     * @param numTasks  Número de tareas
     * @return Matriz de costes validada
     */
    private int[][] collectCostMatrix(int numAgents, int numTasks) {
        int[][] costMatrix = new int[numAgents][numTasks];

        displayMessage(String.format("Ingrese los costes para %d agentes y %d tareas:", numAgents, numTasks));
        displayMessage("(Los costes deben ser números enteros no negativos)");

        for (int agentIndex = 0; agentIndex < numAgents; agentIndex++) {
            displayMessage(String.format("\n=== AGENTE %d ===", agentIndex + 1));

            for (int taskIndex = 0; taskIndex < numTasks; taskIndex++) {
                String prompt = String.format("  Coste para tarea %d: ", taskIndex + 1);
                String fieldDescription = String.format("coste del agente %d para la tarea %d",
                        agentIndex + 1, taskIndex + 1);

                costMatrix[agentIndex][taskIndex] = promptForNonNegativeInteger(prompt, fieldDescription);
            }
        }

        // Validar la matriz completa
        validateCostMatrix(costMatrix, numAgents, numTasks);

        return costMatrix;
    }

    // ========== MÉTODOS DE VALIDACIÓN ==========

    /**
     * Valida que la interfaz esté correctamente inicializada.
     *
     * @throws IllegalStateException si la interfaz no está inicializada
     */
    private void validateInitialization() {
        if (!this.properlyInitialized) {
            throw new IllegalStateException("La interfaz no está correctamente inicializada");
        }
    }

    /**
     * Valida los argumentos de línea de comandos.
     *
     * @throws IllegalArgumentException si los argumentos son inválidos
     */
    private void validateCommandLineArguments() {
        // La validación específica se delega al ArgsHandler
        // Aquí podríamos añadir validaciones adicionales si es necesario
        if (this.argsHandler == null) {
            throw new IllegalArgumentException("Error al procesar argumentos de línea de comandos");
        }
    }

    /**
     * Valida las dimensiones del problema.
     *
     * @param numAgents Número de agentes
     * @param numTasks  Número de tareas
     * @throws IllegalArgumentException si las dimensiones son inválidas
     */
    private void validateProblemDimensions(int numAgents, int numTasks) {
        if (numAgents != numTasks) {
            throw new IllegalArgumentException(
                    String.format("El número de agentes (%d) debe ser igual al número de tareas (%d)",
                            numAgents, numTasks));
        }

        if (numAgents > Constants.MAX_PROBLEM_SIZE) {
            throw new IllegalArgumentException(
                    String.format("El tamaño del problema (%d) excede el máximo permitido (%d)",
                            numAgents, Constants.MAX_PROBLEM_SIZE));
        }
    }

    /**
     * Valida la matriz de costes completa.
     *
     * @param matrix       Matriz a validar
     * @param expectedRows Número esperado de filas
     * @param expectedCols Número esperado de columnas
     * @throws IllegalArgumentException si la matriz es inválida
     */
    private void validateCostMatrix(int[][] matrix, int expectedRows, int expectedCols) {
        if (matrix == null) {
            throw new IllegalArgumentException("La matriz de costes no puede ser null");
        }

        if (matrix.length != expectedRows) {
            throw new IllegalArgumentException(
                    String.format("La matriz debe tener %d filas, pero tiene %d", expectedRows, matrix.length));
        }

        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException(String.format("La fila %d de la matriz no puede ser null", i));
            }

            if (matrix[i].length != expectedCols) {
                throw new IllegalArgumentException(
                        String.format("La fila %d debe tener %d columnas, pero tiene %d",
                                i, expectedCols, matrix[i].length));
            }

            // Validar valores no negativos
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] < 0) {
                    throw new IllegalArgumentException(
                            String.format("El coste en posición [%d][%d] no puede ser negativo: %d",
                                    i, j, matrix[i][j]));
                }
            }
        }
    }

    /**
     * Valida que el nombre de archivo sea válido.
     *
     * @param fileName Nombre del archivo
     * @param fileType Tipo de archivo para mensajes de error
     * @throws IllegalArgumentException si el nombre es inválido
     */
    private void validateFileName(String fileName, String fileType) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("El nombre del archivo de %s no puede estar vacío", fileType));
        }

        // Validaciones adicionales de nombre de archivo si es necesario
        if (fileName.length() > Constants.MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("El nombre del archivo de %s es demasiado largo (máximo %d caracteres)",
                            fileType, Constants.MAX_FILENAME_LENGTH));
        }
    }

    /**
     * Valida los datos finales del juego.
     *
     * @throws IllegalStateException si los datos son inválidos o están incompletos
     */
    private void validateGameData() {
        if (this.gameData == null) {
            throw new IllegalStateException("Los datos del juego no han sido cargados correctamente");
        }

        // Validaciones adicionales delegadas a GameData
        try {
            int agentsCount = this.gameData.getAgentsNumber();
            int tasksCount = this.gameData.getTasksNumber();

            if (agentsCount <= 0 || tasksCount <= 0) {
                throw new IllegalStateException("El número de agentes y tareas debe ser positivo");
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error al validar datos del juego: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS DE ENTRADA ROBUSTOS ==========

    /**
     * Solicita un número entero positivo al usuario con validación robusta.
     *
     * @param prompt           Mensaje que se muestra al usuario
     * @param fieldDescription Descripción del campo para mensajes de error
     * @return Número entero positivo válido
     */
    private int promptForPositiveInteger(String prompt, String fieldDescription) {
        while (true) {
            try {
                displayInLine(prompt);

                if (!this.userInputScanner.hasNextInt()) {
                    displayErrorMessage(String.format("Error: %s debe ser un número entero.", fieldDescription));
                    this.userInputScanner.next(); // Consumir entrada inválida
                    this.validationErrorCount++;
                    continue;
                }

                int value = this.userInputScanner.nextInt();

                if (value <= 0) {
                    displayErrorMessage(
                            String.format("Error: %s debe ser un número positivo (mayor que 0).", fieldDescription));
                    this.validationErrorCount++;
                    continue;
                }

                return value;

            } catch (InputMismatchException e) {
                displayErrorMessage(
                        String.format("Error: Entrada inválida para %s. Intente de nuevo.", fieldDescription));
                this.userInputScanner.next(); // Limpiar buffer
                this.validationErrorCount++;
            } catch (Exception e) {
                displayErrorMessage(String.format("Error inesperado al leer %s: %s", fieldDescription, e.getMessage()));
                this.validationErrorCount++;
            }
        }
    }

    /**
     * Solicita un número entero no negativo al usuario con validación robusta.
     *
     * @param prompt           Mensaje que se muestra al usuario
     * @param fieldDescription Descripción del campo para mensajes de error
     * @return Número entero no negativo válido
     */
    private int promptForNonNegativeInteger(String prompt, String fieldDescription) {
        while (true) {
            try {
                displayInLine(prompt);

                if (!this.userInputScanner.hasNextInt()) {
                    displayErrorMessage(String.format("Error: %s debe ser un número entero.", fieldDescription));
                    this.userInputScanner.next(); // Consumir entrada inválida
                    this.validationErrorCount++;
                    continue;
                }

                int value = this.userInputScanner.nextInt();

                if (value < 0) {
                    displayErrorMessage(String.format("Error: %s no puede ser negativo.", fieldDescription));
                    this.validationErrorCount++;
                    continue;
                }

                return value;

            } catch (InputMismatchException e) {
                displayErrorMessage(
                        String.format("Error: Entrada inválida para %s. Intente de nuevo.", fieldDescription));
                this.userInputScanner.next(); // Limpiar buffer
                this.validationErrorCount++;
            } catch (Exception e) {
                displayErrorMessage(String.format("Error inesperado al leer %s: %s", fieldDescription, e.getMessage()));
                this.validationErrorCount++;
            }
        }
    }

    // ========== MÉTODOS DE CONFIGURACIÓN ==========

    /**
     * Configura el scanner con parámetros optimizados para la aplicación.
     */
    private void configureScanner() {
        // El Scanner ya está inicializado, aquí podríamos configurar parámetros
        // adicionales
        // Por ejemplo, configurar el delimitador si fuera necesario
        // scanner.useDelimiter("\\s+");
    }

    // ========== MÉTODOS DE PRESENTACIÓN ==========

    /**
     * Muestra la ayuda del sistema al usuario.
     */
    private void displayHelp() {
        displayMessage(Constants.HELP);
        System.exit(0);
    }

    /**
     * Muestra un mensaje al usuario con formato estándar.
     *
     * @param message Mensaje a mostrar
     */
    private void displayMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            System.out.println("\n" + message);
        }
    }

    /**
     * Muestra un mensaje de error al usuario con formato distintivo.
     *
     * @param errorMessage Mensaje de error a mostrar
     */
    private void displayErrorMessage(String errorMessage) {
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            System.err.println("*** " + errorMessage + " ***");
        }
    }

    /**
     * Muestra un mensaje en la misma línea (sin salto de línea).
     *
     * @param message Mensaje a mostrar
     */
    private void displayInLine(String message) {
        if (message != null) {
            System.out.print(message);
        }
    }

    // ========== MÉTODOS DE MANEJO DE ERRORES ==========

    /**
     * Maneja errores durante la ejecución principal.
     *
     * @param error Error ocurrido
     */
    private void handleExecutionError(Exception error) {
        String errorMessage = String.format("Error durante la ejecución: %s",
                error.getMessage() != null ? error.getMessage() : "Error desconocido");

        displayErrorMessage(errorMessage);

        if (this.validationErrorCount > 0) {
            displayErrorMessage(String.format("Se produjeron %d errores de validación durante la sesión",
                    this.validationErrorCount));
        }

        // Log adicional para debugging si es necesario
        error.printStackTrace(System.err);

        throw new RuntimeException(errorMessage, error);
    }

    // ========== MÉTODOS PÚBLICOS DE ACCESO ==========

    /**
     * Obtiene los datos del juego recopilados y validados.
     *
     * @return GameData con los datos del problema
     * @throws IllegalStateException si los datos no han sido cargados
     */
    public GameData getGameData() {
        if (this.gameData == null) {
            throw new IllegalStateException("Los datos del juego no han sido cargados. Ejecute run() primero.");
        }
        return this.gameData;
    }

    /**
     * Indica si se debe generar la traza del algoritmo.
     *
     * @return true si la traza está habilitada
     */
    public boolean needTrace() {
        return this.traceEnabled;
    }

    /**
     * Obtiene el número de errores de validación ocurridos durante la sesión.
     *
     * @return Contador de errores de validación
     */
    public int getValidationErrorCount() {
        return this.validationErrorCount;
    }

    /**
     * Verifica si la interfaz está correctamente inicializada.
     *
     * @return true si está inicializada correctamente
     */
    public boolean isProperlyInitialized() {
        return this.properlyInitialized;
    }

    // ========== MÉTODOS DE PROCESAMIENTO DE RESULTADOS ==========

    /**
     * Procesa y presenta los resultados del algoritmo según la configuración.
     *
     * Maneja tanto salida a archivo como salida estándar, con soporte completo
     * para trazas y formateo apropiado.
     *
     * @param algorithmResult Resultado principal del algoritmo
     * @param algorithmTrace  Traza detallada del algoritmo (puede ser null)
     * @throws RuntimeException si ocurre un error durante el procesamiento
     */
    public void processResults(String algorithmResult, String algorithmTrace) {
        try {
            validateResultsInput(algorithmResult, algorithmTrace);

            if (this.argsHandler.hasFileOut()) {
                writeResultsToFile(algorithmResult, algorithmTrace);
            } else {
                displayResultsToConsole(algorithmResult, algorithmTrace);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar resultados: " + e.getMessage(), e);
        }
    }

    /**
     * Valida las entradas de resultados.
     *
     * @param result Resultado del algoritmo
     * @param trace  Traza del algoritmo
     * @throws IllegalArgumentException si las entradas son inválidas
     */
    private void validateResultsInput(String result, String trace) {
        if (result == null) {
            throw new IllegalArgumentException("El resultado del algoritmo no puede ser null");
        }

        if (this.traceEnabled && trace == null) {
            displayErrorMessage("Advertencia: Se solicitó traza pero no se proporcionó");
        }
    }

    /**
     * Escribe los resultados a un archivo de salida.
     *
     * @param result Resultado del algoritmo
     * @param trace  Traza del algoritmo
     * @throws RuntimeException si ocurre un error de escritura
     */
    private void writeResultsToFile(String result, String trace) {
        try {
            String fileName = this.argsHandler.getFileOutName();
            validateFileName(fileName, "salida");

            this.outputFileController = new FileController(fileName);

            StringBuilder output = new StringBuilder();

            // Añadir traza si está habilitada y disponible
            if (this.traceEnabled && trace != null && !trace.trim().isEmpty()) {
                output.append(trace);
                if (!trace.endsWith("\n")) {
                    output.append("\n");
                }
            }

            // Añadir resultado principal
            output.append(result);

            // Escribir al archivo
            this.outputFileController.save("\n" + output.toString());

            displayMessage(String.format("Resultados guardados exitosamente en: %s", fileName));

        } catch (Exception e) {
            throw new RuntimeException("Error al escribir resultados a archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Muestra los resultados en la consola.
     *
     * @param result Resultado del algoritmo
     * @param trace  Traza del algoritmo
     */
    private void displayResultsToConsole(String result, String trace) {
        try {
            // Mostrar traza si está habilitada y disponible
            if (this.traceEnabled && trace != null && !trace.trim().isEmpty()) {
                displayMessage("=== TRAZA DEL ALGORITMO ===");
                displayMessage(trace);
            }

            // Mostrar resultado principal
            displayMessage("=== RESULTADO ===");
            displayMessage(result);

        } catch (Exception e) {
            displayErrorMessage("Error al mostrar resultados: " + e.getMessage());
        }
    }

    // ========== MÉTODOS ESTÁTICOS DE UTILIDAD ==========

    /**
     * Método estático de conveniencia para mostrar mensajes.
     * Útil para situaciones donde no se necesita una instancia de la clase.
     *
     * @param message Mensaje a mostrar
     */
    public static void showMessage(String message) {
        if (message != null) {
            System.out.println(message);
        }
    }

    /**
     * Método estático de conveniencia para mostrar mensajes de error.
     *
     * @param errorMessage Mensaje de error a mostrar
     */
    public static void showErrorMessage(String errorMessage) {
        if (errorMessage != null) {
            System.err.println("*** " + errorMessage + " ***");
        }
    }

    // ========== MÉTODOS HEREDADOS PARA COMPATIBILIDAD ==========

    /**
     * Método heredado mantenido para compatibilidad con código existente.
     *
     * @deprecated Usar {@link #processResults(String, String)} en su lugar
     * @param result Resultado del juego
     * @param trace  Traza del juego
     */
    @Deprecated
    public void works(String result, String trace) {
        processResults(result, trace);
    }

    /**
     * Método heredado mantenido para compatibilidad.
     *
     * @deprecated Usar {@link #displayMessage(String)} en su lugar
     * @param text Mensaje a mostrar
     */
    @Deprecated
    public void show(String text) {
        displayMessage(text);
    }

    /**
     * Método heredado mantenido para compatibilidad.
     *
     * @deprecated Usar {@link #displayInLine(String)} en su lugar
     * @param text Mensaje a mostrar
     */
    @Deprecated
    public void showInLine(String text) {
        displayInLine(text);
    }

    /**
     * Método heredado mantenido para compatibilidad.
     *
     * @deprecated Usar {@link #showMessage(String)} en su lugar
     * @param msg Mensaje a mostrar
     */
    @Deprecated
    public static void showMsg(String msg) {
        showMessage(msg);
    }

    /**
     * Método heredado mantenido para compatibilidad.
     *
     * @deprecated Usar {@link #loadDataFromUserInput()} en su lugar
     * @return GameData con los datos del juego
     */
    @Deprecated
    public GameData introData() {
        try {
            loadDataFromUserInput();
            return this.gameData;
        } catch (Exception e) {
            throw new RuntimeException("Error durante introData: " + e.getMessage(), e);
        }
    }

    // ========== MÉTODOS DE LIMPIEZA ==========

    /**
     * Limpia recursos utilizados por la interfaz.
     * Debe llamarse al finalizar el uso de la interfaz.
     */
    public void cleanup() {
        try {
            if (this.userInputScanner != null) {
                this.userInputScanner.close();
            }
        } catch (Exception e) {
            displayErrorMessage("Advertencia: Error al cerrar scanner: " + e.getMessage());
        }
    }
}
