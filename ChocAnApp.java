import java.util.Scanner;

public class ChocAnApp {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        AuthService auth = new AuthService();

        System.out.println("Welcome to ChocAn!");

        System.out.print("Username: ");
        String username = input.nextLine();

        System.out.print("Password: ");
        String password = input.nextLine();

        User user = auth.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + user.getUsername());

        switch (user.getRole()) {
            case OPERATOR:
                operatorMenu(input);
                break;

            case MANAGER:
                managerMenu(input);
                break;

            case PROVIDER:
                providerMenu(input);
                break;
        }

        input.close();
    }

    // Operator menu
    public static void operatorMenu(Scanner input) {
        System.out.println("\n--- OPERATOR MENU ---");
        System.out.println("1. Add Member");
        System.out.println("2. Update Member");
        System.out.println("3. Delete Member");
        System.out.println("4. Add Provider");
        System.out.println("5. Update Provider");
        System.out.println("6. Delete Provider");
        System.out.println("7. Logout");

        System.out.print("Choose an option: ");
        int choice = input.nextInt();

        switch (choice) {
            case 1:
                System.out.println("Adding member...");
                break;
            case 2:
                System.out.println("Updating member...");
                break;
            case 3:
                System.out.println("Deleting member...");
                break;
            case 4:
                System.out.println("Adding provider...");
                break;
            case 5:
                System.out.println("Updating provider...");
                break;
            case 6:
                System.out.println("Deleting provider...");
                break;
            case 7:
                System.out.println("Logging out...");
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Manager menu
    public static void managerMenu(Scanner input) {
        System.out.println("\n--- MANAGER MENU ---");
        System.out.println("1. Generate Member Report");
        System.out.println("2. Generate Provider Report");
        System.out.println("3. Generate Summary Report");
        System.out.println("4. Logout");

        System.out.print("Choose an option: ");
        int choice = input.nextInt();

        switch (choice) {
            case 1:
                System.out.println("Generating member report...");
                break;
            case 2:
                System.out.println("Generating provider report...");
                break;
            case 3:
                System.out.println("Generating summary report...");
                break;
            case 4:
                System.out.println("Logging out...");
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // Provider menu
    public static void providerMenu(Scanner input) {
        System.out.println("\n--- PROVIDER MENU ---");
        System.out.println("1. Validate Member");
        System.out.println("2. Submit Service Record");
        System.out.println("3. Logout");

        System.out.print("Choose an option: ");
        int choice = input.nextInt();

        switch (choice) {
            case 1:
                System.out.println("Validating member...");
                break;
            case 2:
                System.out.println("Submitting service record...");
                break;
            case 3:
                System.out.println("Logging out...");
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
}