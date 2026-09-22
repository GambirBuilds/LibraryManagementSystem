package com.library.dao;

import com.library.exception.LibraryException;
import com.library.model.Borrowing;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Borrowing transactions.
 */
public class BorrowingDAO {

    public void addBorrowing(Borrowing borrowing) throws LibraryException {
        String sql = "INSERT INTO borrowings (book_id, member_id, issue_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, borrowing.getBookId());
            ps.setInt(2, borrowing.getMemberId());
            ps.setString(3, borrowing.getIssueDate().toString());
            ps.setString(4, borrowing.getDueDate().toString());
            ps.setString(5, borrowing.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Failed to create borrowing record.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    borrowing.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while creating borrowing: " + e.getMessage(), e);
        }
    }

    public Borrowing findById(int id) throws LibraryException {
        String sql = """
            SELECT b.*, bk.title AS book_title, m.name AS member_name
            FROM borrowings b
            JOIN books bk ON b.book_id = bk.id
            JOIN members m ON b.member_id = m.id
            WHERE b.id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while finding borrowing: " + e.getMessage(), e);
        }
    }

    public List<Borrowing> findActiveByMember(int memberId) throws LibraryException {
        String sql = """
            SELECT b.*, bk.title AS book_title, m.name AS member_name
            FROM borrowings b
            JOIN books bk ON b.book_id = bk.id
            JOIN members m ON b.member_id = m.id
            WHERE b.member_id = ? AND b.status = 'BORROWED'
            ORDER BY b.due_date
            """;
        return queryList(sql, memberId);
    }

    public List<Borrowing> findAllActive() throws LibraryException {
        String sql = """
            SELECT b.*, bk.title AS book_title, m.name AS member_name
            FROM borrowings b
            JOIN books bk ON b.book_id = bk.id
            JOIN members m ON b.member_id = m.id
            WHERE b.status = 'BORROWED'
            ORDER BY b.due_date
            """;
        return queryList(sql);
    }

    public List<Borrowing> findOverdue() throws LibraryException {
        String sql = """
            SELECT b.*, bk.title AS book_title, m.name AS member_name
            FROM borrowings b
            JOIN books bk ON b.book_id = bk.id
            JOIN members m ON b.member_id = m.id
            WHERE b.status = 'BORROWED' AND b.due_date < date('now')
            ORDER BY b.due_date
            """;
        return queryList(sql);
    }

    public List<Borrowing> findHistoryByMember(int memberId) throws LibraryException {
        String sql = """
            SELECT b.*, bk.title AS book_title, m.name AS member_name
            FROM borrowings b
            JOIN books bk ON b.book_id = bk.id
            JOIN members m ON b.member_id = m.id
            WHERE b.member_id = ?
            ORDER BY b.issue_date DESC
            """;
        return queryList(sql, memberId);
    }

    public void markReturned(int borrowingId, LocalDate returnDate) throws LibraryException {
        String sql = "UPDATE borrowings SET return_date = ?, status = 'RETURNED' WHERE id = ? AND status = 'BORROWED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, returnDate.toString());
            ps.setInt(2, borrowingId);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Borrowing ID " + borrowingId + " not found or already returned.");
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while returning book: " + e.getMessage(), e);
        }
    }

    private List<Borrowing> queryList(String sql, Object... params) throws LibraryException {
        List<Borrowing> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while querying borrowings: " + e.getMessage(), e);
        }
        return list;
    }

    private Borrowing mapRow(ResultSet rs) throws SQLException {
        Borrowing b = new Borrowing(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                LocalDate.parse(rs.getString("issue_date")),
                LocalDate.parse(rs.getString("due_date")),
                rs.getString("return_date") != null ? LocalDate.parse(rs.getString("return_date")) : null,
                Borrowing.Status.valueOf(rs.getString("status"))
        );
        b.setBookTitle(rs.getString("book_title"));
        b.setMemberName(rs.getString("member_name"));
        return b;
    }
}
