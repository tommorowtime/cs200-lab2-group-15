import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ChocAn Accounting Procedure and Managerial Reporting Controller.
 * Generates:
 * 1. Member Reports (sorted by service date, written to <MemberName>_<Date>.txt)
 * 2. Provider Reports (ordered by computer receipt time, written to <ProviderName>_<Date>.txt)
 * 3. Summary Report for Accounts Payable (written to Summary_Report_<Date>.txt)
 * 4. Electronic Funds Transfer (EFT) Data File (written to EFT_Data_<Date>.txt)
 * 5. Main Accounting Procedure (Friday Midnight batch run generating all reports)
 *
 * @author Cameron (Team 15 - Manager Features)
 */
public class ReportService {

    /**
     * Generates a ChocAn Member Report for a specific member and writes it to its own file
     * beginning with the member name followed by the report date.
     */
    public static String generateMemberReport(Member member) {
        if (member == null) {
            return "[!] Member not found.";
        }
        String reportDate = LocalDate.now().format(Operator.DATE_FMT);
        List<ServiceRecord> records = new ArrayList<>(Operator.getServiceRecordsForMember(member.getMemberId()));

        // Sort services in chronological order of service date (MM-DD-YYYY)
        records.sort(Comparator.comparing(sr -> parseDateSafe(sr.getServiceDate())));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                CHOCOHOLICS ANONYMOUS - MEMBER REPORT                   \n");
        sb.append("                Report Date: ").append(reportDate).append("\n");
        sb.append("========================================================================\n");
        sb.append(String.format("Member Name (25 chars)    : %-25s\n", truncate(member.getName(), 25)));
        sb.append(String.format("Member Number (9 digits)  : %-9s\n", member.getMemberId()));
        sb.append(String.format("Street Address (25 chars) : %-25s\n", truncate(member.getAddress(), 25)));
        sb.append(String.format("City (14 chars)           : %-14s\n", truncate(member.getCity(), 14)));
        sb.append(String.format("State (2 letters)         : %-2s\n", truncate(member.getState(), 2)));
        sb.append(String.format("ZIP Code (5 digits)       : %-5s\n", truncate(member.getZip(), 5)));
        sb.append("------------------------------------------------------------------------\n");
        sb.append("SERVICES PROVIDED (Sorted by Date of Service):\n\n");

        if (records.isEmpty()) {
            sb.append("  (No health care consultations recorded for this member this week.)\n");
        } else {
            sb.append(String.format("  %-14s | %-25s | %-20s\n",
                    "SERVICE DATE", "PROVIDER NAME (25)", "SERVICE NAME (20)"));
            sb.append("  ----------------------------------------------------------------------\n");
            for (ServiceRecord sr : records) {
                Provider prov = Operator.findProviderById(sr.getProviderId());
                String provName = prov != null ? prov.getName() : ("Provider " + sr.getProviderId());
                sb.append(String.format("  %-14s | %-25s | %-20s\n",
                        sr.getServiceDate(),
                        truncate(provName, 25),
                        truncate(sr.getServiceName(), 20)));
            }
        }
        sb.append("========================================================================\n");

        File outFile = writeReportFile(sanitizeFileName(member.getName()) + "_" + reportDate + ".txt", sb.toString());
        if (outFile != null) {
            sb.append("-> Saved Member Report File: ").append(outFile.getPath()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Generates Member Reports for all members who have consulted a ChocAn provider
     * (or all registered members if includeAllIfNoConsultations is true).
     */
    public static String generateAllMemberReports(boolean onlyMembersWithConsultations) {
        List<Member> members = Operator.getMembers();
        StringBuilder combined = new StringBuilder();
        int count = 0;

        for (Member m : members) {
            List<ServiceRecord> recs = Operator.getServiceRecordsForMember(m.getMemberId());
            if (onlyMembersWithConsultations && recs.isEmpty()) {
                continue;
            }
            combined.append(generateMemberReport(m)).append("\n");
            count++;
        }

        if (count == 0) {
            return "No members have consulted a ChocAn provider during this period.\n";
        }
        return combined.toString();
    }

    /**
     * Generates a ChocAn Provider Report for a specific provider in the order data were
     * received by the computer, including total consultations (3 digits) and total fee,
     * and writes it to its own file (<ProviderName>_<Date>.txt).
     */
    public static String generateProviderReport(Provider provider) {
        if (provider == null) {
            return "[!] Provider not found.";
        }
        String reportDate = LocalDate.now().format(Operator.DATE_FMT);
        // Records are already stored and retrieved in the order received by the computer
        List<ServiceRecord> records = Operator.getServiceRecordsForProvider(provider.getProviderId());

        StringBuilder sb = new StringBuilder();
        sb.append("===============================================================================================\n");
        sb.append("                           CHOCOHOLICS ANONYMOUS - PROVIDER REPORT                             \n");
        sb.append("                           Report Date: ").append(reportDate).append("\n");
        sb.append("===============================================================================================\n");
        sb.append(String.format("Provider Name (25 chars)    : %-25s\n", truncate(provider.getName(), 25)));
        sb.append(String.format("Provider Number (9 digits)  : %-9s\n", provider.getProviderId()));
        sb.append(String.format("Street Address (25 chars)   : %-25s\n", truncate(provider.getAddress(), 25)));
        sb.append(String.format("City (14 chars)             : %-14s\n", truncate(provider.getCity(), 14)));
        sb.append(String.format("State (2 letters)           : %-2s\n", truncate(provider.getState(), 2)));
        sb.append(String.format("ZIP Code (5 digits)         : %-5s\n", truncate(provider.getZip(), 5)));
        sb.append("-----------------------------------------------------------------------------------------------\n");
        sb.append("SERVICES PROVIDED (Ordered by Date/Time Received by Computer):\n\n");

        double totalFee = 0.0;
        if (records.isEmpty()) {
            sb.append("  (No services billed to ChocAn by this provider this week.)\n");
        } else {
            sb.append(String.format("  %-12s | %-19s | %-25s | %-9s | %-6s | %-20s | %-10s\n",
                    "SERVICE DATE", "RECEIVED DATE/TIME", "MEMBER NAME (25)", "MEMBER ID", "CODE", "SERVICE NAME (20)", "FEE"));
            sb.append("  --------------------------------------------------------------------------------------------------------------------\n");
            for (ServiceRecord sr : records) {
                Member mem = Operator.findMemberById(sr.getMemberId());
                String memName = mem != null ? mem.getName() : "Unknown Member";
                double fee = sr.getFee();
                totalFee += fee;
                sb.append(String.format("  %-12s | %-19s | %-25s | %-9s | %-6s | %-20s | $%6.2f\n",
                        sr.getServiceDate(),
                        sr.getCurrentDateTime(),
                        truncate(memName, 25),
                        sr.getMemberId(),
                        sr.getServiceCode(),
                        truncate(sr.getServiceName(), 20),
                        fee));
            }
        }
        sb.append("-----------------------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Number of Consultations with Members (3 digits) : %03d\n", records.size()));
        sb.append(String.format("Total Fee for Week (up to $99,999.99)                 : $%.2f\n", totalFee));
        sb.append("===============================================================================================\n");

        File outFile = writeReportFile(sanitizeFileName(provider.getName()) + "_" + reportDate + ".txt", sb.toString());
        if (outFile != null) {
            sb.append("-> Saved Provider Report File: ").append(outFile.getPath()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Generates Provider Reports for all providers who billed ChocAn during that week.
     */
    public static String generateAllProviderReports(boolean onlyProvidersWithBillings) {
        List<Provider> providers = Operator.getProviders();
        StringBuilder combined = new StringBuilder();
        int count = 0;

        for (Provider p : providers) {
            List<ServiceRecord> recs = Operator.getServiceRecordsForProvider(p.getProviderId());
            if (onlyProvidersWithBillings && recs.isEmpty()) {
                continue;
            }
            combined.append(generateProviderReport(p)).append("\n");
            count++;
        }

        if (count == 0) {
            return "No providers have billed ChocAn during this period.\n";
        }
        return combined.toString();
    }

    /**
     * Generates the Accounts Payable Summary Report for the Manager:
     * Lists every provider to be paid that week, the number of consultations each had,
     * and his or her total fee for that week. Finally prints total number of providers
     * who provided services, total number of consultations, and overall fee total.
     */
    public static String generateSummaryReport() {
        String reportDate = LocalDate.now().format(Operator.DATE_FMT);
        List<Provider> providers = Operator.getProviders();

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("       CHOCOHOLICS ANONYMOUS - Accounts Payable Summary Report                  \n");
        sb.append("       Report Date: ").append(reportDate).append(" | Mode: ").append(Operator.getModeName()).append("\n");
        sb.append("================================================================================\n\n");
        sb.append(String.format("%-25s | %-12s | %-15s | %-15s\n",
                "PROVIDER NAME (25)", "PROVIDER ID", "CONSULTATIONS", "TOTAL FEE"));
        sb.append("--------------------------------------------------------------------------------\n");

        int totalProvidersPaid = 0;
        int totalConsultations = 0;
        double overallFeeTotal = 0.0;

        for (Provider p : providers) {
            List<ServiceRecord> records = Operator.getServiceRecordsForProvider(p.getProviderId());
            if (records.isEmpty()) {
                continue;
            }
            totalProvidersPaid++;
            int count = records.size();
            double providerFee = 0.0;
            for (ServiceRecord sr : records) {
                providerFee += sr.getFee();
            }
            totalConsultations += count;
            overallFeeTotal += providerFee;

            sb.append(String.format("%-25s | %-12s | %03d             | $%9.2f\n",
                    truncate(p.getName(), 25),
                    p.getProviderId(),
                    count,
                    providerFee));
        }

        if (totalProvidersPaid == 0) {
            sb.append("  (No providers billed services during this period.)\n");
        }

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Number of Providers Who Provided Services (Total Providers to be Paid): %d\n", totalProvidersPaid));
        sb.append(String.format("Total Number of Consultations                                               : %03d\n", totalConsultations));
        sb.append(String.format("Overall Fee Total for Week                                                  : $%.2f\n", overallFeeTotal));
        sb.append("================================================================================\n");

        File outFile = writeReportFile("Summary_Report_" + reportDate + ".txt", sb.toString());
        if (outFile != null) {
            sb.append("-> Saved Summary Report File: ").append(outFile.getPath()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Generates the Electronic Funds Transfer (EFT) data file containing:
     * Provider Name, Provider Number, and Amount to be Transferred.
     */
    public static String generateEftReport() {
        String reportDate = LocalDate.now().format(Operator.DATE_FMT);
        List<Provider> providers = Operator.getProviders();

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("         CHOCOHOLICS ANONYMOUS - ELECTRONIC FUNDS TRANSFER (EFT)        \n");
        sb.append("         Date: ").append(reportDate).append("\n");
        sb.append("========================================================================\n");
        sb.append(String.format("%-25s | %-12s | %-18s\n",
                "PROVIDER NAME (25)", "PROVIDER ID", "AMOUNT TO TRANSFER"));
        sb.append("------------------------------------------------------------------------\n");

        int count = 0;
        for (Provider p : providers) {
            List<ServiceRecord> records = Operator.getServiceRecordsForProvider(p.getProviderId());
            if (records.isEmpty()) {
                continue;
            }
            double amount = 0.0;
            for (ServiceRecord sr : records) {
                amount += sr.getFee();
            }
            sb.append(String.format("%-25s | %-12s | $%9.2f\n",
                    truncate(p.getName(), 25),
                    p.getProviderId(),
                    amount));
            count++;
        }

        if (count == 0) {
            sb.append("  (No EFT transfers required for this period.)\n");
        }
        sb.append("========================================================================\n");

        File outFile = writeReportFile("EFT_Data_" + reportDate + ".txt", sb.toString());
        if (outFile != null) {
            sb.append("-> Saved EFT Data File: ").append(outFile.getPath()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Runs the ChocAn Main Accounting Procedure (simulating the Friday Midnight batch run).
     * Reads the week's file of services provided and generates:
     * 1. Member Reports for each member who consulted a provider that week
     * 2. Provider Reports for each provider who billed ChocAn that week
     * 3. Accounts Payable Summary Report for the manager
     * 4. EFT Data record file on disk
     */
    public static String runMainAccountingProcedure() {
        StringBuilder sb = new StringBuilder();
        sb.append("################################################################################\n");
        sb.append("#        CHOCAN DATA CENTER - MAIN ACCOUNTING PROCEDURE (FRIDAY MIDNIGHT)      #\n");
        sb.append("################################################################################\n\n");

        sb.append(">>> [1/4] GENERATING WEEKLY MEMBER REPORTS (Members with Consultations)...\n\n");
        sb.append(generateAllMemberReports(true)).append("\n");

        sb.append(">>> [2/4] GENERATING WEEKLY PROVIDER REPORTS (Providers with Billings)...\n\n");
        sb.append(generateAllProviderReports(true)).append("\n");

        sb.append(">>> [3/4] GENERATING ACCOUNTS PAYABLE SUMMARY REPORT...\n\n");
        sb.append(generateSummaryReport()).append("\n");

        sb.append(">>> [4/4] WRITING ELECTRONIC FUNDS TRANSFER (EFT) DATA FILE...\n\n");
        sb.append(generateEftReport()).append("\n");

        sb.append("################################################################################\n");
        sb.append("#        MAIN ACCOUNTING PROCEDURE COMPLETED. ALL FILES SAVED TO /reports      #\n");
        sb.append("################################################################################\n");
        return sb.toString();
    }

    // ==================== INTERNAL FILE & STRING HELPERS ====================

    private static File writeReportFile(String fileName, String content) {
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        File outFile = new File(reportsDir, fileName);
        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            out.print(content);
            return outFile;
        } catch (IOException e) {
            System.err.println("[!] Error writing report file " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "";
        String t = val.trim();
        return t.length() > maxLen ? t.substring(0, maxLen) : t;
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "Unknown";
        }
        return name.trim().replaceAll("[^a-zA-Z0-9\\-_ ]", "").replaceAll("\\s+", "_");
    }

    private static LocalDate parseDateSafe(String dateStr) {
        try {
            return LocalDate.parse(dateStr, Operator.DATE_FMT);
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }
}
