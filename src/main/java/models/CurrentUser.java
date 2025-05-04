package models;

public class CurrentUser {
    private static User currentUser;
    private static String currentRole;

    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null && !user.getRoles().isEmpty()) {
            currentRole = user.getRoles().get(0);
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public static void clear() {
        currentUser = null;
        currentRole = null;
    }
} 