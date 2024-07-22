import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FlightBookingGUI extends Application {
    

    private static final String DB_URL = "jdbc:mysql://localhost:3306/flight";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "devjangid@28";

    private Map<String, Integer> cityIds = new HashMap<>(); // Map to store city IDs
    private Map<String, String> cityNames = new HashMap<>(); // Map to store city names
    private String departureCity = "";
    private String arrivalCity = "";
    private LocalDate selectedDepartureDate;
    private LocalDate selectedReturnDate;
    private String selectedClass = "";
    private int totalAdults = 0;
    private int totalChildren = 0;
    private int totalInfants = 0;
    private String selectedTripType = "";
    private boolean isRegularSelected = false;
    private boolean isStudentSelected = false;
    private boolean isSeniorCitizenSelected = false;
    private boolean isDoctorSelected = false;
    private int flightId = -1;
    private Window primaryStage;

    @Override
    public void start(Stage primaryStage) {
        // Initialize city IDs and names
        initializeCityIds();
        initializecityNames();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        Image backgroundImage = new Image("file:///Users/asus_/OneDrive/Desktop/project/8.jpg");
        BackgroundSize backgroundSize = new BackgroundSize(400, 400, false, false, true, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        grid.setBackground(new Background(background));

        ToggleGroup tripType = new ToggleGroup();
        RadioButton rbOneWay = new RadioButton("One Way");
        rbOneWay.setToggleGroup(tripType);
        rbOneWay.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        RadioButton rbRoundTrip = new RadioButton("Round Trip");
        rbRoundTrip.setToggleGroup(tripType);
        rbRoundTrip.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        rbOneWay.setSelected(true);

        ComboBox<String> cbFrom = new ComboBox<>();
        cbFrom.setPromptText("Select Origin");
        cbFrom.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        ComboBox<String> cbTo = new ComboBox<>();
        cbTo.setPromptText("Select Destination");
        cbTo.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

        List<String> countries = getCountryList();
        cbFrom.getItems().addAll(cityIds.keySet());
        cbTo.getItems().addAll(cityIds.keySet());

        DatePicker dpDeparture = new DatePicker();
        dpDeparture.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        DatePicker dpReturn = new DatePicker();
        dpReturn.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        dpReturn.setManaged(false); // Initially hide the return date picker
        dpReturn.setVisible(false); // Initially hide the return date picker

        dpDeparture.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        dpReturn.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate minSelectableDate = selectedDepartureDate.plusDays(2); // Minimum selectable date, two days after departure
                setDisable(empty || date.compareTo(today) < 0 || date.isBefore(minSelectableDate));
            }
        });

        // Disable text field editing for date pickers
        dpDeparture.getEditor().setDisable(true);
        dpReturn.getEditor().setDisable(true);

        // Event handling for trip type selection
        tripType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            selectedTripType = ((RadioButton) newValue).getText();
            if ("One Way".equals(selectedTripType)) {
                dpReturn.setManaged(false); // Hide return date picker for one way trips
                dpReturn.setVisible(false); // Hide return date picker for one way trips
                dpReturn.setValue(null); // Clear any selected return date
                // Update selectedReturnDate when switching to one way trip
                selectedReturnDate = null;
            } else if ("Round Trip".equals(selectedTripType)) {
                dpReturn.setManaged(true); // Show return date picker for round trips
                dpReturn.setVisible(true); // Show return date picker for round trips
            }
        });

        // Event handling for return date selection
        dpReturn.setOnAction(e -> {
            selectedReturnDate = dpReturn.getValue(); // Update selectedReturnDate when the user selects a return date
        });

        HBox travelersAndClassBox = new HBox(10);
        travelersAndClassBox.setAlignment(Pos.CENTER_LEFT);
        Label travelersClassLabel = new Label("Travellers & Class:");
        travelersClassLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        ComboBox<String> cbTravellers = new ComboBox<>();
        cbTravellers.setPromptText("Select Travellers & Class");
        cbTravellers.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        cbTravellers.getItems().addAll("Economy", "Premium Economy", "Business", "First");
        travelersAndClassBox.getChildren().addAll(travelersClassLabel, cbTravellers);

        ComboBox<String> cbSpecialFare = new ComboBox<>();
        cbSpecialFare.getItems().addAll("Regular", "Student", "Doctor", "Senior Citizen");
        cbSpecialFare.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

        Label flightIdLabel = new Label("Flight ID: ");
        flightIdLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

        // Added Flight Capacity label
        Label flightCapacityLabel = new Label("Flight Capacity: 150");
        flightCapacityLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

        Button btnSearch = new Button("SEARCH");
        btnSearch.setStyle("-fx-background-color: blue; -fx-text-fill: gold; -fx-font-weight: bold;");

        grid.add(rbOneWay, 0, 0);
        grid.add(rbRoundTrip, 1, 0);
        grid.add(new Label("From:"), 0, 1);
        grid.add(cbFrom, 1, 1);
        grid.add(new Label("To:"), 0, 2);
        grid.add(cbTo, 1, 2);
        grid.add(new Label("Departure:"), 0, 3);
        grid.add(dpDeparture, 1, 3);
        grid.add(new Label("Return:"), 0, 4);
        grid.add(dpReturn, 1, 4);
        grid.add(travelersAndClassBox, 0, 5, 2, 1);
        grid.add(new Label("Select a special fare:"), 0, 6);
        grid.add(cbSpecialFare, 1, 6);
        grid.add(flightIdLabel, 0, 7);
        grid.add(flightCapacityLabel, 1, 7); // Added Flight Capacity label
        grid.add(btnSearch, 1, 8);

        Label totalPassengersLabel = new Label("Total Passengers: ");
        totalPassengersLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        grid.add(totalPassengersLabel, 2, 5);

        grid.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
            }
        });

        cbFrom.setOnAction(e -> {
            departureCity = cbFrom.getSelectionModel().getSelectedItem();
            if (departureCity != null) {
                cbTo.getItems().clear();
                cbTo.getItems().addAll(cityIds.keySet());
                cbTo.getItems().remove(departureCity);
                cbTo.setPromptText("Select Destination");
                cbTo.requestFocus();
            }
        });

        cbTo.setOnAction(e -> {
            arrivalCity = cbTo.getSelectionModel().getSelectedItem();
            if (arrivalCity != null) {
                cbTo.setValue(arrivalCity);
                dpDeparture.show();
            }
        });

        dpDeparture.setOnAction(e -> {
            selectedDepartureDate = dpDeparture.getValue();
            if ("Round Trip".equals(selectedTripType)) {
                dpReturn.show();
            }
        });

        cbTravellers.setOnAction(e -> {
            displayTravelerAndClassSelection(primaryStage, cbTravellers.getValue(), totalPassengersLabel);
        });

        cbSpecialFare.setOnAction(e -> {
            switch (cbSpecialFare.getValue()) {
                case "Regular":
                    isRegularSelected = true;
                    isStudentSelected = false;
                    isSeniorCitizenSelected = false;
                    isDoctorSelected = false;
                    break;
                case "Student":
                    isRegularSelected = false;
                    isStudentSelected = true;
                    isSeniorCitizenSelected = false;
                    isDoctorSelected = false;
                    break;
                case "Senior Citizen":
                    isRegularSelected = false;
                    isStudentSelected = false;
                    isSeniorCitizenSelected = true;
                    isDoctorSelected = false;
                    break;
                case "Doctor":
                    isRegularSelected = false;
                    isStudentSelected = false;
                    isSeniorCitizenSelected = false;
                    isDoctorSelected = true;
                    break;
            }
            generateFlightId();
            flightIdLabel.setText("Flight ID: " + flightId);
        });

        btnSearch.setOnAction(e -> {
            if (isInputValid()) {
                boolean cityIdsSaved = saveCityIdsToDatabase();
                boolean flightInfoSaved = saveFlightInfoToDatabase();
                if (cityIdsSaved && flightInfoSaved) {
                    openAnimationWindow(primaryStage);
                } else {
                    showAlert(primaryStage, "Error", "Failed to save data. Please try again.");
                }
            } else {
                showAlert(primaryStage, "Error", "Please fill in all the required fields.");
            }
        });

        Scene scene = new Scene(grid, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean saveCityIdsToDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Prepare SQL statement
            String sql = "INSERT INTO city (departure_id, arrival_id, departure_city, arrival_city) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, cityIds.get(departureCity)); // Set departure city ID
            statement.setInt(2, cityIds.get(arrivalCity)); // Set arrival city ID
            statement.setString(3, departureCity); // Set departure city name
            statement.setString(4, arrivalCity); // Set arrival city name

            // Execute the statement
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }


    private boolean saveFlightInfoToDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Prepare SQL statement
            String sql = "INSERT INTO flight_info (flight_id, departure_city, arrival_city, departure_date, flight_capacity, type) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, flightId);
            
            // Get departure city name
            String departureCityName = cityNames.get(departureCity);
            statement.setString(2, departureCityName);
    
            // Get arrival city name
            String arrivalCityName = cityNames.get(arrivalCity);
            statement.setString(3, arrivalCityName);
    
            // Set departure date
            statement.setDate(4, java.sql.Date.valueOf(selectedDepartureDate)); // Convert LocalDate to java.sql.Date
    
            // Set flight capacity
            statement.setInt(5, 150); // Default flight capacity
    
            // Set trip type
            statement.setString(6, selectedTripType);
    
            // Execute the statement
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    


    private boolean isInputValid() {
        boolean isOneWayTrip = selectedTripType.equals("One Way");

        // Check if any of the required fields are empty
        if (departureCity.isEmpty() || arrivalCity.isEmpty() || selectedDepartureDate == null ||
                selectedClass.isEmpty() || (totalAdults + totalChildren + totalInfants <= 0)) {
            return false;
        }

        // If it's a round trip and the return date is before the departure date, it's invalid
        if (!isOneWayTrip && selectedReturnDate.isBefore(selectedDepartureDate)) {
            return false;
        }
        

        return true;
    }



    private void openAnimationWindow(Stage primaryStage) {
        Stage animationStage = new Stage();
        animationStage.initOwner(primaryStage);
        animationStage.setTitle("Loading Animation");

        Image gifImage = new Image("file:///Users/asus_/OneDrive/Desktop/project/9.gif");
        ImageView imageView = new ImageView(gifImage);

        // Here is where you define the layout for the animation window
        VBox layout = new VBox();
        layout.getChildren().add(imageView);

        Scene scene = new Scene(layout);
        animationStage.setScene(scene);
        animationStage.show();

        Timeline timeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(2), event -> animationStage.close())
        );
        timeline.play();

        timeline.setOnFinished(event -> {
            FlightBookingGUI1 flightBookingGUI1 = new FlightBookingGUI1(departureCity, arrivalCity, selectedDepartureDate, selectedReturnDate, selectedClass, totalAdults, totalChildren, totalInfants, selectedTripType, isRegularSelected, isStudentSelected, isSeniorCitizenSelected, isDoctorSelected);
            openAnimationWindow(primaryStage, flightBookingGUI1);
        });
    }

    private void openAnimationWindow(Stage primaryStage, FlightBookingGUI1 flightBookingGUI1) {
        Timeline timeline = new Timeline(
                new KeyFrame(javafx.util.Duration.seconds(2), event -> {
                    flightBookingGUI1.start(primaryStage);
                })
        );
        timeline.play();
    }

    private List<String> getCountryList() {
        List<String> countries = new ArrayList<>();
        countries.add("United States");
        countries.add("Canada");
        countries.add("Australia");
        countries.add("Japan");
        countries.add("France");
        countries.add("Germany");
        countries.add("Mexico");
        countries.add("Italy");
        countries.add("Thailand");
        countries.add("Egypt");
        countries.add("Hong Kong");
        countries.add("New Zealand");
        countries.add("Poland");
        countries.add("Russia");
        countries.add("Malaysia");
        countries.add("Hong Kong");
        countries.add("United Kingdom");
        countries.add("India");
        return countries;
    }

    private void displayTravelerAndClassSelection(Stage primaryStage, String initialSelectedClass, Label totalPassengersLabel) {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));

        // Load the background image
        Image backgroundImage = new Image("file:///Users/asus_/OneDrive/Desktop/project/19.jpg");
        BackgroundSize backgroundSize = new BackgroundSize(800, 400, false, false, true, false);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        mainLayout.setBackground(new Background(background));

        // Create the content for the window
        Label adultsLabel = new Label("Adults (12+ Years)");
        adultsLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        Spinner<Integer> adultsSpinner = new Spinner<>(0, 9, 1);
        Label childrenLabel = new Label("Children (2-12 Years)");
        childrenLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        Spinner<Integer> childrenSpinner = new Spinner<>(0, 9, 0);
        Label infantsLabel = new Label("Infant (0–2 Years)");
        infantsLabel.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        Spinner<Integer> infantsSpinner = new Spinner<>(0, 9, 0);
        CheckBox moreThanNine = new CheckBox("More than 9 Travellers");
        moreThanNine.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        VBox travelersSection = new VBox(5, adultsLabel, adultsSpinner, childrenLabel, childrenSpinner, infantsLabel, infantsSpinner, moreThanNine);
        travelersSection.setAlignment(Pos.CENTER);

        ToggleGroup classService = new ToggleGroup();
        RadioButton economy = new RadioButton("Economy");
        economy.setToggleGroup(classService);
        economy.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        RadioButton premEconomy = new RadioButton("Prem.Economy");
        premEconomy.setToggleGroup(classService);
        premEconomy.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        RadioButton business = new RadioButton("Business");
        business.setToggleGroup(classService);
        business.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");
        RadioButton first = new RadioButton("First");
        first.setToggleGroup(classService);
        first.setStyle("-fx-text-fill: gold; -fx-font-weight: bold;");

        switch (initialSelectedClass.trim().toLowerCase()) {
            case "economy":
                economy.setSelected(true);
                selectedClass = "Economy"; // Update selectedClass
                break;
            case "premium economy":
                premEconomy.setSelected(true);
                selectedClass = "Prem.Economy"; // Update selectedClass
                break;
            case "business":
                business.setSelected(true);
                selectedClass = "Business"; // Update selectedClass
                break;
            case "first":
                first.setSelected(true);
                selectedClass = "First"; // Update selectedClass
                break;
        }

        VBox classServiceSection = new VBox(5, economy, premEconomy, business, first);
        classServiceSection.setAlignment(Pos.CENTER);

        Button applyButton = new Button("APPLY");
        applyButton.setStyle("-fx-background-color: blue; -fx-text-fill: gold; -fx-font-weight: bold;");

        HBox applyButtonBox = new HBox(applyButton);
        applyButtonBox.setAlignment(Pos.CENTER); // Center the button

        mainLayout.getChildren().addAll(travelersSection, classServiceSection, applyButtonBox);

        classService.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue instanceof RadioButton) {
                selectedClass = ((RadioButton) newValue).getText(); // Update selectedClass
            }
        });

        applyButton.setOnAction(event -> {
            totalAdults = adultsSpinner.getValue();
            totalChildren = childrenSpinner.getValue();
            totalInfants = infantsSpinner.getValue();
            int totalPassengers = totalAdults + totalChildren + totalInfants;
            if (totalPassengers > 9) {
                showAlert(mainLayout.getScene().getWindow(), "Error", "Maximum number of passengers allowed is 9.");
            } else {
                totalPassengersLabel.setText("Total Passengers: " + totalPassengers);
                ((Stage) mainLayout.getScene().getWindow()).close();
            }

        });

        // Create a new scene with the main layout
        Scene scene = new Scene(mainLayout, 800, 400);

        // Create a new stage for the traveler selection window
        Stage travelerSelectionStage = new Stage();
        travelerSelectionStage.setTitle("Traveler Selection");
        travelerSelectionStage.setScene(scene);
        travelerSelectionStage.initOwner(primaryStage);
        travelerSelectionStage.show();
    }

    private void showAlert(Window parentWindow, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(parentWindow);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initializeCityIds() {
        // Here you define permanent city IDs for each city
        cityIds.put("United States (ID: 1001)", 1001);
        cityIds.put("Canada (ID: 1002)", 1002);
        cityIds.put("Australia (ID: 1003)", 1003);
        cityIds.put("Japan (ID: 1004)", 1004);
        cityIds.put("France (ID: 1005)", 1005);
        cityIds.put("Germany (ID: 1006)", 1006);
        cityIds.put("Mexico (ID: 1007)", 1007);
        cityIds.put("Italy (ID: 1008)", 1008);
        cityIds.put("Thailand (ID: 1009)", 1009);
        cityIds.put("Egypt (ID: 1010)", 1010);
        cityIds.put("Hong Kong (ID: 1011)", 1011);
        cityIds.put("New Zealand (ID: 1012)", 1012);
        cityIds.put("Poland (ID: 1013)", 1013);
        cityIds.put("Russia (ID: 1014)", 1014);
        cityIds.put("Malaysia (ID: 1015)", 1015);
        cityIds.put("United Kingdom (ID: 1016)", 1016);
        cityIds.put("India (ID: 1017)", 1017);
    }
    
    private void initializecityNames() {
        cityNames.put("United States (ID: 1001)", "United States");
        cityNames.put("Canada (ID: 1002)", "Canada");
        cityNames.put("Australia (ID: 1003)", "Australia");
        cityNames.put("Japan (ID: 1004)", "Japan");
        cityNames.put("France (ID: 1005)", "France");
        cityNames.put("Germany (ID: 1006)", "Germany");
        cityNames.put("Mexico (ID: 1007)", "Mexico");
        cityNames.put("Italy (ID: 1008)", "Italy");
        cityNames.put("Thailand (ID: 1009)", "Thailand");
        cityNames.put("Egypt (ID: 1010)", "Egypt");
        cityNames.put("Hong Kong (ID: 1011)", "Hong Kong");
        cityNames.put("New Zealand (ID: 1012)", "New Zealand");
        cityNames.put("Poland (ID: 1013)", "Poland");
        cityNames.put("Russia (ID: 1014)", "Russia");
        cityNames.put("Malaysia (ID: 1015)", "Malaysia");
        cityNames.put("United Kingdom (ID: 1016)", "United Kingdom");
        cityNames.put("India (ID: 1017)", "India");
    }
    

    

    private void generateFlightId() {
        Random rand = new Random();
        flightId = rand.nextInt(10000); // Generate a random flight ID
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void updateSeatAvailability(Object selectedClass2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateSeatAvailability'");
    }
}
