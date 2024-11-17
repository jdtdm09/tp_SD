package Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageLogger {

    private static final String MSG_FILE_PATH = "src/Logs/DirectMessagesLog.txt"; 

    static {
        try {
            /// Cria a pasta src/Logs, se não existir
            File logsDir = new File("src/Logs");
            if (!logsDir.exists()) {
                boolean dirsCreated = logsDir.mkdirs(); 
                if (!dirsCreated) {
                    System.out.println("Erro ao criar a pasta src/Logs.");
                }
            }

            // Cria o ficheiro de log, se não existir
            File logFile = new File(MSG_FILE_PATH);
            if (!logFile.exists()) {
                boolean fileCreated = logFile.createNewFile(); 
                if (!fileCreated) {
                    System.out.println("Erro ao criar o ficheiro de log.");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao verificar/criar o ficheiro de log: " + e.getMessage());
        }
    }

    // Método para registrar mensagens no log
    public synchronized static void log(String senderId, String receiverId, String message) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(MSG_FILE_PATH, true), StandardCharsets.UTF_8))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + timestamp + "] " + "Mensagem de " + senderId + " para " + receiverId + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }

    // Método para registrar notificações urgentes no log
    public synchronized static void urgentLog(String senderId, String message) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(MSG_FILE_PATH, true), StandardCharsets.UTF_8))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + timestamp + "] Notificação urgente de " + senderId + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }
}
