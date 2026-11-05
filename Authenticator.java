import java.util.ArrayList;

class AuthService {
    private ArrayList<User> users = new ArrayList<>();

    public AuthService() {
        users.add(new User("operator1", "1984", Role.OPERATOR));
        users.add(new User("manager1", "fishes", Role.MANAGER));
        users.add(new User("provider1", "HotPotato", Role.PROVIDER));
    }

    public User login(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)
                    && user.checkPassword(password)) {
                return user;
            }
        }

        return null;
    }
}