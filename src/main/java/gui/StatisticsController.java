package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import models.Event;
import Services.EventService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Comparator;

public class StatisticsController implements Initializable {
    @FXML
    private Label totalEventsLabel;
    
    @FXML
    private Label averagePriceLabel;

    @FXML
    private Label popularTypeLabel;
    
    @FXML
    private ListView<TypeCount> eventsByTypeList;
    
    @FXML
    private ListView<MonthCount> eventsByMonthList;

    @FXML
    private PieChart eventsByTypeChart;

    @FXML
    private BarChart<String, Number> monthlyTrendsChart;

    private EventService eventService;

    public StatisticsController() {
        this.eventService = new EventService();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configure ListViews
        eventsByTypeList.setCellFactory(lv -> new ListCell<TypeCount>() {
            @Override
            protected void updateItem(TypeCount item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s: %d événements", item.getType(), item.getCount()));
                }
            }
        });

        eventsByMonthList.setCellFactory(lv -> new ListCell<MonthCount>() {
            @Override
            protected void updateItem(MonthCount item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s: %d événements", item.getMonth(), item.getCount()));
                }
            }
        });

        loadStatistics();
    }

    private void loadStatistics() {
        List<Event> allEvents = eventService.getAll();
        
        // Total events
        totalEventsLabel.setText(String.valueOf(allEvents.size()));
        
        // Average price (temporarily set to number of events since price is not available)
        averagePriceLabel.setText(String.format("%d DT", allEvents.size() * 50)); // Example price
        
        // Events by type
        Map<String, Long> eventsByType = allEvents.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getTypeEvent() != null ? event.getTypeEvent().getName() : "Sans type",
                        Collectors.counting()
                ));
        
        // Update type statistics
        ObservableList<TypeCount> typeStats = FXCollections.observableArrayList();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        eventsByType.forEach((type, count) -> {
            typeStats.add(new TypeCount(type, count.intValue()));
            pieChartData.add(new PieChart.Data(type, count));
        });
        
        // Find most popular type
        typeStats.stream()
                .max(Comparator.comparingInt(TypeCount::getCount))
                .ifPresent(mostPopular -> 
                    popularTypeLabel.setText(mostPopular.getType())
                );
        
        eventsByTypeList.setItems(typeStats);
        eventsByTypeChart.setData(pieChartData);
        
        // Events by month
        Map<String, Long> eventsByMonth = allEvents.stream()
                .collect(Collectors.groupingBy(
                        event -> {
                            LocalDate localDate = event.getDate().toLocalDate();
                            return localDate.getMonth().toString();
                        },
                        Collectors.counting()
                ));
        
        // Update month statistics
        ObservableList<MonthCount> monthStats = FXCollections.observableArrayList();
        XYChart.Series<String, Number> monthSeries = new XYChart.Series<>();
        monthSeries.setName("Événements par mois");
        
        eventsByMonth.forEach((month, count) -> {
            monthStats.add(new MonthCount(month, count.intValue()));
            monthSeries.getData().add(new XYChart.Data<>(month, count));
        });
        
        eventsByMonthList.setItems(monthStats);
        monthlyTrendsChart.getData().clear();
        monthlyTrendsChart.getData().add(monthSeries);
        
        // Apply animations
        eventsByTypeChart.setAnimated(true);
        monthlyTrendsChart.setAnimated(true);
    }

    @FXML
    private void goToCreateEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/create_event.fxml"));
            Parent root = loader.load();
            totalEventsLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToEventView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/event_view.fxml"));
            Parent root = loader.load();
            totalEventsLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToTypeEventManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/type_event.fxml"));
            Parent root = loader.load();
            totalEventsLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Helper classes for statistics
    public static class TypeCount {
        private String type;
        private int count;

        public TypeCount(String type, int count) {
            this.type = type;
            this.count = count;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class MonthCount {
        private String month;
        private int count;

        public MonthCount(String month, int count) {
            this.month = month;
            this.count = count;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }
}
