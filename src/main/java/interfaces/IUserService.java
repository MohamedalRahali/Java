package esprit.tn.interfaces;

import esprit.tn.entities.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface IUserService {
    User ajouterUser(User user) throws SQLException;

    void deleteUser(Long idUser) throws SQLException;

    void updateUser(User user) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    User getUserById(Long idUser) throws SQLException;

    User getUserById(int idUser) throws SQLException;

    User getUserByUsername(String username) throws SQLException;

    User getUserByEmail(String email) throws SQLException;

    List<User> searchUsers(String keyword) throws SQLException;



    void updateUserPassword(Long userId, String newPassword) throws SQLException;

    Map<String, Integer> getUsersCreatedPerDay() throws SQLException;



    User authenticate(String username, String password);
}