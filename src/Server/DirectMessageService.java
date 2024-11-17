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
import Models.User;
import Models.UserRoles;

public class DirectMessageService {

    private static final String MESSAGE_HISTORY_DIR = "src/Logs/MessageLogs/";
    private static final int MESSAGE_LIMIT = 10;  
    private static final String USERS_FILE_PATH = "users.txt";
    private static final String MESSAGES_LOG = "src/Logs/DirectMessagesLog.txt";

    static { 
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
        if (!userExists(receiverId)) {
            System.out.println("Erro: O destinatário " + receiverId + " não existe.");
            return;
        }

        String conversationFilePath = getConversationFilePath(senderId, receiverId);

        try (PrintWriter writer = new PrintWriter(new FileWriter(conversationFilePath, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String formattedMessage = "[" + timestamp + "] " + senderId + ": " + message;
            writer.println(formattedMessage); 
            System.out.println("Mensagem enviada de " + senderId + " para " + receiverId + ": " + message);
        } catch (IOException e) {
            System.out.println("Erro ao salvar a mensagem: " + e.getMessage());
        }
    }

    private boolean userExists(String userId) {
        List<String> allUsers = loadUsersFromFile(); 
        return allUsers.contains(userId); 
    }

    // Envia uma notificação para todos os utilizadores
    public synchronized void notifyAllUsers(String senderId, String message) {
        List<String> allUsers = loadUsersFromFile();  

        for (String receiverId : allUsers) {
            if (!receiverId.equals(senderId)) {
                sendMessage(senderId, receiverId, message); 
            }
        }
        MessageLogger.urgentLog(senderId, message);
    }

    // Carrega a lista de utilizadores a partir do arquivo
    private List<String> loadUsersFromFile() {
        List<String> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = parseUser(line);  
                if (user != null) {
                    users.add(user.getUsername()); 
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar o arquivo de usuários: " + e.getMessage());
        }
        return users;
    }

    // Parseia o JSON para criar um objeto User
    private User parseUser(String json) {
        json = json.trim();
        if (json.isEmpty()) return null;

        try {
            int id = Integer.parseInt(json.split("\"id\": ")[1].split(",")[0].trim());
            String username = json.split("\"username\": \"")[1].split("\"")[0];
            String password = json.split("\"password\": \"")[1].split("\"")[0];
            String roleStr = json.split("\"role\": \"")[1].split("\"")[0];
            UserRoles role = UserRoles.valueOf(roleStr);

            return new User(id, username, password, role);
        } catch (Exception e) {
            System.out.println("Erro ao parsear utilizador: " + e.getMessage());
            return null;
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
                    String[] parts = line.split("] ", 2); 
                    if (parts.length > 1) {
                        String message = parts[1]; 
                        String[] messageParts = message.split(": ", 2); 
                        if (messageParts.length > 1) {
                            String sender = messageParts[0].trim(); 
                            if (sender.equals(userId2)) { 
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
    
        return conversationHistory; 
    }    

    // Obtém as mensagens mais recentes para o utilizador
    public List<String> getRecentMessagesForUser(String userId) {
        List<String> recentMessages = new ArrayList<>();
        File directory = new File(MESSAGE_HISTORY_DIR);
    
        File[] conversationFiles = directory.listFiles((dir, name) -> name.contains(userId));
    
        if (conversationFiles != null) {
            for (File file : conversationFiles) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Extrai o remetente da linha
                        String[] parts = line.split("] ", 2); 
                        if (parts.length > 1) {
                            String message = parts[1]; 
                            String[] messageParts = message.split(": ", 2); 
                            if (messageParts.length > 1) {
                                String sender = messageParts[0].trim();
                                if (!sender.equals(userId)) {
                                    recentMessages.add(line);
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
    
    // Obtém as notificações para o utilizador
    public List<String> getNotificationsForUser() {
        List<String> notificationsList = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(MESSAGES_LOG))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Notificação urgente de")) {
                    notificationsList.add(line); 
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo de mensagens: " + e.getMessage());
        }

        return notificationsList;
    }


    // Cria o caminho do arquivo para armazenar o histórico da conversa entre dois utilizadores
    private String getConversationFilePath(String userId1, String userId2) {
        String conversationKey = userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
        return MESSAGE_HISTORY_DIR + conversationKey + ".txt";
    }
}
