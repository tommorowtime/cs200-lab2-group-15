import java.util.Scanner;

public class ChocAnApp {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        AuthService auth = new AuthService();

        boolean appRunning = true;

        //Keeps running until someone types 'exit'
        while (appRunning) {
            System.out.println("   Welcome to ChocAn Terminal    ");
            System.out.println("==================================");

            System.out.print("Username: ");
            System.out.println("Type 'exit' to shut down.");
            String username = input.nextLine().trim();

            if (username.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down.");
                appRunning = false;
                break;
            }

            System.out.print("Password: ");
            String password = input.nextLine().trim();

            User user = auth.login(username, password);

            if (user == null) {
                System.out.println("\n Invalid username or password. Please try again.");
                continue; 
                // Return to the login prompt
            }

            System.out.println("\nLogin successful. Welcome, " + user.getUsername() + ".");

            // Role menu
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

            // Once the user chooses Logout inside any menu, it loops back to the login screen
            System.out.println("\n Logging out. Returning to login screen...");
        }

        input.close();
    }

    // Operator menu
    public static void operatorMenu(Scanner input) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n--- OPERATOR MENU ---");
            System.out.println("1. Add Member");
            System.out.println("2. Update Member");
            System.out.println("3. Delete Member");
            System.out.println("4. Add Provider");
            System.out.println("5. Update Provider");
            System.out.println("6. Delete Provider");
            System.out.println("7. Logout");

            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("Adding member...");
                    break;

                case "2":
                    System.out.println("Updating member...");
                    break;

                case "3":
                    System.out.println("Deleting member...");
                    break;

                case "4":
                    System.out.println("Adding provider...");
                    break;

                case "5":
                    System.out.println("Updating provider...");
                    break;

                case "6":
                    System.out.println("Deleting provider...");
                    break;

                case "7":
                    System.out.println("Logging out from Operator account...");
                    inMenu = false; 
                    // Exits the menu loop and returns to main()
                    break;

                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 7.");
            }
        }
    }

    // Manager menu
    public static void managerMenu(Scanner input) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n--- MANAGER MENU ---");
            System.out.println("1. Generate Member Report");
            System.out.println("2. Generate Provider Report");
            System.out.println("3. Generate Summary Report");
            System.out.println("4. Logout");

            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("Generating member report...");
                    break;

                case "2":
                    System.out.println("Generating provider report...");
                    break;

                case "3":
                    System.out.println("Generating summary report...");
                    break;

                case "4":
                    System.out.println("Logging out from Manager account...");
                    inMenu = false; // Exits the menu loop and returns to main()
                    break;

                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 4.");
            }
        }
    }

    // Provider menu
    public static void providerMenu(Scanner input) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n--- PROVIDER MENU ---");
            System.out.println("1. Validate Member");
            System.out.println("2. Submit Service Record");
            System.out.println("3. Logout");

            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("Validating member...");
                    break;

                case "2":
                    System.out.println("Submitting service record...");
                    break;

                case "3":
                    System.out.println("Logging out from Provider account...");
                    inMenu = false; // Exits the menu loop and returns to main()
                    break;

                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 3.");
            }
        }
    }
}