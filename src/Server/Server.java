package Server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 4840;
    private UserManager userManager = new UserManager(); 

    public void start() {
        new Thread(new ReportService()).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.out.println("Servidor iniciado na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, userManager)).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}

