import static org.junit.jupiter.api.Assertions.*;
import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

class ArgsHandlerTest {
    @Test
    void testConstructor() {
        String[] args = { "-t", "input.txt", "output.txt" };
        ArgsHandler handler = new ArgsHandler(args);
        assertNotNull(handler);
    }

    @Test
    void testHasArgs() {
        String[] args = { "-t", "input.txt" };
        ArgsHandler handler = new ArgsHandler(args);
        assertTrue(handler.hasArgs());

        String[] emptyArgs = {};
        ArgsHandler emptyHandler = new ArgsHandler(emptyArgs);
        assertFalse(emptyHandler.hasArgs());
    }

    @Test
    void testWantTrace() {
        String[] argsWithTrace = { "-t", "input.txt" };
        ArgsHandler handlerTrace = new ArgsHandler(argsWithTrace);
        assertTrue(handlerTrace.wantTrace());

        String[] argsNoTrace = { "input.txt" };
        ArgsHandler handlerNoTrace = new ArgsHandler(argsNoTrace);
        assertFalse(handlerNoTrace.wantTrace());
    }

    @Test
    void testNeedHelp() {
        String[] helpArgs = { "-h" };
        ArgsHandler helpHandler = new ArgsHandler(helpArgs);
        assertTrue(helpHandler.needHelp());

        String[] normalArgs = { "input.txt" };
        ArgsHandler normalHandler = new ArgsHandler(normalArgs);
        assertFalse(normalHandler.needHelp());
    }

    @Test
    void testFileHandling() {
        String[] args = { "-t", "input.txt", "output.txt" };
        ArgsHandler handler = new ArgsHandler(args);

        assertTrue(handler.hasFileIn());
        assertTrue(handler.hasFileOut());
        assertEquals("input.txt", handler.getFileInName());
        assertEquals("output.txt", handler.getFileOutName());
    }

    @Test
    void testGetMessage() {
        String[] args = { "-t", "input.txt" };
        ArgsHandler handler = new ArgsHandler(args);
        String message = handler.getMessage();
        assertNotNull(message);
    }
}

class ArgsHandlerPropertyTest {
    @Property
    void testArgsHandlerWithRandomArgs(@ForAll("randomArgs") String[] args) {
        ArgsHandler handler = new ArgsHandler(args);
        assertNotNull(handler);

        // Verificar que hasArgs() es consistente con args.length
        assertEquals(args.length > 0, handler.hasArgs());

        // El mensaje nunca debe ser null
        assertNotNull(handler.getMessage());
    }

    @Property
    void testTraceDetection(@ForAll("argsWithOptionalTrace") String[] args) {
        ArgsHandler handler = new ArgsHandler(args);

        boolean hasTraceFlag = false;
        for (String arg : args) {
            if ("-t".equals(arg)) {
                hasTraceFlag = true;
                break;
            }
        }

        assertEquals(hasTraceFlag, handler.wantTrace());
    }

    @Provide
    Arbitrary<String[]> randomArgs() {
        return Arbitraries.of("", "-t", "-h", "input.txt", "output.txt", "file.dat")
                .array(String[].class)
                .ofMinSize(0)
                .ofMaxSize(5);
    }

    @Provide
    Arbitrary<String[]> argsWithOptionalTrace() {
        return Arbitraries.oneOf(
                Arbitraries.just(new String[] {}),
                Arbitraries.just(new String[] { "-t" }),
                Arbitraries.just(new String[] { "-t", "input.txt" }),
                Arbitraries.just(new String[] { "input.txt" }),
                Arbitraries.just(new String[] { "-h" }),
                Arbitraries.just(new String[] { "-t", "input.txt", "output.txt" }));
    }
}
