package Models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class User {
    private int Id;
    private String username;
    private String password;
    private UserRoles role;

    // Construtor
    public User(int Id, String username, String password, UserRoles role) {
        this.Id = Id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String username) {
        this.username = username;
        this.role = getUserRole(username);
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    // Getters e Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRoles getRole() {
        return role;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public UserRoles getUserRole(String loggedUserName) {
        // Caminho do ficheiro
        String filePath = "users.txt";

        // Tenta carregar o utilizador a partir do ficheiro
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = parseUser(line);
                if (user != null && user.getUsername().equals(loggedUserName)) {
                    return user.getRole(); // Retorna o role se encontrar o username
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao carregar utilizadores: " + e.getMessage());
        }

        // Se não encontrar o utilizador, retorna null ou algum valor default
        return null; // Ou pode lançar uma exceção, se necessário
    }

    // Converte uma linha JSON para um objeto User
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
}
