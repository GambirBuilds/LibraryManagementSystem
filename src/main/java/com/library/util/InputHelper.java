package com.library.util;

import java.util.Scanner;

/**
 * Utility class for safe console input with validation.
 */
public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [!] Input cannot be empty. Please try again.");
        }
    }

    public int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("  [!] Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please enter digits only.");
            }
        }
    }

    public int readNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= 0) {
                    return value;
                }
                System.out.println("  [!] Please enter zero or a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please enter digits only.");
            }
        }
    }

    public int readMenuChoice(int min, int max) {
        while (true) {
            System.out.print("Enter your choice (" + min + "-" + max + "): ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.println("  [!] Choice must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a number.");
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("  [!] Please enter 'y' or 'n'.");
        }
    }

    public String readOptionalString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
