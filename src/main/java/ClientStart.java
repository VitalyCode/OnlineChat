import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class ClientStart extends Thread{

    private static final String DEFAULT_HOST = "localhost";

    private static final int DEFAULT_PORT = 25565;

    private static final String SETTINGS_FILE = "settings.txt";

    private static final String LOG_FILE = "client.log";

    private static String host;

    private static int port;

    private static String username;

    @Override
    public void run(){

        loadSettings();

        try (Socket socket = new Socket(host, port);

             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

             Scanner scanner = new Scanner(System.in)) {

            // Запрашиваем имя пользователя, если оно не задано в настройках
            if (username == null || username.isEmpty()) {

                System.out.print("Введите имя пользователя: ");

                username = scanner.nextLine();
            }

            System.out.println("Добро пожаловать в чат, " + username + "!");

            // Запускаем поток для чтения сообщений от сервера
            new Thread(() -> {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                String timestamp = dateFormat.format(new Date());

                try {

                    String message;

                    while ((message = reader.readLine()) != null) {

                        System.out.println(timestamp+" "+message);

                        logMessage(message);
                    }
                }
                catch (IOException e) {

                    System.err.println("Ошибка при чтении сообщения от сервера: " + e.getMessage());
                }
            }).start();

            // Отправляем сообщения на сервер
            String message;

            while (true) {

                System.out.println("\r\nВведите сообщение:");
                message = scanner.nextLine();

                writer.println(message);

                logMessage("Я: " + message);  // Логируем отправленное сообщение

                if ("/exit".equals(message)) {

                    break;
                }
            }

        }
        catch (IOException e) {

            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    private static void loadSettings() {

        host = DEFAULT_HOST;

        port = DEFAULT_PORT;

        try (BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("host=")) {

                    host = line.substring(5);
                }
                else if (line.startsWith("port=")) {

                    port = Integer.parseInt(line.substring(5));
                }
                else if (line.startsWith("username=")) {

                    username = line.substring(9);
                }
            }
        }
        catch (IOException e) {

            System.err.println("Ошибка при чтении файла настроек, используем значения по умолчанию: " + e.getMessage());
        }
        catch (NumberFormatException e) {

            System.err.println("Неверный формат порта в файле настроек, используем порт по умолчанию.");

            port = DEFAULT_PORT;
        }
    }

    // Логирование сообщений в файл
    private static void logMessage(String message) {

        try (FileWriter fileWriter = new FileWriter(LOG_FILE, true);

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
