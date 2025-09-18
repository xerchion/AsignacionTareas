import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador de archivos responsable de la lectura y escritura de datos
 * para el sistema de gestión de tareas.
 *
 * Esta clase maneja la persistencia de datos en formato de archivos de texto
 * plano,
 * proporcionando operaciones de lectura para cargar matrices de tiempos de
 * tareas
 * y operaciones de escritura para guardar resultados del algoritmo.
 *
 * Formato de archivo esperado:
 * - Primera línea: [número_agentes] [número_tareas]
 * - Líneas siguientes: tiempos de cada agente para cada tarea
 *
 * Ejemplo:
 * 3 3
 * 1 2 3
 * 4 5 6
 * 7 8 9
 *
 * @author Pasteleros
 * @version 1.5
 * @since 1.0
 */
public final class FileController {

    // ========== CONSTANTES PRIVADAS ==========

    /** Expresión regular para dividir líneas por espacios en blanco */
    private static final String WHITESPACE_REGEX = "\\s+";

    /** Número esperado de dimensiones en la primera línea (agentes y tareas) */
    private static final int EXPECTED_DIMENSIONS = 2;

    // ========== ATRIBUTOS DE INSTANCIA ==========

    /** Ruta del archivo a procesar */
    private final String fileName;

    /** Objeto Path para operaciones de archivo más robustas */
    private final Path filePath;

    // ========== CONSTRUCTOR ==========

    /**
     * Constructor que inicializa el controlador con la ruta del archivo
     * especificado.
     *
     * @param fileName Ruta del archivo a procesar. No puede ser null ni estar
     *                 vacío.
     * @throws IllegalArgumentException si fileName es null o está vacío
     */
    public FileController(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede ser null o estar vacío");
        }

        this.fileName = fileName.trim();
        this.filePath = Paths.get(this.fileName);
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Lee y procesa el archivo completo para crear un objeto GameData.
     *
     * Este método coordina la lectura de las dimensiones y la matriz de datos,
     * manejando todos los errores posibles y proporcionando mensajes descriptivos.
     *
     * @return GameData con los datos leídos del archivo, o null si ocurre algún
     *         error
     */
    public GameData getData() {
        try {
            // Validar que el archivo existe y es legible
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                UserInterface.showMsg(Constants.READ_FILE_ERROR + "El archivo no existe o no es legible");
                return null;
            }

            // Obtener dimensiones de la primera línea
            int[] dimensions = parseFileDimensions();
            if (dimensions == null) {
                return null; // Error ya reportado en parseFileDimensions
            }

            int nAgents = dimensions[0];
            int nTasks = dimensions[1];

            // Leer matriz completa de datos
            int[][] timeMatrix = readTimeMatrix(nAgents, nTasks);
            if (timeMatrix == null) {
                return null; // Error ya reportado en readTimeMatrix
            }

            return new GameData(nAgents, nTasks, timeMatrix);

        } catch (Exception e) {
            UserInterface.showMsg(Constants.DATA_ERROR + e.getMessage());
            return null;
        }
    }

    /**
     * Guarda los datos proporcionados en el archivo especificado.
     *
     * @param data Contenido a escribir en el archivo. Si es null, se escribe una
     *             cadena vacía.
     */
    public void save(String data) {
        if (data == null) {
            data = "";
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName))) {
            writer.write(data);
            writer.flush(); // Asegurar que los datos se escriben inmediatamente
        } catch (IOException e) {
            UserInterface.showMsg(Constants.WRITE_ERROR_FILE + e.getMessage());
        }
    }

    // ========== MÉTODOS PRIVADOS DE PROCESAMIENTO ==========

    /**
     * Lee y parsea las dimensiones de la matriz desde la primera línea del archivo.
     *
     * @return Array con [número_agentes, número_tareas] o null si hay error
     */
    private int[] parseFileDimensions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String firstLine = reader.readLine();

            if (firstLine == null || firstLine.trim().isEmpty()) {
                UserInterface.showMsg(Constants.EMPTY_FILE);
                return null;
            }

            return parseDimensionLine(firstLine.trim());

        } catch (IOException e) {
            UserInterface.showMsg(Constants.READ_FILE_ERROR + e.getMessage());
            return null;
        }
    }

    /**
     * Parsea una línea para extraer las dimensiones de la matriz.
     *
     * @param line Línea a procesar
     * @return Array con las dimensiones o null si hay error
     */
    private int[] parseDimensionLine(String line) {
        String[] parts = line.split(WHITESPACE_REGEX);

        if (parts.length != EXPECTED_DIMENSIONS) {
            UserInterface.showMsg(Constants.ERR_DATA_SIZE);
            return null;
        }

        try {
            int agents = Integer.parseInt(parts[0]);
            int tasks = Integer.parseInt(parts[1]);

            // Validar que las dimensiones son positivas
            if (agents <= 0 || tasks <= 0) {
                UserInterface.showMsg(Constants.NO_VALID_DIM + " (deben ser números positivos)");
                return null;
            }

            return new int[] { agents, tasks };
        } catch (NumberFormatException e) {
            UserInterface.showMsg(Constants.NO_VALID_DIM);
            return null;
        }
    }

    /**
     * Lee la matriz completa de tiempos desde el archivo.
     *
     * @param nAgents Número de agentes (filas)
     * @param nTasks  Número de tareas (columnas)
     * @return Matriz de tiempos o null si hay error
     */
    private int[][] readTimeMatrix(int nAgents, int nTasks) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Saltar la primera línea (dimensiones)
            reader.readLine();

            int[][] matrix = new int[nAgents][nTasks];

            for (int agentIndex = 0; agentIndex < nAgents; agentIndex++) {
                String line = reader.readLine();

                if (line == null || line.trim().isEmpty()) {
                    UserInterface.showMsg(Constants.MISSING_DATA +
                            " (falta información para el agente " + (agentIndex + 1) + ")");
                    return null;
                }

                if (!parseAgentTimeLine(line.trim(), matrix[agentIndex], nTasks, agentIndex + 1)) {
                    return null; // Error ya reportado en parseAgentTimeLine
                }
            }

            return matrix;

        } catch (IOException e) {
            UserInterface.showMsg(Constants.READ_FILE_ERROR + e.getMessage());
            return null;
        }
    }

    /**
     * Parsea una línea de tiempos para un agente específico.
     *
     * @param line          Línea a procesar
     * @param agentTimes    Array donde almacenar los tiempos del agente
     * @param expectedTasks Número esperado de tareas
     * @param agentNumber   Número del agente (para mensajes de error)
     * @return true si el procesamiento fue exitoso, false en caso contrario
     */
    private boolean parseAgentTimeLine(String line, int[] agentTimes, int expectedTasks, int agentNumber) {
        String[] timeParts = line.split(WHITESPACE_REGEX);

        if (timeParts.length != expectedTasks) {
            UserInterface.showMsg("Error: El agente " + agentNumber +
                    " no tiene la cantidad esperada de tareas (" + expectedTasks + " esperadas, " +
                    timeParts.length + " encontradas)");
            return false;
        }

        try {
            for (int taskIndex = 0; taskIndex < expectedTasks; taskIndex++) {
                int time = Integer.parseInt(timeParts[taskIndex]);

                // Validar que los tiempos no sean negativos
                if (time < 0) {
                    UserInterface.showMsg("Error: El tiempo para el agente " + agentNumber +
                            ", tarea " + (taskIndex + 1) + " no puede ser negativo");
                    return false;
                }

                agentTimes[taskIndex] = time;
            }
            return true;
        } catch (NumberFormatException e) {
            UserInterface.showMsg("Error: Formato inválido en los tiempos del agente " +
                    agentNumber + ". Todos los valores deben ser números enteros.");
            return false;
        }
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Obtiene la ruta del archivo configurado.
     *
     * @return Ruta del archivo
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * Verifica si el archivo existe y es legible.
     *
     * @return true si el archivo existe y es legible, false en caso contrario
     */
    public boolean isFileAccessible() {
        return Files.exists(filePath) && Files.isReadable(filePath);
    }
}
