import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlightFilterGUI extends Application {
    private boolean[][] seatAvailability = new boolean[20][15]; // 20 rows, 15 columns
    private VBox customerDetailsVBox;
    private List<Button> selectedSeats = new ArrayList<>();
    private static final String SEAT_FILE = "selected_seats.txt";
    private GridPane gridPane; // Declare GridPane as a class variable
    private TextField passengerIdField; // Change Node to TextField for passengerIdField
    private Node addressField;

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost/flight";
    private static final String USER = "root";
    private static final String PASS = "devjangid@28";

    @Override
    public void start(Stage primaryStage) {
        gridPane = new GridPane(); // Initialize gridPane here

        gridPane.setHgap(5); // Reduced horizontal gap for a more compact layout
        gridPane.setVgap(5); // Reduced vertical gap for a more compact layout

        // Titles for each seating class
        String[] classTitles = {"Business Class", "First Class", "Premium Economy", "Economy"};

        // Add class titles above each set of columns
        for (int i = 0; i < classTitles.length; i++) {
            Label titleLabel = new Label(classTitles[i]);
            titleLabel.setFont(Font.font("Arial", 14));
            titleLabel.setTextFill(Color.GOLD); // Set text color to gold
            titleLabel.setAlignment(Pos.CENTER);
            gridPane.add(titleLabel, i * 5, 0, 5, 1); // Span 5 columns for each class
        }

        // Create seat buttons and assign background colors based on class
        for (int row = 1; row < 21; row++) { // Changed row limit to 21
            for (int col = 0; col < 15; col++) {
                Button seatButton = new Button("Seat " + row + (char) ('A' + col));
                seatButton.setMinSize(20, 20); // Reduced size for smaller buttons
                int finalRow = row - 1; // Capture the row and col variables
                int finalCol = col; // effectively inside the lambda
                seatButton.setOnAction(e -> handleSeatSelection(finalRow, finalCol, seatButton));
                gridPane.add(seatButton, col, row);
                // Apply different background colors based on the seat partition
                if (col < 5) {
                    seatButton.setStyle("-fx-background-color: lightblue;"); // Business class
                } else if (col < 10) {
                    seatButton.setStyle("-fx-background-color: lightpink;"); // First class
                } else if (col < 15) {
                    seatButton.setStyle("-fx-background-color: lightgreen;"); // Premium economy
                } else {
                    seatButton.setStyle("-fx-background-color: lightyellow;"); // Economy
                }

                // Check if seat is selected and mark it as occupied
                if (seatAvailability[row - 1][col]) {
                    seatButton.setDisable(true);
                    seatButton.setStyle("-fx-background-color: red;"); // Mark occupied seats as red
                }
            }
        }

        // Book Now button
        Button bookNowButton = new Button("Book Now");
        bookNowButton.setOnAction(e -> bookNow(primaryStage));

        // Customer Details VBox
        customerDetailsVBox = createCustomerDetailsVBox();

        // Main layout HBox
        HBox mainLayout = new HBox(gridPane, customerDetailsVBox);
        mainLayout.setSpacing(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);

        // Set the VBox alignment to the top-right
        VBox.setMargin(customerDetailsVBox, new Insets(0, 20, 0, 0));

        // Add the Book Now button below the main layout
        VBox root = new VBox(mainLayout, bookNowButton);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 800, 600); // Adjusted width for customer details
        primaryStage.setScene(scene);
        primaryStage.setTitle("Airplane Seat Selection");
        primaryStage.show();

        loadSelectedSeats(); // Load selected seats after the UI is initialized

        // Generate random passenger ID and set it in the field when the application starts
        Random random = new Random();
        int passengerId = random.nextInt(9000) + 1000;
        passengerIdField.setText(String.valueOf(passengerId));
        passengerIdField.setEditable(false); // Make the passengerIdField non-editable
    }

    private VBox createCustomerDetailsVBox() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Customer Details");
        titleLabel.setFont(Font.font("Arial", 18));
        titleLabel.setTextFill(Color.GOLD); // Set text color to gold

        // Sample customer details fields
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        passengerIdField = new TextField(); // Use TextField instead of Node
        passengerIdField.setPromptText("Passenger ID");
        passengerIdField.setEditable(false); // Make the passengerIdField non-editable

        TextField addressField = new TextField(); // Corrected variable name
        addressField.setPromptText("Address");

        TextField ageField = new TextField();
        ageField.setPromptText("Age");

        TextField genderField = new TextField();
        genderField.setPromptText("Gender");

        Button addPassengerButton = new Button("+ Add Passenger");
        addPassengerButton.setOnAction(e -> addPassenger(vbox, addPassengerButton)); // Passing the VBox and the button to the method

        vbox.getChildren().addAll(titleLabel, fullNameField, passengerIdField, addressField, ageField, genderField, addPassengerButton);

        // Assign the nodes to class variables
        this.addressField = addressField;

        return vbox;
    }

    private void handleSeatSelection(int row, int col, Button seatButton) {
        if (!seatAvailability[row][col]) {
            if (selectedSeats.size() < 9) { // Check if the number of selected seats is less than 9
                seatButton.setStyle("-fx-background-color: green;");
                seatAvailability[row][col] = true;
                selectedSeats.add(seatButton);
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Seat Limit Reached", "You can only select a maximum of 9 seats.");
            }
        } else {
            seatButton.setStyle(null); // Revert to default style
            seatAvailability[row][col] = false;
            selectedSeats.remove(seatButton);
        }
    }

    private void addPassenger(VBox vbox, Button addPassengerButton) {
        // Sample customer details fields
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
    
        // Generate a random passenger ID between 1000 and 9999
        Random random = new Random();
        int passengerId = random.nextInt(9000) + 1000;
        TextField newPassengerIdField = new TextField(String.valueOf(passengerId)); // Changed variable name
        newPassengerIdField.setEditable(false); // Make the field non-editable
    
        TextField addressField = new TextField();
        addressField.setPromptText("Address");
    
        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        ageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ageField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    
        TextField genderField = new TextField();
        genderField.setPromptText("Gender");
    
        // Create a list to hold all the text fields
        List<Node> textFields = new ArrayList<>();
        textFields.add(fullNameField);
        textFields.add(newPassengerIdField); // Changed variable name
        textFields.add(addressField);
        textFields.add(ageField);
        textFields.add(genderField);
    
        // Insert the new fields before the button
        int index = vbox.getChildren().indexOf(addPassengerButton);
        vbox.getChildren().addAll(index, textFields);
    
        // Validate input fields before adding the passenger
        addPassengerButton.setOnAction(e -> {
            TextField ageFieldInVBox = (TextField) textFields.get(3); // Get the age field from the list
            if (fullNameField.getText().isEmpty() || addressField.getText().isEmpty() || ageFieldInVBox.getText().isEmpty() || genderField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Empty Fields", "Please fill in all fields.");
            } else {
                // Add the passenger if all fields are filled
                savePassengerToDatabase(fullNameField.getText(), addressField.getText(), Integer.parseInt(ageFieldInVBox.getText()), genderField.getText());
                vbox.getChildren().removeAll(textFields); // Remove the input fields
                addPassenger(vbox, addPassengerButton); // Add another set of input fields
            }
        });
    }
    
    private void bookNow(Stage primaryStage) {
        // Check if any seats are selected
        if (selectedSeats.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No Seats Selected", "Please select at least one seat.");
            return;
        }
    
        // Check if customer details are filled
        TextField fullNameField = (TextField) customerDetailsVBox.getChildren().get(1);
        TextField addressField = (TextField) this.addressField; // Correctly casted as a TextField
        TextField ageField = (TextField) customerDetailsVBox.getChildren().get(3);
        TextField genderField = (TextField) customerDetailsVBox.getChildren().get(4);
    
        if (fullNameField.getText().isEmpty() || addressField.getText().isEmpty() ||
                ageField.getText().isEmpty() || genderField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Empty Fields", "Please fill in all customer details.");
            return;
        }
    
        // Validate age field
        try {
            int age = Integer.parseInt(ageField.getText());
            if (age < 0 || age > 150) { // Adjust age range as needed
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid Age", "Please enter a valid age.");
                return;
            }
            // Save customer details to the database
            savePassengerToDatabase(fullNameField.getText(), addressField.getText(), age, genderField.getText());
    
            // If saving to the database is successful, open the payment GUI
            //Open PaymentGUI
            PaymentGUI paymentGUI = new PaymentGUI();
            Stage paymentStage = new Stage();
            paymentGUI.start(paymentStage);
    
            // Save selected seats to file
            saveSelectedSeats();
    
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Seats Booked", "Your seats have been successfully booked.");
    
            // Clear selected seats list
            selectedSeats.clear();
    
            // Close FlightFilterGUI window
            primaryStage.close();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Age Format", "Please enter a valid age as a number.");
        }
    }
    

    private void savePassengerToDatabase(String fullName, String address, int age, String gender) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // Query to fetch the register_id from the signup table
            String registerIdQuery = "SELECT register_id FROM signup  WHERE Name = ?";
            PreparedStatement registerIdStmt = conn.prepareStatement(registerIdQuery);
            registerIdStmt.setString(1, fullName);
            ResultSet resultSet = registerIdStmt.executeQuery();
            
            if (resultSet.next()) {
                String registerId = resultSet.getString("register_id");
                
                // Insert passenger details into passanger_details table
                String sql = "INSERT INTO passanger_details (passanger_id, register_id, passanger_name, address, age, gender) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, passengerIdField.getText());
                pstmt.setString(2, registerId);
                pstmt.setString(3, fullName);
                pstmt.setString(4, address);
                pstmt.setInt(5, age);
                pstmt.setString(6, gender);
                pstmt.executeUpdate();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Register ID Not Found", "Unable to find register ID for the provided full name.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error", "Failed to save passenger details to the database.");
        }
    }

    // Method to show alert
    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void saveSelectedSeats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SEAT_FILE))) {
            for (Button seatButton : selectedSeats) {
                String seatId = seatButton.getText().substring(5); // Extract seat id from button text
                writer.println(seatId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectedSeats() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SEAT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Seat ")) {
                    // Extract row and column from the seat ID
                    int row = Integer.parseInt(line.substring(5, line.length() - 1)) - 1; // Extract row from seat ID
                    char colChar = line.charAt(line.length() - 1); // Extract column character from seat ID
                    int col = colChar - 'A'; // Convert column character to index
                    if (row >= 0 && row < 20 && col >= 0 && col < 15) {
                        seatAvailability[row][col] = true;
                        String seatId = "Seat " + (row + 1) + colChar;
                        for (Node node : gridPane.getChildren()) {
                            if (node instanceof Button) {
                                Button seatButton = (Button) node;
                                if (seatButton.getText().equals(seatId)) {
                                    seatButton.setDisable(true);
                                    seatButton.setStyle("-fx-background-color: red;"); // Mark occupied seats as red
                                    selectedSeats.add(seatButton); // Add the seatButton to selectedSeats list
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // File not found or error reading file, ignore
        }
    }

    public static void main(String[] args) {
        // Load the MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        launch(args);
    }
}
