import java.util.ArrayList;

public class AuthService {
    private ArrayList<User> users = new ArrayList<>();

    public AuthService() {
        users.add(new User("operator1", "1984", Role.OPERATOR));
        users.add(new User("manager1", "fishes", Role.MANAGER));
        users.add(new User("provider1", "HotPotato", Role.PROVIDER));
    }

    public User login(String username, String password) {
        // Check local users first
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.checkPassword(password)) {
                return user;
            }
        }

        // Only query the cloud database Operator table when running in Neon Database Mode
        if (Operator.isUseDatabase() && ChocAN_DB.authenticateOperator(username, password)) {
            return new User(username, password, Role.OPERATOR);
        }

        return null;
    }
}
