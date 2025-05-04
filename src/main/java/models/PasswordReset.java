package models;

import java.io.*;
import java.util.Properties;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class PasswordReset {
    private static final String DATA_FILE = "password_reset.properties";
    private static final int CODE_EXPIRY_MINUTES = 15;

    public static void storeResetCode(String email, String code) {
        Properties props = new Properties();
        File file = new File(DATA_FILE);
        
        // Load existing properties
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading password reset data: " + e.getMessage());
            }
        }
        
        // Store new code with timestamp
        props.setProperty(email + ".code", code);
        props.setProperty(email + ".timestamp", LocalDateTime.now().toString());
        
        // Save properties
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Password Reset Data");
        } catch (IOException e) {
            System.err.println("Error saving password reset data: " + e.getMessage());
        }
    }

    public static boolean verifyResetCode(String email, String code) {
        Properties props = new Properties();
        File file = new File(DATA_FILE);
        
        if (!file.exists()) {
            return false;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            
            String storedCode = props.getProperty(email + ".code");
            String timestampStr = props.getProperty(email + ".timestamp");
            
            if (storedCode == null || timestampStr == null) {
                return false;
            }
            
            // Check if code matches
            if (!storedCode.equals(code)) {
                return false;
            }
            
            // Check if code has expired
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr);
            LocalDateTime now = LocalDateTime.now();
            long minutesSinceCreation = ChronoUnit.MINUTES.between(timestamp, now);
            
            return minutesSinceCreation <= CODE_EXPIRY_MINUTES;
        } catch (IOException e) {
            System.err.println("Error verifying reset code: " + e.getMessage());
            return false;
        }
    }

    public static void clearResetCode(String email) {
        Properties props = new Properties();
        File file = new File(DATA_FILE);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading password reset data: " + e.getMessage());
                return;
            }
        }
        
        // Remove the code and timestamp
        props.remove(email + ".code");
        props.remove(email + ".timestamp");
        
        // Save the updated properties
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Password Reset Data");
        } catch (IOException e) {
            System.err.println("Error saving password reset data: " + e.getMessage());
        }
    }
} 