import java.util.ArrayList;

/**
 * Handles authentication for ChocAn Operators, Managers, and Providers.
 * Supports both username/password credentials and direct 9-digit Provider Number terminal login
 * (including any new Provider created by an Operator).
 *
 * @author Simeon (Team 15 - Backend & Database)
 */
public class AuthService {
    private final ArrayList<User> users = new ArrayList<>();

    public AuthService() {
        users.add(new User("operator1", "1984", Role.OPERATOR));
        users.add(new User("manager1", "fishes", Role.MANAGER));
        users.add(new User("provider1", "HotPotato", Role.PROVIDER, "111222333"));
        users.add(new User("provider2", "CocoaCare", Role.PROVIDER, "444555666"));
    }

    /**
     * Authenticates a user via username/password or 9-digit Provider Number.
     * Per ChocAn Appendix A specification:
     * "When a provider's terminal is switched on, the provider is asked to enter his or her provider number."
     * Any provider added by an Operator can log in directly using their 9-digit Provider Number
     * (or their Provider Name with their 9-digit Provider ID as the password).
     */
    public User login(String username, String password) {
        if (username == null) {
            return null;
        }
        String cleanUser = username.trim();
        String cleanPass = password != null ? password.trim() : "";

        // 1. Check built-in accounts first
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(cleanUser)
                    && user.checkPassword(cleanPass)) {
                return user;
            }
        }

        // 2. Direct 9-digit Provider Number login (works for any pre-existing or Operator-created Provider)
        if (cleanUser.matches("\\d{9}")) {
            Provider p = Operator.findProviderById(cleanUser);
            if (p != null) {
                return new User(p.getName() + " [" + p.getProviderId() + "]", cleanPass, Role.PROVIDER, p.getProviderId());
            }
        }

        // 3. Provider Name login (if an Operator created "Dr. Smith" with ID "777888999",
        //    allow logging in with username "Dr. Smith" and password "777888999" or "HotPotato")
        for (Provider p : Operator.getProviders()) {
            if (p.getName().equalsIgnoreCase(cleanUser)) {
                if (cleanPass.isEmpty() || cleanPass.equals(p.getProviderId()) || cleanPass.equalsIgnoreCase("HotPotato")) {
                    return new User(p.getName() + " [" + p.getProviderId() + "]", cleanPass, Role.PROVIDER, p.getProviderId());
                }
            }
        }

        // 4. Query the cloud database Operator table when running in Neon Database Mode
        if (Operator.isUseDatabase() && !cleanPass.isEmpty() && ChocAN_DB.authenticateOperator(cleanUser, cleanPass)) {
            return new User(cleanUser, cleanPass, Role.OPERATOR);
        }

        return null;
    }
}
