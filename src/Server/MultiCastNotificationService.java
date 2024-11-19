package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiCastNotificationService {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final String MULTICAST_CHANNEL_GERAL = "230.0.0.2";
    private static final String MULTICAST_CHANNEL_COORDENADOR = "230.0.0.3";
    private static final String MULTICAST_CHANNEL_SUPERVISOR = "230.0.0.4";
    private static final String MULTICAST_CHANNEL_OPERADOR = "230.0.0.5";
    private static final int PORT = 4446;
    private InetAddress groupAddress;
    private InetAddress channelGeralAddress;
    private InetAddress channelCoordenadorAddress;
    private InetAddress channelSupervisorAddress;
    private InetAddress channelOperadorAddress;
    private MulticastSocket socket;

    public MultiCastNotificationService() throws IOException {
        groupAddress = InetAddress.getByName(MULTICAST_GROUP);
        channelGeralAddress = InetAddress.getByName(MULTICAST_CHANNEL_GERAL);
        channelCoordenadorAddress = InetAddress.getByName(MULTICAST_CHANNEL_COORDENADOR);
        channelSupervisorAddress = InetAddress.getByName(MULTICAST_CHANNEL_SUPERVISOR);
        channelOperadorAddress = InetAddress.getByName(MULTICAST_CHANNEL_OPERADOR);
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

    public void sendToGroupGeral(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, channelGeralAddress, PORT);
            socket.send(packet);
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }

    public void sendToGroupCoordenadores(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, channelCoordenadorAddress, PORT);
            socket.send(packet);
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }

    public void sendToGroupSupervisores(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, channelSupervisorAddress, PORT);
            socket.send(packet);
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }

    public void sendToGroupOperadores(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, channelOperadorAddress, PORT);
            socket.send(packet);
            System.out.println(message);
        } catch (IOException e) {
            System.err.println("Erro ao enviar mensagem multicast: " + e.getMessage());
        }
    }

    public void close() {
        socket.close();
    }
}
