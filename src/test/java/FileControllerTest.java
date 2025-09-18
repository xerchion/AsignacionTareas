import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class FileControllerTest {
    private String testFileName = "test_data.txt";

    @BeforeEach
    void setUp() throws IOException {
        // Crear archivo de prueba
        try (FileWriter writer = new FileWriter(testFileName)) {
            writer.write("2 2\n");
            writer.write("1 2\n");
            writer.write("3 4\n");
        }
    }

    @AfterEach
    void tearDown() {
        new File(testFileName).delete();
    }

    @Test
    void testConstructor() {
        FileController fc = new FileController("test.txt");
        assertNotNull(fc);
    }

    @Test
    void testGetData() throws IOException {
        FileController fc = new FileController(testFileName);
        GameData data = fc.getData();
        assertNotNull(data);
        assertEquals(1, data.get(1, 1));
        assertEquals(4, data.get(2, 2));
    }

    @Test
    void testSave() throws IOException {
        String saveFileName = "save_test.txt";
        FileController fc = new FileController(saveFileName);
        String testData = "Test output data";
        fc.save(testData);

        // Verificar que se creó el archivo
        File file = new File(saveFileName);
        assertTrue(file.exists());
        file.delete(); // Limpiar
    }
}

class FileControllerPropertyTest {
    @Property
    void testFileControllerWithRandomMatrices(@ForAll("validMatrixFiles") String content) {
        String fileName = "random_test.txt";
        try {
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(content);
            }

            FileController fc = new FileController(fileName);
            // Solo testear que no lance excepción al intentar leer
            assertDoesNotThrow(() -> {
                fc.getData();
            });

        } catch (IOException e) {
            // Ignorar errores de E/O en property tests
        } finally {
            new File(fileName).delete();
        }
    }

    @Provide
    Arbitrary<String> validMatrixFiles() {
        return Arbitraries.integers().between(1, 5)
                .flatMap(size -> Arbitraries.integers().between(1, 100)
                        .array(int[].class).ofSize(size)
                        .array(int[][].class).ofSize(size)
                        .map(matrix -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append(size).append(" ").append(size).append("\n");
                            for (int[] row : matrix) {
                                for (int i = 0; i < row.length; i++) {
                                    sb.append(row[i]);
                                    if (i < row.length - 1)
                                        sb.append(" ");
                                }
                                sb.append("\n");
                            }
                            return sb.toString();
                        }));
    }
}
