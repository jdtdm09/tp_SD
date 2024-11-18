package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastNotificationService {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 4446;
    private InetAddress groupAddress;
    private MulticastSocket socket;

    public MultiCastNotificationService() throws IOException {
        // Configurar o endereço de grupo e o socket multicast
        groupAddress = InetAddress.getByName(MULTICAST_GROUP);
        socket = new MulticastSocket();
    }

    public void sendNotification(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, groupAddress, PORT);
            socket.send(packet);
            System.out.println("Mensagem multicast enviada: " + message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }

    public void close() {
        socket.close();
    }
}
