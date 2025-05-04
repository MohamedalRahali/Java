package gui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import models.Reclamation;

public class CustomReclamationCell extends ListCell<Reclamation> {
    private final HBox content;
    private final VBox vbox;
    private final Text title;
    private final Text description;
    private final Button editButton;
    private final Button deleteButton;

    public CustomReclamationCell(ReclamationActionHandler handler) {
        super();
        title = new Text();
        description = new Text();
        editButton = new Button("Modifier");
        deleteButton = new Button("Supprimer");
        vbox = new VBox(title, description);
        vbox.setSpacing(5);
        content = new HBox(vbox, editButton, deleteButton);
        content.setSpacing(20);
        content.setPadding(new Insets(10, 10, 10, 10));
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 10;");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10;");

        editButton.setOnAction(e -> {
            if (getItem() != null) handler.onEdit(getItem());
        });
        deleteButton.setOnAction(e -> {
            if (getItem() != null) handler.onDelete(getItem());
        });
    }

    @Override
    protected void updateItem(Reclamation item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            setGraphic(content);
        }
    }

    public interface ReclamationActionHandler {
        void onEdit(Reclamation reclamation);
        void onDelete(Reclamation reclamation);
    }
}
