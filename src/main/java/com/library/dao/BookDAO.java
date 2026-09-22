package com.library.dao;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Book entities - full CRUD using PreparedStatement.
 */
public class BookDAO {

    public void addBook(Book book) throws LibraryException {
        String sql = "INSERT INTO books (title, author, isbn, category, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Failed to add book.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new LibraryException("A book with ISBN '" + book.getIsbn() + "' already exists.", e);
            }
            throw new LibraryException("Database error while adding book: " + e.getMessage(), e);
        }
    }

    public Book findById(int id) throws LibraryException {
        String sql = "SELECT * FROM books WHERE id = ?";
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
            throw new LibraryException("Database error while finding book: " + e.getMessage(), e);
        }
    }

    public Book findByIsbn(String isbn) throws LibraryException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while finding book by ISBN: " + e.getMessage(), e);
        }
    }

    public List<Book> findByTitleOrAuthor(String keyword) throws LibraryException {
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while searching books: " + e.getMessage(), e);
        }
        return books;
    }

    public List<Book> findAll() throws LibraryException {
        String sql = "SELECT * FROM books ORDER BY title";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while listing books: " + e.getMessage(), e);
        }
        return books;
    }

    public void updateBook(Book book) throws LibraryException {
        String sql = "UPDATE books SET title=?, author=?, isbn=?, category=?, total_copies=?, available_copies=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.setInt(7, book.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Book with ID " + book.getId() + " not found.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new LibraryException("A book with ISBN '" + book.getIsbn() + "' already exists.", e);
            }
            throw new LibraryException("Database error while updating book: " + e.getMessage(), e);
        }
    }

    public void deleteBook(int id) throws LibraryException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Book with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while deleting book: " + e.getMessage(), e);
        }
    }

    public void updateAvailableCopies(int bookId, int delta) throws LibraryException {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE id = ? AND available_copies + ? >= 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setInt(2, bookId);
            ps.setInt(3, delta);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Cannot update available copies for book ID " + bookId +
                        " (book not found or would result in negative stock).");
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while updating available copies: " + e.getMessage(), e);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"),
                rs.getString("category"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }
}
