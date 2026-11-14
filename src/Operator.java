import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Operator {
    // Mode flag: false = Regular (Local In-Memory) Mode, true = Neon Database Mode
    private static boolean useDatabase = false;

    // Regular (Local In-Memory) storage
    private static final List<Member> localMembers = new ArrayList<>();
    private static final List<Provider> localProviders = new ArrayList<>();

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
        return useDatabase ? "NEON DATABASE MODE" : "REGULAR (LOCAL MEMORY) MODE";
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

    public static void addMember(Scanner input) {
        System.out.print("Enter 9-digit Member ID: ");
        String id = input.nextLine().trim();
        if (!id.matches("\\d{9}")) {
            System.out.println("[!] Invalid ID. Member ID must be exactly 9 digits.");
            return;
        }
        if (findMemberById(id) != null) {
            System.out.println("[!] A member with ID " + id + " already exists.");
            return;
        }

        System.out.print("Enter Member Name: ");
        String name = input.nextLine().trim();
        System.out.print("Enter Street Address: ");
        String address = input.nextLine().trim();
        System.out.print("Enter City: ");
        String city = input.nextLine().trim();
        System.out.print("Enter State (2 letters): ");
        String state = input.nextLine().trim();
        System.out.print("Enter ZIP Code (5 digits): ");
        String zip = input.nextLine().trim();

        Member newMember = new Member(id, name, address, city, state, zip, false);
        if (useDatabase) {
            if (ChocAN_DB.addMember(newMember)) {
                System.out.println("Member added to Neon database: " + newMember);
            } else {
                System.out.println("[!] Failed to add member to Neon database.");
            }
        } else {
            localMembers.add(newMember);
            System.out.println("Member added (Regular Mode): " + newMember);
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

        System.out.print("Enter new Name (leave blank to keep '" + member.getName() + "'): ");
        String name = input.nextLine().trim();
        if (!name.isEmpty()) member.setName(name);

        System.out.print("Enter new Address (leave blank to keep '" + member.getAddress() + "'): ");
        String address = input.nextLine().trim();
        if (!address.isEmpty()) member.setAddress(address);

        System.out.print("Enter new City (leave blank to keep '" + member.getCity() + "'): ");
        String city = input.nextLine().trim();
        if (!city.isEmpty()) member.setCity(city);

        System.out.print("Enter new State (leave blank to keep '" + member.getState() + "'): ");
        String state = input.nextLine().trim();
        if (!state.isEmpty()) member.setState(state);

        System.out.print("Enter new ZIP (leave blank to keep '" + member.getZip() + "'): ");
        String zip = input.nextLine().trim();
        if (!zip.isEmpty()) member.setZip(zip);

        System.out.print("Suspended status (true/false, leave blank to keep '" + member.isSuspended() + "'): ");
        String suspendedStr = input.nextLine().trim();
        if (!suspendedStr.isEmpty()) {
            member.setSuspended(Boolean.parseBoolean(suspendedStr));
        }

        if (useDatabase) {
            if (ChocAN_DB.updateMember(member)) {
                System.out.println("Member updated in Neon database: " + member);
            } else {
                System.out.println("[!] Failed to update member in Neon database.");
            }
        } else {
            System.out.println("Member updated (Regular Mode): " + member);
        }
    }

    public static void deleteMember(Scanner input) {
        System.out.print("Enter 9-digit Member ID to delete: ");
        String id = input.nextLine().trim();
        if (useDatabase) {
            if (ChocAN_DB.deleteMember(id)) {
                System.out.println("Member deleted from Neon database.");
            } else {
                System.out.println("[!] Member not found or could not be deleted.");
            }
        } else {
            Member member = findMemberById(id);
            if (member != null) {
                localMembers.remove(member);
                System.out.println("Member deleted (Regular Mode).");
            } else {
                System.out.println("[!] Member not found.");
            }
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
            System.out.println("[!] Invalid ID. Provider ID must be exactly 9 digits.");
            return;
        }
        if (findProviderById(id) != null) {
            System.out.println("[!] A provider with ID " + id + " already exists.");
            return;
        }

        System.out.print("Enter Provider Name: ");
        String name = input.nextLine().trim();
        System.out.print("Enter Street Address: ");
        String address = input.nextLine().trim();
        System.out.print("Enter City: ");
        String city = input.nextLine().trim();
        System.out.print("Enter State (2 letters): ");
        String state = input.nextLine().trim();
        System.out.print("Enter ZIP Code (5 digits): ");
        String zip = input.nextLine().trim();

        Provider newProvider = new Provider(id, name, address, city, state, zip);
        if (useDatabase) {
            if (ChocAN_DB.addProvider(newProvider)) {
                System.out.println("Provider added to Neon database: " + newProvider);
            } else {
                System.out.println("[!] Failed to add provider to Neon database.");
            }
        } else {
            localProviders.add(newProvider);
            System.out.println("Provider added (Regular Mode): " + newProvider);
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

        System.out.print("Enter new Name (leave blank to keep '" + provider.getName() + "'): ");
        String name = input.nextLine().trim();
        if (!name.isEmpty()) provider.setName(name);

        System.out.print("Enter new Address (leave blank to keep '" + provider.getAddress() + "'): ");
        String address = input.nextLine().trim();
        if (!address.isEmpty()) provider.setAddress(address);

        System.out.print("Enter new City (leave blank to keep '" + provider.getCity() + "'): ");
        String city = input.nextLine().trim();
        if (!city.isEmpty()) provider.setCity(city);

        System.out.print("Enter new State (leave blank to keep '" + provider.getState() + "'): ");
        String state = input.nextLine().trim();
        if (!state.isEmpty()) provider.setState(state);

        System.out.print("Enter new ZIP (leave blank to keep '" + provider.getZip() + "'): ");
        String zip = input.nextLine().trim();
        if (!zip.isEmpty()) provider.setZip(zip);

        if (useDatabase) {
            if (ChocAN_DB.updateProvider(provider)) {
                System.out.println("Provider updated in Neon database: " + provider);
            } else {
                System.out.println("[!] Failed to update provider in Neon database.");
            }
        } else {
            System.out.println("Provider updated (Regular Mode): " + provider);
        }
    }

    public static void deleteProvider(Scanner input) {
        System.out.print("Enter 9-digit Provider ID to delete: ");
        String id = input.nextLine().trim();
        if (useDatabase) {
            if (ChocAN_DB.deleteProvider(id)) {
                System.out.println("Provider deleted from Neon database.");
            } else {
                System.out.println("[!] Provider not found or could not be deleted.");
            }
        } else {
            Provider provider = findProviderById(id);
            if (provider != null) {
                localProviders.remove(provider);
                System.out.println("Provider deleted (Regular Mode).");
            } else {
                System.out.println("[!] Provider not found.");
            }
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
}
