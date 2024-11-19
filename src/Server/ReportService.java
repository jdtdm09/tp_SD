package Server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportService extends Thread{
    private static final String STATISTICS_FILE_PATH = "src/Logs/Reports.txt";
    
    private static final AtomicInteger clientsOnline = new AtomicInteger(0);
    private static final AtomicInteger messagesSent = new AtomicInteger(0); 

    public static void incrementClientsOnline() {
        clientsOnline.incrementAndGet();
    }

    public static void decrementClientsOnline() {
        clientsOnline.decrementAndGet();
    }

    public static void incrementMessagesSent() {
        messagesSent.incrementAndGet();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(60000);
                
                generateReport();
                messagesSent.set(0);
            } catch (InterruptedException e) {
                System.out.println("Erro ao aguardar 1 minuto para gerar relatório: " + e.getMessage());
            }
        }
    }

    private void generateReport() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        
        String report = String.format("[%s] Utilizadores online: %d | Mensagens enviadas: %d", timestamp, clientsOnline.get(), messagesSent.get());
        
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(STATISTICS_FILE_PATH, true), StandardCharsets.UTF_8))) {
            writer.println(report);
        } catch (IOException e) {
            System.out.println("Erro ao escrever o relatório no arquivo: " + e.getMessage());
        }
    }
}
