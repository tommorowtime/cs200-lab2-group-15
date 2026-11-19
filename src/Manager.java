import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Manager {

    private static final List<ServiceRecord> serviceRecords = new ArrayList<>();

    static {
        serviceRecords.add(new ServiceRecord("11-14-2026", "111222333", "123456789", "123456", "Dietitian Session", 100.00, "Initial consult"));
        serviceRecords.add(new ServiceRecord("11-15-2026", "111222333", "987654321", "598470", "Nutrition Consult", 85.50, "Dietary follow-up"));
        serviceRecords.add(new ServiceRecord("11-16-2026", "444555666", "123456789", "883921", "Exercise Therapy", 120.00, "Cardio session"));
        serviceRecords.add(new ServiceRecord("11-16-2026", "444555666", "555666777", "345678", "Physical Rehab", 95.00, "Shoulder therapy"));
    }

    public static List<ServiceRecord> getServiceRecords() {
        return serviceRecords;
    }

    public static void addServiceRecord(ServiceRecord record) {
        if (record != null) {
            serviceRecords.add(record);
        }
    }

    public static String generateMemberReport(String memberId) {
        Member member = Operator.findMemberById(memberId);
        if (member == null) {
            return "Member not found with ID: " + memberId;
        }

        List<ServiceRecord> records = getRecordsForMember(memberId);
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Member Report ---\n");
        sb.append("Name: ").append(member.getName()).append("\n");
        sb.append("ID: ").append(member.getMemberId()).append("\n");
        sb.append("Address: ").append(member.getAddress()).append(", ")
          .append(member.getCity()).append(", ").append(member.getState())
          .append(" ").append(member.getZip()).append("\n");
        sb.append("Status: ").append(member.isSuspended() ? "SUSPENDED" : "ACTIVE").append("\n");
        sb.append("Services:\n");

        if (records.isEmpty()) {
            sb.append("  No services recorded.\n");
        } else {
            for (ServiceRecord r : records) {
                Provider p = Operator.findProviderById(r.getProviderId());
                String pName = (p != null) ? p.getName() : r.getProviderId();
                sb.append("  Date: ").append(r.getServiceDate())
                  .append(", Provider: ").append(pName)
                  .append(", Service: ").append(r.getServiceName()).append("\n");
            }
        }
        sb.append("Total Services: ").append(records.size()).append("\n");

        String report = sb.toString();
        saveReportToFile("MemberReport_" + member.getMemberId() + ".txt", report);
        return report;
    }

    public static void generateMemberReport(Scanner input) {
        System.out.print("Enter 9-digit Member ID: ");
        String memberId = input.nextLine().trim();
        System.out.println(generateMemberReport(memberId));
    }

    public static String generateProviderReport(String providerId) {
        Provider provider = Operator.findProviderById(providerId);
        if (provider == null) {
            return "Provider not found with ID: " + providerId;
        }

        List<ServiceRecord> records = getRecordsForProvider(providerId);
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Provider Report ---\n");
        sb.append("Name: ").append(provider.getName()).append("\n");
        sb.append("ID: ").append(provider.getProviderId()).append("\n");
        sb.append("Address: ").append(provider.getAddress()).append(", ")
          .append(provider.getCity()).append(", ").append(provider.getState())
          .append(" ").append(provider.getZip()).append("\n");
        sb.append("Services:\n");

        double totalFee = 0.0;
        if (records.isEmpty()) {
            sb.append("  No services recorded.\n");
        } else {
            for (ServiceRecord r : records) {
                Member m = Operator.findMemberById(r.getMemberId());
                String mName = (m != null) ? m.getName() : r.getMemberId();
                sb.append(String.format("  Date: %s, Received: %s, Member: %s (%s), Code: %s, Service: %s, Fee: $%.2f%n",
                        r.getServiceDate(), r.getReceivedTimestamp(), mName, r.getMemberId(),
                        r.getServiceCode(), r.getServiceName(), r.getFee()));
                totalFee += r.getFee();
            }
        }
        sb.append("Total Consultations: ").append(records.size()).append("\n");
        sb.append(String.format("Total Fee to be Paid: $%.2f%n", totalFee));

        String report = sb.toString();
        saveReportToFile("ProviderReport_" + provider.getProviderId() + ".txt", report);
        return report;
    }

    public static void generateProviderReport(Scanner input) {
        System.out.print("Enter 9-digit Provider ID: ");
        String providerId = input.nextLine().trim();
        System.out.println(generateProviderReport(providerId));
    }

    public static String generateSummaryReport() {
        List<Provider> providers = Operator.getProviders();
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Summary Report (Accounts Payable) ---\n");

        int totalProvidersToPay = 0;
        int totalConsultations = 0;
        double overallFee = 0.0;

        for (Provider p : providers) {
            List<ServiceRecord> records = getRecordsForProvider(p.getProviderId());
            if (!records.isEmpty()) {
                totalProvidersToPay++;
                totalConsultations += records.size();
                double provFee = records.stream().mapToDouble(ServiceRecord::getFee).sum();
                overallFee += provFee;

                sb.append(String.format("Provider: %s (ID: %s) - Consultations: %d, Total Fee: $%.2f%n",
                        p.getName(), p.getProviderId(), records.size(), provFee));
            }
        }

        if (totalProvidersToPay == 0) {
            sb.append("No providers require payment at this time.\n");
        }

        sb.append("Total Providers to be Paid: ").append(totalProvidersToPay).append("\n");
        sb.append("Total Consultations: ").append(totalConsultations).append("\n");
        sb.append(String.format("Overall Fee to be Paid: $%.2f%n", overallFee));

        String report = sb.toString();
        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        saveReportToFile("SummaryReport_" + date + ".txt", report);
        return report;
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
                List<ServiceRecord> records = getRecordsForProvider(p.getProviderId());
                double totalFee = records.stream().mapToDouble(ServiceRecord::getFee).sum();

                sb.append("\nProvider: ").append(p.getName()).append(" (ID: ").append(p.getProviderId()).append(")\n");
                sb.append("Address: ").append(p.getAddress()).append(", ").append(p.getCity()).append(", ")
                  .append(p.getState()).append(" ").append(p.getZip()).append("\n");
                sb.append("Consultations: ").append(records.size()).append("\n");
                sb.append(String.format("Total Due: $%.2f%n", totalFee));
                if (!records.isEmpty()) {
                    sb.append("Services:\n");
                    for (ServiceRecord r : records) {
                        Member m = Operator.findMemberById(r.getMemberId());
                        String mName = (m != null) ? m.getName() : r.getMemberId();
                        sb.append(String.format("  Date: %s, Member: %s, Service: %s, Fee: $%.2f%n",
                                r.getServiceDate(), mName, r.getServiceName(), r.getFee()));
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
            System.out.println("\n--- Manager Menu ---");
            System.out.println("1. Generate Member Report");
            System.out.println("2. Generate Provider Report");
            System.out.println("3. Generate Summary Report");
            System.out.println("4. Search Providers Requiring Payment");
            System.out.println("5. Logout");

            System.out.print("Choose an option: ");
            String choice = input.nextLine().trim();

            switch (choice) {
                case "1":
                    generateMemberReport(input);
                    break;
                case "2":
                    generateProviderReport(input);
                    break;
                case "3":
                    System.out.println(generateSummaryReport());
                    break;
                case "4":
                    searchProvidersToPay(input);
                    break;
                case "5":
                    System.out.println("Logging out from Manager account...");
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1 and 5.");
            }
        }
    }

    private static List<ServiceRecord> getRecordsForMember(String memberId) {
        List<ServiceRecord> result = new ArrayList<>();
        for (ServiceRecord r : serviceRecords) {
            if (r.getMemberId().equals(memberId)) {
                result.add(r);
            }
        }
        return result;
    }

    private static List<ServiceRecord> getRecordsForProvider(String providerId) {
        List<ServiceRecord> result = new ArrayList<>();
        for (ServiceRecord r : serviceRecords) {
            if (r.getProviderId().equals(providerId)) {
                result.add(r);
            }
        }
        return result;
    }

    private static void saveReportToFile(String filename, String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(content);
            System.out.println("Report saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }
}
