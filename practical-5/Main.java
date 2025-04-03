import java.util.Scanner;

import service.LDAPClientService;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LDAPClientService ldapService = new LDAPClientService();

        while (true) {
            displayMenu();
            int choice = getMenuChoice(scanner);

            switch (choice) {
                case 1:
                    ldapService.searchFriend(scanner);
                    break;
                case 2:
                    ldapService.addFriend(scanner);
                    break;
                case 3:
                    ldapService.modifyPhoneNumber(scanner);
                    break;
                case 4:
                    ldapService.deleteFriend(scanner);
                    break;
                case 5:
                    ldapService.listAllFriends();
                    break;
                case 6:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== LDAP Client Menu ===");
        System.out.println("1. Search Friend");
        System.out.println("2. Add Friend");
        System.out.println("3. Modify Phone Number");
        System.out.println("4. Delete Friend");
        System.out.println("5. List All Friends");
        System.out.println("6. Exit");
        System.out.print("Enter your choice (1-6): ");
    }

    private static int getMenuChoice(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number (1-6): ");
            }
        }
    }
}