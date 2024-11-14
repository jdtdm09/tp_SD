package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import Logs.Logger;

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
                    // Caso o utilizador esteja autenticado, processa outras mensagens
                    System.out.println("Mensagem recebida de " + username + ": " + message);
                    out.println("Mensagem recebida: " + message); // Envia de volta a confirmação
                    Logger.log("Mensagem recebida de " + username + ": " + message);
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
