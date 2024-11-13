package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private UserManager userManager;
    private boolean authenticated = false; // Variável para controlar o estado de autenticação
    private String username; // Nome de utilizador autenticado

    public ClientHandler(Socket clientSocket, UserManager userManager) {
        this.clientSocket = clientSocket;
        this.userManager = userManager;
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
                        if (parts.length == 3) {
                            String username = parts[1];
                            String password = parts[2];
                            if (userManager.registerUser(username, password)) {
                                out.println("Registo realizado com sucesso!");
                            } else {
                                out.println("Nome de utilizador já existe.");
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
                            } else {
                                out.println("Credenciais inválidas.");
                            }
                        } else {
                            out.println("Formato de login inválido. Use: login <username> <password>");
                        }
                    } else {
                        out.println("Comando não reconhecido. Faça login para continuar.");
                    }
                } else {
                    // Caso o utilizador esteja autenticado, processa outras mensagens
                    System.out.println("Mensagem recebida de " + username + ": " + message);
                    out.println("Mensagem recebida: " + message); // Envia de volta a confirmação
                }
            }
        } catch (IOException e) {
            System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar a conexão do cliente: " + e.getMessage());
            }
        }
    }
}
