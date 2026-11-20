/**
 * Represents a Chocoholics Anonymous (ChocAn) Health Care Provider.
 * Enforces ChocAn data field constraints:
 * - Provider number: 9 digits
 * - Provider name: 25 characters
 * - Provider street address: 25 characters
 * - Provider city: 14 characters
 * - Provider state: 2 letters
 * - Provider ZIP code: 5 digits
 *
 * @author Teal'c (Team 15 - Operator Features)
 */
public class Provider {
    private String providerId; // 9-digit ChocAn Provider ID
    private String name;       // 25 characters max
    private String address;    // 25 characters max
    private String city;       // 14 characters max
    private String state;      // 2 letters
    private String zip;        // 5 digits

    public Provider(String providerId, String name, String address, String city, String state, String zip) {
        setProviderId(providerId);
        setName(name);
        setAddress(address);
        setCity(city);
        setState(state);
        setZip(zip);
    }

    public Provider(String providerId, String name) {
        this(providerId, name, "", "", "", "");
    }

    private static String limitLength(String value, int maxLen) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() > maxLen ? trimmed.substring(0, maxLen) : trimmed;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = limitLength(providerId, 9);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = limitLength(name, 25);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = limitLength(address, 25);
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = limitLength(city, 14);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = limitLength(state, 2).toUpperCase();
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = limitLength(zip, 5);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-25s - %s, %s, %s %s",
                providerId, name, address, city, state, zip);
    }
}
