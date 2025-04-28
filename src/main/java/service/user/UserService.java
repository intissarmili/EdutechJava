// Updated UserService.java
package services.user;

import models.User;
import utils.MaConnexion;
import utils.EmailUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService implements IUserService {

    private final Connection connection;

    public UserService() {
        connection = MaConnexion.getInstance().getConnection();
    }

    @Override
    public boolean login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                boolean banned = rs.getBoolean("banned");
                boolean approved = rs.getBoolean("is_approved");
                if (!approved) {
                    return false; // user not approved
                }
                if (banned) {
                    EmailUtil.sendEmail(
                            email,
                            "Tentative de connexion refusée",
                            "EduTech\n\nVotre compte est banni pour une durée indéterminée.\n\nPour toute information, veuillez contacter le support technique ou l'administration."
                    );
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur login: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRoles(rs.getString("roles"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setEmailVerified(rs.getBoolean("is_verified"));
                u.setResetToken(rs.getString("reset_token"));
                u.setBanned(rs.getBoolean("banned"));
                u.setApproved(rs.getBoolean("is_approved"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Erreur getUserByEmail: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Erreur emailExists: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addUser(User user) {
        String sql = "INSERT INTO users (email, password, roles, first_name, last_name, phone_number, is_verified, banned, is_approved) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRoles());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getPhoneNumber());
            statement.setBoolean(7, user.isEmailVerified());
            statement.setBoolean(8, user.isBanned());
            statement.setBoolean(9, user.isApproved());
            statement.executeUpdate();
            System.out.println("✅ Utilisateur ajouté !");
        } catch (SQLException e) {
            System.err.println("Erreur addUser: " + e.getMessage());
        }
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET email=?, password=?, roles=?, first_name=?, last_name=?, phone_number=?, is_verified=?, banned=?, is_approved=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getRoles());
            statement.setString(4, user.getFirstName());
            statement.setString(5, user.getLastName());
            statement.setString(6, user.getPhoneNumber());
            statement.setBoolean(7, user.isEmailVerified());
            statement.setBoolean(8, user.isBanned());
            statement.setBoolean(9, user.isApproved());
            statement.setInt(10, user.getId());
            statement.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour !");
        } catch (SQLException e) {
            System.err.println("Erreur updateUser: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("✅ Utilisateur supprimé !");
        } catch (SQLException e) {
            System.err.println("Erreur deleteUser: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPassword(rs.getString("password"));
                u.setRoles(rs.getString("roles"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                u.setPhoneNumber(rs.getString("phone_number"));
                u.setEmailVerified(rs.getBoolean("is_verified"));
                u.setBanned(rs.getBoolean("banned"));
                u.setApproved(rs.getBoolean("is_approved"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getAllUsers: " + e.getMessage());
        }
        return users;
    }

    public void banUser(int id) {
        String sql = "UPDATE users SET banned = 1 WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur banUser: " + e.getMessage());
        }
    }

    public void unbanUser(int id) {
        String sql = "UPDATE users SET banned = 0 WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur unbanUser: " + e.getMessage());
        }
    }
}
