/**
 * Represents a single health care service entry in the ChocAn Provider Directory.
 * Contains a 6-digit service code, a service name (up to 20 characters),
 * and a service fee (up to $999.99).
 *
 * @author Jackson (Team 15 - Provider Features)
 */
public class ServiceEntry implements Comparable<ServiceEntry> {
    private final String serviceCode; // 6 digits
    private final String serviceName; // Up to 20 characters
    private final double fee;         // Up to $999.99

    public ServiceEntry(String serviceCode, String serviceName, double fee) {
        this.serviceCode = serviceCode != null ? serviceCode.trim() : "";
        String trimmedName = serviceName != null ? serviceName.trim() : "";
        this.serviceName = trimmedName.length() > 20 ? trimmedName.substring(0, 20) : trimmedName;
        this.fee = Math.min(Math.max(fee, 0.0), 999.99);
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getFee() {
        return fee;
    }

    @Override
    public int compareTo(ServiceEntry other) {
        return this.serviceName.compareToIgnoreCase(other.serviceName);
    }

    @Override
    public String toString() {
        return String.format("%-20s | Code: %-6s | Fee: $%6.2f", serviceName, serviceCode, fee);
    }
}
