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
    private static int currentUserId = -1; 

    public static void startSession(int userId) {
        currentUserId = userId;
    }

    public static void clearSession() {
        currentUserId = -1;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static boolean isLoggedIn() {
        return currentUserId != -1;
    }
}
