package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import Logs.MessageLogger;

public class DirectMessageService {

    // Diretório onde os arquivos de histórico de conversas serão armazenados
    private static final String MESSAGE_HISTORY_DIR = "src/Logs/MessageLogs/";

    static {
        // Cria o diretório para o histórico de mensagens, se ele não existir
        File directory = new File(MESSAGE_HISTORY_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                System.out.println("Erro ao criar o diretório de histórico de mensagens.");
            }
        }
    }

    // Método para enviar uma mensagem privada de um utilizador para outro
    public synchronized void sendMessage(String senderId, String receiverId, String message) {
        String conversationFilePath = getConversationFilePath(senderId, receiverId);

        // Regista a mensagem no log e grava no arquivo de histórico de conversa
        try (PrintWriter writer = new PrintWriter(new FileWriter(conversationFilePath, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String formattedMessage = "[" + timestamp + "] " + senderId + ": " + message;
            writer.println(formattedMessage);  // Escreve a mensagem no arquivo de histórico
            MessageLogger.log(senderId, receiverId, message);  // Regista a mensagem no log de atividade
            System.out.println("Mensagem enviada de " + senderId + " para " + receiverId + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao salvar a mensagem: " + e.getMessage());
        }
    }

    // Método para obter o histórico de mensagens entre dois utilizadores
    public void getConversationHistory(String userId1, String userId2) {
        String conversationFilePath = getConversationFilePath(userId1, userId2);
        File conversationFile = new File(conversationFilePath);

        // Exibe o histórico de mensagens, se existir
        if (conversationFile.exists()) {
            System.out.println("Histórico de mensagens entre " + userId1 + " e " + userId2 + ":");
            try (BufferedReader reader = new BufferedReader(new FileReader(conversationFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Erro ao ler o histórico de mensagens: " + e.getMessage());
            }
        } else {
            System.out.println("Nenhum histórico encontrado para a conversa entre " + userId1 + " e " + userId2 + ".");
        }
    }

    // Gera o caminho do arquivo para armazenar o histórico da conversa entre dois utilizadores
    private String getConversationFilePath(String userId1, String userId2) {
        String conversationKey = userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
        return MESSAGE_HISTORY_DIR + conversationKey + ".txt";
    }
}
