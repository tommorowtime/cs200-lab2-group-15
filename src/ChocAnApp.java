import java.io.File;
import java.util.List;
import java.util.Scanner;

/**
 * Main Command-Line Terminal Interface for the Chocoholics Anonymous (ChocAn) System.
 * Simulates the ChocAn Data Center Operator terminal, Manager terminal, and Provider terminal.
 *
 * @author Daniel (Team 15 - Frontend & UML)
 * @author Simeon (Team 15 - Backend & Database)
 */
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
            System.out.println(" Type 'mode' to switch mode, 'gui' to open graphical UI, or 'exit' to shut down.");
            System.out.println(" (Logins: operator1/1984 | manager1/fishes | provider1/HotPotato | or any 9-digit Provider ID)");
            System.out.print("\nUsername (or 9-digit Provider Number): ");
            if (!input.hasNextLine()) break;
            String username = input.nextLine().trim();

            if (username.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down ChocAn Data Center Terminal.");
                appRunning = false;
                break;
            }

            if (username.equalsIgnoreCase("mode")) {
                selectMode(input);
                continue;
            }

            if (username.equalsIgnoreCase("gui")) {
                System.out.println("Launching Graphical User Interface (GUI)...");
                ChocAnGUI.main(new String[0]);
                continue;
            }

            // Per ChocAn spec: "When a provider's terminal is switched on, the provider is asked
            // to enter his or her provider number." If a 9-digit number is entered, authenticate directly.
            User user;
            if (username.matches("\\d{9}")) {
                user = auth.login(username, "");
                if (user == null) {
                    System.out.println("\n[!] Invalid Provider Number (" + username + "). No registered provider with that 9-digit ID.");
                    continue;
                }
            } else {
                System.out.print("Password: ");
                if (!input.hasNextLine()) break;
                String password = input.nextLine().trim();
                user = auth.login(username, password);
            }

            if (user == null) {
                System.out.println("\n[!] Invalid username or password. Please try again.");
                continue;
            }

            System.out.println("\nLogin successful. Welcome, " + user.getUsername() + " (" + user.getRole() + ").");

            // Role menu dispatch
            switch (user.getRole()) {
                case OPERATOR:
                    operatorMenu(input);
                    break;

                case MANAGER:
                    managerMenu(input);
                    break;

                case PROVIDER:
                    providerMenu(input, user);
                    break;
            }

            System.out.println("\nLogging out. Returning to login screen...");
        }

        input.close();
    }

    public static void selectMode(Scanner input) {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("      SELECT OPERATING MODE       ");
            System.out.println("==================================");
            System.out.println("1. Regular Mode (Local Persistent Storage - no DB required)");
            System.out.println("2. Real Database Mode (Neon PostgreSQL Cloud DB)");
            System.out.print("Choose mode (1 or 2): ");
            if (!input.hasNextLine()) return;
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

    // ==================== OPERATOR MENU ====================

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
            if (!input.hasNextLine()) break;
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
                    break;
                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 9.");
            }
        }
    }

    // ==================== MANAGER MENU ====================

    public static void managerMenu(Scanner input) {
        Manager.managerMenu(input);
    }

    // ==================== PROVIDER MENU ====================

    public static void providerMenu(Scanner input, User user) {
        boolean inMenu = true;
        String activeProviderId = user.getProviderId();
        if (activeProviderId == null || !activeProviderId.matches("\\d{9}") || Operator.findProviderById(activeProviderId) == null) {
            List<Provider> providers = Operator.getProviders();
            activeProviderId = providers.isEmpty() ? "111222333" : providers.get(0).getProviderId();
        }

        while (inMenu) {
            Provider currentProv = Operator.findProviderById(activeProviderId);
            String provDisplay = currentProv != null
                    ? (currentProv.getName() + " [" + currentProv.getProviderId() + "]")
                    : ("Provider [" + activeProviderId + "]");

            System.out.println("\n--- PROVIDER TERMINAL (" + Operator.getModeName() + ") ---");
            System.out.println("Active Provider: " + provDisplay);
            System.out.println("1. Validate Member Card");
            System.out.println("2. Bill ChocAn (Submit Service Record)");
            System.out.println("3. Request Provider Directory (Display & Save File)");
            System.out.println("4. Switch Active 9-Digit Provider Number");
            System.out.println("5. Logout");

            System.out.print("Choose an option: ");
            if (!input.hasNextLine()) break;
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Slide member card or enter 9-digit Member Number: ");
                    String memberId = input.nextLine().trim();
                    String status = Operator.validateMemberCard(memberId);
                    System.out.println("Terminal Display: " + status);
                    if ("Validated".equals(status)) {
                        Member member = Operator.findMemberById(memberId);
                        if (member != null) {
                            System.out.println("Member Verified : " + member.getName() + " (" + member.getMemberId() + ")");
                        }
                    }
                    break;

                case "2":
                    Operator.billChocAn(input, activeProviderId);
                    break;

                case "3":
                    System.out.println("\n" + ProviderDirectory.formatDirectory());
                    File dirFile = ProviderDirectory.writeDirectoryToFile();
                    if (dirFile != null) {
                        System.out.println("-> Provider Directory file saved (simulated email attachment): " + dirFile.getPath());
                    }
                    break;

                case "4":
                    Operator.viewProviders();
                    System.out.print("Enter 9-digit Provider Number to use on this terminal: ");
                    String newProvId = input.nextLine().trim();
                    if (!newProvId.matches("\\d{9}")) {
                        System.out.println("[!] Provider Number must be 9 numeric digits.");
                    } else {
                        Provider found = Operator.findProviderById(newProvId);
                        if (found == null) {
                            System.out.println("[!] Provider Number not found in ChocAn Data Center.");
                        } else {
                            activeProviderId = found.getProviderId();
                            user.setProviderId(activeProviderId);
                            System.out.println("-> Active Provider set to: " + found.getName() + " [" + activeProviderId + "]");
                        }
                    }
                    break;

                case "5":
                    System.out.println("Logging out from Provider terminal...");
                    inMenu = false;
                    break;

                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 5.");
            }
        }
    }
}
