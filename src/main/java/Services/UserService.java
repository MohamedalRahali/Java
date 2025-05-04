package Services;

import models.User;
import util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection conn;

    public UserService() {
        conn = MyConnection.getInstance().getConnection();
    }

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setLastname(rs.getString("lastname"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                // Parse JSON roles back to List
                String rolesJson = rs.getString("roles");
                // Remove brackets and quotes, then split
                String rolesStr = rolesJson.replaceAll("[\\[\\]\"]", "");
                u.setRoles(List.of(rolesStr.split(",")));
                u.setDateOfBirth(rs.getDate("date_of_birth"));
                u.setIsBlocked(rs.getBoolean("is_blocked"));
                users.add(u);
            }
        } catch (SQLException ex) {
            System.err.println("Error retrieving users: " + ex.getMessage());
            ex.printStackTrace();
        }

        return users;
    }

    public void ajouter(User u) {
        String query = "INSERT INTO user (name, lastname, email, password, roles, date_of_birth, is_blocked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            // Validate roles before inserting
            if (u.getRoles().isEmpty()) {
                throw new SQLException("Roles cannot be empty");
            }

            // Validate each role
            for (String role : u.getRoles()) {
                if (!role.matches("^(ADMIN|USER|ARTIST)$")) {
                    throw new SQLException("Invalid role: " + role + ". Valid roles are: ADMIN, USER, ARTIST");
                }
            }

            // Convert roles to JSON array format
            String rolesJson = "[\"" + String.join("\",\"", u.getRoles()) + "\"]";

            pstmt.setString(1, u.getName());
            pstmt.setString(2, u.getLastname());
            pstmt.setString(3, u.getEmail());
            pstmt.setString(4, u.getPassword());
            pstmt.setString(5, rolesJson);
            pstmt.setDate(6, u.getDateOfBirth());
            pstmt.setBoolean(7, u.getIsBlocked());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        u.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("User added successfully with ID: " + u.getId());
            }
        } catch (SQLException ex) {
            System.err.println("Error adding user: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to add user: " + ex.getMessage(), ex);
        }
    }

    public void modifier(User updated) {
        String query = "UPDATE user SET name = ?, lastname = ?, email = ?, password = ?, " +
                "roles = ?, date_of_birth = ?, is_blocked = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            // Validate roles before updating
            if (updated.getRoles().isEmpty()) {
                throw new SQLException("Roles cannot be empty");
            }

            // Validate each role
            for (String role : updated.getRoles()) {
                if (!role.matches("^(ADMIN|USER|ARTIST)$")) {
                    throw new SQLException("Invalid role: " + role + ". Valid roles are: ADMIN, USER, ARTIST");
                }
            }

            // Convert roles to JSON array format
            String rolesJson = "[\"" + String.join("\",\"", updated.getRoles()) + "\"]";

            pstmt.setString(1, updated.getName());
            pstmt.setString(2, updated.getLastname());
            pstmt.setString(3, updated.getEmail());
            pstmt.setString(4, updated.getPassword());
            pstmt.setString(5, rolesJson);
            pstmt.setDate(6, updated.getDateOfBirth());
            pstmt.setBoolean(7, updated.getIsBlocked());
            pstmt.setInt(8, updated.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User updated successfully: ID " + updated.getId());
            } else {
                System.out.println("No user found with ID " + updated.getId());
            }
        } catch (SQLException ex) {
            System.err.println("Error updating user: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to update user: " + ex.getMessage(), ex);
        }
    }

    public void supprimer(int id) {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User deleted successfully: ID " + id);
            } else {
                System.out.println("No user found with ID " + id);
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting user: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to delete user: " + ex.getMessage(), ex);
        }
    }

    public User authenticate(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setLastname(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                String rolesJson = rs.getString("roles");
                String rolesStr = rolesJson.replaceAll("[\\[\\]\"]", "");
                user.setRoles(List.of(rolesStr.split(",")));
                user.setDateOfBirth(rs.getDate("date_of_birth"));
                user.setIsBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
        } catch (SQLException ex) {
            System.err.println("Error authenticating user: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Error checking email existence: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    public User getUserByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setLastname(rs.getString("lastname"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                String rolesJson = rs.getString("roles");
                String rolesStr = rolesJson.replaceAll("[\\[\\]\"]", "");
                user.setRoles(List.of(rolesStr.split(",")));
                user.setDateOfBirth(rs.getDate("date_of_birth"));
                user.setIsBlocked(rs.getBoolean("is_blocked"));
                return user;
            }
        } catch (SQLException ex) {
            System.err.println("Error retrieving user by email: " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}