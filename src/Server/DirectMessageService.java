package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Logs.MessageLogger;

public class DirectMessageService {

    // Diretório onde os arquivos de histórico de conversas serão armazenados
    private static final String MESSAGE_HISTORY_DIR = "src/Logs/MessageLogs/";
    private static final int MESSAGE_LIMIT = 10;  // Limite de mensagens a serem exibidas

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
    public List<String> getConversationHistory(String userId1, String userId2) {
        List<String> conversationHistory = new ArrayList<>();
        String conversationFilePath = getConversationFilePath(userId1, userId2);
        File conversationFile = new File(conversationFilePath);
    
        if (conversationFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(conversationFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Verifica se a mensagem foi enviada pelo outro utilizador (userId2)
                    String[] parts = line.split("] ", 2); // Divide em timestamp e remetente: mensagem
                    if (parts.length > 1) {
                        String message = parts[1]; // Parte após o timestamp
                        String[] messageParts = message.split(": ", 2); // Divide remetente e conteúdo
                        if (messageParts.length > 1) {
                            String sender = messageParts[0].trim(); // Extrai o remetente
                            if (sender.equals(userId2)) { // Adiciona somente se o remetente for o outro utilizador
                                conversationHistory.add(line);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro ao ler o histórico de mensagens: " + e.getMessage());
            }
        } else {
            System.out.println("Nenhum histórico encontrado para a conversa entre " + userId1 + " e " + userId2 + ".");
        }
    
        return conversationHistory; // Retorna apenas as mensagens enviadas pelo outro utilizador
    }    

    public List<String> getRecentMessagesForUser(String userId) {
        List<String> recentMessages = new ArrayList<>();
        File directory = new File(MESSAGE_HISTORY_DIR);
    
        // Filtra os arquivos que incluem o userId no nome (como remetente ou destinatário)
        File[] conversationFiles = directory.listFiles((dir, name) -> name.contains(userId));
    
        if (conversationFiles != null) {
            for (File file : conversationFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Extrai o remetente da linha
                        String[] parts = line.split("] ", 2); // Divide em timestamp e restante da mensagem
                        if (parts.length > 1) {
                            String message = parts[1]; // Parte após o timestamp
                            String[] messageParts = message.split(": ", 2); // Divide remetente e conteúdo
                            if (messageParts.length > 1) {
                                String sender = messageParts[0].trim();
                                // Adiciona à lista apenas se o remetente não for o userId
                                if (!sender.equals(userId)) {
                                    recentMessages.add(line);
                                    // Limita o número de mensagens a 10
                                    if (recentMessages.size() >= MESSAGE_LIMIT) {
                                        return recentMessages;
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Erro ao ler o arquivo de conversa " + file.getName() + ": " + e.getMessage());
                }
            }
        }
    
        return recentMessages;
    }    

    // Gera o caminho do arquivo para armazenar o histórico da conversa entre dois utilizadores
    private String getConversationFilePath(String userId1, String userId2) {
        String conversationKey = userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
        return MESSAGE_HISTORY_DIR + conversationKey + ".txt";
    }
}
