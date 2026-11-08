/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.User;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Maksims Pļehanovs, Vadims Lavrenovs
 */
public class UserDAOTest {

    private UserDAO dao = new UserDAO();

    // Izveido lietotāju ar norādītajiem datiem
    private User createUser(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    @Test
    public void testSaveUserRejectsNullUser() throws Exception {
        assertFalse(dao.saveUser(null));
    }

    @Test
    public void testSaveUserRejectsEmptyUsername() throws Exception {
        User user = createUser("   ", "Parole123", "Respondent");
        assertFalse(dao.saveUser(user));
    }

    @Test
    public void testSaveUserRejectsUsernameLongerThan50Characters()
            throws Exception {

        String username = "A".repeat(51);
        User user = createUser(username, "Parole123", "Respondent");

        assertFalse(dao.saveUser(user));
    }

    @Test
    public void testSaveUserRejectsEmptyPassword() throws Exception {
        User user = createUser("TestUser", "   ", "Respondent");
        assertFalse(dao.saveUser(user));
    }

    @Test
    public void testSaveUserRejectsPasswordLongerThan15Characters()
            throws Exception {

        String password = "A".repeat(16);
        User user = createUser("TestUser", password, "Respondent");

        assertFalse(dao.saveUser(user));
    }

    @Test
    public void testSaveUserRejectsInvalidRole() throws Exception {
        User user = createUser("TestUser", "Parole123", "Guest");
        assertFalse(dao.saveUser(user));
    }

    @Test
    public void testUpdatePasswordRejectsInvalidUserId()
            throws Exception {

        assertFalse(dao.updatePassword(0, "Jauna123"));
    }

    @Test
    public void testUpdatePasswordRejectsEmptyPassword()
            throws Exception {

        assertFalse(dao.updatePassword(1, "   "));
    }

    @Test
    public void testUpdatePasswordRejectsPasswordLongerThan15Characters()
            throws Exception {

        String password = "A".repeat(16);

        assertFalse(dao.updatePassword(1, password));
    }

    @Test
    public void testGetUserByUsernameRejectsEmptyUsername()
            throws Exception {

        assertNull(dao.getUserByUsername("   "));
    }
}
