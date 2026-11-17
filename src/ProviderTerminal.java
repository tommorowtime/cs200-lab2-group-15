import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * ProviderTerminal.java
 * 
 * Standalone Provider Terminal interface for Chocoholics Anonymous (ChocAn).
 * Implements provider workflows:
 *   1. Validate Member card
 *   2. Bill ChocAn (Record healthcare service)
 *   3. Request Provider Directory (alphabetized file output)
 */
public class ProviderTerminal {

    // --- Helper Models (Static Inner Classes) ---

    // Represents a ChocAn member for status validation
    static class Member {
        String number; // 9 digits
        String name;
        String status; // "VALID" or "SUSPENDED"

        public Member(String number, String name, String status) {
            this.number = number;
            this.name = name;
            this.status = status;
        }
    }

    // Represents an entry in the Provider Directory
    static class ServiceEntry {
        String code; // 6 digits
        String name; // up to 20 characters
        double fee;  // up to $999.99

        public ServiceEntry(String code, String name, double fee) {
            this.code = code;
            this.name = name;
            this.fee = fee;
        }
    }

    // --- Mock Data Lookups ---
    private static final Map<String, String> mockProviders = new HashMap<>();
    private static final Map<String, Member> mockMembers = new HashMap<>();
    private static final Map<String, ServiceEntry> mockServices = new HashMap<>();

    // Populate mock data for testing out of the box
    static {
        // Sample Providers (9 digits)
        mockProviders.put("900000001", "Dr. Sarah Connor");
        mockProviders.put("900000002", "Dr. Gregory House");
        mockProviders.put("111222333", "Dr. Meredith Grey");

        // Sample Members (9 digits)
        mockMembers.put("100000001", new Member("100000001", "Alice Smith", "VALID"));
        mockMembers.put("100000002", new Member("100000002", "Bob Jones", "SUSPENDED"));
        mockMembers.put("100000003", new Member("100000003", "Charlie Brown", "VALID"));
        mockMembers.put("123456789", new Member("123456789", "Diana Prince", "VALID"));
        mockMembers.put("987654321", new Member("987654321", "Evan Wright", "SUSPENDED"));

        // Sample Services (6-digit code, name <= 20 chars, fee)
        mockServices.put("123456", new ServiceEntry("123456", "Dietitian Session", 100.00));
        mockServices.put("598470", new ServiceEntry("598470", "Nutrition Consult", 85.50));
        mockServices.put("883921", new ServiceEntry("883921", "Exercise Therapy", 120.00));
        mockServices.put("345678", new ServiceEntry("345678", "Physical Rehab", 95.00));
        mockServices.put("765432", new ServiceEntry("765432", "Psych Assessment", 150.00));
        mockServices.put("654321", new ServiceEntry("654321", "Group Counseling", 45.00));
    }

    // --- Main Entry Point ---
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("       CHOCOHOLICS ANONYMOUS PROVIDER TERMINAL    ");
        System.out.println("=================================================");

        // Step 1: Prompt for 9-digit providerNumber to log in
        String loggedInProvider = loginProvider(scanner);

        // Step 2: Main menu loop
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option (1-4): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleValidateMember(scanner);
                    break;
                case "2":
                    handleBillChocAn(scanner, loggedInProvider);
                    break;
                case "3":
                    handleRequestProviderDirectory();
                    break;
                case "4":
                    System.out.println("\nLogging out. Thank you for using the ChocAn Terminal!");
                    running = false;
                    break;
                default:
                    System.out.println("[!] Invalid option. Please enter 1, 2, 3, or 4.");
                    break;
            }
        }

        scanner.close();
    }

    // --- Menu Display ---
    private static void printMenu() {
        System.out.println("\n-------------------------------------------------");
        System.out.println("                  PROVIDER MENU                  ");
        System.out.println("-------------------------------------------------");
        System.out.println("1. Validate Member");
        System.out.println("2. Bill ChocAn (Record Service)");
        System.out.println("3. Request Provider Directory");
        System.out.println("4. Exit");
    }

    // --- 1. Terminal Startup / Login ---
    private static String loginProvider(Scanner scanner) {
        while (true) {
            System.out.print("Enter 9-digit Provider Number to log in: ");
            String providerNumber = scanner.nextLine().trim();

            if (!providerNumber.matches("\\d{9}")) {
                System.out.println("[!] Provider number must be exactly 9 digits (e.g. 900000001). Please try again.\n");
                continue;
            }

            String providerName = mockProviders.get(providerNumber);
            if (providerName != null) {
                System.out.println("Login successful. Welcome, " + providerName + "!");
            } else {
                System.out.println("Login successful for Provider #" + providerNumber + ".");
            }
            return providerNumber;
        }
    }

    // --- 2. Validate Member ---
    /**
     * Checks member status against lookup table.
     * Active/valid       -> prints "Validated"
     * Suspended          -> prints "Member suspended"
     * Nonexistent/malformed -> prints "Invalid number"
     *
     * @return "VALID", "SUSPENDED", or "INVALID"
     */
    public static String validateMember(String memberNumber) {
        // Validate format (must be 9 digits)
        if (memberNumber == null || !memberNumber.matches("\\d{9}")) {
            System.out.println("Invalid number");
            return "INVALID";
        }

        // Check if member exists in mock records
        Member member = mockMembers.get(memberNumber);
        if (member == null) {
            System.out.println("Invalid number");
            return "INVALID";
        }

        // Check active vs suspended status
        if ("SUSPENDED".equalsIgnoreCase(member.status)) {
            System.out.println("Member suspended");
            return "SUSPENDED";
        }

        // Active and valid member
        System.out.println("Validated");
        return "VALID";
    }

    // Handler for Option 1 on menu
    private static void handleValidateMember(Scanner scanner) {
        System.out.println("\n--- Validate Member ---");
        System.out.print("Enter 9-digit Member Number (or swipe card): ");
        String memberNumber = scanner.nextLine().trim();
        validateMember(memberNumber);
    }

    // --- 3. Bill ChocAn (Record Service) ---
    private static void handleBillChocAn(Scanner scanner, String providerNumber) {
        System.out.println("\n--- Bill ChocAn (Record Service) ---");

        // Step 1: Validate member number first; must display "Validated" to continue
        System.out.print("Enter 9-digit Member Number (or swipe card): ");
        String memberNumber = scanner.nextLine().trim();
        String memberStatus = validateMember(memberNumber);

        if (!"VALID".equals(memberStatus)) {
            System.out.println("Cannot proceed with billing. Member validation required.");
            return;
        }

        // Step 2: Prompt for date of service in MM-DD-YYYY format
        String serviceDate = "";
        while (true) {
            System.out.print("Enter date of service (MM-DD-YYYY): ");
            serviceDate = scanner.nextLine().trim();
            if (isValidDate(serviceDate)) {
                break;
            }
            System.out.println("[!] Invalid date format. Please use MM-DD-YYYY (e.g. 11-16-2026).");
        }

        // Step 3: Prompt for 6-digit serviceCode. Look up in directory.
        // If invalid, display error; if valid, display service name (up to 20 chars) and prompt provider to verify (Y/N).
        ServiceEntry service = null;
        while (true) {
            System.out.print("Enter 6-digit Service Code: ");
            String serviceCode = scanner.nextLine().trim();
            service = mockServices.get(serviceCode);

            if (service == null) {
                System.out.println("Error: Service code not found in directory.");
                System.out.print("Would you like to try another code? (Y/N): ");
                String retry = scanner.nextLine().trim();
                if (!retry.equalsIgnoreCase("Y")) {
                    System.out.println("Billing cancelled.");
                    return;
                }
            } else {
                // Display service name (up to 20 chars) and ask for verification
                System.out.println("Service: " + service.name);
                System.out.print("Is this service correct? (Y/N): ");
                String confirm = scanner.nextLine().trim();

                if (confirm.equalsIgnoreCase("Y")) {
                    break; // Confirmed, proceed to next step
                } else {
                    System.out.println("Service unconfirmed. Please re-enter code or type 'C' to cancel.");
                    System.out.print("Enter 'C' to cancel or press Enter to try again: ");
                    String cancelChoice = scanner.nextLine().trim();
                    if (cancelChoice.equalsIgnoreCase("C")) {
                        System.out.println("Billing cancelled.");
                        return;
                    }
                }
            }
        }

        // Step 4: Prompt for optional comments (up to 100 characters)
        System.out.print("Enter optional comments (up to 100 characters, press Enter to skip): ");
        String comments = scanner.nextLine().trim();
        if (comments.length() > 100) {
            System.out.println("Comments exceeded 100 characters. Automatically truncated to 100 characters.");
            comments = comments.substring(0, 100);
        }

        // Step 5: Automatically capture current system timestamp (MM-DD-YYYY HH:MM:SS)
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        String currentTimestamp = sdf.format(new Date());

        // Step 6: Append the service record to a local text file
        File recordFile = new File("service_records.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(recordFile, true))) {
            writer.println("Current Date and Time: " + currentTimestamp);
            writer.println("Date Service Provided: " + serviceDate);
            writer.println("Provider Number: " + providerNumber);
            writer.println("Member Number: " + memberNumber);
            writer.println("Service Code: " + service.code);
            writer.println("Comments: " + (comments.isEmpty() ? "None" : comments));
            writer.println("--------------------------------------------------");
        } catch (IOException e) {
            System.out.println("Error saving service record to file: " + e.getMessage());
            return;
        }

        // Step 7: Look up and print the fee to be paid on the screen for the provider's verification form
        System.out.println("\n[+] Service successfully recorded to " + recordFile.getName() + "!");
        System.out.printf("Fee to be paid: $%.2f%n", service.fee);
        System.out.println("(Provider Verification Form: Please record the fee above on your paper copy)");
    }

    // Helper method to validate date in MM-DD-YYYY format
    private static boolean isValidDate(String dateStr) {
        if (dateStr == null || !dateStr.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // --- 4. Request Provider Directory ---
    private static void handleRequestProviderDirectory() {
        System.out.println("\n--- Requesting Provider Directory ---");

        // Alphabetize the available services by service name
        List<ServiceEntry> serviceList = new ArrayList<>(mockServices.values());
        serviceList.sort(Comparator.comparing(s -> s.name.toLowerCase()));

        // Write the sorted directory to a local file named ProviderDirectory.txt
        File directoryFile = new File("ProviderDirectory.txt");
        try (PrintWriter writer = new PrintWriter(new FileWriter(directoryFile))) {
            writer.println("==============================================================");
            writer.println("            CHOCOHOLICS ANONYMOUS PROVIDER DIRECTORY          ");
            writer.println("==============================================================");
            writer.printf("%-25s %-15s %-10s%n", "Service Name", "Service Code", "Fee");
            writer.println("--------------------------------------------------------------");

            for (ServiceEntry service : serviceList) {
                writer.printf("%-25s %-15s $%.2f%n", service.name, service.code, service.fee);
            }

            writer.println("==============================================================");
            System.out.println("[+] Provider directory successfully generated: " + directoryFile.getName());
            System.out.println("(Simulated: Directory file sent as an email attachment)");
        } catch (IOException e) {
            System.out.println("Error writing Provider Directory file: " + e.getMessage());
        }
    }
}
