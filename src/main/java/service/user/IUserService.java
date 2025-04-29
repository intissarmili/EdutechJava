package service.user;

import models.User;
import java.util.List;

public interface IUserService {
    boolean login(String email, String password);
    User getUserByEmail(String email);
    boolean emailExists(String email);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    List<User> getAllUsers();

    // ✅ NEW Methods for Ban/Unban
    void banUser(int id);
    void unbanUser(int id);
}
