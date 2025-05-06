package service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;
import model.Participant;

public class NotificationService {

    private final String emailExpediteur = "nourchendhifaoui1@gmail.com"; // Votre email Gmail
    private final String motDePasse = "zhob lkrs tmqo vbkv"; // Votre mot de passe d'application

    public void envoyerNotificationAnnulation(int idFormation, String titreFormation, List<Participant> participants) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.debug", "true"); // Activer le mode débogage pour diagnostiquer les erreurs

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailExpediteur, motDePasse);
            }
        });

        for (Participant participant : participants) {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailExpediteur));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(participant.getEmail()));
                message.setSubject("Annulation de la formation : " + titreFormation);
                message.setText("Bonjour " + participant.getName() + ",\n\n" +
                        "Nous vous informons que la formation \"" + titreFormation + "\" a été annulée.\n" +
                        "Nous nous excusons pour tout inconvénient causé.\n\n" +
                        "Cordialement,\nL'équipe de formation");

                Transport.send(message);
                System.out.println("✅ Notification envoyée à " + participant.getEmail());
            } catch (MessagingException e) {
                System.err.println("❌ Erreur lors de l'envoi de la notification à " + participant.getEmail() + " : " + e.getMessage());
                e.printStackTrace(); // Afficher la trace complète pour le débogage
            }
        }
    }
}