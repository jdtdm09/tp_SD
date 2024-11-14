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
    
                                if (message.equals("/mensagens")) {
                                    
                                    System.out.println("O utilizador pediu para ver as mensagens");

                                } else if (message.startsWith("/enviar")) {
                                    
                                    

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
