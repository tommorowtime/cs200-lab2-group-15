/**
 * Represents a Chocoholics Anonymous (ChocAn) Member.
 * Enforces ChocAn data field constraints:
 * - Member number: 9 digits
 * - Member name: 25 characters
 * - Member street address: 25 characters
 * - Member city: 14 characters
 * - Member state: 2 letters
 * - Member ZIP code: 5 digits
 *
 * @author Teal'c (Team 15 - Operator Features)
 */
public class Member {
    private String memberId; // 9-digit ChocAn Member ID
    private String name;     // 25 characters max
    private String address;  // 25 characters max
    private String city;     // 14 characters max
    private String state;    // 2 letters
    private String zip;      // 5 digits
    private boolean suspended;

    public Member(String memberId, String name, String address, String city, String state, String zip, boolean suspended) {
        setMemberId(memberId);
        setName(name);
        setAddress(address);
        setCity(city);
        setState(state);
        setZip(zip);
        this.suspended = suspended;
    }

    public Member(String memberId, String name) {
        this(memberId, name, "", "", "", "", false);
    }

    private static String limitLength(String value, int maxLen) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() > maxLen ? trimmed.substring(0, maxLen) : trimmed;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = limitLength(memberId, 9);
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

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    @Override
    public String toString() {
        String status = suspended ? "SUSPENDED" : "ACTIVE";
        return String.format("[%s] %-25s (%-9s) - %s, %s, %s %s",
                memberId, name, status, address, city, state, zip);
    }
}
