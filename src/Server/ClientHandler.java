package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import Logs.Logger;
import Logs.MessageLogger;
import Models.Request;

public class ClientHandler implements Runnable {
    private final Socket clientSocket; 
    private UserManager userManager;
    private DirectMessageService directMessageService;
    private RequestService requestService;
    private MulticastService notificationService;
    private boolean authenticated = false; 
    private String username; 

    public ClientHandler(Socket clientSocket, UserManager userManager) {
        this.clientSocket = clientSocket;
        this.userManager = userManager;
        this.directMessageService = new DirectMessageService();
        this.requestService = new RequestService();
        try {
            notificationService = new MulticastService();
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o serviço de notificações multicast.");
        }
    }

    private String formatRequest(Request request) {
        String status;
    
        if (request.isAceite()) {
            status = "Resolvido (Aceite)";
        } else if (request.isRejeitado()) {
            status = "Resolvido (Rejeitado)";
        } else {
            status = "Pendente";
        }
    
        return String.format("ID: %d | Pedido: %s | Criador: %s | Status: %s",
                request.getId(), request.getPedido(), request.getCriador(), status);
    }    

    @Override
    public void run() {
        try (
                InputStreamReader isr = new InputStreamReader(clientSocket.getInputStream(), "UTF-8");
                BufferedReader in = new BufferedReader(isr);
                OutputStreamWriter osw = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
                PrintWriter out = new PrintWriter(osw, true);
        ) {
            out.println("Bem-vindo ao servidor!");

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
                                System.out.println("Login efetuado por: " + username);
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
                        System.out.println("Cliente desconectado: " + clientSocket.getInetAddress());
                        Logger.log("Cliente desconectado: " + clientSocket.getInetAddress());
                        clientSocket.close();
                        break; 
                    } else {
                        out.println("Comando não reconhecido. Faça login para continuar.");
                    }
                } else {
                    String[] tokens = message.split(" ", 3);
                    String command = tokens[0].toLowerCase(); 
                    String response = "";

                    if (message.equalsIgnoreCase("logout")) {
                        System.out.println("Encerramento realizado: " + username);
                        authenticated = false;
                    }
                    System.out.println("Mensagem recebida do cliente: " + message);
                    switch (command) {
                        /**
                         * ! MENSAGENS PRIVADAS
                         */
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
                            if (tokens.length < 3) {
                                response = "Formato inválido. Use: /enviar 'user' 'mensagem'";
                            } else {
                                String recipientId = tokens[1];
                                String userMessage = tokens[2];
                                directMessageService.sendMessage(username, recipientId, userMessage);
                                Logger.log(username + " mandou uma mensagem!");
                                MessageLogger.log(username, recipientId, userMessage);
                                
                                ReportService.incrementMessagesSent();

                            }

                            break;

                        /**
                         * ! PEDIDOS
                         */

                        case "/pedido":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /pedido 'pedido'";
                            } else {
                                String userRequest = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
                                requestService.addRequest(userRequest, username);
                                notificationService.sendRequest(userRequest);
                                Logger.log(username + " fez um pedido!");
                            }

                            break;

                        case "/pedidos":
                            List<Request> pendingRequests = requestService.getPendingRequests();
                            if (pendingRequests.isEmpty()) {
                                out.println("Nenhum pedido pendente no momento.");
                            } else {
                                pendingRequests.forEach(request -> out.println(formatRequest(request)));
                            }
                            out.println("FIM_DE_PEDIDOS");

                            break;

                        case "/aceitar":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /aceitar 'id'";
                            } else {
                                int requestId = Integer.parseInt(tokens[1]);
                                requestService.updateRequestStatus(requestId, true, false, username);
                                Request request = requestService.getRequestById(requestId);
                                String requestMessage = request.getPedido();
                                notificationService.sendToGroupGeral("");
                                notificationService.sendToGroupGeral("==============================================");
                                notificationService.sendToGroupGeral("Ordem nova: " + requestMessage);
                                notificationService.sendToGroupGeral("==============================================");
                                Logger.log(username + " aceitou um pedido!");
                            }

                            break;

                        case "/rejeitar":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /rejeitar 'id'";
                            } else {
                                int requestId = Integer.parseInt(tokens[1]);
                                requestService.updateRequestStatus(requestId, false, true, username);
                                Logger.log(username + " rejeitou um pedido!");
                            }

                            break;

                        case "/todospedidos":
                            List<Request> allRequests = requestService.getAllRequests();
                            if (allRequests.isEmpty()) {
                                out.println("Nenhum pedido registado no momento.");
                            } else {
                                allRequests.forEach(request -> out.println(formatRequest(request)));
                            }
                            out.println("FIM_DE_PEDIDOS");

                            break;

                        /**
                         * ! NOTIFICAÇÕES
                         */
                        case "/notificar":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /notificar 'mensagem'";
                            } else {
                                String userMessage = message.substring("/notificar".length()).trim();
                                directMessageService.notifyAllUsers(username, userMessage);
                                notificationService.sendNotification(userMessage);
                                Logger.log(username + " enviou uma notificação!");
                            }

                            break;

                        case "/notificacoes":
                            directMessageService.getNotificationsForUser().forEach(out::println);
                            out.println("FIM_DE_NOTIFICACOES");

                            break;

                        /**
                         * ! CANAIS
                         */

                        case "/entrar":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /joinChannel 'canal'";
                            } else {
                                int channel = Integer.parseInt(tokens[1]);
                                System.out.println("Canal: " + channel);
                                System.out.println(username + " entrou no canal "+ channel);
                            }
                         
                           break;

                        case "/sair":
                            if (tokens.length < 2) {
                                response = "Formato inválido. Use: /leaveChannel 'canal'";
                            } else {
                                int channel = Integer.parseInt(tokens[1]);
                                System.out.println(username + " saiu do canal "+ channel);
                            }
                            break;

                        case "/enviarcanal":
                            if (tokens.length < 3) {
                                response = "Formato inválido. Use: /enviar 'mensagem'";
                            } else {
                                String channelID = tokens[1];
                                String userMessage = tokens[2];
                                int porta = Integer.parseInt(channelID);
                                String channelName;
                                String fullMessage;

                                switch (porta) {
                                    case 1:
                                        channelName = "Chat Geral";
                                        fullMessage = "[" + username + "]: " + userMessage;
                                        notificationService.sendToGroupGeral(fullMessage);
                                        break;
                                    case 2:
                                        channelName = "Chat de Coordenadores";
                                        fullMessage = "[" + username + "]: " + userMessage;
                                        notificationService.sendToGroupCoordenadores(fullMessage);
                                        break;
                                    case 3:
                                        channelName = "Chat de Supervisores";
                                        fullMessage = "[" + username + "]: " + userMessage;
                                        notificationService.sendToGroupSupervisores(fullMessage);
                                        break;
                                    case 4:
                                        channelName = "Chat de Operadores";
                                        fullMessage = "[" + username + "]: " + userMessage;
                                        notificationService.sendToGroupOperadores(fullMessage);
                                        break;
                                    default:
                                        channelName = "Canal inválido.";
                                        break;
                                }
                                Logger.log(username + " mandou uma mensagem para o " + channelName + "!");
                                ReportService.incrementMessagesSent();
                            }
                            
                            break;
                    }

                    if (!response.isEmpty()) {
                        out.println(response);
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Conexão encerrada abruptamente por um cliente.");
        } finally {
            try {
                ReportService.decrementClientsOnline();
                
                clientSocket.close();
                Logger.log("Conexão fechada para o cliente: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                System.out.println("Erro ao fechar a conexão do cliente: " + e.getMessage());
            }
        }
    }
}
