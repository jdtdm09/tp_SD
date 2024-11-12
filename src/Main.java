import Client.Client;
import Server.Server;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String mode;

        // Verifica se algum argumento foi passado; caso contrário, pede ao utilizador
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Escolha o modo de execução: 'server' ou 'client'");
            mode = scanner.nextLine().toLowerCase();
        } else {
            mode = args[0].toLowerCase();
        }

        switch (mode) {
            case "server":
                startServer();
                break;
            case "client":
                startClient();
                break;
            default:
                System.out.println("Modo inválido. Use 'server' para iniciar o servidor ou 'client' para iniciar o cliente.");
                System.exit(1);
        }
    }

    private static void startServer() {
        System.out.println("Iniciando o servidor...");
        Server server = new Server();
        server.start();
    }

    private static void startClient() {
        System.out.println("Iniciando o cliente...");
        Client client = new Client();
        client.start();
    }
}