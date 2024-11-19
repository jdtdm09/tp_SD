package Logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageLogger {

    private static final String MSG_FILE_PATH = "src/Logs/DirectMessagesLog.txt"; 

    static {
        try {
            File logsDir = new File("src/Logs");
            if (!logsDir.exists()) {
                boolean dirsCreated = logsDir.mkdirs(); 
                if (!dirsCreated) {
                    System.out.println("Erro ao criar a pasta src/Logs.");
                }
            }

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

    public synchronized static void log(String senderId, String receiverId, String message) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(MSG_FILE_PATH, true), "UTF-8"))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write("[" + timestamp + "] " + "Mensagem de " + senderId + " para " + receiverId + ": " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }

    public synchronized static void urgentLog(String senderId, String message) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(MSG_FILE_PATH, true), "UTF-8"))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write("[" + timestamp + "] Notificação urgente de " + senderId + ": " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }
}
