package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastReceiver {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 4446;

    public void startListening() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            socket.joinGroup(group);

            System.out.println("Cliente está ouvindo mensagens multicast...");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println();
                System.out.println("==============================================");
                System.out.println("  Nova Notificação: " + message);
                System.out.println("==============================================");
            }
        } catch (IOException e) {
            System.err.println("Erro no recebimento multicast: " + e.getMessage());
        }
    }
}
