package gui;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Reclamation;

import java.util.Map;
import java.util.stream.Collectors;

public class StatsBarDialog {
    public static void show(ObservableList<Reclamation> reclamations) {
        Stage dialog = new Stage();
        dialog.setTitle("Statistiques des réclamations");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Préparer les axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Type de réclamation");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre");

        // Préparer le BarChart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Statistiques par type de réclamation");
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-background-color: #fff3e0; -fx-border-radius: 12; -fx-background-radius: 12;");

        // Préparer les données
        Map<String, Long> stats = reclamations.stream()
                .collect(Collectors.groupingBy(Reclamation::getTypeName, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }
        barChart.getData().add(series);

        // Styliser les barres après affichage
        barChart.setAnimated(false);
        barChart.applyCss();
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #e1701a; -fx-background-radius: 8;");
        }

        BorderPane root = new BorderPane(barChart);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #fff3e0, #ffe0bd);");
        Label title = new Label("📊 Statistiques des réclamations par type");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4d3428; -fx-padding: 0 0 15 0;");
        root.setTop(title);
        BorderPane.setMargin(title, new Insets(0,0,10,0));

        Scene scene = new Scene(root, 550, 400, Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
