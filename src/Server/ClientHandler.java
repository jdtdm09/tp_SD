package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import Logs.Logger;
import Logs.MessageLogger;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private UserManager userManager;
    private DirectMessageService directMessageService;
    private boolean authenticated = false; // Variável para controlar o estado de autenticação
    private String username; // Nome de utilizador autenticado

    public ClientHandler(Socket clientSocket, UserManager userManager) {
        this.clientSocket = clientSocket;
        this.userManager = userManager;
        this.directMessageService = new DirectMessageService();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("Bem-vindo ao servidor!");

            String message;
            while ((message = in.readLine()) != null) {
                // Se não estiver autenticado, permite apenas comandos de registo ou login
                if (!authenticated) {
                    if (message.startsWith("register")) {
                        String[] parts = message.split(" ");
                        if (parts.length == 4) {
                            String username = parts[1];
                            String password = parts[2];
                            String role = parts[3];

                            String result = userManager.registerUser(username, password, role);

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
                                authenticated = true; // Atualiza o estado de autenticação
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
                    String[] tokens = message.split(" ", 3); // Divide o comando em no máximo 3 partes
                    String command = tokens[0].toLowerCase(); // Primeiro token é o comando
                    String response = "";

                    switch (command) {
                        case "/mensagens":
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
                            // Verifica se o destinatário e a mensagem foram fornecidos
                            if (tokens.length < 3) {
                                response = "Formato inválido. Use: /enviar 'user' 'mensagem'";
                            } else {
                                String recipientId = tokens[1];
                                String userMessage = tokens[2];
                                directMessageService.sendMessage(username, recipientId, userMessage);
                                Logger.log(username + " mandou uma mensagem!");
                                MessageLogger.log(username, recipientId, userMessage);
                                response = "Mensagem enviada para " + recipientId;
                            }

                            break;

                        case "/notificar":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /notificar 'mensagem'";
                            } else {
                                // Pega a mensagem inteira após o "/notificar "
                                String userMessage = message.substring("/notificar".length()).trim();
                                directMessageService.notifyAllUsers(username, userMessage);
                                Logger.log(username + " enviou uma notificação!");
                                response = "Notificação enviada para todos os utilizadores";
                            }

                            break;

                        case "/notificacoes":
                            directMessageService.getNotificationsForUser().forEach(out::println);
                            out.println("FIM_DE_NOTIFICACOES");

                            break;
                    }

                    // Envia a resposta final para o cliente (se houver)
                    if (!response.isEmpty()) {
                        out.println(response);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                Logger.log("Conexão fechada para o cliente: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                System.out.println("Erro ao fechar a conexão do cliente: " + e.getMessage());
            }
        }
    }
}
