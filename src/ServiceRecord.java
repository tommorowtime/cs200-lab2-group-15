import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a health care consultation/service record billed to ChocAn.
 * According to the ChocAn specification, each service record written to disk includes:
 * - Current date and time (MM-DD-YYYY HH:MM:SS)
 * - Date service was provided (MM-DD-YYYY)
 * - Provider number (9 digits)
 * - Member number (9 digits)
 * - Service code (6 digits)
 * - Comments (up to 100 characters, optional)
 *
 * Also supports explicit serviceName and fee overrides for unit tests (e.g., ManagerTest).
 *
 * @author Jackson (Team 15 - Provider Features)
 * @author Cameron (Team 15 - Manager Features)
 */
public class ServiceRecord {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    private String currentDateTime; // MM-DD-YYYY HH:MM:SS
    private String serviceDate;     // MM-DD-YYYY
    private String providerId;      // 9 digits
    private String memberId;        // 9 digits
    private String serviceCode;     // 6 digits
    private String comments;        // Up to 100 characters (optional)
    private String customServiceName;
    private Double customFee;

    /**
     * Standard ChocAn ServiceRecord constructor (looks up serviceName and fee from ProviderDirectory).
     */
    public ServiceRecord(String currentDateTime, String serviceDate, String providerId,
                         String memberId, String serviceCode, String comments) {
        this.currentDateTime = (currentDateTime != null && !currentDateTime.isBlank())
                ? currentDateTime.trim()
                : LocalDateTime.now().format(DT_FMT);
        this.serviceDate = serviceDate != null ? serviceDate.trim() : "";
        this.providerId = providerId != null ? providerId.trim() : "";
        this.memberId = memberId != null ? memberId.trim() : "";
        this.serviceCode = serviceCode != null ? serviceCode.trim() : "";
        String cleanComments = comments != null ? comments.trim() : "";
        this.comments = cleanComments.length() > 100 ? cleanComments.substring(0, 100) : cleanComments;
        this.customServiceName = null;
        this.customFee = null;
    }

    /**
     * Overloaded 7-argument constructor used by Manager and ManagerTest.
     */
    public ServiceRecord(String serviceDate, String providerId, String memberId,
                         String serviceCode, String serviceName, double fee, String comments) {
        this(LocalDateTime.now().format(DT_FMT), serviceDate, providerId, memberId, serviceCode, comments);
        if (serviceName != null && !serviceName.isBlank()) {
            String trimmed = serviceName.trim();
            this.customServiceName = trimmed.length() > 20 ? trimmed.substring(0, 20) : trimmed;
        }
        this.customFee = fee;
    }

    public String getCurrentDateTime() {
        return currentDateTime;
    }

    public String getReceivedTimestamp() {
        return currentDateTime;
    }

    public void setCurrentDateTime(String currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public String getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        String clean = comments != null ? comments.trim() : "";
        this.comments = clean.length() > 100 ? clean.substring(0, 100) : clean;
    }

    /**
     * Looks up the 20-character service name from the Provider Directory (or custom override).
     */
    public String getServiceName() {
        ServiceEntry entry = ProviderDirectory.findServiceByCode(serviceCode);
        if (entry != null) {
            return entry.getServiceName();
        }
        if (customServiceName != null && !customServiceName.isBlank()) {
            return customServiceName;
        }
        return "Unknown Service";
    }

    /**
     * Looks up the fee to be paid for this service from the Provider Directory (or custom override).
     */
    public double getFee() {
        ServiceEntry entry = ProviderDirectory.findServiceByCode(serviceCode);
        if (entry != null) {
            return entry.getFee();
        }
        if (customFee != null) {
            return customFee;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return String.format("Recorded: %s | Service Date: %s | Provider: %s | Member: %s | Code: %s (%s) | Fee: $%.2f | Comments: %s",
                currentDateTime, serviceDate, providerId, memberId, serviceCode, getServiceName(), getFee(),
                comments.isEmpty() ? "(none)" : comments);
    }
}
