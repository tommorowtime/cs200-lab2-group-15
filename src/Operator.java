import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Operator & Data Center Controller for ChocAn.
 * Manages Member records, Provider records, Member validation, and ServiceRecord billing
 * across both Regular (Local Persistent) Mode and Real Database (Neon PostgreSQL) Mode.
 *
 * @author Teal'c (Team 15 - Operator Features)
 * @author Jackson (Team 15 - Provider Features)
 * @author Simeon (Team 15 - Backend & Database)
 */
public class Operator {
    // Mode flag: false = Regular (Local Persistent) Mode, true = Neon Database Mode
    private static boolean useDatabase = false;

    // Local storage lists
    private static final List<Member> localMembers = new ArrayList<>();
    private static final List<Provider> localProviders = new ArrayList<>();
    private static final List<ServiceRecord> localServiceRecords = new ArrayList<>();

    // Local persistence file so records remain available across program restarts
    private static final File LOCAL_DATA_FILE = new File("data", "chocan_local_store.txt");

    public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    static {
        loadLocalData();
    }

    /**
     * Loads local members, providers, and service records from disk,
     * or seeds default sample data on first run.
     */
    public static synchronized void loadLocalData() {
        localMembers.clear();
        localProviders.clear();
        localServiceRecords.clear();

        if (LOCAL_DATA_FILE.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(LOCAL_DATA_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\|", -1);
                    String type = parts[0];
                    if ("MEMBER".equals(type) && parts.length >= 8) {
                        localMembers.add(new Member(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6],
                                Boolean.parseBoolean(parts[7])));
                    } else if ("PROVIDER".equals(type) && parts.length >= 7) {
                        localProviders.add(new Provider(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
                    } else if ("SERVICE".equals(type) && parts.length >= 7) {
                        localServiceRecords.add(new ServiceRecord(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]));
                    }
                }
            } catch (IOException e) {
                System.err.println("[!] Warning: Could not read local data store: " + e.getMessage());
            }
        }

        // Seed default records if empty
        if (localMembers.isEmpty() && localProviders.isEmpty()) {
            seedDefaultLocalData();
            saveLocalData();
        }
    }

    private static void seedDefaultLocalData() {
        localMembers.add(new Member("123456789", "Alice Smith", "100 Choc St", "Portland", "OR", "97201", false));
        localMembers.add(new Member("987654321", "Bob Johnson", "200 Cocoa Ave", "Seattle", "WA", "98101", true));
        localMembers.add(new Member("555666777", "Charlie Brown", "300 Truffle Rd", "San Jose", "CA", "95112", false));

        localProviders.add(new Provider("111222333", "Dr. Sweet Tooth", "500 Dental Way", "Portland", "OR", "97205"));
        localProviders.add(new Provider("444555666", "Choc Care Clinic", "600 Health Blvd", "Seattle", "WA", "98104"));

        localServiceRecords.add(new ServiceRecord(
                "11-18-2026 10:15:30", "11-17-2026", "111222333", "123456789", "598470",
                "Initial dietitian consultation for dark chocolate cravings"));
        localServiceRecords.add(new ServiceRecord(
                "11-19-2026 14:22:10", "11-19-2026", "111222333", "555666777", "883948",
                "Completed 45-minute aerobics exercise session"));
        localServiceRecords.add(new ServiceRecord(
                "11-20-2026 09:05:00", "11-19-2026", "444555666", "123456789", "149263",
                "Follow-up internist checkup"));
    }

    /**
     * Persists local members, providers, and service records to disk.
     */
    public static synchronized void saveLocalData() {
        File parent = LOCAL_DATA_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(LOCAL_DATA_FILE))) {
            out.println("# ChocAn Local Persistent Data Store");
            for (Member m : localMembers) {
                out.printf("MEMBER|%s|%s|%s|%s|%s|%s|%b%n",
                        sanitize(m.getMemberId()), sanitize(m.getName()), sanitize(m.getAddress()),
                        sanitize(m.getCity()), sanitize(m.getState()), sanitize(m.getZip()), m.isSuspended());
            }
            for (Provider p : localProviders) {
                out.printf("PROVIDER|%s|%s|%s|%s|%s|%s%n",
                        sanitize(p.getProviderId()), sanitize(p.getName()), sanitize(p.getAddress()),
                        sanitize(p.getCity()), sanitize(p.getState()), sanitize(p.getZip()));
            }
            for (ServiceRecord sr : localServiceRecords) {
                out.printf("SERVICE|%s|%s|%s|%s|%s|%s%n",
                        sanitize(sr.getCurrentDateTime()), sanitize(sr.getServiceDate()),
                        sanitize(sr.getProviderId()), sanitize(sr.getMemberId()),
                        sanitize(sr.getServiceCode()), sanitize(sr.getComments()));
            }
        } catch (IOException e) {
            System.err.println("[!] Warning: Could not save local data store: " + e.getMessage());
        }
    }

    private static String sanitize(String s) {
        return s == null ? "" : s.replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
    }

    // ==================== MEMBER VALIDATION (PROVIDER TERMINAL) ====================

    /**
     * Verifies a 9-digit member number at the ChocAn Data Center.
     * Per ChocAn specification:
     * - If valid and active: returns "Validated"
     * - If invalid or not found: returns "Invalid number"
     * - If suspended (fees owed): returns "Member suspended"
     */
    public static String validateMemberCard(String memberId) {
        if (memberId == null || !memberId.trim().matches("\\d{9}")) {
            return "Invalid number";
        }
        Member member = findMemberById(memberId.trim());
        if (member == null) {
            return "Invalid number";
        }
        if (member.isSuspended()) {
            return "Member suspended";
        }
        return "Validated";
    }

    // ==================== PROGRAMMATIC CRUD HELPERS ====================

    public static boolean addMember(Member member) {
        if (useDatabase) {
            return ChocAN_DB.addMember(member);
        } else {
            localMembers.add(member);
            saveLocalData();
            return true;
        }
    }

    public static boolean updateMember(Member member) {
        if (useDatabase) {
            return ChocAN_DB.updateMember(member);
        } else {
            for (int i = 0; i < localMembers.size(); i++) {
                if (localMembers.get(i).getMemberId().equals(member.getMemberId())) {
                    localMembers.set(i, member);
                    saveLocalData();
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean deleteMember(String memberId) {
        if (useDatabase) {
            return ChocAN_DB.deleteMember(memberId);
        } else {
            Member m = findMemberById(memberId);
            if (m != null) {
                localMembers.remove(m);
                saveLocalData();
                return true;
            }
            return false;
        }
    }

    public static boolean addProvider(Provider provider) {
        if (useDatabase) {
            return ChocAN_DB.addProvider(provider);
        } else {
            localProviders.add(provider);
            saveLocalData();
            return true;
        }
    }

    public static boolean updateProvider(Provider provider) {
        if (useDatabase) {
            return ChocAN_DB.updateProvider(provider);
        } else {
            for (int i = 0; i < localProviders.size(); i++) {
                if (localProviders.get(i).getProviderId().equals(provider.getProviderId())) {
                    localProviders.set(i, provider);
                    saveLocalData();
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean deleteProvider(String providerId) {
        if (useDatabase) {
            return ChocAN_DB.deleteProvider(providerId);
        } else {
            Provider p = findProviderById(providerId);
            if (p != null) {
                localProviders.remove(p);
                saveLocalData();
                return true;
            }
            return false;
        }
    }

    public static boolean addServiceRecord(ServiceRecord record) {
        if (useDatabase) {
            return ChocAN_DB.addServiceRecord(record);
        } else {
            localServiceRecords.add(record);
            saveLocalData();
            return true;
        }
    }

    public static List<ServiceRecord> getServiceRecords() {
        if (useDatabase) {
            return ChocAN_DB.getAllServiceRecords();
        }
        return new ArrayList<>(localServiceRecords);
    }

    public static List<ServiceRecord> getServiceRecordsForMember(String memberId) {
        List<ServiceRecord> result = new ArrayList<>();
        for (ServiceRecord sr : getServiceRecords()) {
            if (sr.getMemberId().equals(memberId)) {
                result.add(sr);
            }
        }
        return result;
    }

    public static List<ServiceRecord> getServiceRecordsForProvider(String providerId) {
        List<ServiceRecord> result = new ArrayList<>();
        for (ServiceRecord sr : getServiceRecords()) {
            if (sr.getProviderId().equals(providerId)) {
                result.add(sr);
            }
        }
        return result;
    }

    public static void setUseDatabase(boolean useDb) {
        useDatabase = useDb;
        if (useDatabase) {
            if (!ChocAN_DB.initializeDatabase()) {
                System.out.println("[!] Could not connect to Neon DB. Falling back to Regular (Local) Mode.");
                useDatabase = false;
            }
        }
    }

    public static boolean isUseDatabase() {
        return useDatabase;
    }

    public static String getModeName() {
        return useDatabase ? "NEON DATABASE MODE" : "REGULAR (LOCAL PERSISTENT) MODE";
    }

    public static List<Member> getMembers() {
        if (useDatabase) {
            return ChocAN_DB.getAllMembers();
        }
        return localMembers;
    }

    public static List<Provider> getProviders() {
        if (useDatabase) {
            return ChocAN_DB.getAllProviders();
        }
        return localProviders;
    }

    public static Member findMemberById(String memberId) {
        if (useDatabase) {
            return ChocAN_DB.getMemberById(memberId);
        }
        for (Member m : localMembers) {
            if (m.getMemberId().equals(memberId)) {
                return m;
            }
        }
        return null;
    }

    public static Provider findProviderById(String providerId) {
        if (useDatabase) {
            return ChocAN_DB.getProviderById(providerId);
        }
        for (Provider p : localProviders) {
            if (p.getProviderId().equals(providerId)) {
                return p;
            }
        }
        return null;
    }

    // ==================== CONSOLE OPERATOR WORKFLOWS ====================

    public static void addMember(Scanner input) {
        System.out.print("Enter 9-digit Member ID: ");
        String id = input.nextLine().trim();
        if (!id.matches("\\d{9}")) {
            System.out.println("[!] Invalid ID. Member ID must be exactly 9 numeric digits.");
            return;
        }
        if (findMemberById(id) != null) {
            System.out.println("[!] A member with ID " + id + " already exists.");
            return;
        }

        System.out.print("Enter Member Name (up to 25 chars): ");
        String name = input.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("[!] Member name cannot be empty.");
            return;
        }
        System.out.print("Enter Street Address (up to 25 chars): ");
        String address = input.nextLine().trim();
        System.out.print("Enter City (up to 14 chars): ");
        String city = input.nextLine().trim();
        System.out.print("Enter State (2 letters): ");
        String state = input.nextLine().trim().toUpperCase();
        if (!state.isEmpty() && !state.matches("[A-Za-z]{2}")) {
            System.out.println("[!] Warning: State should be 2 letters (truncating/formatting).");
        }
        System.out.print("Enter ZIP Code (5 digits): ");
        String zip = input.nextLine().trim();
        if (!zip.isEmpty() && !zip.matches("\\d{5}")) {
            System.out.println("[!] Invalid ZIP code. ZIP code must be 5 numeric digits.");
            return;
        }

        Member newMember = new Member(id, name, address, city, state, zip, false);
        if (addMember(newMember)) {
            System.out.println("Member added (" + getModeName() + "): " + newMember);
        } else {
            System.out.println("[!] Failed to add member.");
        }
    }

    public static void updateMember(Scanner input) {
        System.out.print("Enter 9-digit Member ID to update: ");
        String id = input.nextLine().trim();
        Member member = findMemberById(id);
        if (member == null) {
            System.out.println("[!] Member not found.");
            return;
        }

        System.out.print("Enter new Name (<=25 chars, leave blank to keep '" + member.getName() + "'): ");
        String name = input.nextLine().trim();
        if (!name.isEmpty()) member.setName(name);

        System.out.print("Enter new Address (<=25 chars, leave blank to keep '" + member.getAddress() + "'): ");
        String address = input.nextLine().trim();
        if (!address.isEmpty()) member.setAddress(address);

        System.out.print("Enter new City (<=14 chars, leave blank to keep '" + member.getCity() + "'): ");
        String city = input.nextLine().trim();
        if (!city.isEmpty()) member.setCity(city);

        System.out.print("Enter new State (2 letters, leave blank to keep '" + member.getState() + "'): ");
        String state = input.nextLine().trim();
        if (!state.isEmpty()) member.setState(state);

        System.out.print("Enter new ZIP (5 digits, leave blank to keep '" + member.getZip() + "'): ");
        String zip = input.nextLine().trim();
        if (!zip.isEmpty()) {
            if (!zip.matches("\\d{5}")) {
                System.out.println("[!] Invalid ZIP code. Must be 5 digits.");
                return;
            }
            member.setZip(zip);
        }

        System.out.print("Suspended status (true/false, leave blank to keep '" + member.isSuspended() + "'): ");
        String suspendedStr = input.nextLine().trim();
        if (!suspendedStr.isEmpty()) {
            member.setSuspended(Boolean.parseBoolean(suspendedStr));
        }

        if (updateMember(member)) {
            System.out.println("Member updated (" + getModeName() + "): " + member);
        } else {
            System.out.println("[!] Failed to update member.");
        }
    }

    public static void deleteMember(Scanner input) {
        System.out.print("Enter 9-digit Member ID to delete: ");
        String id = input.nextLine().trim();
        if (deleteMember(id)) {
            System.out.println("Member deleted (" + getModeName() + ").");
        } else {
            System.out.println("[!] Member not found or could not be deleted.");
        }
    }

    public static void viewMembers() {
        List<Member> members = getMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }
        System.out.println("--- Registered Members (" + getModeName() + ") ---");
        for (Member m : members) {
            System.out.println(m);
        }
    }

    public static void addProvider(Scanner input) {
        System.out.print("Enter 9-digit Provider ID: ");
        String id = input.nextLine().trim();
        if (!id.matches("\\d{9}")) {
            System.out.println("[!] Invalid ID. Provider ID must be exactly 9 numeric digits.");
            return;
        }
        if (findProviderById(id) != null) {
            System.out.println("[!] A provider with ID " + id + " already exists.");
            return;
        }

        System.out.print("Enter Provider Name (up to 25 chars): ");
        String name = input.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("[!] Provider name cannot be empty.");
            return;
        }
        System.out.print("Enter Street Address (up to 25 chars): ");
        String address = input.nextLine().trim();
        System.out.print("Enter City (up to 14 chars): ");
        String city = input.nextLine().trim();
        System.out.print("Enter State (2 letters): ");
        String state = input.nextLine().trim().toUpperCase();
        System.out.print("Enter ZIP Code (5 digits): ");
        String zip = input.nextLine().trim();
        if (!zip.isEmpty() && !zip.matches("\\d{5}")) {
            System.out.println("[!] Invalid ZIP code. ZIP code must be 5 numeric digits.");
            return;
        }

        Provider newProvider = new Provider(id, name, address, city, state, zip);
        if (addProvider(newProvider)) {
            System.out.println("Provider added (" + getModeName() + "): " + newProvider);
        } else {
            System.out.println("[!] Failed to add provider.");
        }
    }

    public static void updateProvider(Scanner input) {
        System.out.print("Enter 9-digit Provider ID to update: ");
        String id = input.nextLine().trim();
        Provider provider = findProviderById(id);
        if (provider == null) {
            System.out.println("[!] Provider not found.");
            return;
        }

        System.out.print("Enter new Name (<=25 chars, leave blank to keep '" + provider.getName() + "'): ");
        String name = input.nextLine().trim();
        if (!name.isEmpty()) provider.setName(name);

        System.out.print("Enter new Address (<=25 chars, leave blank to keep '" + provider.getAddress() + "'): ");
        String address = input.nextLine().trim();
        if (!address.isEmpty()) provider.setAddress(address);

        System.out.print("Enter new City (<=14 chars, leave blank to keep '" + provider.getCity() + "'): ");
        String city = input.nextLine().trim();
        if (!city.isEmpty()) provider.setCity(city);

        System.out.print("Enter new State (2 letters, leave blank to keep '" + provider.getState() + "'): ");
        String state = input.nextLine().trim();
        if (!state.isEmpty()) provider.setState(state);

        System.out.print("Enter new ZIP (5 digits, leave blank to keep '" + provider.getZip() + "'): ");
        String zip = input.nextLine().trim();
        if (!zip.isEmpty()) {
            if (!zip.matches("\\d{5}")) {
                System.out.println("[!] Invalid ZIP code. Must be 5 digits.");
                return;
            }
            provider.setZip(zip);
        }

        if (updateProvider(provider)) {
            System.out.println("Provider updated (" + getModeName() + "): " + provider);
        } else {
            System.out.println("[!] Failed to update provider.");
        }
    }

    public static void deleteProvider(Scanner input) {
        System.out.print("Enter 9-digit Provider ID to delete: ");
        String id = input.nextLine().trim();
        if (deleteProvider(id)) {
            System.out.println("Provider deleted (" + getModeName() + ").");
        } else {
            System.out.println("[!] Provider not found or could not be deleted.");
        }
    }

    public static void viewProviders() {
        List<Provider> providers = getProviders();
        if (providers.isEmpty()) {
            System.out.println("No providers registered.");
            return;
        }
        System.out.println("--- Registered Providers (" + getModeName() + ") ---");
        for (Provider p : providers) {
            System.out.println(p);
        }
    }

    // ==================== CONSOLE PROVIDER BILLING WORKFLOW ====================

    /**
     * Interactive Provider Terminal billing flow per ChocAn Appendix A specification:
     * 1. Provider slides card / keys in 9-digit member number -> displays "Validated" (or error).
     * 2. Provider keys in date service was provided (MM-DD-YYYY).
     * 3. Provider keys in 6-digit service code from Provider Directory.
     * 4. Software displays 20-char service name and asks provider to verify (Y/N).
     * 5. Provider enters optional comments (up to 100 characters).
     * 6. Software writes record to disk/DB (Current date/time, service date, provider ID, member ID, service code, comments).
     * 7. Software looks up and displays fee to be paid for that service.
     */
    public static void billChocAn(Scanner input, String providerId) {
        System.out.println("\n--- BILL CHOCAN FOR HEALTH CARE SERVICE ---");
        if (providerId == null || !providerId.matches("\\d{9}")) {
            System.out.print("Enter your 9-digit Provider Number: ");
            providerId = input.nextLine().trim();
            if (!providerId.matches("\\d{9}")) {
                System.out.println("[!] Invalid Provider Number. Must be 9 digits.");
                return;
            }
        }

        System.out.print("Swipe card or key in 9-digit Member Number: ");
        String memberId = input.nextLine().trim();
        String status = validateMemberCard(memberId);
        System.out.println("Terminal Display: " + status);
        if (!"Validated".equals(status)) {
            return;
        }
        Member member = findMemberById(memberId);

        String defaultDate = LocalDate.now().format(DATE_FMT);
        System.out.print("Enter Date Service Was Provided (MM-DD-YYYY) [default " + defaultDate + "]: ");
        String serviceDate = input.nextLine().trim();
        if (serviceDate.isEmpty()) {
            serviceDate = defaultDate;
        }
        if (!isValidDate(serviceDate)) {
            System.out.println("[!] Invalid date format. Please use MM-DD-YYYY (e.g., 11-20-2026).");
            return;
        }

        ServiceEntry matchedService = null;
        while (matchedService == null) {
            System.out.print("Enter 6-digit Service Code (or type 'dir' to view Provider Directory, 'cancel' to abort): ");
            String codeInput = input.nextLine().trim();
            if (codeInput.equalsIgnoreCase("cancel")) {
                System.out.println("Billing cancelled.");
                return;
            }
            if (codeInput.equalsIgnoreCase("dir")) {
                System.out.println("\n" + ProviderDirectory.formatDirectory());
                continue;
            }
            if (!codeInput.matches("\\d{6}")) {
                System.out.println("[!] Error: Service code must be a 6-digit number.");
                continue;
            }
            ServiceEntry candidate = ProviderDirectory.findServiceByCode(codeInput);
            if (candidate == null) {
                System.out.println("[!] Error: Nonexistent service code '" + codeInput + "'. Please check the Provider Directory.");
                continue;
            }

            // Display service name (up to 20 chars) and ask provider to verify
            System.out.println("Service Name: " + candidate.getServiceName());
            System.out.print("Verify that '" + candidate.getServiceName() + "' (Code " + candidate.getServiceCode() + ") was provided? (Y/N): ");
            String confirm = input.nextLine().trim();
            if (confirm.equalsIgnoreCase("Y") || confirm.equalsIgnoreCase("YES")) {
                matchedService = candidate;
            } else {
                System.out.println("Service code not confirmed. Please re-enter the 6-digit service code.");
            }
        }

        System.out.print("Enter Comments (up to 100 characters, optional - press Enter to skip): ");
        String comments = input.nextLine().trim();
        if (comments.length() > 100) {
            comments = comments.substring(0, 100);
            System.out.println("(Comments truncated to 100 characters)");
        }

        String currentDateTime = LocalDateTime.now().format(DATETIME_FMT);
        ServiceRecord record = new ServiceRecord(
                currentDateTime,
                serviceDate,
                providerId,
                memberId,
                matchedService.getServiceCode(),
                comments
        );

        if (addServiceRecord(record)) {
            System.out.println("\n=======================================================");
            System.out.println("        SERVICE RECORD WRITTEN TO CHOCAN DISK          ");
            System.out.println("=======================================================");
            System.out.println("Current Date & Time : " + record.getCurrentDateTime());
            System.out.println("Date of Service     : " + record.getServiceDate());
            System.out.println("Provider Number     : " + record.getProviderId());
            System.out.println("Member Name & Number: " + (member != null ? member.getName() : "") + " (" + record.getMemberId() + ")");
            System.out.println("Service Code & Name : " + record.getServiceCode() + " - " + matchedService.getServiceName());
            System.out.println("Comments            : " + (record.getComments().isEmpty() ? "(none)" : record.getComments()));
            System.out.printf("FEE TO BE PAID      : $%.2f%n", matchedService.getFee());
            System.out.println("=======================================================");
        } else {
            System.out.println("[!] Error: Failed to save service record.");
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || !dateStr.matches("\\d{2}-\\d{2}-\\d{4}")) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DATE_FMT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
