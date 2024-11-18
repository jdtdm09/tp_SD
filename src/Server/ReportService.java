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
    
    private static final AtomicInteger clientsOnline = new AtomicInteger(0); // Contagem de clientes online
    private static final AtomicInteger messagesSent = new AtomicInteger(0);  // Contagem de mensagens enviadas

    // Método para incrementar o contador de clientes online
    public static void incrementClientsOnline() {
        clientsOnline.incrementAndGet();
    }

    // Método para decrementar o contador de clientes online
    public static void decrementClientsOnline() {
        clientsOnline.decrementAndGet();
    }

    // Método para incrementar o contador de mensagens enviadas
    public static void incrementMessagesSent() {
        messagesSent.incrementAndGet();
    }

    // Método para gerar o relatório a cada 1 minuto
    @Override
    public void run() {
        while (true) {
            try {
                // Espera 1 minuto (60000 milissegundos)
                Thread.sleep(60000);
                
                // Gera o relatório
                generateReport();
                messagesSent.set(0);
            } catch (InterruptedException e) {
                System.out.println("Erro ao aguardar 1 minuto para gerar relatório: " + e.getMessage());
            }
        }
    }

    // Método para gerar e salvar o relatório
    private void generateReport() {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        
        // Dados do relatório
        String report = String.format("[%s] Utilizadores online: %d | Mensagens enviadas: %d", timestamp, clientsOnline.get(), messagesSent.get());
        
        // Escreve os dados no arquivo de relatório
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(STATISTICS_FILE_PATH, true), StandardCharsets.UTF_8))) {
            writer.println(report);
        } catch (IOException e) {
            System.out.println("Erro ao escrever o relatório no arquivo: " + e.getMessage());
        }
    }
}
