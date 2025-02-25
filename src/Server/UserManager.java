package Server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import Models.User;
import Models.UserRoles;

public class UserManager {
    private static final String FILE_PATH = "users.txt";
    private List<User> users;

    public UserManager() {
        users = loadUsers();
    }

    private List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return userList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = parseUser(line);
                if (user != null) {
                    userList.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar utilizadores: " + e.getMessage());
        }

        return userList;
    }

    private void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                writer.println(formatUser(user));
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar utilizadores: " + e.getMessage());
        }
    }

    private int getNextId() {
        int maxId = users.stream().mapToInt(User::getId).max().orElse(0);
        return maxId + 1;
    }

    public synchronized String registerUser(String username, String password, String role) {
        if (users.stream().anyMatch(user -> user.getUsername().equals(username))) {
            return "Utilizador já existente";
        }

        UserRoles userRole;
        try {
            userRole = UserRoles.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Role inválido. Usa: Coordenador, Supervisor, Operador");
            return "Cargo inválido";
        }

        User newUser = new User(getNextId(), username, password, userRole);
        users.add(newUser);
        saveUsers();
        return "Utilizador registado";
    }

    private User parseUser(String json) {
        json = json.trim();
        if (json.isEmpty()) return null;

        try {
            int id = Integer.parseInt(json.split("\"id\": ")[1].split(",")[0].trim());
            String username = json.split("\"username\": \"")[1].split("\"")[0];
            String password = json.split("\"password\": \"")[1].split("\"")[0];
            String roleStr = json.split("\"role\": \"")[1].split("\"")[0];
            UserRoles role = UserRoles.valueOf(roleStr);

            return new User(id, username, password, role);
        } catch (Exception e) {
            System.out.println("Erro ao parsear utilizador: " + e.getMessage());
            return null;
        }
    }

    private String formatUser(User user) {
        return String.format("{\"id\": %d, \"username\": \"%s\", \"password\": \"%s\", \"role\": \"%s\"}",
                user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }

    public synchronized boolean loginUser(String username, String password) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username) && user.getPassword().equals(password));
    }
    
}
