module com.wings.pi_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.wings.pi_java to javafx.fxml;
    exports com.wings.pi_java;
}