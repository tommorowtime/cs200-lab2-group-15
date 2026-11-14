import java.util.Scanner;

public class ChocAnApp {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        AuthService auth = new AuthService();

        selectMode(input);

        boolean appRunning = true;

        // Keeps running until someone types 'exit'
        while (appRunning) {
            System.out.println("\n   Welcome to ChocAn Terminal    ");
            System.out.println("   [" + Operator.getModeName() + "]");
            System.out.println("==================================");

            System.out.println(" Type 'mode' to switch mode, or 'exit' to shut down.");
            System.out.print("\nUsername: ");
            String username = input.nextLine().trim();

            if (username.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down.");
                appRunning = false;
                break;
            }

            if (username.equalsIgnoreCase("mode")) {
                selectMode(input);
                continue;
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

    public static void selectMode(Scanner input) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("      SELECT OPERATING MODE       ");
            System.out.println("==================================");
            System.out.println("1. Regular Mode (Local In-Memory - no DB required)");
            System.out.println("2. Real Database Mode (Neon PostgreSQL Cloud DB)");
            System.out.print("Choose mode (1 or 2): ");
            String modeChoice = input.nextLine().trim();

            if (modeChoice.equals("1")) {
                Operator.setUseDatabase(false);
                System.out.println("-> Switched to " + Operator.getModeName());
                break;
            } else if (modeChoice.equals("2")) {
                Operator.setUseDatabase(true);
                System.out.println("-> Switched to " + Operator.getModeName());
                break;
            } else {
                System.out.println("[!] Invalid option. Please enter 1 or 2.");
            }
        }
    }

    // Operator menu
    public static void operatorMenu(Scanner input) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n--- OPERATOR MENU (" + Operator.getModeName() + ") ---");
            System.out.println("1. Add Member");
            System.out.println("2. Update Member");
            System.out.println("3. Delete Member");
            System.out.println("4. View Members");
            System.out.println("5. Add Provider");
            System.out.println("6. Update Provider");
            System.out.println("7. Delete Provider");
            System.out.println("8. View Providers");
            System.out.println("9. Logout");

            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    Operator.addMember(input);
                    break;

                case "2":
                    Operator.updateMember(input);
                    break;

                case "3":
                    Operator.deleteMember(input);
                    break;

                case "4":
                    Operator.viewMembers();
                    break;

                case "5":
                    Operator.addProvider(input);
                    break;

                case "6":
                    Operator.updateProvider(input);
                    break;

                case "7":
                    Operator.deleteProvider(input);
                    break;

                case "8":
                    Operator.viewProviders();
                    break;

                case "9":
                    System.out.println("Logging out from Operator account...");
                    inMenu = false; 
                    // Exits the menu loop and returns to main()
                    break;

                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 9.");
            }
        }
    }

    // Manager menu
    public static void managerMenu(Scanner input) {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n--- MANAGER MENU (" + Operator.getModeName() + ") ---");
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
            System.out.println("\n--- PROVIDER MENU (" + Operator.getModeName() + ") ---");
            System.out.println("1. Validate Member");
            System.out.println("2. Submit Service Record");
            System.out.println("3. Logout");

            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter 9-digit Member ID to validate: ");
                    String memberId = input.nextLine().trim();
                    Member member = Operator.findMemberById(memberId);
                    if (member == null) {
                        System.out.println("Invalid Number");
                    } else if (member.isSuspended()) {
                        System.out.println("Member Suspended");
                    } else {
                        System.out.println("Validated (" + member.getName() + ")");
                    }
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
