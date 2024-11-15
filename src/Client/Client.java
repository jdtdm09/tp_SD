package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import Logs.Logger;

public class Client extends Thread{
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4840;
    private boolean authenticated = false; // Variável que controla o estado de autenticação
    private String loggedUserName; // Nome de utilizador autenticado

    public void start() {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Conectado ao servidor.");
            System.out.println("Mensagem do servidor: " + in.readLine());  // Mensagem inicial

            String message;
            int option;
            while (true) {
                // Se não estiver autenticado, pede login ou registo
                if (!authenticated) {
                    System.out.println("Digite 'register <username> <password> <cargo>' para registo");
                    System.out.println("Cargos: 'coordenador', 'supervisor', 'operador'");
                    System.out.println("Digite 'login <username> <password>' para autenticação");
                    System.out.println("Digite 'sair' para terminar o programa");
                    message = scanner.nextLine();
                    out.println(message);  // Envia o comando ao servidor

                    String response = in.readLine();  // Resposta do servidor
                    System.out.println("Resposta do servidor: " + response);

                    if (response.startsWith("A terminar cliente")) {
                        System.exit(0);  // Atualiza o estado de autenticação
                    }

                    // Verifica se o login foi bem-sucedido
                    if (response.startsWith("Login realizado com sucesso")) {
                        authenticated = true;  // Atualiza o estado de autenticação
                        String[] parts = response.split(", ");
                        loggedUserName = parts[1].trim();  // Atribui a parte do username após "Bem-vindo, "
                    }
                } else {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrompida: " + e.getMessage());
                    }

                    option = 999;
                    do {
                        System.out.println("==============================================");
                        System.out.println("Menu: ");
                        System.out.println("1 - Mensagens");
                        System.out.println("2 - Canais");
                        System.out.println("0 - Sair");
                        System.out.println("==============================================");
                        option = Integer.parseInt(scanner.nextLine());

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.out.println("Thread interrompida: " + e.getMessage());
                        }
                        
                        if (option == 0) {
                            authenticated = false;
                            Logger.log(loggedUserName + " desconectou-se");
                            System.exit(0);
                        } else if (option == 1) {
                            System.out.println("/mensagens para ler mensagens");
                            System.out.println("/enviar 'user' 'mensagem' para enviar mensagens ");
                            System.out.println("/exit para voltar");
                            do {
                                message = scanner.nextLine();
    
                                if (message.startsWith("/mensagens")) {
                                    String[] parts = message.split(" ", 2); // Divide o comando e o argumento
                                    if (parts.length == 2) {
                                        String recipient = parts[1];  // O destinatário especificado
                                        out.println("/mensagens " + recipient);  // Envia comando ao servidor
                                        System.out.println("Pedido de mensagens de " + loggedUserName + " com " + recipient);
                                
                                        System.out.println("Mensagens recentes de " + recipient + ":");
                                        String serverResponse;
                                        while ((serverResponse = in.readLine()) != null) {
                                            if (serverResponse.equals("FIM_DE_MENSAGENS")) {
                                                break; // Termina a leitura quando o servidor indica o fim das mensagens
                                            }
                                            System.out.println(serverResponse); // Exibe cada mensagem recebida
                                        }
                                        System.out.println("Fim das mensagens recentes.");
                                    } else if (parts.length == 1) {
                                        out.println("/mensagens");
                                        System.out.println("Pedido de mensagens de " + loggedUserName);
                                
                                        System.out.println("Mensagens recentes:");
                                        String serverResponse;
                                        while ((serverResponse = in.readLine()) != null) {
                                            if (serverResponse.equals("FIM_DE_MENSAGENS")) {
                                                break; // Termina a leitura quando o servidor indica o fim das mensagens
                                            }
                                            System.out.println(serverResponse); // Exibe cada mensagem recebida
                                        }
                                        System.out.println("Fim das mensagens recentes.");
                                    } else {
                                        System.out.println("Formato inválido. Use: /mensagens ou /mensagens 'user'");
                                    }
                                } else if (message.startsWith("/enviar")) {
                                    String[] parts = message.split(" ", 3);
                                    if (parts.length < 3) {
                                        System.out.println("Formato inválido. Use: /enviar 'user' 'mensagem'");
                                    } else {
                                        String recipient = parts[1];
                                        String userMessage = parts[2];
                                        out.println("/enviar " + recipient + " " + userMessage);  // Envia comando ao servidor
                                        System.out.println("Mensagem enviada para " + recipient);
                                    }
                                } else if (message.equalsIgnoreCase("/exit")) {
                                    break;
                                } else {
                                    System.out.println("Por favor introduza 1 comando válido");
                                }
                            } while (true);
                        } else if (option == 2) {
                            System.out.println("");
                            System.out.println(" ");
                            System.out.println("/exit");
                            do {
                                message = scanner.nextLine();
    
                                if (message.equals("")) {
                                    


                                } else if (message.startsWith("")) {
                                    
                                    

                                } else if (message.startsWith("")) {
                                    
                                    
                                
                                } else if (message.equalsIgnoreCase("/exit")) {
                                    break;
                                } else {
                                    System.out.println("Por favor introduza 1 comando válido");
                                }
                            } while (true);
                        }
                    } while (authenticated);
                    
                    // out.println(option);  // Envia a mensagem ao servidor

                    // String response = in.readLine();  // Resposta do servidor
                    // System.out.println("Resposta do servidor: " + response);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
