module com.example.genzictionary {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.genzictionary to javafx.fxml;
    exports com.example.genzictionary;
}
