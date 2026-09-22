package com.library.model;

import java.time.LocalDate;

/**
 * Represents a book borrowing transaction.
 */
public class Borrowing {
    public enum Status {
        BORROWED, RETURNED, OVERDUE
    }

    private int id;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Status status;

    // Optional display fields (joined data)
    private String bookTitle;
    private String memberName;

    public Borrowing() {
    }

    public Borrowing(int id, int bookId, int memberId, LocalDate issueDate,
                     LocalDate dueDate, LocalDate returnDate, Status status) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public boolean isOverdue() {
        return status == Status.BORROWED && LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return String.format(
                "Borrowing[ID=%d, Book='%s' (ID=%d), Member='%s' (ID=%d), Issued=%s, Due=%s, Returned=%s, Status=%s]",
                id,
                bookTitle != null ? bookTitle : "N/A", bookId,
                memberName != null ? memberName : "N/A", memberId,
                issueDate, dueDate,
                returnDate != null ? returnDate : "Not returned",
                status);
    }
}
