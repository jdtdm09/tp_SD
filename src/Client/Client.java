package Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import Logs.Logger;
import Models.User;
import Models.UserRoles;

public class Client extends Thread {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4840;
    private static final String USERS_FILE_PATH = "users.txt";
    private boolean authenticated = false; 
    private String loggedUserName; 
    private User authenticatedUser;
    MulticastReceiver receiver = new MulticastReceiver();

    /**
     * 
     * ? Verificação se o utilizador existe.
     * @param userId
     */
    private synchronized boolean userExists(String userId) {
        List<String> allUsers = loadUsersFromFile();
        return allUsers.contains(userId);   
    }

    /**
     * 
     * ? Carregar utilizadores do ficheiro.
     */
    private synchronized List<String> loadUsersFromFile() {
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

    /**
     * 
     * ? Ler ficheiro JSON e transformar numa class Utilizador.
     * @param json
     */
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
    

    public void start() {
        /**
         * ? Thread que está sempre á espera de uma notificação
         */
        new Thread(receiver::startListening).start();
        try (
                /**
                 * ? Comunicação entre servidor e cliente através de TCP
                 */
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); 
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            System.out.println("------------------------------------------------------------------");
            System.out.println("Conectado. " + in.readLine());
            System.out.println("------------------------------------------------------------------");

            String message;
            String serverResponse;
            int option;
            int channel = 99;
            
            while (true) {
                if (!authenticated) {
                    /** 
                     * ! MENU DE AUTENTICAÇÃO
                     */
                    System.out.print(Menu.getAuthenticationMenu());
                    
                    message = scanner.nextLine();
                    out.println(message);

                    String response = in.readLine();
                    System.out.println("Resposta do servidor: " + response);

                    if (response.startsWith("A terminar cliente")) {
                        try {
                            socket.close(); 
                        } catch (IOException e) {
                            System.out.println("Erro ao fechar a conexão com o servidor: " + e.getMessage());
                        }
                        System.exit(0);
                    }

                    if (response.startsWith("Login realizado com sucesso")) {
                        authenticated = true;
                        String[] parts = response.split(", ");
                        loggedUserName = parts[1].trim(); 
                        authenticatedUser = new User(loggedUserName);
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Erro de interrupção: " + e.getMessage());
                    }

                    if (authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                        new Thread(receiver::listeningRequests).start();
                    }
                    do {
                        /**
                         * ! MENU PRINCIPAL
                         */
                        System.out.print(Menu.getMainMenu(authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR));
                        String input = scanner.nextLine().trim();
                    
                        if (input.isEmpty()) {
                            System.out.println("Opção inválida. Por favor, insira uma opção.");
                        } else {
                            try {
                                option = Integer.parseInt(input);

                                if (option == 0) { 
                                    out.println("logout");
                                    authenticated = false;
                                    Logger.log(loggedUserName + " desconectou-se");
                                } else if (option == 1) {
                                    /**
                                     * ! MENU DE MENSAGENS
                                     */
                                    System.out.print(Menu.getMessageMenu());
                                    do {
                                        System.out.print("Comando: ");
                                        message = scanner.nextLine();
                    
                                        if (message.startsWith("/mensagens")) {
                                            String[] parts = message.split(" ", 2);
                                            if (parts.length == 2) {
                                                String recipient = parts[1];
                                                out.println("/mensagens " + recipient);
                                                System.out.println("Mensagens recentes de " + recipient + ":");
                                            } else {
                                                out.println("/mensagens");
                                                System.out.println("Mensagens recentes:");
                                            }
                                            while ((serverResponse = in.readLine()) != null) {
                                                if (serverResponse.contains("FIM_DE_MENSAGENS"))
                                                    break;
                                                System.out.println(serverResponse);
                                            }
                                            System.out.println("----------------------------------------------------------------------------------------------");
                                            try {
                                                Thread.sleep(1500);
                                            } catch (InterruptedException e) {
                                                System.out.println("Erro de interrupção: " + e.getMessage());
                                            }
                                        } else if (message.startsWith("/enviar")) {
                                            String[] parts = message.split(" ", 3);
                                            if (parts.length < 3) {
                                                System.out.println("Formato inválido. Use: /enviar <user> <mensagem>");
                                            } else {
                                                String recipient = parts[1];
                                                String userMessage = parts[2];

                                                if (!userExists(recipient)) {
                                                    System.out.println("Erro: O destinatário " + recipient + " não existe.");
                                                } else {
                                                out.println("/enviar " + recipient + " " + userMessage);
                                                System.out.println("Mensagem enviada para " + recipient); 
                                                }
                                            }
                                        } else if (message.equalsIgnoreCase("/exit")) {
                                            break;
                                        } else {
                                            System.out.println("Comando inválido. Tente novamente.");
                                        }
                                    } while (true);
                                } else if (option == 2) {
                                    /**
                                     * ! MENU DE PEDIDOS
                                     */
                                    System.out.print(Menu.getPedidosMenu(authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR));
                                    do {
                                        System.out.print("Comando: ");
                                        message = scanner.nextLine();

                                        if (message.startsWith("/pedido ")) {
                                            String[] parts = message.split(" ", 2);
                                            if (parts.length < 2) {
                                                System.out.println("Formato inválido. Use: /pedido <mensagem>");
                                            } else {
                                                String request = parts[1];
                                                out.println("/pedido " + request);
                                                System.out.println("Pedido enviado com sucesso!");
                                            }
                                        } else if (message.startsWith("/pedidos") && authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                                            out.println("/pedidos");
                                            while ((serverResponse = in.readLine()) != null) {
                                                if (serverResponse.contains("FIM_DE_PEDIDOS"))
                                                    break;
                                                System.out.println(serverResponse);
                                            }
                                            System.out.println("----------------------------------------------------------------------------------------------");
                                            try {
                                                Thread.sleep(1500);
                                            } catch (InterruptedException e) {
                                                System.out.println("Erro de interrupção: " + e.getMessage());
                                            }
                                        } else if (message.startsWith("/aceitar") && authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                                            String[] parts = message.split(" ", 2);
                                            if (parts.length < 2) {
                                                System.out.println("Formato inválido. Use: /aceitar <id>");
                                            } else {
                                                String id = parts[1];
                                                out.println("/aceitar " + id);
                                                System.out.println("Pedido aceito com sucesso!");
                                            }
                                        } else if (message.startsWith("/rejeitar") && authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                                            String[] parts = message.split(" ", 2);
                                            if (parts.length < 2) {
                                                System.out.println("Formato inválido. Use: /rejeitar <id>");
                                            } else {
                                                String id = parts[1];
                                                out.println("/rejeitar " + id);
                                                System.out.println("Pedido rejeitado com sucesso!");
                                            }
                                        } else if (message.equalsIgnoreCase("/todospedidos") && authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                                            out.println("/todospedidos");
                                            while ((serverResponse = in.readLine()) != null) {
                                                if (serverResponse.contains("FIM_DE_PEDIDOS"))
                                                    break;
                                                System.out.println(serverResponse);
                                            }
                                            System.out.println("----------------------------------------------------------------------------------------------");
                                            try {
                                                Thread.sleep(1500);
                                            } catch (InterruptedException e) {
                                                System.out.println("Erro de interrupção: " + e.getMessage());
                                            }
                                        } else if (message.equalsIgnoreCase("/exit")) {
                                            break;
                                        } else {
                                            System.out.println("Comando inválido. Tente novamente.");
                                        }
                                    } while (true);
                                } else if (option == 3) {
                                    /**
                                     * ! MENU DE CANAIS
                                     */
                                    System.out.print(Menu.getChannelMenu(channel));
                                    do {
                                        System.out.print("Comando: ");
                                        message = scanner.nextLine();
                    
                                        if (message.equalsIgnoreCase("/exit")) {
                                            if (channel != 99) {
                                                System.out.println("A sair do canal " + channel + "...");
                                                channel = 99;
                                            }
                                            break;
                                        } else if (message.equalsIgnoreCase("/canais")) {
                                            System.out.print(Menu.getChannelList());
                                        } else if (message.startsWith("/canal")) {
                                            String[] parts = message.split(" ", 2);
                                            if (parts.length < 2) {
                                                System.out.println("Formato inválido. Use: /canal <porta>");
                                            } else {
                                                String porta = parts[1];
                                            
                                                if (porta.equals("1") || porta.equals("2") || porta.equals("3") || porta.equals("4")) {
                                                    System.out.println("A entrar no canal " + porta + "...");
                                            
                                                    boolean exitChannel = false;
                                                    while (!exitChannel) { // Loop contínuo até o comando /exit
                                                        switch (porta) {
                                                            case "1":
                                                                channel = Integer.parseInt(porta);
                                                                System.out.println("Entrou no canal 1 com sucesso!");
                                                                System.out.println(Menu.getChannelMenu(channel));
                                                                out.println("/entrar " + channel);

                                                                receiver = new MulticastReceiver();
                                                                Thread receiverGeralThread = new Thread(receiver::listeningChannelGeral);
                                                                receiverGeralThread.start();

                                                                while (!exitChannel) {
                                                                    System.out.print("Comando ou mensagem (/exit para sair): ");
                                                                    String userCommand = scanner.nextLine();
                                                            
                                                                    if (userCommand.equalsIgnoreCase("/exit")) {
                                                                        out.println("/sair " + channel);
                                                                        exitChannel = true;
                                                            
                                                                        // Interrompe a thread de escuta e sai do canal multicast
                                                                        receiver.stopListening();
                                                            
                                                                        channel = 99; // Volta ao estado de não estar num canal
                                                                        System.out.println("Você saiu do canal " + porta);
                                                                        System.out.println(Menu.getChannelMenu(channel));
                                                                    } else {
                                                                        out.println("/enviarcanal " + channel + " " + userCommand);
                                                                    }
                                                                }
                                                                break;
                                            
                                                            case "2":
                                                                if (authenticatedUser.getUserRole(loggedUserName) != UserRoles.COORDENADOR) {
                                                                    System.out.println("Erro: Apenas coordenadores podem entrar neste canal.");
                                                                    exitChannel = true; // Força saída do switch
                                                                    break;
                                                                }
                                                                channel = Integer.parseInt(porta);
                                                                System.out.println("Entrou no canal 2 com sucesso!");
                                                                System.out.println(Menu.getChannelMenu(channel));
                                                                out.println("/entrar " + channel);

                                                                receiver = new MulticastReceiver();
                                                                Thread receiverCoordenadorThread = new Thread(receiver::listeningChannelCoordenadores);
                                                                receiverCoordenadorThread.start();

                                                                while (!exitChannel) {
                                                                    System.out.print("Comando ou mensagem (/exit para sair): ");
                                                                    String userCommand = scanner.nextLine();
                                                            
                                                                    if (userCommand.equalsIgnoreCase("/exit")) {
                                                                        out.println("/sair " + channel);
                                                                        exitChannel = true;
                                                            
                                                                        // Interrompe a thread de escuta e sai do canal multicast
                                                                        receiver.stopListening();
                                                            
                                                                        channel = 99; // Volta ao estado de não estar num canal
                                                                        System.out.println("Você saiu do canal " + porta);
                                                                        System.out.println(Menu.getChannelMenu(channel));
                                                                    } else {
                                                                        out.println("/enviarcanal " + channel + " " + userCommand);
                                                                    }
                                                                }
                                                                break;
                                            
                                                            case "3":
                                                                if (authenticatedUser.getUserRole(loggedUserName) != UserRoles.SUPERVISOR) {
                                                                    System.out.println("Erro: Apenas supervisores podem entrar neste canal.");
                                                                    exitChannel = true;
                                                                    break;
                                                                }
                                                                channel = Integer.parseInt(porta);
                                                                System.out.println("Entrou no canal 3 com sucesso!");
                                                                System.out.println(Menu.getChannelMenu(channel));
                                                                out.println("/entrar " + channel);

                                                                receiver = new MulticastReceiver();
                                                                Thread receiverSupervisorThread = new Thread(receiver::listeningChannelSupervisores);
                                                                receiverSupervisorThread.start();

                                                                while (!exitChannel) {
                                                                    System.out.print("Comando ou mensagem (/exit para sair): ");
                                                                    String userCommand = scanner.nextLine();
                                                            
                                                                    if (userCommand.equalsIgnoreCase("/exit")) {
                                                                        out.println("/sair " + channel);
                                                                        exitChannel = true;
                                                            
                                                                        // Interrompe a thread de escuta e sai do canal multicast
                                                                        receiver.stopListening();
                                                            
                                                                        channel = 99; // Volta ao estado de não estar num canal
                                                                        System.out.println("Você saiu do canal " + porta);
                                                                        System.out.println(Menu.getChannelMenu(channel));
                                                                    } else {
                                                                        out.println("/enviarcanal " + channel + " " + userCommand);
                                                                    }
                                                                }
                                                                break;
                                            
                                                            case "4":
                                                                if (authenticatedUser.getUserRole(loggedUserName) != UserRoles.OPERADOR) {
                                                                    System.out.println("Erro: Apenas operadores podem entrar neste canal.");
                                                                    exitChannel = true;
                                                                    break;
                                                                }
                                                                channel = Integer.parseInt(porta);
                                                                System.out.println("Entrou no canal 4 com sucesso!");
                                                                System.out.println(Menu.getChannelMenu(channel));
                                                                out.println("/entrar " + channel);
                                            
                                                                receiver = new MulticastReceiver();
                                                                Thread receiverOperadorThread = new Thread(receiver::listeningChannelOperadores);
                                                                receiverOperadorThread.start();

                                                                while (!exitChannel) {
                                                                    System.out.print("Comando ou mensagem (/exit para sair): ");
                                                                    String userCommand = scanner.nextLine();
                                                            
                                                                    if (userCommand.equalsIgnoreCase("/exit")) {
                                                                        out.println("/sair " + channel);
                                                                        exitChannel = true;
                                                            
                                                                        // Interrompe a thread de escuta e sai do canal multicast
                                                                        receiver.stopListening();
                                                            
                                                                        channel = 99; // Volta ao estado de não estar num canal
                                                                        System.out.println("Você saiu do canal " + porta);
                                                                        System.out.println(Menu.getChannelMenu(channel));
                                                                    } else {
                                                                        out.println("/enviarcanal " + channel + " " + userCommand);
                                                                    }
                                                                }
                                                                break;
                                                        }
                                                    }
                                                } else {
                                                    System.out.println("Erro: O canal com a porta " + porta + " não existe.");
                                                }
                                            }                                            
                                        } else {
                                            System.out.println("Comando inválido. Tente novamente.");
                                        }
                                    } while (true);
                                } else if (option == 4) {
                                    out.println("/notificacoes");
                                    System.out.println("Notificações recentes:");

                                    while ((serverResponse = in.readLine()) != null) {
                                        if (serverResponse.equals("FIM_DE_NOTIFICACOES"))
                                            break;
                                        System.out.println(serverResponse);
                                    }
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        System.out.println("Erro de interrupção: " + e.getMessage());
                                    }
                                } else if (option == 5 && authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR){
                                    /**
                                     * ! ENVIAR UMA NOTIFICAÇÃO
                                     */
                                    System.out.print(Menu.getNotificationSendMessage());
                                    String notificationMessage = scanner.nextLine();
                                    
                                    if (notificationMessage.trim().isEmpty()) {
                                        System.out.println("Erro: A mensagem de notificação não pode estar vazia.");
                                    } else {
                                        out.println("/notificar " + notificationMessage); 
                                        System.out.println("Notificação enviada com sucesso!");
                                    }
                                    System.out.println("----------------------------------------------------------------------------------------------");
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        System.out.println("Erro de interrupção: " + e.getMessage());
                                    }
                                } else {
                                    System.out.println("Opção inválida. Escolha novamente.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Erro: Por favor, insira um número válido!");
                            }
                        }
                    } while (authenticated);
                }
            } 
        } catch (SocketException e) {
            System.out.println("Conexão encerrada abruptamente por um cliente.");
        } catch (IOException  e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("Entrada fechada. Finalizando o cliente.");
        }
    }
}
