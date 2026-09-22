package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.BorrowingDAO;
import com.library.dao.MemberDAO;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Member;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic layer. Uses Collections Framework extensively
 * (ArrayList, HashMap) and demonstrates sorting / filtering.
 */
public class LibraryService {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowingDAO borrowingDAO = new BorrowingDAO();

    // In-memory caches for fast lookup (demonstrates HashMap usage)
    private final Map<Integer, Book> bookCache = new HashMap<>();
    private final Map<Integer, Member> memberCache = new HashMap<>();

    // ==================== BOOK OPERATIONS ====================

    public void addBook(String title, String author, String isbn, String category, int copies)
            throws LibraryException {
        if (copies <= 0) {
            throw new LibraryException("Number of copies must be positive.");
        }
        Book book = new Book(0, title, author, isbn, category, copies, copies);
        bookDAO.addBook(book);
        bookCache.put(book.getId(), book);
        System.out.println("  [OK] Book added successfully with ID: " + book.getId());
    }

    public void updateBook(int id, String title, String author, String isbn, String category,
                           int totalCopies) throws LibraryException {
        Book existing = getBookOrThrow(id);
        int borrowed = existing.getTotalCopies() - existing.getAvailableCopies();
        if (totalCopies < borrowed) {
            throw new LibraryException("Cannot set total copies below currently borrowed quantity (" + borrowed + ").");
        }
        existing.setTitle(title);
        existing.setAuthor(author);
        existing.setIsbn(isbn);
        existing.setCategory(category);
        existing.setTotalCopies(totalCopies);
        existing.setAvailableCopies(totalCopies - borrowed);
        bookDAO.updateBook(existing);
        bookCache.put(id, existing);
        System.out.println("  [OK] Book updated successfully.");
    }

    public void deleteBook(int id) throws LibraryException {
        // Check if any active borrowings exist
        List<Borrowing> active = borrowingDAO.findAllActive();
        boolean hasActive = active.stream().anyMatch(b -> b.getBookId() == id);
        if (hasActive) {
            throw new LibraryException("Cannot delete book - it has active borrowings.");
        }
        bookDAO.deleteBook(id);
        bookCache.remove(id);
        System.out.println("  [OK] Book deleted successfully.");
    }

    public Book getBookOrThrow(int id) throws LibraryException {
        Book book = bookCache.get(id);
        if (book == null) {
            book = bookDAO.findById(id);
            if (book != null) {
                bookCache.put(id, book);
            }
        }
        if (book == null) {
            throw new LibraryException("Book with ID " + id + " not found.");
        }
        return book;
    }

    public List<Book> searchBooks(String keyword) throws LibraryException {
        List<Book> results = bookDAO.findByTitleOrAuthor(keyword);
        // Cache them
        for (Book b : results) {
            bookCache.put(b.getId(), b);
        }
        return results;
    }

    public List<Book> listAllBooks() throws LibraryException {
        List<Book> books = bookDAO.findAll();
        // Refresh cache
        bookCache.clear();
        for (Book b : books) {
            bookCache.put(b.getId(), b);
        }
        // Demonstrate sorting with Comparator
        books.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return books;
    }

    public List<Book> listBooksByCategory(String category) throws LibraryException {
        List<Book> all = listAllBooks();
        return all.stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    // ==================== MEMBER OPERATIONS ====================

    public void registerMember(String name, String email, String phone) throws LibraryException {
        Member member = new Member(0, name, email, phone, LocalDate.now());
        memberDAO.addMember(member);
        memberCache.put(member.getId(), member);
        System.out.println("  [OK] Member registered successfully with ID: " + member.getId());
    }

    public void updateMember(int id, String name, String email, String phone) throws LibraryException {
        Member existing = getMemberOrThrow(id);
        existing.setName(name);
        existing.setEmail(email);
        existing.setPhone(phone);
        memberDAO.updateMember(existing);
        memberCache.put(id, existing);
        System.out.println("  [OK] Member updated successfully.");
    }

    public void deleteMember(int id) throws LibraryException {
        List<Borrowing> active = borrowingDAO.findActiveByMember(id);
        if (!active.isEmpty()) {
            throw new LibraryException("Cannot delete member - they have active borrowings.");
        }
        memberDAO.deleteMember(id);
        memberCache.remove(id);
        System.out.println("  [OK] Member deleted successfully.");
    }

    public Member getMemberOrThrow(int id) throws LibraryException {
        Member member = memberCache.get(id);
        if (member == null) {
            member = memberDAO.findById(id);
            if (member != null) {
                memberCache.put(id, member);
            }
        }
        if (member == null) {
            throw new LibraryException("Member with ID " + id + " not found.");
        }
        return member;
    }

    public List<Member> searchMembers(String name) throws LibraryException {
        List<Member> results = memberDAO.findByName(name);
        for (Member m : results) {
            memberCache.put(m.getId(), m);
        }
        return results;
    }

    public List<Member> listAllMembers() throws LibraryException {
        List<Member> members = memberDAO.findAll();
        memberCache.clear();
        for (Member m : members) {
            memberCache.put(m.getId(), m);
        }
        // Sort by name
        members.sort(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER));
        return members;
    }

    // ==================== BORROW / RETURN ====================

    public void issueBook(int bookId, int memberId, int loanDays) throws LibraryException {
        Book book = getBookOrThrow(bookId);
        Member member = getMemberOrThrow(memberId);

        if (!book.isAvailable()) {
            throw new LibraryException("No copies of '" + book.getTitle() + "' are currently available.");
        }

        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(loanDays);

        Borrowing borrowing = new Borrowing(0, bookId, memberId, issueDate, dueDate, null, Borrowing.Status.BORROWED);
        borrowingDAO.addBorrowing(borrowing);

        // Decrease available copies
        bookDAO.updateAvailableCopies(bookId, -1);
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookCache.put(bookId, book);

        System.out.println("  [OK] Book issued successfully.");
        System.out.println("       Borrowing ID : " + borrowing.getId());
        System.out.println("       Due Date     : " + dueDate);
    }

    public void returnBook(int borrowingId) throws LibraryException {
        Borrowing borrowing = borrowingDAO.findById(borrowingId);
        if (borrowing == null) {
            throw new LibraryException("Borrowing record with ID " + borrowingId + " not found.");
        }
        if (borrowing.getStatus() != Borrowing.Status.BORROWED) {
            throw new LibraryException("This book has already been returned.");
        }

        LocalDate returnDate = LocalDate.now();
        borrowingDAO.markReturned(borrowingId, returnDate);

        // Increase available copies
        bookDAO.updateAvailableCopies(borrowing.getBookId(), 1);
        Book book = bookCache.get(borrowing.getBookId());
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        } else {
            // reload
            book = bookDAO.findById(borrowing.getBookId());
            if (book != null) bookCache.put(book.getId(), book);
        }

        System.out.println("  [OK] Book returned successfully.");
        if (returnDate.isAfter(borrowing.getDueDate())) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(borrowing.getDueDate(), returnDate);
            System.out.println("  [!] Note: Book was returned " + daysLate + " day(s) overdue.");
        }
    }

    // ==================== REPORTS ====================

    public List<Borrowing> getCurrentlyBorrowed() throws LibraryException {
        return borrowingDAO.findAllActive();
    }

    public List<Borrowing> getOverdueBooks() throws LibraryException {
        return borrowingDAO.findOverdue();
    }

    public List<Borrowing> getMemberHistory(int memberId) throws LibraryException {
        getMemberOrThrow(memberId); // validate exists
        return borrowingDAO.findHistoryByMember(memberId);
    }

    public List<Borrowing> getMemberActiveBorrowings(int memberId) throws LibraryException {
        getMemberOrThrow(memberId);
        return borrowingDAO.findActiveByMember(memberId);
    }

    /**
     * Demonstrates Map usage: category → list of books, then compute stats.
     */
    public Map<String, Integer> getCategoryStatistics() throws LibraryException {
        List<Book> books = listAllBooks();
        Map<String, Integer> stats = new TreeMap<>(); // sorted by category name
        for (Book b : books) {
            stats.merge(b.getCategory(), b.getTotalCopies(), Integer::sum);
        }
        return stats;
    }
}
