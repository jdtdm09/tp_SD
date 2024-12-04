package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
@SuppressWarnings("deprecation")

public class MulticastReceiver {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final String MULTICAST_CHANNEL_GERAL = "230.0.0.2";
    private static final String MULTICAST_CHANNEL_COORDENADOR = "230.0.0.3";
    private static final String MULTICAST_CHANNEL_SUPERVISOR = "230.0.0.4";
    private static final String MULTICAST_CHANNEL_OPERADOR = "230.0.0.5";
    private static final int PORT = 4446;
    private boolean running = true;

    
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

    public void listeningChannelGeral() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_CHANNEL_GERAL);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }

            socket.leaveGroup(group);
        } catch (IOException e) {
            if (running) {
                System.err.println("Erro no receptor multicast: " + e.getMessage());
            }
        }
    }

    public void listeningChannelCoordenadores() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_CHANNEL_COORDENADOR);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }

            socket.leaveGroup(group);
        } catch (IOException e) {
            if (running) {
                System.err.println("Erro no receptor multicast: " + e.getMessage());
            }
        }
    }

    public void listeningChannelSupervisores() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_CHANNEL_SUPERVISOR);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }

            socket.leaveGroup(group);
        } catch (IOException e) {
            if (running) {
                System.err.println("Erro no receptor multicast: " + e.getMessage());
            }
        }
    }

    public void listeningChannelOperadores() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_CHANNEL_OPERADOR);
            socket.joinGroup(group);

            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }

            socket.leaveGroup(group);
        } catch (IOException e) {
            if (running) {
                System.err.println("Erro no receptor multicast: " + e.getMessage());
            }
        }
    }

    public void stopListening() {
        running = false;
    }
}
