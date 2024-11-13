package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 4840;
    private boolean authenticated = false; // Variável que controla o estado de autenticação

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
            while (true) {
                // Se não estiver autenticado, pede login ou registo
                if (!authenticated) {
                    System.out.print("Digite 'register <username> <password>' para registo ou 'login <username> <password>' para autenticação: ");
                    message = scanner.nextLine();
                    out.println(message);  // Envia o comando ao servidor

                    String response = in.readLine();  // Resposta do servidor
                    System.out.println("Resposta do servidor: " + response);

                    // Verifica se o login foi bem-sucedido
                    if (response.startsWith("Login realizado com sucesso")) {
                        authenticated = true;  // Atualiza o estado de autenticação
                    }
                } else {
                    System.out.print("Digite uma mensagem para enviar ao servidor: ");
                    message = scanner.nextLine();
                    out.println(message);  // Envia a mensagem ao servidor

                    String response = in.readLine();  // Resposta do servidor
                    System.out.println("Resposta do servidor: " + response);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
