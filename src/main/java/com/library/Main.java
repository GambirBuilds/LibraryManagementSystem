package com.library;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Member;
import com.library.service.LibraryService;
import com.library.util.InputHelper;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point - menu-driven console application for Library Management System.
 */
public class Main {

    private static final LibraryService service = new LibraryService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final InputHelper input = new InputHelper(scanner);

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = input.readMenuChoice(0, 5);
            System.out.println();
            try {
                switch (choice) {
                    case 1 -> bookMenu();
                    case 2 -> memberMenu();
                    case 3 -> borrowReturnMenu();
                    case 4 -> reportsMenu();
                    case 5 -> seedSampleData();
                    case 0 -> {
                        System.out.println("Thank you for using the Library Management System. Goodbye!");
                        running = false;
                    }
                }
            } catch (LibraryException e) {
                System.out.println("  [ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
            }
            if (running) {
                System.out.println();
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    // ==================== MENUS ====================

    private static void printBanner() {
        System.out.println("======================================================");
        System.out.println("       LIBRARY MANAGEMENT SYSTEM                      ");
        System.out.println("       Core Java + JDBC + Collections                 ");
        System.out.println("======================================================");
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("==================== MAIN MENU ====================");
        System.out.println("  1. Book Management");
        System.out.println("  2. Member Management");
        System.out.println("  3. Issue / Return Books");
        System.out.println("  4. Reports");
        System.out.println("  5. Seed Sample Data (for testing)");
        System.out.println("  0. Exit");
        System.out.println("===================================================");
    }

    private static void bookMenu() throws LibraryException {
        boolean back = false;
        while (!back) {
            System.out.println("--- Book Management ---");
            System.out.println("  1. Add Book");
            System.out.println("  2. Update Book");
            System.out.println("  3. Delete Book");
            System.out.println("  4. Search Books");
            System.out.println("  5. List All Books");
            System.out.println("  0. Back to Main Menu");
            int choice = input.readMenuChoice(0, 5);
            System.out.println();
            switch (choice) {
                case 1 -> addBook();
                case 2 -> updateBook();
                case 3 -> deleteBook();
                case 4 -> searchBooks();
                case 5 -> listAllBooks();
                case 0 -> back = true;
            }
            if (!back) System.out.println();
        }
    }

    private static void memberMenu() throws LibraryException {
        boolean back = false;
        while (!back) {
            System.out.println("--- Member Management ---");
            System.out.println("  1. Register Member");
            System.out.println("  2. Update Member");
            System.out.println("  3. Delete Member");
            System.out.println("  4. Search Members");
            System.out.println("  5. List All Members");
            System.out.println("  0. Back to Main Menu");
            int choice = input.readMenuChoice(0, 5);
            System.out.println();
            switch (choice) {
                case 1 -> registerMember();
                case 2 -> updateMember();
                case 3 -> deleteMember();
                case 4 -> searchMembers();
                case 5 -> listAllMembers();
                case 0 -> back = true;
            }
            if (!back) System.out.println();
        }
    }

    private static void borrowReturnMenu() throws LibraryException {
        boolean back = false;
        while (!back) {
            System.out.println("--- Issue / Return ---");
            System.out.println("  1. Issue Book");
            System.out.println("  2. Return Book");
            System.out.println("  3. View Member's Active Borrowings");
            System.out.println("  0. Back to Main Menu");
            int choice = input.readMenuChoice(0, 3);
            System.out.println();
            switch (choice) {
                case 1 -> issueBook();
                case 2 -> returnBook();
                case 3 -> viewMemberActive();
                case 0 -> back = true;
            }
            if (!back) System.out.println();
        }
    }

    private static void reportsMenu() throws LibraryException {
        boolean back = false;
        while (!back) {
            System.out.println("--- Reports ---");
            System.out.println("  1. Currently Borrowed Books");
            System.out.println("  2. Overdue Books");
            System.out.println("  3. Member Borrowing History");
            System.out.println("  4. Category-wise Book Statistics");
            System.out.println("  0. Back to Main Menu");
            int choice = input.readMenuChoice(0, 4);
            System.out.println();
            switch (choice) {
                case 1 -> showCurrentlyBorrowed();
                case 2 -> showOverdue();
                case 3 -> showMemberHistory();
                case 4 -> showCategoryStats();
                case 0 -> back = true;
            }
            if (!back) System.out.println();
        }
    }

    // ==================== BOOK ACTIONS ====================

    private static void addBook() throws LibraryException {
        System.out.println(">> Add New Book");
        String title = input.readNonEmptyString("Title: ");
        String author = input.readNonEmptyString("Author: ");
        String isbn = input.readNonEmptyString("ISBN: ");
        String category = input.readNonEmptyString("Category: ");
        int copies = input.readPositiveInt("Number of copies: ");
        service.addBook(title, author, isbn, category, copies);
    }

    private static void updateBook() throws LibraryException {
        System.out.println(">> Update Book");
        int id = input.readPositiveInt("Book ID: ");
        Book existing = service.getBookOrThrow(id);
        System.out.println("Current: " + existing);
        String title = input.readNonEmptyString("New Title [" + existing.getTitle() + "]: ");
        String author = input.readNonEmptyString("New Author [" + existing.getAuthor() + "]: ");
        String isbn = input.readNonEmptyString("New ISBN [" + existing.getIsbn() + "]: ");
        String category = input.readNonEmptyString("New Category [" + existing.getCategory() + "]: ");
        int total = input.readPositiveInt("New Total Copies [" + existing.getTotalCopies() + "]: ");
        service.updateBook(id, title, author, isbn, category, total);
    }

    private static void deleteBook() throws LibraryException {
        System.out.println(">> Delete Book");
        int id = input.readPositiveInt("Book ID: ");
        Book book = service.getBookOrThrow(id);
        System.out.println("About to delete: " + book);
        if (input.readYesNo("Are you sure?")) {
            service.deleteBook(id);
        } else {
            System.out.println("  Cancelled.");
        }
    }

    private static void searchBooks() throws LibraryException {
        System.out.println(">> Search Books (by title or author)");
        String keyword = input.readNonEmptyString("Keyword: ");
        List<Book> results = service.searchBooks(keyword);
        if (results.isEmpty()) {
            System.out.println("  No books found matching '" + keyword + "'.");
        } else {
            System.out.println("  Found " + results.size() + " book(s):");
            results.forEach(b -> System.out.println("    " + b));
        }
    }

    private static void listAllBooks() throws LibraryException {
        System.out.println(">> All Books");
        List<Book> books = service.listAllBooks();
        if (books.isEmpty()) {
            System.out.println("  No books in the catalog.");
        } else {
            books.forEach(b -> System.out.println("  " + b));
            System.out.println("  Total: " + books.size() + " book(s)");
        }
    }

    // ==================== MEMBER ACTIONS ====================

    private static void registerMember() throws LibraryException {
        System.out.println(">> Register New Member");
        String name = input.readNonEmptyString("Name: ");
        String email = input.readNonEmptyString("Email: ");
        String phone = input.readOptionalString("Phone (optional): ");
        service.registerMember(name, email, phone.isEmpty() ? null : phone);
    }

    private static void updateMember() throws LibraryException {
        System.out.println(">> Update Member");
        int id = input.readPositiveInt("Member ID: ");
        Member existing = service.getMemberOrThrow(id);
        System.out.println("Current: " + existing.getDisplayInfo());
        String name = input.readNonEmptyString("New Name [" + existing.getName() + "]: ");
        String email = input.readNonEmptyString("New Email [" + existing.getEmail() + "]: ");
        String phone = input.readOptionalString("New Phone [" + (existing.getPhone() == null ? "" : existing.getPhone()) + "]: ");
        service.updateMember(id, name, email, phone.isEmpty() ? existing.getPhone() : phone);
    }

    private static void deleteMember() throws LibraryException {
        System.out.println(">> Delete Member");
        int id = input.readPositiveInt("Member ID: ");
        Member member = service.getMemberOrThrow(id);
        System.out.println("About to delete: " + member.getDisplayInfo());
        if (input.readYesNo("Are you sure?")) {
            service.deleteMember(id);
        } else {
            System.out.println("  Cancelled.");
        }
    }

    private static void searchMembers() throws LibraryException {
        System.out.println(">> Search Members by Name");
        String name = input.readNonEmptyString("Name: ");
        List<Member> results = service.searchMembers(name);
        if (results.isEmpty()) {
            System.out.println("  No members found.");
        } else {
            results.forEach(m -> System.out.println("  " + m.getDisplayInfo()));
        }
    }

    private static void listAllMembers() throws LibraryException {
        System.out.println(">> All Members");
        List<Member> members = service.listAllMembers();
        if (members.isEmpty()) {
            System.out.println("  No members registered.");
        } else {
            members.forEach(m -> System.out.println("  " + m.getDisplayInfo()));
            System.out.println("  Total: " + members.size() + " member(s)");
        }
    }

    // ==================== BORROW / RETURN ====================

    private static void issueBook() throws LibraryException {
        System.out.println(">> Issue Book");
        int bookId = input.readPositiveInt("Book ID: ");
        int memberId = input.readPositiveInt("Member ID: ");
        int days = input.readPositiveInt("Loan period (days) [default 14]: ");
        service.issueBook(bookId, memberId, days);
    }

    private static void returnBook() throws LibraryException {
        System.out.println(">> Return Book");
        int borrowingId = input.readPositiveInt("Borrowing ID: ");
        service.returnBook(borrowingId);
    }

    private static void viewMemberActive() throws LibraryException {
        System.out.println(">> Member's Active Borrowings");
        int memberId = input.readPositiveInt("Member ID: ");
        List<Borrowing> list = service.getMemberActiveBorrowings(memberId);
        if (list.isEmpty()) {
            System.out.println("  No active borrowings for this member.");
        } else {
            list.forEach(b -> System.out.println("  " + b));
        }
    }

    // ==================== REPORTS ====================

    private static void showCurrentlyBorrowed() throws LibraryException {
        System.out.println(">> Currently Borrowed Books");
        List<Borrowing> list = service.getCurrentlyBorrowed();
        if (list.isEmpty()) {
            System.out.println("  No books currently borrowed.");
        } else {
            list.forEach(b -> System.out.println("  " + b));
            System.out.println("  Total active: " + list.size());
        }
    }

    private static void showOverdue() throws LibraryException {
        System.out.println(">> Overdue Books");
        List<Borrowing> list = service.getOverdueBooks();
        if (list.isEmpty()) {
            System.out.println("  No overdue books. Great!");
        } else {
            list.forEach(b -> System.out.println("  " + b));
            System.out.println("  Total overdue: " + list.size());
        }
    }

    private static void showMemberHistory() throws LibraryException {
        System.out.println(">> Member Borrowing History");
        int memberId = input.readPositiveInt("Member ID: ");
        List<Borrowing> list = service.getMemberHistory(memberId);
        if (list.isEmpty()) {
            System.out.println("  No borrowing history for this member.");
        } else {
            list.forEach(b -> System.out.println("  " + b));
        }
    }

    private static void showCategoryStats() throws LibraryException {
        System.out.println(">> Category-wise Book Statistics (total copies)");
        Map<String, Integer> stats = service.getCategoryStatistics();
        if (stats.isEmpty()) {
            System.out.println("  No books in catalog.");
        } else {
            stats.forEach((cat, count) ->
                    System.out.printf("  %-20s : %d copies%n", cat, count));
        }
    }

    // ==================== SAMPLE DATA ====================

    private static void seedSampleData() {
        System.out.println(">> Seeding sample data...");
        try {
            service.addBook("Clean Code", "Robert C. Martin", "978-0132350884", "Programming", 3);
            service.addBook("Effective Java", "Joshua Bloch", "978-0134685991", "Programming", 2);
            service.addBook("Design Patterns", "Gang of Four", "978-0201633610", "Software Engineering", 2);
            service.addBook("The Pragmatic Programmer", "Hunt & Thomas", "978-0135957059", "Programming", 4);
            service.addBook("Introduction to Algorithms", "CLRS", "978-0262033848", "Computer Science", 1);

            service.registerMember("Alice Johnson", "alice@example.com", "555-0101");
            service.registerMember("Bob Smith", "bob@example.com", "555-0102");
            service.registerMember("Carol White", "carol@example.com", null);

            System.out.println("  [OK] Sample books and members added.");
            System.out.println("  You can now issue books using the IDs shown above.");
        } catch (LibraryException e) {
            System.out.println("  [NOTE] Some sample data may already exist: " + e.getMessage());
        }
    }
}
