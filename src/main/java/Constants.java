/**
 * Clase que centraliza todas las constantes utilizadas en la aplicación de
 * gestión de tareas.
 * Esta clase define mensajes de error, textos de interfaz de usuario, versiones
 * y otros valores
 * constantes que se utilizan a lo largo de toda la aplicación.
 *
 * Organizada por módulos para facilitar el mantenimiento y la localización.
 *
 * @author Pasteleros
 * @version 1.5
 */
public final class Constants {

    /**
     * Constructor privado para evitar la instanciación de esta clase de utilidades.
     */
    private Constants() {
        throw new UnsupportedOperationException("Esta es una clase de utilidades y no debe ser instanciada");
    }

    // ========== CONFIGURACIÓN GENERAL ==========

    /** Versión actual de la aplicación */
    public static final double VERSION_APP = 1.5;

    /** Línea separadora utilizada en los headers de salida */
    public static final String LINE_HEADER = "\n-------------------------------------";

    /** Tamaño máximo permitido para problemas (número de agentes/tareas) */
    public static final int MAX_PROBLEM_SIZE = 50;

    /** Longitud máxima permitida para nombres de archivo */
    public static final int MAX_FILENAME_LENGTH = 255;

    // ========== MENSAJES GENERALES DE ERROR ==========

    /** Prefijo estándar para mensajes de error de la aplicación principal */
    public static final String ERR = "Ha ocurrido un error durante la ejecución: ";

    // ========== MENSAJES DE ArgsHandler ==========

    /**
     * Mensaje mostrado cuando no se proporcionan argumentos de línea de comandos
     */
    public static final String NO_ARGS = "No se pasaron argumentos.";

    // ========== MENSAJES Y TEXTOS DE UserInterface ==========

    /**
     * Texto de ayuda completo que se muestra al usuario con la sintaxis del
     * programa.
     * Incluye todas las opciones disponibles y sus descripciones.
     */
    public static final String HELP = "SINTAXIS: tareas [-t][-h] [fichero entrada]\n" +
            "-t                     Traza el algoritmo\n" +
            "-h                     Muestra esta ayuda\n" +
            "[fichero entrada]      Nombre del fichero de entrada\n" +
            "[fichero salida]       Nombre del fichero de salida";

    /** Header para la sección de configuración del tablero de entrada */
    public static final String DATA_INTRO_HEADER = "\nConfiguración del tablero:" + LINE_HEADER;

    /** Prompt para solicitar el número de agentes al usuario */
    public static final String N_AGENTS_IN = "Ingrese el nº de agentes : ";

    /** Prompt para solicitar el número de tareas al usuario */
    public static final String N_TASKS_IN = "Ingrese el nº de tareas : ";

    /** Mensaje de error para entrada de datos inválida */
    public static final String ASK_VALID_INT = "Por favor, introduce un número válido.";

    // ========== MENSAJES DE FileController ==========

    /** Error cuando el archivo de entrada está vacío */
    public static final String EMPTY_FILE = "El archivo está vacío.";

    /** Error cuando el formato de datos del archivo es incorrecto */
    public static final String NO_VALID_FORMAT = "Formato de datos en el archivo incorrecto.";

    /** Error cuando la primera línea no contiene exactamente dos números */
    public static final String ERR_DATA_SIZE = "La primera línea debe contener exactamente dos números.";

    /** Error cuando las dimensiones no son números enteros válidos */
    public static final String NO_VALID_DIM = "Las dimensiones de la matriz deben ser números enteros.";

    /** Error cuando faltan datos para completar la matriz esperada */
    public static final String MISSING_DATA = "Faltan datos para completar la matriz.";

    /** Prefijo para errores de lectura de archivo */
    public static final String READ_FILE_ERROR = "Error al leer el archivo: ";

    /** Prefijo para errores de escritura de archivo */
    public static final String WRITE_ERROR_FILE = "Error al escribir en el archivo: ";

    /** Error genérico para problemas inesperados al leer datos */
    public static final String DATA_ERROR = "Error inesperado al leer los datos: ";

    /** Error cuando el archivo no existe o tiene formato inválido */
    public static final String FILE_NO_EXISTS = "Error: El archivo está vacío o tiene formato inválido.";

    // ========== MENSAJES DE Game ==========

    /** Header para la sección de traza completa del algoritmo */
    public static final String TRACE_HEADER = "\nTraza completa:" + LINE_HEADER;
}