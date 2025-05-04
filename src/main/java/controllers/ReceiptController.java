package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import models.Event;
import utils.TranslationService;
import Services.EmailEventService;
import javax.mail.MessagingException;


public class ReceiptController {
    @FXML private Text eventTitleText;
    @FXML private Text eventDateText;
    @FXML private Text eventLocationText;
    @FXML private Text reservationNameText;
    @FXML private Text reservationEmailText;
    @FXML private Text reservationPlacesText;
    @FXML private Button backButton;
    @FXML private Button downloadPdfButton;
    @FXML private ImageView qrCodeImageView;

    private Event event;
    private String name;
    private String email;
    private int nbPlaces;
    private String currentLanguage = "fr";
    private TranslationService translationService;

    public void initialize() {
        translationService = new TranslationService();
        updateTranslations();
    }

    public void setData(Event event, String name, String email, int nbPlaces) {
        this.event = event;
        this.name = name;
        this.email = email;
        this.nbPlaces = nbPlaces;
        updateReceiptDetails();
        generateQRCode();
        sendConfirmationEmail();
    }

    public void setLanguage(String language) {
        this.currentLanguage = language;
        updateTranslations();
    }

    private void updateReceiptDetails() {
        if (event != null) {
            eventTitleText.setText("Titre : " + event.getTitle());
            eventDateText.setText("Date : " + event.getDate().toString());
            eventLocationText.setText("Lieu : " + event.getLieux());
            reservationNameText.setText("Nom : " + name);
            reservationEmailText.setText("Email : " + email);
            reservationPlacesText.setText("Nombre de places : " + nbPlaces);
        }
    }

    private void updateTranslations() {
        backButton.setText(translationService.translateText("Retour à la liste des événements", currentLanguage));
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/client_events.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateQRCode() {
        if (event != null) {
            try {
                // Créer le contenu du QR code
                String qrContent = String.format(
                    "Événement: %s\nDate: %s\nLieu: %s\nNom: %s\nEmail: %s\nPlaces: %d",
                    event.getTitle(),
                    event.getDate().toString(),
                    event.getLieux(),
                    name,
                    email,
                    nbPlaces
                );

                // Générer le QR code
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);

                // Convertir en image JavaFX
                WritableImage qrImage = new WritableImage(200, 200);
                PixelWriter pixelWriter = qrImage.getPixelWriter();
                for (int x = 0; x < 200; x++) {
                    for (int y = 0; y < 200; y++) {
                        Color color = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                        pixelWriter.setColor(x, y, color);
                    }
                }

                qrCodeImageView.setImage(qrImage);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDownloadPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le reçu en PDF");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File file = fileChooser.showSaveDialog(downloadPdfButton.getScene().getWindow());
        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Ajouter le contenu
                document.add(new Paragraph("CONFIRMATION DE RÉSERVATION\n\n"));
                document.add(new Paragraph("Détails de l'événement:"));
                document.add(new Paragraph(eventTitleText.getText()));
                document.add(new Paragraph(eventDateText.getText()));
                document.add(new Paragraph(eventLocationText.getText()));
                document.add(new Paragraph("\nDétails de la réservation:"));
                document.add(new Paragraph(reservationNameText.getText()));
                document.add(new Paragraph(reservationEmailText.getText()));
                document.add(new Paragraph(reservationPlacesText.getText()));

                document.close();
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendConfirmationEmail() {
        try {
            Services.EmailEventService emailService = new Services.EmailEventService();
            String subject = "Confirmation de réservation - " + event.getTitle();
            
            // Créer le contenu HTML de l'email
            String htmlContent = String.format("""
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #8B4513; color: white; padding: 20px; text-align: center; }
                        .content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; }
                        .footer { text-align: center; margin-top: 20px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Confirmation de Réservation</h1>
                        </div>
                        <div class="content">
                            <h2>Détails de l'événement</h2>
                            <p><strong>Événement:</strong> %s</p>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Lieu:</strong> %s</p>
                            
                            <h2>Détails de la réservation</h2>
                            <p><strong>Nom:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Nombre de places:</strong> %d</p>
                            
                            <p style="text-align: center; margin-top: 30px;">
                                ✓ Votre réservation est confirmée!
                            </p>
                        </div>
                        <div class="footer">
                            <p>Merci de votre confiance!</p>
                            <p>Wings Solutions</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                event.getTitle(),
                event.getDate().toString(),
                event.getLieux(),
                name,
                email,
                nbPlaces
            );

            emailService.sendConfirmationEmail(email, subject, htmlContent);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Afficher une alerte en cas d'erreur d'envoi
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'envoyer l'email de confirmation.");
            alert.showAndWait();
        }
    }

} 