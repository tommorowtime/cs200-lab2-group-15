import java.util.Scanner;

public class Operator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] members = new String[10];
        String[] providers = new String[10];

        int choice;
        int memberCount = 0;
        int providerCount = 0;

        do {
            System.out.println("1. Add member");
            System.out.println("2. View Members");
            System.out.println("3. Delete Members");
            System.out.println("4. Add provider");
            System.out.println("5. View Providers");
            System.out.println("6. Delete Providers");
            System.out.println("7. Exit");
            
            System.out.print("Enter your choice: ");
            
            choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter member name: ");
                members[memberCount] = scanner.nextLine();
                memberCount++;
            }
            else if (choice == 2) {
                for (int i = 0; i < memberCount; i++) {
                    System.out.println(members[i]);
                }
            }
            else if (choice == 3) {
                System.out.print("Enter member number to delete: ");
                int number = scanner.nextInt();
                scanner.nextLine();

                if (number >= 0 && number < memberCount) {
                    members[number] = null;
                    System.out.println("Member deleted.");
                }
            }
            else if (choice == 4) {
                System.out.print("Enter provider name: ");
                providers[providerCount] = scanner.nextLine();
                providerCount++;
            }
            else if (choice == 5) {
                for (int i = 0; i < providerCount; i++) {
                    System.out.println(providers[i]);
                }
            }
            else if (choice == 6) {
                System.out.print("Enter provider number to delete: ");
                int number = scanner.nextInt();
                scanner.nextLine();

                if (number >= 0 && number < providerCount) {
                    providers[number] = null;
                    System.out.println("Provider deleted.");
                }
            }
            else if (choice == 7) {
                System.out.println("Exiting...");
            }

        } while (choice != 7);
    }
}
