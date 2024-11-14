package Logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    // Caminho relativo dentro da pasta src/Logs
    private static final String LOG_FILE_PATH = "src/Logs/Logs.txt"; // Caminho atualizado

    static {
        try {
            // Verifica se a pasta Logs dentro de src existe, se não, cria a pasta
            File logsDir = new File("src/Logs");
            if (!logsDir.exists()) {
                boolean dirsCreated = logsDir.mkdirs(); // Cria a pasta src/Logs se não existir
                if (!dirsCreated) {
                    System.out.println("Erro ao criar a pasta src/Logs.");
                }
            }

            // Verifica se o arquivo de log existe
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                boolean fileCreated = logFile.createNewFile(); // Cria o ficheiro de log se não existir
                if (!fileCreated) {
                    System.out.println("Erro ao criar o ficheiro de log.");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao verificar/criar o ficheiro de log: " + e.getMessage());
        }
    }

    // Método para escrever no log
    public static void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + timestamp + "] " + message);
        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }
}
