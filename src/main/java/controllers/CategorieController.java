package esprit.tn.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import esprit.tn.entities.Categorie;
import esprit.tn.service.CategorieService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CategorieController {
    @FXML private ListView<Categorie> categorieListView;
    @FXML private TextField libelleField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView qrCodeImageView;
    @FXML private Button generateQRButton;
    @FXML private Button scanQRButton;
    @FXML private VBox notificationPane;

    private CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        setupListView();
        loadCategories();
    }

    private void setupListView() {
        categorieListView.setCellFactory(new Callback<ListView<Categorie>, ListCell<Categorie>>() {
            @Override
            public ListCell<Categorie> call(ListView<Categorie> param) {
                return new ListCell<Categorie>() {
                    @Override
                    protected void updateItem(Categorie categorie, boolean empty) {
                        super.updateItem(categorie, empty);
                        if (empty || categorie == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox vbox = new VBox(5);
                            vbox.getChildren().addAll(
                                    new Label("Libellé: " + categorie.getLibelle()),
                                    new Label("Description: " + categorie.getDescription())
                            );

                            HBox hbox = new HBox(10);
                            Button editBtn = new Button("Éditer");
                            editBtn.setOnAction(e -> handleEdit(categorie));

                            Button deleteBtn = new Button("Supprimer");
                            deleteBtn.setOnAction(e -> handleDelete(categorie));

                            Button qrBtn = new Button("QR Code");
                            qrBtn.setOnAction(e -> generateItemQRCode(categorie));

                            hbox.getChildren().addAll(editBtn, deleteBtn, qrBtn);
                            vbox.getChildren().add(hbox);

                            setGraphic(vbox);
                        }
                    }
                };
            }
        });
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.getAllCategories();
            categorieListView.getItems().setAll(categories);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCategorie() {
        if (libelleField.getText().isEmpty()) {
            showAlert("Erreur", "Le libellé est obligatoire");
            return;
        }

        Categorie categorie = new Categorie(libelleField.getText(), descriptionArea.getText());
        try {
            categorieService.ajouterCategorie(categorie);
            loadCategories();
            clearFields();
            showNotification("Catégorie ajoutée avec succès");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void handleEdit(Categorie categorie) {
        libelleField.setText(categorie.getLibelle());
        descriptionArea.setText(categorie.getDescription());

        Button updateBtn = new Button("Mettre à jour");
        updateBtn.setOnAction(e -> {
            categorie.setLibelle(libelleField.getText());
            categorie.setDescription(descriptionArea.getText());
            try {
                categorieService.updateCategorie(categorie);
                loadCategories();
                clearFields();
                showNotification("Catégorie mise à jour avec succès");
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur lors de la mise à jour: " + ex.getMessage());
            }
        });

        notificationPane.getChildren().clear();
        notificationPane.getChildren().add(updateBtn);
    }

    private void handleDelete(Categorie categorie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la catégorie ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + categorie.getLibelle() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categorieService.deleteCategorie(categorie.getId());
                    loadCategories();
                    showNotification("Catégorie supprimée avec succès");
                } catch (SQLException e) {
                    showAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void generateQRCode() {
        StringBuilder qrText = new StringBuilder("Liste des Catégories:\n");
        for (Categorie categorie : categorieListView.getItems()) {
            qrText.append(String.format("- %s: %s\n", categorie.getLibelle(), categorie.getDescription()));
        }

        generateQRImage(qrText.toString());
    }

    private void generateItemQRCode(Categorie categorie) {
        String qrText = String.format("Catégorie:\nLibellé: %s\nDescription: %s",
                categorie.getLibelle(), categorie.getDescription());
        generateQRImage(qrText);
    }

    private void generateQRImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            Image qrImage = new Image(new ByteArrayInputStream(pngData));
            qrCodeImageView.setImage(qrImage);
            qrCodeImageView.setVisible(true);

            showNotification("QR Code généré avec succès");
        } catch (WriterException | IOException e) {
            showAlert("Erreur", "Erreur lors de la génération du QR Code: " + e.getMessage());
        }
    }

    @FXML
    private void scanQRCode() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir un QR Code");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(qrCodeImageView.getScene().getWindow());
        if (file != null) {
            // En production, utiliser une vraie bibliothèque de décodage QR
            Image image = new Image(file.toURI().toString());
            qrCodeImageView.setImage(image);

            // Simulation de décodage
            String decodedText = "Contenu simulé du QR Code:\nLibellé: Exemple\nDescription: Ceci est un exemple";

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("QR Code scanné");
            alert.setHeaderText("Information du QR Code");
            alert.setContentText(decodedText);
            alert.showAndWait();
        }
    }

    @FXML
    private void clearFields() {
        libelleField.clear();
        descriptionArea.clear();
        notificationPane.getChildren().clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotification(String message) {
        Label notification = new Label(message);
        notificationPane.getChildren().add(notification);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() ->
                                notificationPane.getChildren().remove(notification));
                    }
                },
                3000
        );
    }

    public void setCategorie(Categorie categorie) {
    }
}