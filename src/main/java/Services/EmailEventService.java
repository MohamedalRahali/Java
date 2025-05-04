package Services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import io.github.cdimascio.dotenv.Dotenv;

public class EmailEventService {
    private static final Dotenv dotenv = Dotenv.load();
    private final String username = dotenv.get("GMAIL_USERNAME"); // Votre email Gmail
    private final String password = dotenv.get("GMAIL_APP_PASSWORD"); // Votre mot de passe d'application Gmail généré

    public void sendConfirmationEmail(String recipientEmail, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        // Set the email content as HTML
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
