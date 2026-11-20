import java.util.List;
import java.util.Scanner;

/**
 * Manager Controller for ChocAn.
 * Coordinates managerial report requests (Member Report, Provider Report, Summary Report,
 * EFT Data, Main Accounting Procedure, and searching providers requiring payment).
 * Delegates report generation and file output to ReportService and Operator data store.
 *
 * @author Cameron (Team 15 - Manager Features)
 */
public class Manager {

    public static List<ServiceRecord> getServiceRecords() {
        return Operator.getServiceRecords();
    }

    public static void addServiceRecord(ServiceRecord record) {
        if (record != null) {
            Operator.addServiceRecord(record);
        }
    }

    public static String generateMemberReport(String memberId) {
        Member member = Operator.findMemberById(memberId);
        if (member == null) {
            return "Member not found with ID: " + memberId;
        }
        return ReportService.generateMemberReport(member);
    }

    public static void generateMemberReport(Scanner input) {
        System.out.print("Enter 9-digit Member ID (or press Enter for all consulted members): ");
        String memberId = input.nextLine().trim();
        if (memberId.isEmpty()) {
            System.out.println(ReportService.generateAllMemberReports(true));
        } else {
            System.out.println(generateMemberReport(memberId));
        }
    }

    public static String generateProviderReport(String providerId) {
        Provider provider = Operator.findProviderById(providerId);
        if (provider == null) {
            return "Provider not found with ID: " + providerId;
        }
        return ReportService.generateProviderReport(provider);
    }

    public static void generateProviderReport(Scanner input) {
        System.out.print("Enter 9-digit Provider ID (or press Enter for all billing providers): ");
        String providerId = input.nextLine().trim();
        if (providerId.isEmpty()) {
            System.out.println(ReportService.generateAllProviderReports(true));
        } else {
            System.out.println(generateProviderReport(providerId));
        }
    }

    public static String generateSummaryReport() {
        return ReportService.generateSummaryReport();
    }

    public static String searchProvidersToPay(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Please enter a valid Provider ID or name to search.";
        }
        String q = query.trim().toLowerCase();

        List<Provider> providers = Operator.getProviders();
        StringBuilder sb = new StringBuilder();
        boolean found = false;

        for (Provider p : providers) {
            if (p.getProviderId().equalsIgnoreCase(q) || p.getName().toLowerCase().contains(q)) {
                found = true;
                List<ServiceRecord> records = Operator.getServiceRecordsForProvider(p.getProviderId());
                double totalFee = records.stream().mapToDouble(ServiceRecord::getFee).sum();

                sb.append("\nProvider: ").append(p.getName()).append(" (ID: ").append(p.getProviderId()).append(")\n");
                sb.append("Address: ").append(p.getAddress()).append(", ").append(p.getCity()).append(", ")
                  .append(p.getState()).append(" ").append(p.getZip()).append("\n");
                sb.append(String.format("Consultations: %03d%n", records.size()));
                sb.append(String.format("Total Due: $%.2f%n", totalFee));
                if (!records.isEmpty()) {
                    sb.append("Services:\n");
                    for (ServiceRecord r : records) {
                        Member m = Operator.findMemberById(r.getMemberId());
                        String mName = (m != null) ? m.getName() : r.getMemberId();
                        sb.append(String.format("  Date: %s, Received: %s, Member: %s (%s), Code: %s, Service: %s, Fee: $%.2f%n",
                                r.getServiceDate(), r.getCurrentDateTime(), mName, r.getMemberId(),
                                r.getServiceCode(), r.getServiceName(), r.getFee()));
                    }
                }
            }
        }

        if (!found) {
            return "No provider found matching '" + query + "'.";
        }
        return sb.toString();
    }

    public static void searchProvidersToPay(Scanner input) {
        System.out.print("Enter Provider ID or Name to search: ");
        String query = input.nextLine().trim();
        System.out.println(searchProvidersToPay(query));
    }

    public static void managerMenu(Scanner input) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n--- MANAGER MENU (" + Operator.getModeName() + ") ---");
            System.out.println("1. Run Main Accounting Procedure (Friday Midnight Batch - All Reports & EFT)");
            System.out.println("2. Generate Member Report");
            System.out.println("3. Generate Provider Report");
            System.out.println("4. Generate Summary Report (Accounts Payable)");
            System.out.println("5. Generate Electronic Funds Transfer (EFT) Data File");
            System.out.println("6. Search Providers Requiring Payment");
            System.out.println("7. Logout");

            System.out.print("Choose an option: ");
            if (!input.hasNextLine()) break;
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.println("\n" + ReportService.runMainAccountingProcedure());
                    break;
                case "2":
                    generateMemberReport(input);
                    break;
                case "3":
                    generateProviderReport(input);
                    break;
                case "4":
                    System.out.println("\n" + generateSummaryReport());
                    break;
                case "5":
                    System.out.println("\n" + ReportService.generateEftReport());
                    break;
                case "6":
                    searchProvidersToPay(input);
                    break;
                case "7":
                    System.out.println("Logging out from Manager account...");
                    inMenu = false;
                    break;
                default:
                    System.out.println("[!] Invalid option. Please enter a number between 1 and 7.");
            }
        }
    }
}
