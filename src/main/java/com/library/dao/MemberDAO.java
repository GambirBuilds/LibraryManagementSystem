package com.library.dao;

import com.library.exception.LibraryException;
import com.library.model.Member;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Member entities - full CRUD.
 */
public class MemberDAO {

    public void addMember(Member member) throws LibraryException {
        String sql = "INSERT INTO members (name, email, phone, join_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getJoinDate().toString());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Failed to register member.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    member.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new LibraryException("A member with email '" + member.getEmail() + "' already exists.", e);
            }
            throw new LibraryException("Database error while adding member: " + e.getMessage(), e);
        }
    }

    public Member findById(int id) throws LibraryException {
        String sql = "SELECT * FROM members WHERE id = ?";
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
            throw new LibraryException("Database error while finding member: " + e.getMessage(), e);
        }
    }

    public List<Member> findByName(String name) throws LibraryException {
        String sql = "SELECT * FROM members WHERE name LIKE ? ORDER BY name";
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while searching members: " + e.getMessage(), e);
        }
        return members;
    }

    public List<Member> findAll() throws LibraryException {
        String sql = "SELECT * FROM members ORDER BY name";
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while listing members: " + e.getMessage(), e);
        }
        return members;
    }

    public void updateMember(Member member) throws LibraryException {
        String sql = "UPDATE members SET name=?, email=?, phone=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setInt(4, member.getId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Member with ID " + member.getId() + " not found.");
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new LibraryException("A member with email '" + member.getEmail() + "' already exists.", e);
            }
            throw new LibraryException("Database error while updating member: " + e.getMessage(), e);
        }
    }

    public void deleteMember(int id) throws LibraryException {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new LibraryException("Member with ID " + id + " not found.");
            }
        } catch (SQLException e) {
            throw new LibraryException("Database error while deleting member: " + e.getMessage(), e);
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                LocalDate.parse(rs.getString("join_date"))
        );
    }
}
