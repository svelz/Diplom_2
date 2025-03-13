package model;

import java.util.UUID;

public class User {
    private String email;
    private String password;
    private String name;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static User generateRandomUser() {
        return new User(UUID.randomUUID() + "@mail.com", "testpassword", "TestUser");
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
}
