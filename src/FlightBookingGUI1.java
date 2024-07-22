import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.mysql.cj.xdevapi.Statement;

public class FlightBookingGUI1 extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/flight";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "devjangid@28";

    private Connection conn;

    private String departureCity;
    private String arrivalCity;
    private LocalDate selectedDepartureDate;
    private LocalDate selectedReturnDate;
    private String selectedClass;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
    private String selectedTripType;
    private boolean isRegularSelected;
    private boolean isStudentSelected;
    private boolean isSeniorCitizenSelected;
    private boolean isDoctorSelected;

    // Map to store flights for each departure city
    private Map<String, List<Flight>> flightMap;
    private String departureTime;
    private String address;
    private ComboBoxBase<String> departureDatePicker;
    private int flightId;

    public FlightBookingGUI1() {
        
        flightMap = new HashMap<>();
    }

    public FlightBookingGUI1(String departureCity, String arrivalCity, LocalDate selectedDepartureDate, LocalDate selectedReturnDate, String selectedClass, int totalAdults, int totalChildren, int totalInfants, String selectedTripType, boolean isRegularSelected, boolean isStudentSelected, boolean isSeniorCitizenSelected, boolean isDoctorSelected) {
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.selectedDepartureDate = selectedDepartureDate;
        this.selectedReturnDate = selectedReturnDate;
        this.selectedClass = selectedClass;
        this.totalAdults = totalAdults;
        this.totalChildren = totalChildren;
        this.totalInfants = totalInfants;
        this.selectedTripType = selectedTripType;
        this.isRegularSelected = isRegularSelected;
        this.isStudentSelected = isStudentSelected;
        this.isSeniorCitizenSelected = isSeniorCitizenSelected;
        this.isDoctorSelected = isDoctorSelected;
        flightMap = new HashMap<>();
    }

    public void setSelectedReturnDate(LocalDate selectedReturnDate) {
        this.selectedReturnDate = selectedReturnDate;
    }

    

    @Override
    public void start(Stage primaryStage) {
        Image backgroundImage = new Image("file:///C:/Users/asus_/OneDrive/Desktop/project/20.jpg");
        BackgroundSize backgroundSize = new BackgroundSize(1800, 1200, false, false, false, false); // Set width and height to 100
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        Background backgroundWithImage = new Background(background);

        // Create a main layout with a background image
        BorderPane mainLayout = new BorderPane();
        mainLayout.setBackground(backgroundWithImage);

                // Generate a journey ID between 1000 and 9999
        int journeyId = ThreadLocalRandom.current().nextInt(1000, 10000);

        // Create the label for the journey ID
        Label journeyIdLabel = new Label("Journey ID: " + journeyId);
        journeyIdLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(journeyIdLabel, Pos.TOP_CENTER);

        // Apply CSS styling to make the font bold and increase its size
        journeyIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        mainLayout.setTop(journeyIdLabel);


        // Create a VBox for the content
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        contentBox.setAlignment(Pos.CENTER);

        // Travelers Section
        HBox travelersSection = new HBox(10);
        travelersSection.setAlignment(Pos.CENTER);
        travelersSection.setBackground(Background.EMPTY); // Set background to transparent
        ComboBox<String> departureCityDropdown = new ComboBox<>();
        departureCityDropdown.setPromptText(departureCity);
        ComboBox<String> arrivalCityDropdown = new ComboBox<>();
        arrivalCityDropdown.setPromptText(arrivalCity);
        List<String> countries = getCountryList();
        departureCityDropdown.getItems().addAll(countries);
        arrivalCityDropdown.getItems().addAll(countries);

        if (departureCity != null && !departureCity.isEmpty()) {
            departureCityDropdown.setValue(departureCity);
        }

        if (arrivalCity != null && !arrivalCity.isEmpty()) {
            arrivalCityDropdown.setValue(arrivalCity);
        }

        TextField departureCityTextField = new TextField(departureCity);
        departureCityTextField.setPromptText("Departure City");
        TextField arrivalCityTextField = new TextField(arrivalCity);
        arrivalCityTextField.setPromptText("Arrival City");

        // Remove the selected departure city from the arrival city dropdown
        departureCityDropdown.setOnAction(event -> {
            // Disable selected country in arrival city dropdown
            String selectedDeparture = departureCityDropdown.getSelectionModel().getSelectedItem();
            if (selectedDeparture != null && !selectedDeparture.isEmpty()) {
                arrivalCityDropdown.getItems().remove(selectedDeparture);
                // Update flights for the selected departure city
                updateFlights(selectedDeparture, arrivalCityDropdown.getValue());
            }
        });

        // Remove the selected arrival city from the departure city dropdown
        arrivalCityDropdown.setOnAction(event -> {
            // Disable selected country in departure city dropdown
            String selectedArrival = arrivalCityDropdown.getSelectionModel().getSelectedItem();
            if (selectedArrival != null && !selectedArrival.isEmpty()) {
                departureCityDropdown.getItems().remove(selectedArrival);
            }
        });

        DatePicker departureDatePicker = new DatePicker(selectedDepartureDate);
        DatePicker returnDatePicker = new DatePicker(selectedReturnDate);

        // Disable return date picker by default
        returnDatePicker.setDisable(true);

        // Update return date picker based on trip type
        ToggleGroup tripTypeGroup = new ToggleGroup();
        RadioButton oneWayRadioButton = new RadioButton("One Way");
        oneWayRadioButton.setToggleGroup(tripTypeGroup);
        RadioButton roundTripRadioButton = new RadioButton("Round Trip");
        roundTripRadioButton.setToggleGroup(tripTypeGroup);
        oneWayRadioButton.setSelected(true);

        tripTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == oneWayRadioButton) {
                returnDatePicker.setDisable(true);
            } else if (newValue == roundTripRadioButton) {
                returnDatePicker.setDisable(false);
            }
        });

        // Set the selected return date in the return date picker
        returnDatePicker.setValue(selectedReturnDate);

        // Initialize the return date picker state based on the selected trip type
        if (selectedTripType != null && selectedTripType.equals("Round Trip")) {
            roundTripRadioButton.setSelected(true);
            returnDatePicker.setDisable(false);
            returnDatePicker.setValue(selectedReturnDate); // Display the selected return date
        } else {
            oneWayRadioButton.setSelected(true);
            returnDatePicker.setDisable(true);
        }

        travelersSection.getChildren().addAll(departureCityDropdown, arrivalCityDropdown, departureDatePicker, returnDatePicker);

        ComboBox<Integer> travelersDropdown = new ComboBox<>();
        travelersDropdown.setPromptText("Travelers: " + (totalAdults + totalChildren + totalInfants)); // Update prompt text with total passengers
        ComboBox<String> classDropdown = new ComboBox<>();
        classDropdown.setPromptText("Class: " + selectedClass); // Update prompt text with selected class

        // Set the selected class from FlightBookingGUI
        classDropdown.setValue(selectedClass);

        Button searchButton = new Button("Search");
        travelersSection.getChildren().addAll(travelersDropdown, classDropdown, searchButton); // Changed the order of adding nodes

        contentBox.getChildren().add(travelersSection);

        // Special categories container
        HBox specialCategoriesContainer = new HBox(90);
        specialCategoriesContainer.setAlignment(Pos.CENTER); // Align the checkboxes to the center
        CheckBox defenceForcesCheckbox = new CheckBox("Regular");
        defenceForcesCheckbox.setSelected(isRegularSelected);
        CheckBox studentsCheckbox = new CheckBox("Student");
        studentsCheckbox.setSelected(isStudentSelected);
        CheckBox seniorCitizensCheckbox = new CheckBox("Senior Citizen");
        seniorCitizensCheckbox.setSelected(isSeniorCitizenSelected);
        CheckBox doctorsNursesCheckbox = new CheckBox("Doctor");
        doctorsNursesCheckbox.setSelected(isDoctorSelected);
        specialCategoriesContainer.getChildren().addAll(defenceForcesCheckbox, studentsCheckbox, seniorCitizensCheckbox, doctorsNursesCheckbox);

        contentBox.getChildren().add(specialCategoriesContainer);

        // Flight Details Section
        VBox flightDetailsSection = new VBox(10);
        flightDetailsSection.setAlignment(Pos.CENTER);
        flightDetailsSection.setPadding(new Insets(20));
        flightDetailsSection.setBackground(Background.EMPTY); // Set background to transparent



       

        // Accordion for flight details
        Accordion flightDetailsAccordion = new Accordion();

        // Populate flights for the initial departure city
        if (departureCity != null) {
            updateFlights(departureCity, arrivalCity);

            // Add flights for the initial departure city to the accordion
            List<Flight> initialFlights = flightMap.getOrDefault(departureCity, new ArrayList<>());
            for (Flight flight : initialFlights) {
                GridPane flightGrid = new GridPane();
                flightGrid.setHgap(10);
                flightGrid.setVgap(10);
                flightGrid.setPrefWidth(900); // Set width to 900 pixels
                flightGrid.add(new Label("Flight Name:"), 0, 0);
                flightGrid.add(new Label(flight.getName()), 1, 0);
                flightGrid.add(new Label("Airline:"), 0, 1);
                flightGrid.add(new Label(flight.getAirline()), 1, 1);
                flightGrid.add(new Label("Departure Time:"), 0, 2);
                flightGrid.add(new Label(flight.getDepartureTime()), 1, 2);
                flightGrid.add(new Label("Luggage:"), 0, 3);
                flightGrid.add(new Label(flight.getLuggage()), 1, 3);
                flightGrid.add(new Label("Restrictions:"), 0, 4);
                flightGrid.add(new Label(flight.getRestrictions()), 1, 4);
                Button bookFlightButton = new Button("BOOK NOW");
                bookFlightButton.setOnAction(event -> bookFlight(flight.getName(), journeyId, address));
                flightGrid.add(bookFlightButton, 0, 5, 2, 1);

                TitledPane flightPane = new TitledPane(flight.getName() + " Details", flightGrid);
                flightDetailsAccordion.getPanes().add(flightPane);
            }
        }

        flightDetailsSection.getChildren().add(flightDetailsAccordion);

        contentBox.getChildren().add(flightDetailsSection);

        // Filter Section
        VBox filterSection = new VBox(10);
        filterSection.setPadding(new Insets(15));
        filterSection.setBackground(Background.EMPTY); // Set background to transparent
        filterSection.setAlignment(Pos.TOP_LEFT);

        
        // Price Range Slider
        Label priceRangeLabel = new Label("Price Range:");
        Slider priceRangeSlider = new Slider(0, 25000, 5000);
        priceRangeSlider.setShowTickLabels(true);
        priceRangeSlider.setShowTickMarks(true);

        // Set tick unit to 5000
        priceRangeSlider.setMajorTickUnit(5000);
        priceRangeSlider.setMinorTickCount(4);

        // Label to display the current price
        Label currentPriceLabel = new Label("Current Price: ₹" + (int) priceRangeSlider.getValue());

        // Listener to update the label when the slider value changes
        priceRangeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPriceLabel.setText("Current Price: ₹" + (int) newValue.doubleValue());
        });

        // Buttons for selecting the number of stops
        Label stopsLabel = new Label("Number of Stops:");
        Button noStopsButton = new Button("0");
        Button oneStopButton = new Button("1");
        Button twoStopsButton = new Button("2+");

        // Images for time of day
        Image morningImage = new Image("file:///Users/asus_/OneDrive/Desktop/project/morning.png"); // replace with your image file path
        Image eveningImage = new Image("file:///Users/asus_/OneDrive/Desktop/project/evening.png"); // replace with your image file path
        Image nightImage = new Image("file:///Users/asus_/OneDrive/Desktop/project/night.png"); // replace with your image file path

        // Buttons with images
        Button morningButton = new Button("", new ImageView(morningImage));
        Button eveningButton = new Button("", new ImageView(eveningImage));
        Button nightButton = new Button("", new ImageView(nightImage));

        // Set size of buttons
        morningButton.setPrefSize(20, 20);
        eveningButton.setPrefSize(50, 50);
        nightButton.setPrefSize(50, 50);

        // Arrange the buttons horizontally
        HBox imageButtons = new HBox(10);
        imageButtons.getChildren().addAll(morningButton, eveningButton, nightButton);

        // Adding elements to the filter section
        filterSection.getChildren().addAll(
                priceRangeLabel,
                priceRangeSlider,
                currentPriceLabel, // Add the label to display the current price
                stopsLabel,
                noStopsButton,
                oneStopButton,
                twoStopsButton,
                imageButtons // Add the HBox containing the image buttons
        );

        contentBox.getChildren().add(filterSection);

        mainLayout.setCenter(contentBox);

        // Display the main layout
        Scene scene = new Scene(mainLayout);
        primaryStage.setTitle("Flight Booking GUI");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true); // Set the stage to full screen
        primaryStage.show();

        // Event handler for the search button
        searchButton.setOnAction(event -> {
            // Get the selected arrival city
            String selectedArrivalCity = arrivalCityDropdown.getValue();
            String selecteddepartureCity = arrivalCityDropdown.getValue();
            // Update flights based on the selected arrival city
            updateFlights(departureCityDropdown.getValue(), selectedArrivalCity);
            updateFlights(arrivalCityDropdown.getValue(), selecteddepartureCity);
            // Refresh the flight details section with updated flights
            refreshFlightDetailsAccordion(flightDetailsAccordion, selectedArrivalCity);
            refreshFlightDetailsAccordion(flightDetailsAccordion, selecteddepartureCity);
        });
    }

    private boolean saveJourneyDetailsToDatabase(int passengerId, int flightId, LocalDate departureDate, String departureTime, int totalPassengers) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Prepare SQL statement for journey details
            String sql = "INSERT INTO journey_details (journey_id, passanger_id, flight_id, departure_date, departure_time, no_of_passenger) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            // Generate journey_id randomly
            int journeyId = generateJourneyId();

            // Set values for the statement
            statement.setInt(1, journeyId); // Set journey_id
            statement.setInt(2, passengerId); // Set passenger_id
            statement.setInt(3, flightId); // Set flight_id
            statement.setDate(4, java.sql.Date.valueOf(departureDate)); // Set departure_date
            statement.setString(5, departureTime); // Set departure_time
            statement.setInt(6, totalPassengers); // Set total_passengers

            // Execute the statement
            statement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private int generateJourneyId() {
        // Generate a random journey ID between 1000 and 9999
        return ThreadLocalRandom.current().nextInt(1000, 10000);
    }
    
    

            
        


    // Method to open a new window with seat selection GUI
    private void openSeatSelectionWindow() {
        // Create a new stage
        Stage seatSelectionStage = new Stage();
        seatSelectionStage.setTitle("Airplane Seat Selection");

        // Create a new instance of the FlightFilterGUI and call its start method
        FlightFilterGUI flightFilterGUI = new FlightFilterGUI();
        flightFilterGUI.start(seatSelectionStage);
    }

    private void bookFlight(String flightName, int flightId, String departureTime) {
        System.out.println("Booking " + flightName);

        // Check if departure date is fetched successfully
        if (selectedDepartureDate != null) {
            boolean bookingSuccess = saveJourneyDetailsToDatabase(totalAdults, flightId, selectedDepartureDate, departureTime, totalAdults);

            if (bookingSuccess) {
                System.out.println("Booking successful!");
                // Optionally, you can perform additional actions here after successful booking
            } else {
                System.out.println("Booking failed. Please try again.");
                // Optionally, you can show an error message to the user
            }
            openSeatSelectionWindow();
        } else {
            System.out.println("Failed to fetch departure date. Please try again.");
            // Optionally, you can show an error message to the user
        }
    }
    
    private LocalDate fetchDepartureDateFromDatabase(int flightId) {
        LocalDate departureDate = null;
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Prepare SQL statement to fetch departure date
            String sql = "SELECT departure_date FROM flight_info WHERE flight_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, flightId);

            // Execute the query
            ResultSet resultSet = statement.executeQuery();

            // Check if the result set has a row
            if (resultSet.next()) {
                // Fetch departure date from the result set
                departureDate = resultSet.getDate("departure_date").toLocalDate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return departureDate;
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

    private void updateFlights(String departureCity, String arrivalCity) {
        // Simulating fetching flights for the selected cities
        List<Flight> flights = new ArrayList<>();
        // You can add more flights with detailed information here
        // Add flights from departure city to arrival city if One Way is selected
        if (selectedTripType.equals("One Way")) {
            for (Flight flight : flightMap.getOrDefault(departureCity, new ArrayList<>())) {
                if (flight.getArrivalCity().equals(arrivalCity)) {
                    flights.add(flight);
                }
            }
            if (departureCity.equals("Japan (ID: 1004")) {
                flights.add(new Flight("Japan to United Kingdom Flight", "Air India", "5:30 AM", "21kg", "No cigarette or any sharp weapons"));
            }

            if (arrivalCity.equals("United Kingdom (ID: 1016)")) {
                flights.add(new Flight("Japan to United Kingdom Flight", "Air India", "4:00 PM", "21kg", "No cigarette or any sharp weapons"));

            }
        }

        // Add flights from arrival city to departure city if Round Trip is selected
        if (selectedTripType.equals("Round Trip")) {
            for (Flight flight : flightMap.getOrDefault(arrivalCity, new ArrayList<>())) {
                if (flight.getArrivalCity().equals(departureCity)) {
                    flights.add(flight);
                }
            }
            if (departureCity.equals("India (ID: 1017)")) {
                flights.add(new Flight("India to Canada Flight", "Air India", "5:30 AM", "21kg", "No cigarette or any sharp weapons"));

            }

            if (arrivalCity.equals("Canada (ID: 1002)")) {
                flights.add(new Flight("Canada to India Flight", "Air India", "4:00 PM", "21kg", "No cigarette or any sharp weapons"));

            }

        }

        flightMap.put(departureCity, flights);
    }

    private void refreshFlightDetailsAccordion(Accordion accordion, String arrivalCity) {
        // Clear existing panes
        accordion.getPanes().clear();

        

        // Add updated flight details
        List<Flight> flights = flightMap.getOrDefault(arrivalCity, new ArrayList<>());
        for (Flight flight : flights) {
            GridPane flightGrid = new GridPane();
            flightGrid.setHgap(10);
            flightGrid.setVgap(10);
            flightGrid.setPrefWidth(900); // Set width to 900 pixels
            flightGrid.add(new Label("Flight Name:"), 0, 0);
            flightGrid.add(new Label(flight.getName()), 1, 0);
            flightGrid.add(new Label("Airline:"), 0, 1);
            flightGrid.add(new Label(flight.getAirline()), 1, 1);
            flightGrid.add(new Label("Departure Time:"), 0, 2);
            flightGrid.add(new Label(flight.getDepartureTime()), 1, 2);
            flightGrid.add(new Label("Luggage:"), 0, 3);
            flightGrid.add(new Label(flight.getLuggage()), 1, 3);
            flightGrid.add(new Label("Restrictions:"), 0, 4);
            flightGrid.add(new Label(flight.getRestrictions()), 1, 4);
            Button bookFlightButton = new Button("BOOK NOW");
            bookFlightButton.setOnAction(event -> bookFlight(flight.getName(), flightId, arrivalCity));
            flightGrid.add(bookFlightButton, 0, 5, 2, 1);

            TitledPane flightPane = new TitledPane(flight.getName() + " Details", flightGrid);
            accordion.getPanes().add(flightPane);
        }
    }
    
    private static class Flight {
        private final String name;
        private final String airline;
        private final String departureTime;
        private final String luggage;
        private final String restrictions;

        public Flight(String name, String airline, String departureTime, String luggage, String restrictions) {
            this.name = name;
            this.airline = airline;
            this.departureTime = departureTime;
            this.luggage = luggage;
            this.restrictions = restrictions;
        }

        public String getArrivalCity() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getArrivalCity'");
        }

        public String getName() {
            return name;
        }

        public String getAirline() {
            return airline;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public String getLuggage() {
            return luggage;
        }

        public String getRestrictions() {
            return restrictions;
        }
    }
}
