package models;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LoginAttempt {
    private static final int MAX_ATTEMPTS = 3;
    private static final int COOLDOWN_MINUTES = 5;
    private static final String DATA_FILE = "login_attempts.properties";
    private static final Map<String, Integer> failedAttempts = new HashMap<>();
    private static final Map<String, LocalDateTime> cooldownStart = new HashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    static {
        loadData();
    }

    private static void loadData() {
        Properties props = new Properties();
        File file = new File(DATA_FILE);
        
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                
                for (String key : props.stringPropertyNames()) {
                    if (key.endsWith(".attempts")) {
                        String email = key.substring(0, key.length() - 9);
                        int attempts = Integer.parseInt(props.getProperty(key));
                        String timestampStr = props.getProperty(email + ".timestamp");
                        
                        if (timestampStr != null) {
                            failedAttempts.put(email, attempts);
                            cooldownStart.put(email, LocalDateTime.parse(timestampStr, formatter));
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading login attempt data: " + e.getMessage());
            }
        }
    }

    private static void saveData() {
        Properties props = new Properties();
        
        for (Map.Entry<String, Integer> entry : failedAttempts.entrySet()) {
            String email = entry.getKey();
            LocalDateTime timestamp = cooldownStart.get(email);
            props.setProperty(email + ".attempts", String.valueOf(entry.getValue()));
            
            // Only save timestamp if it exists
            if (timestamp != null) {
                props.setProperty(email + ".timestamp", timestamp.format(formatter));
            }
        }
        
        try (FileOutputStream fos = new FileOutputStream(DATA_FILE)) {
            props.store(fos, "Login Attempt Data");
        } catch (IOException e) {
            System.err.println("Error saving login attempt data: " + e.getMessage());
        }
    }

    public static void recordFailedAttempt(String email) {
        int attempts = failedAttempts.getOrDefault(email, 0) + 1;
        failedAttempts.put(email, attempts);
        
        if (attempts >= MAX_ATTEMPTS) {
            cooldownStart.put(email, LocalDateTime.now());
        }
        
        saveData();
    }

    public static boolean canAttemptLogin(String email) {
        if (!cooldownStart.containsKey(email)) {
            return true;
        }

        LocalDateTime cooldownTime = cooldownStart.get(email);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(cooldownTime.plusMinutes(COOLDOWN_MINUTES))) {
            clearAttempts(email);
            return true;
        }
        
        return false;
    }

    public static void clearAttempts(String email) {
        failedAttempts.remove(email);
        cooldownStart.remove(email);
        saveData();
    }

    public static void resetAttempts(String email) {
        clearAttempts(email);
    }

    public static int getRemainingAttempts(String email) {
        int attempts = failedAttempts.getOrDefault(email, 0);
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    public static long[] getRemainingCooldown(String email) {
        if (!cooldownStart.containsKey(email)) {
            return new long[]{0, 0};
        }

        LocalDateTime start = cooldownStart.get(email);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(COOLDOWN_MINUTES);
        
        if (now.isAfter(end)) {
            clearAttempts(email);
            return new long[]{0, 0};
        }
        
        long totalSeconds = java.time.Duration.between(now, end).getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        
        return new long[]{minutes, seconds};
    }
} 