package Services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class EmailService {
    private static final String CREDENTIALS_FILE = "email_config.properties";
    private static String fromEmail;
    private static String emailPassword;
    
    static {
        loadCredentials();
    }
    
    private static void loadCredentials() {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream(CREDENTIALS_FILE);
            props.load(in);
            in.close();
            fromEmail = props.getProperty("email", "").trim();
            emailPassword = props.getProperty("password", "").trim();
        } catch (IOException e) {
            System.err.println("Warning: Could not load email credentials. Please set them up.");
        }
    }
    
    // Call this after updating credentials to force reload
    public static void reloadCredentials() {
        loadCredentials();
    }
    
    public static void setupCredentials(String email, String password) throws IOException {
        Properties props = new Properties();
        props.setProperty("email", email.trim());
        props.setProperty("password", password.trim());
        
        FileOutputStream out = new FileOutputStream(CREDENTIALS_FILE);
        props.store(out, "Email Configuration");
        out.close();
        
        fromEmail = email.trim();
        emailPassword = password.trim();
    }
    
    public static void sendResetCode(String toEmail, String resetCode) throws MessagingException {
        if (fromEmail == null || fromEmail.isEmpty() || emailPassword == null || emailPassword.isEmpty()) {
            throw new MessagingException("Email credentials not set up. Please call setupCredentials first.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Password Reset Code");
        
        String emailContent = "Hello,\n\n" +
            "You have requested to reset your password. Here is your reset code:\n\n" +
            resetCode + "\n\n" +
            "This code will expire in 15 minutes.\n\n" +
            "If you did not request this reset, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Your Application Team";
        
        message.setText(emailContent);
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            throw new MessagingException("Failed to send email: " + e.getMessage(), e);
        }
    }
} 