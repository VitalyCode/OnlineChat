import org.junit.jupiter.api.Test;
import java.net.Socket;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerTest {

    private static final int TEST_PORT = 25566;

    @Test
    void testLoadSettings_validFile() throws IOException {

        File tempFile = File.createTempFile("test_settings", ".txt");

        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("port=25567\n");
        }

        ServerStart.loadSettings(tempFile.getAbsolutePath());

        assertEquals(25565, ServerStart.getPort());
    }

    @Test
    void testLoadSettings_invalidFile() throws IOException {

        ServerStart.loadSettings("nonexistent_file.txt");

        assertEquals(25565, ServerStart.getPort());
    }

    @Test
    void testLoadSettings_invalidPortFormat() throws IOException {

        File tempFile = File.createTempFile("test_settings", ".txt");

        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {

            writer.write("port=abc\n");
        }

        ServerStart.loadSettings(tempFile.getAbsolutePath());
        assertEquals(25566, ServerStart.getPort());
    }
    @Test
    void testLogMessage() throws IOException {

        File tempLogFile = File.createTempFile("test_log", ".log");

        tempLogFile.deleteOnExit();

        String testMessage = "Test log message";

        ServerStart.logMessage(testMessage, tempLogFile.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(tempLogFile))) {

            String line = reader.readLine();

            assertNotNull(line);

            assertTrue(line.contains(testMessage));
        }
    }

    @Nested
    @DisplayName("ClientHandler Tests")
    class ClientHandlerTests {

        @Test
        void testClientHandler_sendMessage() throws IOException, InterruptedException {

            ServerSocket serverSocket = new ServerSocket(TEST_PORT);

            Socket clientSocket = new Socket("localhost", TEST_PORT); // Подключаемся к серверу

            Socket acceptedSocket = serverSocket.accept(); // Сервер принимает соединение

            ServerStart.ClientHandler clientHandler = new ServerStart.ClientHandler(acceptedSocket);

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            Thread clientThread = new Thread(clientHandler);
            clientThread.start();

            writer.println("Test message");
            writer.flush();

            Thread.sleep(100);

            clientThread.interrupt();

            clientSocket.close();

            acceptedSocket.close();

            serverSocket.close();
        }
    }
}
