import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChocAn Provider Directory.
 * Provides an alphabetically ordered list of health care service names (up to 20 characters),
 * corresponding 6-digit service codes, and fees (up to $999.99).
 * Supports looking up service codes during billing and writing the directory to a file
 * (simulating an email attachment sent to the provider).
 *
 * @author Jackson (Team 15 - Provider Features)
 */
public class ProviderDirectory {

    private static final List<ServiceEntry> directory = new ArrayList<>();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    static {
        // Initialize standard ChocAn health care services (including Appendix A textbook codes)
        directory.add(new ServiceEntry("883948", "Aerobics Exercise", 45.00));
        directory.add(new ServiceEntry("412580", "Choc Detox Therapy", 125.00));
        directory.add(new ServiceEntry("731920", "Cognitive Behavioral", 110.00));
        directory.add(new ServiceEntry("598470", "Dietitian Session", 65.00));
        directory.add(new ServiceEntry("304815", "Group Support Sess", 35.00));
        directory.add(new ServiceEntry("149263", "Internist Consult", 150.00));
        directory.add(new ServiceEntry("652104", "Nutrition Counseling", 80.00));
        directory.add(new ServiceEntry("927411", "Wellness Checkup", 95.00));
        Collections.sort(directory);
    }

    /**
     * Returns an alphabetically ordered list of all available ChocAn services.
     */
    public static List<ServiceEntry> getAllServices() {
        List<ServiceEntry> copy = new ArrayList<>(directory);
        Collections.sort(copy);
        return copy;
    }

    /**
     * Looks up a service entry by its 6-digit service code.
     *
     * @param serviceCode 6-digit service code
     * @return matching ServiceEntry, or null if nonexistent
     */
    public static ServiceEntry findServiceByCode(String serviceCode) {
        if (serviceCode == null) {
            return null;
        }
        String trimmed = serviceCode.trim();
        for (ServiceEntry entry : directory) {
            if (entry.getServiceCode().equals(trimmed)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Formats the Provider Directory as an alphabetically ordered text report.
     */
    public static String formatDirectory() {
        StringBuilder sb = new StringBuilder();
        String today = LocalDate.now().format(DATE_FMT);
        sb.append("====================================================================\n");
        sb.append("             CHOCOHOLICS ANONYMOUS - PROVIDER DIRECTORY             \n");
        sb.append("             Alphabetical List of Service Names, Codes & Fees       \n");
        sb.append("             Generated: ").append(today).append("\n");
        sb.append("====================================================================\n\n");
        sb.append(String.format("%-22s | %-12s | %-10s\n", "SERVICE NAME (<=20)", "SERVICE CODE", "FEE"));
        sb.append("--------------------------------------------------------------------\n");

        for (ServiceEntry entry : getAllServices()) {
            sb.append(String.format("%-22s | %-12s | $%6.2f\n",
                    entry.getServiceName(),
                    entry.getServiceCode(),
                    entry.getFee()));
        }
        sb.append("--------------------------------------------------------------------\n");
        sb.append("Total Services Available: ").append(directory.size()).append("\n");
        return sb.toString();
    }

    /**
     * Writes the Provider Directory to a file on disk (simulating an email attachment).
     *
     * @return the written File, or null if an error occurred
     */
    public static File writeDirectoryToFile() {
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        String dateStr = LocalDate.now().format(DATE_FMT);
        File outFile = new File(reportsDir, "Provider_Directory_" + dateStr + ".txt");
        try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
            out.print(formatDirectory());
            return outFile;
        } catch (IOException e) {
            System.err.println("[!] Error writing Provider Directory file: " + e.getMessage());
            return null;
        }
    }
}
