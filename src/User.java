/**
 * Represents an authenticated user session in the ChocAn terminal system.
 * For Provider terminals, also stores the active 9-digit Provider Number.
 *
 * @author Daniel (Team 15 - Frontend & UML)
 */
public class User {
    private final String username;
    private final String password;
    private final Role role;
    private String providerId; // Associated 9-digit Provider ID for PROVIDER role

    public User(String username, String password, Role role) {
        this(username, password, role, role == Role.PROVIDER ? "111222333" : null);
    }

    public User(String username, String password, Role role, String providerId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.providerId = providerId;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public Role getRole() {
        return this.role;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
