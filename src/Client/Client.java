package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import Logs.Logger;
import Models.User;
import Models.UserRoles;

public class Client extends Thread {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4840;
    private boolean authenticated = false; // Variável que controla o estado de autenticação
    private String loggedUserName; // Nome de utilizador autenticado
    private User authenticatedUser; // Utilizador autenticado

    public void start() {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            System.out.println("------------------------------------------------------------------");
            System.out.println("Conectado. " + in.readLine());
            System.out.println("------------------------------------------------------------------"); // Mensagem inicial

            String message;
            int option;
            while (true) {
                // Se não estiver autenticado, pede login ou registo
                if (!authenticated) {
                    System.out.println("                           AUTENTICAÇÃO         ");
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
                    System.out.print("Insira o comando desejado: ");
                    message = scanner.nextLine();
                    out.println(message); // Envia o comando ao servidor

                    String response = in.readLine(); // Resposta do servidor
                    System.out.println("Resposta do servidor: " + response);

                    if (response.startsWith("A terminar cliente")) {
                        System.exit(0); // Termina a aplicação
                    }

                    if (response.startsWith("Login realizado com sucesso")) {
                        authenticated = true; // Atualiza o estado de autenticação
                        String[] parts = response.split(", ");
                        loggedUserName = parts[1].trim(); // Extrai o nome de utilizador
                        authenticatedUser = new User(loggedUserName);
                    }
                } else {
                    // Pausa breve para melhorar a experiência do utilizador
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
                        String input = scanner.nextLine().trim(); // Lê e remove espaços em branco extras
                    
                        // Verifica se a entrada não está vazia e é um número válido
                        if (input.isEmpty()) {
                            System.out.println("Opção inválida. Por favor, insira uma opção.");
                        } else {
                            try {
                                option = Integer.parseInt(input); // Tenta converter a entrada para número
                                if (option == 0) {
                                    authenticated = false;
                                    Logger.log(loggedUserName + " desconectou-se");
                                    System.exit(0);
                                } else if (option == 1) {
                                    System.out.println("==============================================================================================");
                                    System.out.println("                                   SISTEMA DE MENSAGENS                           ");
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
                                                String recipient = parts[1]; // Destinatário especificado
                                                out.println("/mensagens " + recipient);
                                                System.out.println("Mensagens recentes de " + recipient + ":");
                                            } else {
                                                out.println("/mensagens");
                                                System.out.println("Mensagens recentes:");
                                            }
                                            // Lê e apresenta mensagens do servidor
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
                                                out.println("/enviar " + recipient + " " + userMessage);
                                                System.out.println("Mensagem enviada para " + recipient);
                                            }
                                        } else if (message.equalsIgnoreCase("/exit")) {
                                            break;
                                        } else {
                                            System.out.println("Comando inválido. Tente novamente.");
                                        }
                                    } while (true);
                                } else if (option == 2) {
                                    System.out.println("==============================================================================================");
                                    System.out.println("                                     SISTEMA DE CANAIS               ");
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
                                    System.out.println("                                    ENVIAR NOTIFICAÇÃO                ");
                                    System.out.println("==============================================================================================");
                                    System.out.println("Digite a mensagem de notificação para enviar:");
                                    
                                    String notificationMessage = scanner.nextLine();
                                    
                                    if (notificationMessage.trim().isEmpty()) {
                                        System.out.println("Erro: A mensagem de notificação não pode estar vazia.");
                                    } else {
                                        out.println("/notificar " + notificationMessage);  // Envia a notificação ao servidor
                                        System.out.println("Notificação enviada com sucesso!");
                                    }
                                    System.out.println("----------------------------------------------------------------------------------------------");
                                } else {
                                    System.out.println("Opção inválida. Escolha novamente.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Erro: Por favor, insira um número válido!");
                            }
                        }
                    } while (authenticated);
                    

                    // out.println(option); // Envia a mensagem ao servidor

                    // String response = in.readLine(); // Resposta do servidor
                    // System.out.println("Resposta do servidor: " + response);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
