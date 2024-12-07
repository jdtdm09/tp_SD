package Server;

import Models.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RequestService {
    private static final String FILE_PATH = "pedidos.txt";
    private List<Request> requests;

    public RequestService() {
        requests = loadRequests();
    }

    private List<Request> loadRequests() {
        List<Request> requestList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return requestList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Request request = parseRequest(line);
                if (request != null) {
                    requestList.add(request);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar pedidos: " + e.getMessage());
        }

        return requestList;
    }

    private void saveRequests() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Request request : requests) {
                writer.println(formatRequest(request));
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar pedidos: " + e.getMessage());
        }
    }

    private int getNextId() {
        int maxId = requests.stream().mapToInt(Request::getId).max().orElse(0);
        return maxId + 1;
    }

    public synchronized void addRequest(String pedido, String criador) {
        Request newRequest = new Request(getNextId(), pedido, false, false, "", criador);
        requests.add(newRequest);
        saveRequests();
    }

    public synchronized void updateRequestStatus(int id, boolean aceite, boolean rejeitado, String coordenador) {
        boolean found = false;
    
        for (Request request : requests) {
            if (request.getId() == id) {
                System.out.println("Atualizando pedido ID: " + id);
                request.setAceite(aceite);
                request.setRejeitado(rejeitado);
                request.setCoordenador(coordenador);
                found = true;
                break; 
            }
        }
    
        if (found) {
            saveRequests(); 
            System.out.println("Pedido ID: " + id + " atualizado com sucesso.");
        } else {
            System.out.println("Pedido com ID " + id + " não encontrado.");
        }
    }
    

    public synchronized List<Request> getAllRequests() {
        return new ArrayList<>(requests);
    }

    public synchronized List<Request> getPendingRequests() {
        if (requests == null) {
            requests = loadRequests();
        }
    
        return requests.stream()
                .filter(request -> !request.isAceite() && !request.isRejeitado())
                .toList();
    }
    
    

    public synchronized Request getRequestById(int id) {
        for (Request request : requests) {
            if (request.getId() == id) {
                return request;
            }
        }
        return null;
    }

    private Request parseRequest(String json) {
        json = json.trim();
        if (json.isEmpty()) return null;

        try {
            int id = Integer.parseInt(json.split("\"Id\": ")[1].split(",")[0].trim());
            String pedido = json.split("\"pedido\": \"")[1].split("\"")[0];
            boolean aceite = Boolean.parseBoolean(json.split("\"aceite\": ")[1].split(",")[0].trim());
            boolean rejeitado = Boolean.parseBoolean(json.split("\"rejeitado\": ")[1].split(",")[0].trim());
            String coordenador = json.split("\"coordenador\": \"")[1].split("\"")[0];
            String criador = json.split("\"criador\": \"")[1].split("\"")[0];

            return new Request(id, pedido, aceite, rejeitado, coordenador, criador);
        } catch (Exception e) {
            System.out.println("Erro ao parsear pedido: " + e.getMessage());
            return null;
        }
    }

    private String formatRequest(Request request) {
        return String.format(
                "{\"Id\": %d, \"pedido\": \"%s\", \"aceite\": %b, \"rejeitado\": %b, \"coordenador\": \"%s\", \"criador\": \"%s\"}",
                request.getId(), request.getPedido(), request.isAceite(), request.isRejeitado(),
                request.getCoordenador(), request.getCriador()
        );
    }
}
