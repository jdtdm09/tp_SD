package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import Logs.Logger;
import Logs.MessageLogger;

public class ClientHandler implements Runnable {
    private final Socket clientSocket; 
    private UserManager userManager;
    private DirectMessageService directMessageService;
    private MultiCastNotificationService notificationService;
    private boolean authenticated = false; 
    private String username; 

    // Construtor que inicializa o socket e os serviços necessários
    public ClientHandler(Socket clientSocket, UserManager userManager) {
        this.clientSocket = clientSocket;
        this.userManager = userManager;
        this.directMessageService = new DirectMessageService();
        try {
            notificationService = new MultiCastNotificationService();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o serviço de notificações multicast.");
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)
        ) {
            out.println("Bem-vindo ao servidor!");

            // Atualiza o contador de clientes online ao conectar
            ReportService.incrementClientsOnline();

            String message;
            while ((message = in.readLine()) != null) {
                if (!authenticated) {
                    if (message.startsWith("register")) {
                        String[] parts = message.split(" ");
                        if (parts.length == 4) {
                            String username = parts[1];
                            String password = parts[2];
                            String role = parts[3];

                            String result = userManager.registerUser(username, password, role);

                            // Mensagens de Sucesso ou Erro
                            if (result.equals("Utilizador registado")) {
                                out.println("Registo realizado com sucesso!");
                                Logger.log("Novo registo de utilizador: " + username);
                            } else if (result.equals("Cargo inválido")) {
                                out.println("Cargo inválido. Usa: Coordenador, Supervisor, Operador");
                            } else {
                                out.println("Utilizador já existente");
                            }
                        } else {
                            out.println("Formato de registo inválido. Use: register <username> <password>");
                        }
                    } else if (message.startsWith("login")) {
                        String[] parts = message.split(" ");
                        if (parts.length == 3) {
                            username = parts[1];
                            String password = parts[2];

                            if (userManager.loginUser(username, password)) {
                                out.println("Login realizado com sucesso! Bem-vindo, " + username);
                                authenticated = true;
                                Logger.log("Login efetuado por: " + username);
                            } else {
                                out.println("Credenciais inválidas.");
                                Logger.log("Tentativa de login falhada para utilizador: " + username);
                            }
                        } else {
                            out.println("Formato de login inválido. Use: login <username> <password>");
                        }
                    } else if (message.startsWith("sair")) {
                        out.println("A terminar cliente!");
                        Logger.log("Cliente desconectado: " + clientSocket.getInetAddress());
                    } else {
                        out.println("Comando não reconhecido. Faça login para continuar.");
                    }
                } else {
                    String[] tokens = message.split(" ", 3);
                    String command = tokens[0].toLowerCase(); // Primeiro token é o comando
                    String response = "";

                    if (message.equalsIgnoreCase("sair")) {
                        System.out.println("Cliente desconectado: " + username);
                        // Fechar a conexão do cliente de forma limpa
                        clientSocket.close();
                        break; // Sai do loop de leitura do cliente
                    }

                    switch (command) {
                        case "/mensagens":
                            // Histórico de mensagens
                            if (tokens.length > 2) {
                                response = "Formato inválido. Use: /mensagens ou /mensagens 'user'";
                            } else if (tokens.length == 2) {
                                String recipientId = tokens[1];
                                directMessageService.getConversationHistory(username, recipientId).forEach(out::println);
                                out.println("FIM_DE_MENSAGENS");
                            } else {
                                directMessageService.getRecentMessagesForUser(username).forEach(out::println);
                                out.println("FIM_DE_MENSAGENS");
                            }
                            break;

                        case "/enviar":
                            // Envia mensagem para outro utilizador
                            if (tokens.length < 3) {
                                response = "Formato inválido. Use: /enviar 'user' 'mensagem'";
                            } else {
                                String recipientId = tokens[1];
                                String userMessage = tokens[2];
                                directMessageService.sendMessage(username, recipientId, userMessage);
                                Logger.log(username + " mandou uma mensagem!");
                                MessageLogger.log(username, recipientId, userMessage);
                                
                                // Incrementa o contador de mensagens no ReportService
                                ReportService.incrementMessagesSent();

                                response = "Mensagem enviada para " + recipientId;
                            }
                            break;

                        case "/notificar":
                            // Envia notificação para todos os utilizadores
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /notificar 'mensagem'";
                            } else {
                                String userMessage = message.substring("/notificar".length()).trim();
                                directMessageService.notifyAllUsers(username, userMessage);
                                notificationService.sendNotification(userMessage);
                                Logger.log(username + " enviou uma notificação!");
                                response = "Notificação enviada para todos os utilizadores";
                            }
                            break;

                        case "/notificacoes":
                            // Exibe notificações para o utilizador
                            directMessageService.getNotificationsForUser().forEach(out::println);
                            out.println("FIM_DE_NOTIFICACOES");
                            break;
                    }

                    if (!response.isEmpty()) {
                        out.println(response);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
        } finally {
            try {
                // Decrementa o contador de clientes online quando o cliente se desconectar
                ReportService.decrementClientsOnline();
                
                clientSocket.close();
                Logger.log("Conexão fechada para o cliente: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                System.out.println("Erro ao fechar a conexão do cliente: " + e.getMessage());
            }
        }
    }
}
