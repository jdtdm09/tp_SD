package Models;

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
}
