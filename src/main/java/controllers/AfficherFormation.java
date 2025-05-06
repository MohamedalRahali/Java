package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.Formation;
import service.FormationService;
import service.IService;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;
import java.awt.Desktop;
import java.net.URISyntaxException;
import java.net.URI;
import javafx.event.ActionEvent;
import javafx.scene.Parent;

public class AfficherFormation {

    @FXML
    private ListView<Formation> listView;

    @FXML
    private TextField rechercheTitre;

    private final IService<Formation> formationService = new FormationService();
    private final String STRIPE_SECRET_KEY = "sk_test_51QxzTC06sEncOfCZUazHjq3672gRylLg5hgp0c2ir3TnLUH6yw2zkgabEaAKrNWH0mC8rStbRxTI9QHRdIHJoTce003rYRFXg5";
    private FilteredList<Formation> filteredFormations;

    @FXML
    private void retourAjoutFormation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AjouterFormation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une formation");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout : " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        try {
            // Charger toutes les formations
            List<Formation> formations = formationService.display();
            ObservableList<Formation> observableList = FXCollections.observableArrayList(formations);
            filteredFormations = new FilteredList<>(observableList, p -> true);
            listView.setItems(filteredFormations);

            // Configurer la cellule personnalisée
            listView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Formation> call(ListView<Formation> param) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(Formation f, boolean empty) {
                            super.updateItem(f, empty);
                            if (empty || f == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                Text text = new Text(
                                        "Titre: " + f.getTitre() +
                                                " | Description: " + f.getDescription() +
                                                " | Prix: " + f.getPrix() + " €" +
                                                " | Places: " + f.getPlacesDisponibles()
                                );

                                Button btnSupprimer = new Button("Supprimer");
                                btnSupprimer.setOnAction(e -> {
                                    try {
                                        formationService.delete(f.getId());
                                        observableList.remove(f);
                                    } catch (Exception ex) {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression : " + ex.getMessage());
                                    }
                                });

                                Button btnModifier = new Button("Modifier");
                                btnModifier.setOnAction(e -> openModifierFormation(f, observableList));

                                Button btnPayer = new Button("Payer");
                                btnPayer.setOnAction(e -> {
                                    try {
                                        payerFormation(f);
                                    } catch (StripeException stripeException) {
                                        showAlert(Alert.AlertType.ERROR, "Erreur Stripe", "Erreur lors du paiement : " + stripeException.getMessage());
                                    }
                                });

                                Button btnSinscrire = new Button("S'inscrire");
                                btnSinscrire.setOnAction(e -> {
                                    try {
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterParticipant.fxml"));
                                        Parent root = loader.load();
                                        AjouterParticipant controller = loader.getController();
                                        controller.setFormationId(f.getId());
                                        Stage stage = new Stage();
                                        stage.setScene(new Scene(root));
                                        stage.setTitle("S'inscrire à la Formation");
                                        stage.show();
                                    } catch (IOException ex) {
                                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'inscription : " + ex.getMessage());
                                    }
                                });

                                HBox box = new HBox(10, text, btnModifier, btnSupprimer, btnPayer, btnSinscrire);
                                box.setPadding(new Insets(5));
                                setGraphic(box);
                            }
                        }
                    };
                }
            });

            // Configurer le filtre de recherche par titre
            configurerFiltreRecherche();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors du chargement des formations : " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur inattendue", "Une erreur s'est produite : " + e.getMessage());
        }
    }

    private void configurerFiltreRecherche() {
        rechercheTitre.textProperty().addListener((obs, oldValue, newValue) -> mettreAJourFiltre());
    }

    private void mettreAJourFiltre() {
        filteredFormations.setPredicate(formation -> {
            String titreSaisi = rechercheTitre.getText().trim().toLowerCase();
            return titreSaisi.isEmpty() || formation.getTitre().toLowerCase().contains(titreSaisi);
        });
    }

    private void openModifierFormation(Formation formation, ObservableList<Formation> formationsList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ModifierFormation.fxml"));
            AnchorPane pane = loader.load();

            ModifierFormation controller = loader.getController();
            controller.setFormation(formation, formationsList);

            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setTitle("Modifier Formation");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification : " + e.getMessage());
        }
    }

    private void payerFormation(Formation formation) throws StripeException {
        Stripe.apiKey = STRIPE_SECRET_KEY;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://example.com/success")
                .setCancelUrl("https://example.com/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount((long) (formation.getPrix() * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(formation.getTitre())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        String checkoutUrl = session.getUrl();
        openInBrowser(checkoutUrl);
    }

    private void openInBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            showError("Erreur navigateur : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String titre, String contenu) {
        Alert alerte = new Alert(type);
        alerte.setTitle(titre);
        alerte.setHeaderText(null);
        alerte.setContentText(contenu);
        alerte.showAndWait();
    }

    @FXML
    private void exportToPDF(ActionEvent event) {
        try {
            String dest = "formations_list.pdf";
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont("Times-Roman");

            document.add(new Paragraph("Liste des formations")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold()
                    .setMarginBottom(20));

            float[] columnWidths = {50, 100, 150, 50, 50};
            Table table = new Table(columnWidths);
            table.setWidth(400);

            table.addHeaderCell(new Cell().add(new Paragraph("ID").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Titre").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Prix (€)").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph("Places").setFont(font)));

            // Utiliser filteredFormations pour exporter uniquement les formations affichées
            for (Formation f : filteredFormations) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(f.getId())).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(f.getTitre()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(f.getDescription()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", f.getPrix())).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(f.getPlacesDisponibles())).setFont(font)));
            }

            document.add(table);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le fichier PDF a été créé avec succès : " + dest);
            Desktop.getDesktop().open(new java.io.File(dest));

        } catch (IOException e) {
            showError("Erreur lors de la création du PDF : " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur inattendue : " + e.getMessage());
        }
    }
}