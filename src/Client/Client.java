package Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
    private boolean authenticated = false; // Variável que controla o estado de autenticação
    private String loggedUserName; // Nome de utilizador autenticado
    private User authenticatedUser; // Utilizador autenticado
    MultiCastReceiver receiver = new MultiCastReceiver();

    private boolean userExists(String userId) {
    List<String> allUsers = loadUsersFromFile(); 
    return allUsers.contains(userId); 
    }

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
    

    public void start() {
        new Thread(receiver::startListening).start();
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            System.out.println("------------------------------------------------------------------");
            System.out.println("Conectado. " + in.readLine());
            System.out.println("------------------------------------------------------------------");

            String message;
            int option;
            while (true) {
                if (!authenticated) {
                    System.out.println("                           AUTENTICAÇÃO                           ");
                    System.out.println("------------------------------------------------------------------");
                    System.out.println("Escolha uma das opções abaixo:");
                    System.out.println();
                    System.out.println("- Registar-se:");
                    System.out.println("    Comando: 'register <username> <password> <cargo>'");
                    System.out.println("    Cargos Disponíveis: 'coordenador', 'supervisor', 'operador'");
                    System.out.println();
                    System.out.println("- Fazer Login:");
                    System.out.println("    Comando: 'login <username> <password>'");
                    System.out.println();
                    System.out.println("- Sair do sistema:");
                    System.out.println("    Comando: 'sair'");
                    System.out.println();
                    System.out.println("__________________________________________________________________");
                    System.out.print("Insira o comando: ");
                    
                    message = scanner.nextLine();
                    out.println(message);

                    String response = in.readLine();
                    System.out.println("Resposta do servidor: " + response);

                    if (response.startsWith("A terminar cliente")) {
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

                    do {
                        System.out.println("==============================================================================================");
                        System.out.println("                        SISTEMA DE GESTÃO DE COMUNICAÇÕES E OPERAÇÕES                         ");
                        System.out.println("==============================================================================================");
                        System.out.println("1 - Mensagens");
                        System.out.println("2 - Canais");
                        System.out.println("3 - Notificações");
                        if (authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                            System.out.println("4 - Enviar Notificação");
                        }
                        System.out.println("0 - Sair");
                        System.out.println("----------------------------------------------------------------------------------------------");
                        System.out.print("Escolha uma opção: ");
                        String input = scanner.nextLine().trim();
                    
                        if (input.isEmpty()) {
                            System.out.println("Opção inválida. Por favor, insira uma opção.");
                        } else {
                            try {
                                option = Integer.parseInt(input);

                                if (option == 0) { 
                                    out.println("sair");
                                    authenticated = false;
                                    Logger.log(loggedUserName + " desconectou-se");
                                    try {
                                        socket.close();  // Fechar a conexão de rede com o servidor
                                    } catch (IOException e) {
                                        System.out.println("Erro ao fechar a conexão com o servidor: " + e.getMessage());
                                    }
                                    System.exit(0);
                                } else if (option == 1) {
                                    System.out.println("==============================================================================================");
                                    System.out.println("                                     SISTEMA DE MENSAGENS                                     ");
                                    System.out.println("==============================================================================================");
                                    System.out.println("/mensagens                -> Ler mensagens");
                                    System.out.println("/enviar <user> <mensagem> -> Enviar mensagem");
                                    System.out.println("/exit                     -> Voltar ao menu anterior");
                                    System.out.println("----------------------------------------------------------------------------------------------");
                    
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
                                            String serverResponse;
                                            while ((serverResponse = in.readLine()) != null) {
                                                if (serverResponse.equals("FIM_DE_MENSAGENS"))
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
                                                    out.println("Erro: O destinatário " + recipient + " não existe.");
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
                                    System.out.println("==============================================================================================");
                                    System.out.println("                                       SISTEMA DE CANAIS                                      ");
                                    System.out.println("==============================================================================================");
                                    System.out.println("Opções de canais disponíveis em breve...");
                                    System.out.println("/exit -> Voltar ao menu anterior");
                                    System.out.println("----------------------------------------------------------------------------------------------");
                    
                                    do {
                                        System.out.print("Comando: ");
                                        message = scanner.nextLine();
                    
                                        if (message.equalsIgnoreCase("/exit")) {
                                            break;
                                        } else {
                                            System.out.println("Comando inválido ou ainda não implementado.");
                                        }
                                    } while (true);
                                } else if (option == 3) {
                                    out.println("/notificacoes");
                                    System.out.println("Notificações recentes:");

                                    String serverResponse;
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
                                } else if (option == 4 && authenticatedUser != null && authenticatedUser.getUserRole(loggedUserName) == UserRoles.COORDENADOR) {
                                    System.out.println("==============================================================================================");
                                    System.out.println("                                      ENVIAR NOTIFICAÇÃO                                      ");
                                    System.out.println("==============================================================================================");
                                    System.out.println("Digite a mensagem de notificação para enviar:");
                                    
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
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("Entrada fechada. Finalizando o cliente.");
        }
    }
}
