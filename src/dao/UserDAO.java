/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

/**
 *
 * @author Maksims Plehanovs, Vadims Lavrenovs
 */
public class UserDAO {
// Pārbauda lietotāja ievades datus

    private boolean isValidUser(User user) {
        if (user == null) {
            return false;
        }

        String username = user.getUsername();
        String password = user.getPassword();
        String role = user.getRole();

        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (username.trim().length() > 50) {
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        if (password.length() > 15) {
            return false;
        }

        if (role == null) {
            return false;
        }

        return role.equals("Interviewer")
                || role.equals("Respondent")
                || role.equals("Admin");
    }

    // Saglabā jaunu lietotāju
    public boolean saveUser(User user) throws SQLException {

        if (!isValidUser(user)) {
            return false;
        }

        String sql = "INSERT INTO APP_USER "
                + "(username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());

            return ps.executeUpdate() > 0;
        }
    }

    // Atrod lietotāju pēc lietotājvārda
    public User getUserByUsername(String username)
            throws SQLException {

        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT userid, username, password, role "
                + "FROM APP_USER WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();

                    user.setUserId(rs.getInt("userid"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));

                    return user;
                }
            }
        }

        return null;
    }

    // Maina lietotāja paroli
    public boolean updatePassword(int userId, String newPassword)
            throws SQLException {

        if (userId <= 0
                || newPassword == null
                || newPassword.trim().isEmpty()
                || newPassword.length() > 15) {
            return false;
        }

        String sql = "UPDATE APP_USER SET password = ? "
                + "WHERE userid = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // Iegūst visus lietotājus
    public List<User> getAllUsers() throws SQLException {

        List<User> users = new ArrayList<>();

        String sql = "SELECT userid, username, role "
                + "FROM APP_USER ORDER BY userid";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();

                user.setUserId(rs.getInt("userid"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));

                users.add(user);
            }
        }

        return users;
    }
}
