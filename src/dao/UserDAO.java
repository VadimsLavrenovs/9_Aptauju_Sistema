/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import model.User;

/**
 *
 * @author Maksims Plehanovs, Vadims Lavrenovs
 */
public class UserDAO {

    private Connection connection;

    public boolean saveUser(User user) {
        return false;
    }

    public User getUserByUsername(String username) {
        return null;
    }

    public boolean updatePassword(int userId, String newPassword) {
        return false;
    }
}
