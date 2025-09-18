import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UserInterfaceTest {
    @Test
    void testConstructor() {
        String[] args = { "-t", "input.txt", "output.txt" };
        UserInterface ui = new UserInterface(args);
        assertNotNull(ui);
    }
}
