import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {


    private static final int PORT = 25565;

    private static final String FILE = "settings.txt";

    private static final String LOG = "server.log";

    private static int port;

    private static final List<ClientHandler> clients = new ArrayList<>();


    public static void main(String[] args) {

        loadSettings();

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Сервер запущен на порту " + port);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Новый клиент подключился: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);

                clients.add(clientHandler);

                new Thread(clientHandler).start();
            }

        }
        catch (IOException e) {

            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }

    private static void loadSettings() {

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("port=")) {

                    port = Integer.parseInt(line.substring(5));

                    break;
                }
            }
        }
        catch (IOException e) {

            System.err.println("Ошибка при чтении файла настроек, используем порт по умолчанию: " + e.getMessage());

            port = PORT;
        }
        catch (NumberFormatException e) {

            System.err.println("Неверный формат порта в файле настроек, используем порт по умолчанию.");

            port = PORT;
        }
    }

    // Вложенный класс для обработки каждого клиента в отдельном потоке
    private static class ClientHandler implements Runnable {

        private final Socket socket;

        private BufferedReader reader;

        private PrintWriter writer;

        private String username;

        public ClientHandler(Socket socket) {

            this.socket = socket;
        }

        @Override
        public void run() {

            try {

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                writer = new PrintWriter(socket.getOutputStream(), true);

                // Запрашиваем имя пользователя
                writer.println("Введите имя пользователя:");

                username = reader.readLine();

                System.out.println("Пользователь " + username + " присоединился.");

                logMessage("Пользователь " + username + " присоединился.");

                String message;

                while ((message = reader.readLine()) != null) {

                    if ("/exit".equals(message)) {

                        break; // Выход из цикла, если клиент отправил "/exit"
                    }

                    String formattedMessage = username + ": " + message;

                    System.out.println(formattedMessage);

                    logMessage(formattedMessage);

                    broadcastMessage(formattedMessage);
                }

                System.out.println("Пользователь " + username + " отключился.");

                logMessage("Пользователь " + username + " отключился.");
            }
            catch (IOException e) {

                System.err.println("Ошибка при обработке клиента: " +"\""+username+"\" - " + e.getMessage());
            }
            finally {

                try {

                    socket.close();

                    clients.remove(this);
                }
                catch (IOException e) {

                    System.err.println("Ошибка при закрытии сокета: " + e.getMessage());
                }
            }
        }

        // Отправка сообщения всем клиентам (кроме себя)
        private void broadcastMessage(String message) {

            for (ClientHandler client : clients) {

                if (client != this) {

                    client.writer.println(message);
                }
            }
        }
    }


    // Логирование сообщений в файл
    private static void logMessage(String message) {
        try (FileWriter fileWriter = new FileWriter(LOG, true); // Append mode

             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

             PrintWriter logWriter = new PrintWriter(bufferedWriter)) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            String timestamp = dateFormat.format(new Date());

            logWriter.println(timestamp + " " + message);

        }
        catch (IOException e) {

            System.err.println("Ошибка при записи в лог-файл: " + e.getMessage());
        }
    }
}

