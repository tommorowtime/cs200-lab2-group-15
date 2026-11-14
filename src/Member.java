public class Member {
    private String memberId; // 9-digit ChocAn Member ID
    private String name;
    private String address;
    private String city;
    private String state;
    private String zip;
    private boolean suspended;

    public Member(String memberId, String name, String address, String city, String state, String zip, boolean suspended) {
        this.memberId = memberId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.suspended = suspended;
    }

    public Member(String memberId, String name) {
        this(memberId, name, "", "", "", "", false);
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
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
        return String.format("[%s] %s (%s) - %s, %s, %s %s",
                memberId, name, status, address, city, state, zip);
    }
}
