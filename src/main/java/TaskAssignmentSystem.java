import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Clase principal del sistema de asignación óptima de tareas usando
 * ramificación y poda.
 *
 * Esta es la clase de entrada (main) que orquesta todo el proceso de resolución
 * del problema
 * de asignación de tareas a agentes, minimizando el coste total mediante un
 * algoritmo
 * sofisticado de branch-and-bound.
 *
 * Funcionalidades principales:
 * - Inicialización y configuración del sistema completo
 * - Coordinación entre interfaz de usuario, algoritmo y presentación de
 * resultados
 * - Manejo robusto de errores con logging detallado
 * - Medición de rendimiento y estadísticas de ejecución
 * - Soporte para ejecución asíncrona y timeouts
 * - Validación integral del proceso de inicio a fin
 *
 * Arquitectura implementada:
 * - Patrón Facade: Simplifica la orquestación de múltiples subsistemas
 * - Patrón Command: Encapsula la ejecución del algoritmo
 * - Manejo de excepciones en cascada con recovery strategies
 * - Logging estructurado para debugging y auditoría
 *
 * Flujo de ejecución:
 * 1. Validación de entorno y configuración inicial
 * 2. Inicialización de la interfaz de usuario
 * 3. Carga y validación de datos del problema
 * 4. Creación y configuración del motor del algoritmo
 * 5. Ejecución del algoritmo con monitoreo
 * 6. Procesamiento y presentación de resultados
 * 7. Limpieza de recursos y finalización
 *
 * @author Pasteleros
 * @version 2.0
 * @since 1.0
 */
public final class TaskAssignmentSystem {

    // ========== CONSTANTES DE CONFIGURACIÓN ==========

    /** Tiempo máximo permitido para la ejecución del algoritmo (en segundos) */
    private static final int MAX_EXECUTION_TIME_SECONDS = 300; // 5 minutos

    /** Nombre del sistema para logging */
    private static final String SYSTEM_NAME = "Sistema de Asignación Óptima de Tareas";

    /** Versión del sistema */
    private static final String SYSTEM_VERSION = "v2.0";

    /** Formato para timestamps en logs */
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== VARIABLES DE ESTADO DEL SISTEMA ==========

    /** Tiempo de inicio de la ejecución */
    private static long systemStartTime;

    /** Contador de ejecuciones del sistema */
    private static int executionCounter = 0;

    /** Estado de éxito de la última ejecución */
    private static boolean lastExecutionSuccessful = false;

    // ========== MÉTODO PRINCIPAL ==========

    /**
     * Punto de entrada principal del sistema.
     *
     * Coordina todo el proceso de resolución desde la entrada de datos hasta la
     * presentación de resultados, con manejo robusto de errores y logging completo.
     *
     * @param args Argumentos de línea de comandos del usuario
     */
    public static void main(String[] args) {
        systemStartTime = System.currentTimeMillis();
        executionCounter++;

        logSystemStart(args);

        try {
            // === FASE 1: VALIDACIÓN DE ENTORNO ===
            validateExecutionEnvironment();

            // === FASE 2: INICIALIZACIÓN DEL SISTEMA ===
            SystemExecutionContext context = initializeSystem(args);

            // === FASE 3: EJECUCIÓN PRINCIPAL ===
            ExecutionResult result = executeMainProcess(context);

            // === FASE 4: FINALIZACIÓN EXITOSA ===
            finalizeExecution(context, result);

            lastExecutionSuccessful = true;
            logSystemSuccess(result);

        } catch (SystemInitializationException e) {
            handleInitializationError(e);
        } catch (AlgorithmExecutionException e) {
            handleAlgorithmError(e);
        } catch (Exception e) {
            handleUnexpectedError(e);
        } finally {
            performCleanup();
            logSystemEnd();
        }
    }

    // ========== MÉTODOS DE INICIALIZACIÓN ==========

    /**
     * Valida que el entorno de ejecución esté correctamente configurado.
     *
     * @throws SystemInitializationException si el entorno no es válido
     */
    private static void validateExecutionEnvironment() throws SystemInitializationException {
        try {
            // Validar versión de Java
            String javaVersion = System.getProperty("java.version");
            logInfo("Versión de Java detectada: " + javaVersion);

            // Validar memoria disponible
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();

            logInfo(String.format("Memoria - Máxima: %d MB, Total: %d MB, Libre: %d MB",
                    maxMemory / (1024 * 1024), totalMemory / (1024 * 1024), freeMemory / (1024 * 1024)));

            // Verificar espacio suficiente
            if (freeMemory < 10 * 1024 * 1024) { // Menos de 10 MB
                logWarning("Advertencia: Poca memoria libre disponible");
            }

        } catch (Exception e) {
            throw new SystemInitializationException("Error al validar entorno de ejecución", e);
        }
    }

    /**
     * Inicializa todos los componentes del sistema.
     *
     * @param args Argumentos de línea de comandos
     * @return Contexto de ejecución inicializado
     * @throws SystemInitializationException si falla la inicialización
     */
    private static SystemExecutionContext initializeSystem(String[] args) throws SystemInitializationException {
        try {
            logInfo("Iniciando componentes del sistema...");

            // Crear contexto de ejecución
            SystemExecutionContext context = new SystemExecutionContext();

            // Inicializar interfaz de usuario
            context.userInterface = new UserInterface(args);
            logInfo("Interfaz de usuario inicializada correctamente");

            // Ejecutar carga de datos
            context.userInterface.run();
            logInfo("Datos del problema cargados y validados");

            // Validar datos cargados
            validateLoadedData(context);

            return context;

        } catch (Exception e) {
            throw new SystemInitializationException("Error durante la inicialización del sistema", e);
        }
    }

    /**
     * Valida los datos cargados en el contexto.
     *
     * @param context Contexto de ejecución
     * @throws SystemInitializationException si los datos son inválidos
     */
    private static void validateLoadedData(SystemExecutionContext context) throws SystemInitializationException {
        try {
            GameData gameData = context.userInterface.getGameData();

            if (gameData == null) {
                throw new SystemInitializationException("No se pudieron cargar los datos del problema");
            }

            int problemSize = gameData.getAgentsNumber();
            logInfo(String.format("Problema cargado: %d agentes, %d tareas", problemSize, gameData.getTasksNumber()));

            if (problemSize > Constants.MAX_PROBLEM_SIZE) {
                throw new SystemInitializationException(
                        String.format("El tamaño del problema (%d) excede el límite máximo (%d)",
                                problemSize, Constants.MAX_PROBLEM_SIZE));
            }

        } catch (Exception e) {
            throw new SystemInitializationException("Error al validar datos cargados", e);
        }
    }

    // ========== MÉTODOS DE EJECUCIÓN PRINCIPAL ==========

    /**
     * Ejecuta el proceso principal del algoritmo con monitoreo completo.
     *
     * @param context Contexto de ejecución del sistema
     * @return Resultado de la ejecución
     * @throws AlgorithmExecutionException si falla la ejecución del algoritmo
     */
    private static ExecutionResult executeMainProcess(SystemExecutionContext context)
            throws AlgorithmExecutionException {
        try {
            logInfo("Iniciando ejecución del algoritmo de ramificación y poda...");

            // Crear el motor del algoritmo
            Game algorithmEngine = new Game(context.userInterface.getGameData(), context.userInterface.needTrace());

            // Ejecutar algoritmo con timeout
            ExecutionResult result = executeWithTimeout(algorithmEngine, context);

            logInfo(String.format("Algoritmo completado exitosamente en %d ms", result.executionTime));

            return result;

        } catch (Exception e) {
            throw new AlgorithmExecutionException("Error durante la ejecución del algoritmo", e);
        }
    }

    /**
     * Ejecuta el algoritmo con límite de tiempo.
     *
     * @param algorithmEngine Motor del algoritmo
     * @param context         Contexto de ejecución
     * @return Resultado de la ejecución
     * @throws AlgorithmExecutionException si la ejecución falla o supera el timeout
     */
    private static ExecutionResult executeWithTimeout(Game algorithmEngine, SystemExecutionContext context)
            throws AlgorithmExecutionException {
        try {
            CompletableFuture<ExecutionResult> algorithmFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    long startTime = System.currentTimeMillis();

                    // Ejecutar algoritmo
                    String algorithmResult = algorithmEngine.run();
                    String algorithmTrace = algorithmEngine.getTrace();

                    long endTime = System.currentTimeMillis();

                    // Crear resultado
                    ExecutionResult result = new ExecutionResult();
                    result.algorithmResult = algorithmResult;
                    result.algorithmTrace = algorithmTrace;
                    result.executionTime = endTime - startTime;
                    result.successful = true;

                    return result;

                } catch (Exception e) {
                    throw new RuntimeException("Error durante la ejecución del algoritmo", e);
                }
            });

            // Esperar resultado con timeout
            return algorithmFuture.get(MAX_EXECUTION_TIME_SECONDS, TimeUnit.SECONDS);

        } catch (java.util.concurrent.TimeoutException e) {
            throw new AlgorithmExecutionException(
                    String.format("El algoritmo superó el tiempo límite de %d segundos", MAX_EXECUTION_TIME_SECONDS),
                    e);
        } catch (Exception e) {
            throw new AlgorithmExecutionException("Error durante la ejecución con timeout", e);
        }
    }

    // ========== MÉTODOS DE FINALIZACIÓN ==========

    /**
     * Finaliza la ejecución procesando y presentando los resultados.
     *
     * @param context Contexto de ejecución
     * @param result  Resultado de la ejecución
     */
    private static void finalizeExecution(SystemExecutionContext context, ExecutionResult result) {
        try {
            logInfo("Procesando y presentando resultados...");

            // Procesar resultados a través de la interfaz de usuario
            context.userInterface.processResults(result.algorithmResult, result.algorithmTrace);

            // Mostrar estadísticas finales
            displayExecutionStatistics(result);

            logInfo("Resultados presentados exitosamente");

        } catch (Exception e) {
            logError("Error al finalizar ejecución: " + e.getMessage());
            // No relanzar la excepción para no ocultar el resultado principal
        }
    }

    /**
     * Muestra estadísticas detalladas de la ejecución.
     *
     * @param result Resultado de la ejecución
     */
    private static void displayExecutionStatistics(ExecutionResult result) {
        try {
            long totalSystemTime = System.currentTimeMillis() - systemStartTime;

            StringBuilder stats = new StringBuilder();
            stats.append("\n").append("=".repeat(50)).append("\n");
            stats.append("ESTADÍSTICAS DE EJECUCIÓN DEL SISTEMA\n");
            stats.append("=".repeat(50)).append("\n");
            stats.append(String.format("Tiempo total del sistema: %d ms\n", totalSystemTime));
            stats.append(String.format("Tiempo del algoritmo: %d ms\n", result.executionTime));
            stats.append(String.format("Overhead del sistema: %d ms (%.1f%%)\n",
                    totalSystemTime - result.executionTime,
                    ((double) (totalSystemTime - result.executionTime) / totalSystemTime) * 100));
            stats.append(String.format("Ejecución número: %d\n", executionCounter));
            stats.append(String.format("Estado: %s\n", result.successful ? "EXITOSO" : "FALLIDO"));
            stats.append("=".repeat(50)).append("\n");

            System.out.println(stats.toString());

        } catch (Exception e) {
            logError("Error al mostrar estadísticas: " + e.getMessage());
        }
    }

    // ========== MÉTODOS DE MANEJO DE ERRORES ==========

    /**
     * Maneja errores de inicialización del sistema.
     *
     * @param e Excepción de inicialización
     */
    private static void handleInitializationError(SystemInitializationException e) {
        lastExecutionSuccessful = false;

        logError("Error de inicialización del sistema: " + e.getMessage());

        System.err.println("\n" + "=".repeat(60));
        System.err.println("ERROR DE INICIALIZACIÓN DEL SISTEMA");
        System.err.println("=".repeat(60));
        System.err.println("El sistema no pudo inicializarse correctamente.");
        System.err.println("Detalles: " + e.getMessage());
        System.err.println("\nSugerencias:");
        System.err.println("- Verifique los argumentos de línea de comandos");
        System.err.println("- Asegúrese de que los archivos de entrada existan y sean válidos");
        System.err.println("- Compruebe que haya suficiente memoria disponible");
        System.err.println("=".repeat(60));

        if (e.getCause() != null) {
            System.err.println("\nCausa raíz: " + e.getCause().getMessage());
            e.getCause().printStackTrace();
        }

        System.exit(1);
    }

    /**
     * Maneja errores durante la ejecución del algoritmo.
     *
     * @param e Excepción de ejecución del algoritmo
     */
    private static void handleAlgorithmError(AlgorithmExecutionException e) {
        lastExecutionSuccessful = false;

        logError("Error en la ejecución del algoritmo: " + e.getMessage());

        System.err.println("\n" + "=".repeat(60));
        System.err.println("ERROR EN LA EJECUCIÓN DEL ALGORITMO");
        System.err.println("=".repeat(60));
        System.err.println("El algoritmo de ramificación y poda falló durante la ejecución.");
        System.err.println("Detalles: " + e.getMessage());
        System.err.println("\nSugerencias:");
        System.err.println("- Verifique que los datos del problema sean válidos");
        System.err.println("- Reduzca el tamaño del problema si es muy grande");
        System.err.println("- Asegúrese de que haya suficiente memoria disponible");
        System.err.println("=".repeat(60));

        if (e.getCause() != null) {
            System.err.println("\nCausa raíz: " + e.getCause().getMessage());
            e.getCause().printStackTrace();
        }

        System.exit(2);
    }

    /**
     * Maneja errores inesperados del sistema.
     *
     * @param e Excepción inesperada
     */
    private static void handleUnexpectedError(Exception e) {
        lastExecutionSuccessful = false;

        logError("Error inesperado del sistema: " + e.getMessage());

        System.err.println("\n" + "=".repeat(60));
        System.err.println("ERROR INESPERADO DEL SISTEMA");
        System.err.println("=".repeat(60));
        System.err.println("Se produjo un error inesperado durante la ejecución.");
        System.err.println("Detalles: " + e.getMessage());
        System.err.println("\nEsto puede indicar un problema en el código o una situación no prevista.");
        System.err.println("Por favor, reporte este error junto con la traza completa.");
        System.err.println("=".repeat(60));

        e.printStackTrace();

        System.exit(3);
    }

    // ========== MÉTODOS DE LIMPIEZA ==========

    /**
     * Realiza la limpieza de recursos del sistema.
     */
    private static void performCleanup() {
        try {
            logInfo("Realizando limpieza de recursos...");

            // Forzar garbage collection
            System.gc();

            logInfo("Limpieza completada");

        } catch (Exception e) {
            logError("Error durante la limpieza: " + e.getMessage());
        }
    }

    // ========== MÉTODOS DE LOGGING ==========

    /**
     * Registra el inicio del sistema.
     *
     * @param args Argumentos de línea de comandos
     */
    private static void logSystemStart(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(SYSTEM_NAME + " " + SYSTEM_VERSION);
        System.out.println("=".repeat(80));
        System.out.println("Inicio de ejecución: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("Argumentos: " + (args.length > 0 ? String.join(" ", args) : "ninguno"));
        System.out.println("Ejecución número: " + executionCounter);
        System.out.println("=".repeat(80));
    }

    /**
     * Registra información general del sistema.
     *
     * @param message Mensaje a registrar
     */
    private static void logInfo(String message) {
        System.out.println("[INFO] " + LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - " + message);
    }

    /**
     * Registra advertencias del sistema.
     *
     * @param message Mensaje de advertencia
     */
    private static void logWarning(String message) {
        System.out.println("[WARNING] " + LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - " + message);
    }

    /**
     * Registra errores del sistema.
     *
     * @param message Mensaje de error
     */
    private static void logError(String message) {
        System.err.println("[ERROR] " + LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - " + message);
    }

    /**
     * Registra el éxito del sistema.
     *
     * @param result Resultado de la ejecución
     */
    private static void logSystemSuccess(ExecutionResult result) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EJECUCIÓN COMPLETADA EXITOSAMENTE");
        System.out.println("=".repeat(80));
        System.out.println("Tiempo total: " + (System.currentTimeMillis() - systemStartTime) + " ms");
        System.out.println("Tiempo del algoritmo: " + result.executionTime + " ms");
        System.out.println("Finalización: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("=".repeat(80));
    }

    /**
     * Registra el final del sistema.
     */
    private static void logSystemEnd() {
        long totalTime = System.currentTimeMillis() - systemStartTime;
        String status = lastExecutionSuccessful ? "EXITOSO" : "FALLIDO";

        System.out.println("\n[INFO] " + LocalDateTime.now().format(TIMESTAMP_FORMAT) +
                " - Sistema finalizado. Estado: " + status + ", Tiempo total: " + totalTime + " ms");
    }

    // ========== CLASES INTERNAS DE SOPORTE ==========

    /**
     * Contexto de ejecución del sistema que mantiene el estado durante la
     * ejecución.
     */
    @SuppressWarnings("unused")
    private static class SystemExecutionContext {
        /** Interfaz de usuario inicializada */
        UserInterface userInterface;

        /** Datos del problema cargados */
        GameData gameData;

        /** Configuración de traza */
        boolean traceEnabled;

        /** Tiempo de inicio del contexto */
        long contextStartTime = System.currentTimeMillis();

        /**
         * Constructor por defecto.
         */
        SystemExecutionContext() {
            // Constructor vacío intencional
        }

        /**
         * Obtiene el tiempo transcurrido desde la creación del contexto.
         *
         * @return Tiempo transcurrido en milisegundos
         */
        long getElapsedTime() {
            return System.currentTimeMillis() - contextStartTime;
        }
    }

    /**
     * Resultado de la ejecución del algoritmo con metadatos completos.
     */
    private static class ExecutionResult {
        /** Resultado textual del algoritmo */
        String algorithmResult;

        /** Traza detallada del algoritmo */
        String algorithmTrace;

        /** Tiempo de ejecución en milisegundos */
        long executionTime;

        /** Indica si la ejecución fue exitosa */
        boolean successful = false;

        /** Timestamp de finalización */
        LocalDateTime completionTime = LocalDateTime.now();

        /**
         * Constructor por defecto.
         */
        ExecutionResult() {
            // Constructor vacío intencional
        }

        /**
         * Obtiene una representación textual del resultado.
         *
         * @return Descripción del resultado
         */
        @Override
        public String toString() {
            return String.format("ExecutionResult{successful=%s, executionTime=%d ms, completionTime=%s}",
                    successful, executionTime, completionTime.format(TIMESTAMP_FORMAT));
        }
    }

    // ========== EXCEPCIONES PERSONALIZADAS ==========

    /**
     * Excepción para errores durante la inicialización del sistema.
     */
    private static class SystemInitializationException extends Exception {
        /**
         * Constructor con mensaje.
         *
         * @param message Mensaje de error
         */
        SystemInitializationException(String message) {
            super(message);
        }

        /**
         * Constructor con mensaje y causa.
         *
         * @param message Mensaje de error
         * @param cause   Causa raíz
         */
        SystemInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Excepción para errores durante la ejecución del algoritmo.
     */
    @SuppressWarnings("unused")
    private static class AlgorithmExecutionException extends Exception {
        /**
         * Constructor con mensaje.
         *
         * @param message Mensaje de error
         */
        AlgorithmExecutionException(String message) {
            super(message);
        }

        /**
         * Constructor con mensaje y causa.
         *
         * @param message Mensaje de error
         * @param cause   Causa raíz
         */
        AlgorithmExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // ========== MÉTODOS DE UTILIDAD ESTÁTICOS ==========

    /**
     * Obtiene información del estado actual del sistema.
     *
     * @return Información del sistema
     */
    public static String getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        return String.format(
                "Sistema: %s %s | Java: %s | Memoria libre: %d MB | Ejecuciones: %d | Última ejecución: %s",
                SYSTEM_NAME, SYSTEM_VERSION,
                System.getProperty("java.version"),
                runtime.freeMemory() / (1024 * 1024),
                executionCounter,
                lastExecutionSuccessful ? "EXITOSA" : "FALLIDA");
    }

    /**
     * Verifica si el sistema está en un estado saludable.
     *
     * @return true si el sistema está saludable
     */
    public static boolean isSystemHealthy() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();

        // Considerar saludable si hay al menos 20% de memoria libre
        double memoryUsageRatio = (double) (totalMemory - freeMemory) / totalMemory;

        return memoryUsageRatio < 0.8 && lastExecutionSuccessful;
    }
}
