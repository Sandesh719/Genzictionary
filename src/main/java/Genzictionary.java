/*package com.example.genzictionary;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class Genzictionary extends Application {

    private TextField searchField;
    private TextArea resultArea;
    private Button addToFavButton, clearButton;
    private ImageView imageView;
    private Image defaultImage;
    private String currentUser = null;

    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage primaryStage) {
        showLoginPage(primaryStage);
    }

    private void showLoginPage(Stage primaryStage) {
        primaryStage.setTitle("Genzictionary - Login/Signup");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Button signupButton = new Button("Sign Up");

        VBox loginBox = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signupButton);
        loginBox.setPadding(new Insets(20));
        Scene loginScene = new Scene(loginBox, 300, 250);

        primaryStage.setScene(loginScene);
        primaryStage.show();


        loginButton.setOnAction(e -> loginUser(primaryStage, usernameField.getText(), passwordField.getText()));


        signupButton.setOnAction(e -> signupUser(primaryStage, usernameField.getText(), passwordField.getText()));
    }

    private void signupUser(Stage primaryStage, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username and password cannot be empty.");
            return;
        }

        try (Connection userConn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/genz", "root", "yash")) {

            String checker = "SELECT name FROM username WHERE name = ?";
            PreparedStatement userStmt1 = userConn1.prepareStatement(checker);
            userStmt1.setString(1, username);
            ResultSet rs = userStmt1.executeQuery();

            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Username already exists", "Please choose another username.");
                return;
            }

            String insertUser = "INSERT INTO username (name) VALUES (?)";
            PreparedStatement userStmt = userConn1.prepareStatement(insertUser);
            userStmt.setString(1, username);
            userStmt.executeUpdate();

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "yash")) {
                String createDbQuery = "CREATE DATABASE IF NOT EXISTS " + username;
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(createDbQuery);


                try (Connection userConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + username, "root", "yash")) {
                    String createTableQuery = "CREATE TABLE IF NOT EXISTS favorites (word VARCHAR(255) unique)";
                    Statement userStmt2 = userConn.createStatement();
                    userStmt2.executeUpdate(createTableQuery);
                }
            }

            currentUser = username;
            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "User " + username + " has been created.");
            showMainPage(primaryStage);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error creating user database.");
        }
    }



    private void loginUser(Stage primaryStage, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username and password cannot be empty.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "yash")) {

            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet rs = dbMeta.getCatalogs();
            boolean userExists = false;

            while (rs.next()) {
                if (rs.getString(1).equals(username)) {
                    userExists = true;
                    break;
                }
            }

            if (userExists) {
                currentUser = username;
                showMainPage(primaryStage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Error", "User does not exist. Please sign up.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error during login.");
        }
    }


    private void showMainPage(Stage primaryStage) {
        primaryStage.setTitle("Genzictionary - Search GenZ Slang");


        MenuBar menuBar = new MenuBar();
        Menu loginMenu = new Menu("Account");
        Menu favMenu = new Menu("Favourites");
        Menu recMenu = new Menu("Recommendations");

        MenuItem logoutItem = new MenuItem("Logout");
        loginMenu.getItems().add(logoutItem);

        MenuItem showFavItem = new MenuItem("Show Favourites");
        favMenu.getItems().add(showFavItem);

        MenuItem genzItem = new MenuItem("GenZ Slangs");
        MenuItem englishItem = new MenuItem("English Words");
        recMenu.getItems().addAll(genzItem, englishItem);

        menuBar.getMenus().addAll(loginMenu, recMenu, favMenu);


        searchField = new TextField();
        searchField.setPromptText("Enter GenZ word...");
        searchField.setPrefWidth(300);

        addToFavButton = new Button("Add to Favourites");
        clearButton = new Button("Clear");
        Button searchButton = new Button("Search");

        resultArea = new TextArea();
        resultArea.setPrefSize(400, 150);
        resultArea.setWrapText(true);
        resultArea.setEditable(false);


        imageView = new ImageView();
        defaultImage = new Image("file:default_image.png");
        imageView.setImage(defaultImage);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);

        // Create a layout
        HBox searchBox = new HBox(10, searchField, searchButton);
        HBox buttonBox = new HBox(10, addToFavButton, clearButton);
        VBox vBox = new VBox(10, searchBox, buttonBox, resultArea, imageView);
        vBox.setPadding(new Insets(15));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(vBox);

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add functionality for buttons
        searchButton.setOnAction(e -> searchWord());
        addToFavButton.setOnAction(e -> addToFavorites());
        clearButton.setOnAction(e -> clearFields());


        showFavItem.setOnAction(e -> showFavoritesPage());


        logoutItem.setOnAction(e -> showLoginPage(primaryStage));
    }

    private void searchWord() {
        String searchWord = searchField.getText();
        resultArea.clear();

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/genz", "root", "yash");
            String query = "SELECT * FROM data WHERE slang LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + searchWord + "%");
            ResultSet rs = pstmt.executeQuery();

            String query1 = "SELECT * FROM data WHERE synonms LIKE ?";
            PreparedStatement pstmt1 = conn.prepareStatement(query1);
            pstmt1.setString(1, "%" + searchWord + "%");
            ResultSet rs1 = pstmt1.executeQuery();


            if (rs.next()) {
                String meaning = rs.getString("meaning");
                String example = rs.getString("example");
                String synonyms = rs.getString("synonms");
                resultArea.setText("Meaning: " + meaning + "\nExample: " + example + "\nSynonyms: " + synonyms);
            } else if (rs1.next()) {
                String meaning = rs1.getString("meaning");
                String example = rs1.getString("example");
                String slang = rs1.getString("slang");
                resultArea.setText("Meaning: " + meaning + "\nExample: " + example + "\nGenz Slang: " + slang);
            } else {
                resultArea.setText("Word not found.");
            }

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error while searching.");
        }
    }

    private void addToFavorites() {
        String word = searchField.getText();
        if (word.isEmpty()) {
            resultArea.setText("Please enter a word.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + currentUser, "root", "yash")) {
            String query = "INSERT INTO favorites (word) VALUES (?)";
            PreparedStatement p = con.prepareStatement(query);
            p.setString(1, word);
            int result = p.executeUpdate();

            if (result > 0) {
                resultArea.setText("Added to favorites!");
            } else {
                resultArea.setText("Error adding to favorites.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error while adding to personal favorites.");
        }



        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/favorites", "root", "yash")) {
            String query = "INSERT INTO words (word) VALUES (?)";
            PreparedStatement p = con.prepareStatement(query);
            p.setString(1, word);
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error while adding to common favorites.");
        }
    }

    private void clearFields() {
        searchField.clear();
        resultArea.clear();
        imageView.setImage(defaultImage);
    }

    private void showFavoritesPage() {
        Stage favoritesStage = new Stage();
        favoritesStage.setTitle("Favorites");

        TextArea favoritesArea = new TextArea();
        favoritesArea.setEditable(false);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + currentUser, "root", "yash")) {
            String query = "SELECT word FROM favorites";
            PreparedStatement p = con.prepareStatement(query);
            ResultSet rs = p.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("word")).append("\n");
            }
            favoritesArea.setText(sb.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
            favoritesArea.setText("Error loading personal favorites.");
        }

        Scene favoritesScene = new Scene(favoritesArea, 400, 300);
        favoritesStage.setScene(favoritesScene);
        favoritesStage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}*/
package com.example.genzictionary;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;

import java.sql.*;

public class Genzictionary extends Application {

    private TextField searchField;
    private TextArea resultArea;
    private Button addToFavButton, clearButton;
    private ImageView imageView;
    private Image defaultImage;
    private String currentUser = null;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        showLoginPage(primaryStage);
    }

    private void showLoginPage(Stage primaryStage) {
        primaryStage.setTitle("Genzictionary - Login/Signup");

        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(new Font("Arial", 16));
        usernameLabel.setTextFill(Color.web("#FFFFFF"));

        TextField usernameField = new TextField();
        usernameField.setPrefWidth(200);
        usernameField.setFont(Font.font("Verdana", 14));
        usernameField.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(new Font("Arial", 16));
        passwordLabel.setTextFill(Color.web("#FFFFFF"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        passwordField.setFont(Font.font("Verdana", 14));
        passwordField.setStyle("-fx-background-color: #4a4a4a; -fx-text-fill: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;");

        Button loginButton = new Button("Login");
        Button signupButton = new Button("Sign Up");

        styleButton(loginButton);
        styleButton(signupButton);

        VBox loginBox = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signupButton);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: #303030; -fx-background-radius: 10; -fx-padding: 20;");

        Scene loginScene = new Scene(loginBox, 300, 250);

        primaryStage.setScene(loginScene);
        primaryStage.show();

        loginButton.setOnAction(e -> loginUser(primaryStage, usernameField.getText(), passwordField.getText()));
        signupButton.setOnAction(e -> signupUser(primaryStage, usernameField.getText(), passwordField.getText()));
    }

    private void styleButton(Button button) {
        button.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #d35400; -fx-text-fill: white; -fx-background-radius: 20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 20;"));
        button.setPrefWidth(100);
    }

    private void signupUser(Stage primaryStage, String username, String password) {

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Signup Error", "Username and password cannot be empty.");
            return;
        }

        try (Connection userConn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/genz", "root", "yash")) {

            String checker = "SELECT name FROM username WHERE name = ?";
            PreparedStatement userStmt1 = userConn1.prepareStatement(checker);
            userStmt1.setString(1, username);
            ResultSet rs = userStmt1.executeQuery();

            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Username already exists", "Please choose another username.");
                return;
            }

            String insertUser = "INSERT INTO username (name) VALUES (?)";
            PreparedStatement userStmt = userConn1.prepareStatement(insertUser);
            userStmt.setString(1, username);
            userStmt.executeUpdate();

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "yash")) {
                String createDbQuery = "CREATE DATABASE IF NOT EXISTS " + username;
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(createDbQuery);


                try (Connection userConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + username, "root", "yash")) {
                    String createTableQuery = "CREATE TABLE IF NOT EXISTS favorites (word VARCHAR(255) unique)";
                    Statement userStmt2 = userConn.createStatement();
                    userStmt2.executeUpdate(createTableQuery);
                }
            }

            currentUser = username;
            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "User " + username + " has been created.");
            showMainPage(primaryStage);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error creating user database.");
        }
    }


    private void loginUser(Stage primaryStage, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username and password cannot be empty.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "yash")) {

            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet rs = dbMeta.getCatalogs();
            boolean userExists = false;

            while (rs.next()) {
                if (rs.getString(1).equals(username)) {
                    userExists = true;
                    break;
                }
            }

            if (userExists) {
                currentUser = username;
                showMainPage(primaryStage);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Error", "User does not exist. Please sign up.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error during login.");
        }
    }

    private void showMainPage(Stage primaryStage) {
        primaryStage.setTitle("Genzictionary - Search GenZ Slang");

        MenuBar menuBar = new MenuBar();
        Menu loginMenu = new Menu("Account");
        Menu favMenu = new Menu("Favourites");
        Menu recMenu = new Menu("Recommendations");

        MenuItem logoutItem = new MenuItem("Logout");
        loginMenu.getItems().add(logoutItem);

        MenuItem showFavItem = new MenuItem("Show Favourites");
        favMenu.getItems().add(showFavItem);

        MenuItem genzItem = new MenuItem("GenZ Slangs");
        MenuItem englishItem = new MenuItem("English Words");
        recMenu.getItems().addAll(genzItem, englishItem);

        menuBar.getMenus().addAll(loginMenu, recMenu, favMenu);
        menuBar.setStyle("-fx-background-color: #8ec4fa -fx-text-fill: #030303;");

        searchField = new TextField();
        searchField.setPromptText("Enter GenZ word...");
        searchField.setPrefWidth(300);
        searchField.setFont(Font.font("Verdana", 14));
        searchField.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-background-radius: 10;");

        addToFavButton = new Button("Add to Favourites");
        clearButton = new Button("Clear");
        Button searchButton = new Button("Search");

        styleButton(addToFavButton);
        styleButton(clearButton);
        styleButton(searchButton);

        resultArea = new TextArea();
        resultArea.setPrefSize(400, 150);
        resultArea.setWrapText(true);
        resultArea.setEditable(false);
        resultArea.setFont(Font.font("Verdana", 14));
        resultArea.setStyle("-fx-control-inner-background: #2c3e50; -fx-text-fill: white; -fx-background-radius: 10;");

        imageView = new ImageView();
        defaultImage = new Image("file:default_image.png");
        imageView.setImage(defaultImage);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setStyle("-fx-background-radius: 10;");

        HBox searchBox = new HBox(10, searchField, searchButton);
        HBox buttonBox = new HBox(10, addToFavButton, clearButton);
        VBox vBox = new VBox(10, searchBox, buttonBox, resultArea, imageView);
        vBox.setPadding(new Insets(15));
        vBox.setStyle("-fx-background-color: #34495e; -fx-background-radius: 10; -fx-padding: 15;");

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(vBox);

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        searchButton.setOnAction(e -> searchWord());
        addToFavButton.setOnAction(e -> addToFavorites());
        clearButton.setOnAction(e -> clearFields());

        showFavItem.setOnAction(e -> showFavoritesPage());
        logoutItem.setOnAction(e -> showLoginPage(primaryStage));
    }

    private void searchWord() {
        String searchWord = searchField.getText();
        resultArea.clear();

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/genz", "root", "yash");
            String query = "SELECT * FROM data WHERE slang LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + searchWord + "%");
            ResultSet rs = pstmt.executeQuery();

            String query1 = "SELECT * FROM data WHERE synonms LIKE ?";
            PreparedStatement pstmt1 = conn.prepareStatement(query1);
            pstmt1.setString(1, "%" + searchWord + "%");
            ResultSet rs1 = pstmt1.executeQuery();


            if (rs.next()) {
                String meaning = rs.getString("meaning");
                String example = rs.getString("synonym_example");
                String synonyms = rs.getString("synonms");
                String synonyms1 = rs.getString("example");
                resultArea.setText("Meaning: " + meaning + "\nSynonyms: " + synonyms +"\nExample: " + synonyms1 + "\nEnglish example : " + example);
            } else if (rs1.next()) {
                String meaning = rs1.getString("meaning");
                String example = rs1.getString("synonym_example");
                String example1 = rs1.getString("example");
                String slang = rs1.getString("slang");
                resultArea.setText("Meaning: " + meaning + "\nExample: " + example + "\nGenz Slang: " + slang +"\nGenz example : "+ example1);
            } else {
                resultArea.setText("Word not found.");
            }

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error while searching.");
        }
    }

    private void addToFavorites() {
        String word = searchField.getText();
        if (word.isEmpty()) {
            resultArea.setText("Please enter a word.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + currentUser, "root", "yash")) {
            String query = "INSERT INTO favorites (word) VALUES (?)";
            PreparedStatement p = con.prepareStatement(query);
            p.setString(1, word);
            int result = p.executeUpdate();

            if (result > 0) {
                resultArea.setText("Added to favorites!");
            } else {
                resultArea.setText("Error adding to favorites.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error while adding to personal favorites.");
        }



        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/favorites", "root", "yash")) {
            String query = "INSERT INTO words (word) VALUES (?)";
            PreparedStatement p = con.prepareStatement(query);
            p.setString(1, word);
            p.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultArea.setText("Error while adding to common favorites.");
        }
    }

    private void clearFields() {
        searchField.clear();
        resultArea.clear();
        imageView.setImage(defaultImage);
    }

    private void showFavoritesPage() {
        Stage favoritesStage = new Stage();
        favoritesStage.setTitle("Favorites");

        TextArea favoritesArea = new TextArea();
        favoritesArea.setEditable(false);
        favoritesArea.setFont(Font.font("Verdana", 14));
        favoritesArea.setStyle("-fx-control-inner-background: #2c3e50; -fx-text-fill: white; -fx-background-radius: 10;");

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + currentUser, "root", "yash")) {
            String query = "SELECT word FROM favorites";
            PreparedStatement p = con.prepareStatement(query);
            ResultSet rs = p.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append(rs.getString("word")).append("\n");
            }
            favoritesArea.setText(sb.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
            favoritesArea.setText("Error loading personal favorites.");
        }

        Scene favoritesScene = new Scene(favoritesArea, 400, 300);
        favoritesStage.setScene(favoritesScene);
        favoritesStage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}






