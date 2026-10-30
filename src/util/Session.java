/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

/**
 *
 * @author Maksims Pļehanovs, Vadims Lavrenovs
 */
public class Session {
    private static int userId;
    private static String userRole;
    private static boolean loggedIn = false;

    public static void startSession(int id, String role) {
        userId = id;
        userRole = role;
        loggedIn = true;
    }

    public static int getUserId() {
        return userId;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }
    
    public static String getRole() {
        return userRole;
    }
    
    public static void endSession() {
        userId = 0;
        userRole = null;
        loggedIn = false;
    }
}
