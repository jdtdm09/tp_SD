package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Logs.ChannelLogger;

public class ChannelMessageService {
    private static final String MESSAGE_HISTORY_DIR = "src/Logs/ChannelLogs/";
    private static final int MESSAGE_LIMIT = 10;  
    private static final String CHAT_GERAL = "chatGeral.txt";
    private static final String CHAT_COORDENADOR = "chatCoordenador.txt";
    private static final String CHAT_SUPERVISOR = "chatSupervisor.txt";
    private static final String CHAT_OPERADOR = "schatOperador.txt";

    public synchronized void sendMessage(int channel, String message, String username) {
        String fileName;
        switch (channel) {
            case 1:
                fileName = CHAT_GERAL;
                break;
            case 2:
                fileName = CHAT_COORDENADOR;
                break;
            case 3:
                fileName = CHAT_SUPERVISOR;
                break;
            case 4:
                fileName = CHAT_OPERADOR;
                break;
            default:
                System.out.println("Erro: Canal inválido.");
                return;
        }

        ChannelLogger.logMessage(fileName, message, username);
    }

    public synchronized List<String> getChannelHistory(int channelId) {
        List<String> channelHistory = new ArrayList<>();
        String fileName;

        switch (channelId) {
            case 1:
                fileName = CHAT_GERAL;
                break;
            case 2:
                fileName = CHAT_COORDENADOR;
                break;
            case 3:
                fileName = CHAT_SUPERVISOR;
                break;
            case 4:
                fileName = CHAT_OPERADOR;
                break;
            default:
                System.out.println("Erro: Canal inválido.");
                return channelHistory;
        }

        File channelFile = new File(MESSAGE_HISTORY_DIR, fileName);

        if (channelFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(channelFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    channelHistory.add(line);
                    if (channelHistory.size() >= MESSAGE_LIMIT) {
                        return channelHistory;
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro ao ler o histórico de mensagens do canal: " + e.getMessage());
            }
        } else {
            System.out.println("Nenhum histórico encontrado para o canal " + channelId + ".");
        }

        return channelHistory;
    }
}
