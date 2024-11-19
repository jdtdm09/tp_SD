package Logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChannelLogger {

    private static final String LOGS_DIR_PATH = "src/Logs/ChannelLogs";
    private static final String CHAT_GERAL = "chatGeral.txt";
    private static final String CHAT_COORDENADOR = "chatCoordenador.txt";
    private static final String CHAT_SUPERVISOR = "chatSupervisor.txt";
    private static final String CHAT_OPERADOR = "chatOperador.txt";

    static {
        try {
            File logsDir = new File(LOGS_DIR_PATH);
            if (!logsDir.exists()) {
                boolean dirsCreated = logsDir.mkdirs();
                if (!dirsCreated) {
                    System.out.println("Erro ao criar a pasta " + LOGS_DIR_PATH);
                }
            }

            createLogFile(CHAT_GERAL);
            createLogFile(CHAT_COORDENADOR);
            createLogFile(CHAT_SUPERVISOR);
            createLogFile(CHAT_OPERADOR);
        } catch (IOException e) {
            System.out.println("Erro ao verificar/criar os ficheiros de log dos canais: " + e.getMessage());
        }
    }

    private static void createLogFile(String fileName) throws IOException {
        File logFile = new File(LOGS_DIR_PATH, fileName);
        if (!logFile.exists()) {
            boolean fileCreated = logFile.createNewFile();
            if (!fileCreated) {
                System.out.println("Erro ao criar o ficheiro de log: " + fileName);
            }
        }
    }

    public static void logMessage(String fileName, String message, String username) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(LOGS_DIR_PATH, fileName), true), "UTF-8"))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.write("[" + timestamp + "] " + username + ": " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Erro ao escrever no ficheiro de log: " + fileName + " - " + e.getMessage());
        }
    }
}
