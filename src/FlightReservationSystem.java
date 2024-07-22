import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Application;
import javafx.stage.Stage;
import com.twilio.Twilio;
import javafx.scene.text.Text;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Random;

public class FlightReservationSystem extends Application {

    private boolean otpSent = false; // Flag to track if OTP has been sent
    private Button sendOTPButton; // Button to send OTP

    // Twilio credentials
    public static final String ACCOUNT_SID = "AC46b2ca8d9b1c958ebdf266bc201d71c6";
    public static final String AUTH_TOKEN = "12d4b50835fd2f3763afb46082250665";
    public static final String TWILIO_PHONE_NUMBER = "+16572207734";


    // MySQL database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/flight";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "devjangid@28";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FLIGHT RESERVATION SYSTEM");
        

        // Root layout
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        // Logo 1 (right)
        ImageView logo1 = new ImageView(new Image("file:///Users/asus_/OneDrive/Desktop/project/logo1.jpeg")); // Adjust path accordingly
        logo1.setFitWidth(200);
        logo1.setFitHeight(150);
        logo1.setTranslateX(80); // Adjust x-coordinate as needed
        logo1.setTranslateY(20);   // Adjust y-coordinate as needed

        // Logo 2 (right)
        ImageView additionalImage = new ImageView(new Image("file:///Users/asus_/OneDrive/Desktop/project/1.jpeg")); // Adjust path accordingly
        additionalImage.setFitWidth(1500);
        additionalImage.setFitHeight(150);
        additionalImage.setTranslateX(300); // Adjust x-coordinate as needed
        additionalImage.setTranslateY(20);   // Adjust y-coordinate as needed

        root.getChildren().addAll(logo1, additionalImage);

       // Login button
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: black; -fx-text-fill: gold;");
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Set font to bold and size to 20
        loginButton.setAlignment(Pos.CENTER);
        loginButton.setTranslateX(-560); // Adjust x-coordinate as needed
        loginButton.setTranslateY(72); // Adjust y-coordinate as needed
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        loginButton.setOnAction(e -> showLoginDialog(primaryStage)); // Show login dialog upon button click

        // Sign Up button
        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: black; -fx-text-fill: gold;");
        signUpButton.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Set font to bold and size to 20
        signUpButton.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Set font to bold and size to 20
        signUpButton.setAlignment(Pos.CENTER);
        signUpButton.setTranslateX(-260); // Adjust x-coordinate as needed
        signUpButton.setTranslateY(72);
        signUpButton.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        signUpButton.setOnAction(e -> showSignUpDialog(primaryStage)); // Show sign up dialog upon button click
        

       // Add buttons to the HBox
        HBox buttonsBox = new HBox(20); // Horizontal box to contain the buttons with spacing of 20
        buttonsBox.setAlignment(Pos.CENTER_RIGHT); // Align buttons to the right
        buttonsBox.getChildren().addAll(signUpButton, loginButton); // Swap the order of buttons

        Text welcomeText = new Text("Welcome To Foreign Flights");
        welcomeText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        welcomeText.setFill(Color.WHITE);
        welcomeText.setTranslateX(-180); // Adjust x-coordinate as needed
        welcomeText.setTranslateY(135); // Adjust y-coordinate as needed

        // Create the "OR" text
        Text orText = new Text("OR");
        orText.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        orText.setFill(Color.WHITE);
        orText.setTranslateX(240);
        orText.setTranslateY(18);

        // Add the "OR" text to the center panel
        VBox centerPanel = new VBox();
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setSpacing(20);
        centerPanel.getChildren().addAll(welcomeText, buttonsBox, orText); // Add the "OR" text

        root.setCenter(centerPanel);

        
        
        // Image
        ImageView imageView = new ImageView(new Image("file:///Users/asus_/OneDrive/Desktop/project/2.jpeg")); // Adjust path accordingly
        imageView.setFitWidth(1550);
        imageView.setFitHeight(625);
        BorderPane.setAlignment(imageView, Pos.CENTER); // Centering the image
        root.setBottom(imageView);

        Scene scene = new Scene(root, 1550, 850);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

            // Variable to store the temporarily generated OTP
    private String generatedOTP;
    private String address;

    // Method to send OTP to the provided mobile number using Twilio
    private void sendOTPWithTwilio(String country, String mobileNumber, String otp, Button sendOTPButton) {
        // Check if the mobile number exists in the database
        if (isMobileNumberRegistered(mobileNumber)) {
            // Mobile number is registered, proceed to send OTP
            try {
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                Message message = Message.creator(
                        new PhoneNumber("+" + country + mobileNumber),
                        new PhoneNumber(TWILIO_PHONE_NUMBER),
                        "Your OTP for flight reservation: " + otp)
                        .create();
                System.out.println("OTP sent successfully: " + message.getSid());
                otpSent = true; // Set the flag to true after sending OTP
                sendOTPButton.setDisable(true); // Disable the button after OTP is sent
                // Store the generated OTP temporarily
                generatedOTP = otp;

                // Create a timeline to re-enable the button after 2 minutes
                Timeline timeline = new Timeline(new KeyFrame(javafx.util.Duration.minutes(2), event -> {
                    sendOTPButton.setDisable(false); // Re-enable the button after 2 minutes
                }));
                timeline.play();
            } catch (Exception e) {
                System.out.println("Error sending OTP: " + e.getMessage());
            }
        } else {
            showAlert("Error", "The provided mobile number is not registered. Please sign up first.");
        }
    }

    // Method to save login details to the database
    private void saveLoginDetailsToDatabase(String mobileNumber, String otp, String type) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO login (`OTP/Password`, `Mobile_No/EmailID`, `Type`) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, otp);
            statement.setString(2, mobileNumber);
            statement.setString(3, type);
            statement.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error saving login details to database: " + e.getMessage());
        }
    }

    // Method to check if the mobile number is registered in the database
    private boolean isMobileNumberRegistered(String mobileNumber) {
        boolean isRegistered = false;
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT * FROM signup WHERE mobile_no = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, mobileNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                isRegistered = true;
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Error checking mobile number registration: " + e.getMessage());
        }
        return isRegistered;
    }

    // Method to check if the mobile number contains repeating digits
    private boolean hasRepeatingDigits(String mobileNumber) {
        // Remove non-digit characters and check for repeating digits
        String digitsOnly = mobileNumber.replaceAll("\\D", "");
        return digitsOnly.matches("(\\d)\\1{9}"); // Returns true if 10 consecutive digits are the same
    }

    // Method to check if the mobile number contains sequential digits
    private boolean hasSequentialDigits(String mobileNumber) {
        // Remove non-digit characters
        String digitsOnly = mobileNumber.replaceAll("\\D", "");
        // Check for sequential digits
        for (int i = 0; i < digitsOnly.length() - 1; i++) {
            if (digitsOnly.charAt(i) == digitsOnly.charAt(i + 1) - 1) {
                return true;
            }
        }
        return false;
    }



    // Method to save OTP to the database
    private void saveOTPToDatabase(String mobileNumber, String otp) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO login (`Mobile_No/EmailID`, `OTP/Password`) VALUES (?, ?)"; // Modified SQL query
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, mobileNumber);
            statement.setString(2, otp);
            statement.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error saving OTP to database: " + e.getMessage());
        }
    }

        // Method to show signup dialog
private void showSignUpDialog(Stage primaryStage) {
    // Main layout
    BorderPane borderPane = new BorderPane();
    borderPane.setPadding(new Insets(50));

    // Left panel with branding
    VBox leftPanel = new VBox(20);
    leftPanel.setAlignment(Pos.CENTER);
    leftPanel.setStyle("-fx-background-color: #009688;");

    // Placeholder for company logo
    ImageView logoPlaceholder = new ImageView();
    logoPlaceholder.setFitWidth(200);
    logoPlaceholder.setFitHeight(200);
    // Set the image for the logoPlaceholder (replace placeholder URL with your image URL)
    logoPlaceholder.setImage(new Image("file:///Users/asus_/OneDrive/Desktop/project/logo1.jpeg"));

    Text companyName = new Text("Foreign Flights");
    companyName.setFont(Font.font("Arial", 24));
    companyName.setFill(Color.WHITE);

    Text copyright = new Text("© 2024 Foregin Flights");
    copyright.setFont(Font.font("Arial", 15));
    copyright.setFill(Color.WHITE);

    leftPanel.getChildren().addAll(logoPlaceholder, companyName, copyright);

    // Right panel with sign-up form
    VBox rightPanel = new VBox(10);
    rightPanel.setAlignment(Pos.CENTER);
    rightPanel.setStyle("-fx-background-color: #FFFFFF;");

    Text signUpTitle = new Text("SIGN UP");
    signUpTitle.setFont(Font.font("Arial", 20));

    TextField fullNameField = new TextField();
    fullNameField.setPromptText("Full name");
    fullNameField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("^[a-zA-Z\\s]*$")) { // Allow alphabets and spaces
            fullNameField.setText(oldValue);
        }
    });

    TextField addressField = new TextField(); // Address field
    addressField.setPromptText("Address");

    TextField mobilenoField = new TextField();
    mobilenoField.setPromptText("Mobile Number");
    mobilenoField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("\\d{0,10}")) { // Limit to maximum 10 digits
            mobilenoField.setText(oldValue);
        }
    });

    DatePicker dobPicker = new DatePicker();
    dobPicker.setPromptText("Date of Birth");

    dobPicker.setOnAction(event -> {
        // Validate date of birth
        LocalDate selectedDate = dobPicker.getValue();
        if (selectedDate != null && selectedDate.isAfter(LocalDate.now().minusYears(21))) {
            showAlert("Error", "Minimum age to register is 21 years. Please select a valid date of birth.");
            dobPicker.setValue(null);
        } else if (selectedDate != null) {
            String dobString = selectedDate.toString();
            // Proceed with further actions if needed
        } else {
            // Handle the case where no date is selected
            System.out.println("No date selected");
        }
    });

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Password");

    TextField cityField = new TextField();
    cityField.setPromptText("City");
    cityField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue.matches("^[a-zA-Z]*$")) {
            cityField.setText(oldValue);
        }
    });

    TextField emailField = new TextField();
    emailField.setPromptText("Email ID");

    // Generate random registration ID
    Random random = new Random();
    int registrationID = random.nextInt(9999);
    TextField registeridField = new TextField(Integer.toString(registrationID)); // Set the registration ID
    registeridField.setDisable(true); // Disable the text field

    // Generate random passenger ID
    int passengerID = random.nextInt(10000);
    TextField passengerIdField = new TextField("Passenger ID: " + Integer.toString(passengerID)); // Set the passenger ID
    passengerIdField.setDisable(true); // Disable the text field

    Button signUpButton = new Button("Sign Up");
    signUpButton.setOnAction(e -> {
        extracted(primaryStage, fullNameField, addressField, mobilenoField, dobPicker, passwordField, cityField, emailField, random, registrationID);
    });

    Hyperlink loginLink = new Hyperlink("Already have an account? Log in");
    loginLink.setTextFill(Color.BLUE);
    loginLink.setOnAction(e -> {
        showLoginDialog(primaryStage); // Show login dialog when the login link is clicked
    });

    rightPanel.getChildren().addAll(signUpTitle, fullNameField, registeridField, mobilenoField, dobPicker, cityField, emailField, passwordField, signUpButton, loginLink);

    // Add panels to the main layout
    HBox centerBox = new HBox(leftPanel, rightPanel);
    centerBox.setAlignment(Pos.CENTER);
    borderPane.setCenter(centerBox);

    // Scene and stage setup
    Scene scene = new Scene(borderPane, 600, 400); // Changed to make the window square
    primaryStage.setTitle("Sign Up Interface");
    primaryStage.setScene(scene);
    primaryStage.show();

    // Center the sign-up dialog on the screen
    primaryStage.centerOnScreen();
}

private void extracted(Stage primaryStage, TextField fullNameField, TextField addressField, TextField mobilenoField,
        DatePicker dobPicker, PasswordField passwordField, TextField cityField, TextField emailField, Random random,
        int registrationID) {
    String fullName = fullNameField.getText();
    String address = addressField.getText(); // Get address value
    String mobileNumber = mobilenoField.getText();
    LocalDate dob = dobPicker.getValue(); // Get selected date from DatePicker
    String city = cityField.getText();
    String email = emailField.getText();
    String password = passwordField.getText();

    // Check if any field is empty
    if (fullName.isEmpty() ||  mobileNumber.isEmpty() || dob == null || city.isEmpty() || email.isEmpty() || password.isEmpty()) {
        showAlert("Error", "All fields are required.");
    } else {
        // Generate random passenger ID
        int passengerID = random.nextInt(10000);

        // Call method to save user details to the database
        saveUserToDatabase(fullName, Integer.toString(registrationID), mobileNumber, dob.toString(), city, email, password, Integer.toString(passengerID), address); // Pass address here

        // After signup, show the login dialog
        showLoginDialog(primaryStage);
    }
}

private void saveUserToDatabase(String fullName, String registerID, String mobileNumber, String dob, String city, String email, String password, String passengerID, String address) {
    try {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        // Insert user details into the signup table
        String sql = "INSERT INTO signup (register_id, Name, password, mobile_no, date_of_birth, cid_city, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, registerID); // Use the provided register ID
        statement.setString(2, fullName);
        statement.setString(3, password);
        statement.setString(4, mobileNumber);
        statement.setString(5, dob);
        statement.setString(6, city);
        statement.setString(7, email);
        statement.executeUpdate();

       
        conn.close();
        showAlert("Sign Up Successful", "You have successfully signed up!");
    } catch (Exception e) {
        System.out.println("Error saving user to database: " + e.getMessage());
        showAlert("Error", "Failed to sign up. Please try again.");
    }
}

   


    // Method to show login dialog
private void showLoginDialog(Stage primaryStage) {
    // Main layout
    BorderPane mainLayout = new BorderPane();
    mainLayout.setPadding(new Insets(20));

    // Left panel with branding
    VBox leftPanel = new VBox();
    leftPanel.setStyle("-fx-background-color: #009688;");
    leftPanel.setAlignment(Pos.CENTER);
    leftPanel.setPadding(new Insets(20));
    leftPanel.setSpacing(20);

    // Company logo
    Image companyLogo = new Image("file:///Users/asus_/OneDrive/Desktop/project/logo1.jpeg"); // replace "logo.png" with your image file path
    ImageView logoView = new ImageView(companyLogo);
    logoView.setFitHeight(200);
    logoView.setFitWidth(200);

    // Copyright
    Label copyright = new Label("© 2024 Foregin Flights");
    copyright.setFont(Font.font("Arial", 15));
    copyright.setTextFill(Color.WHITE);

    leftPanel.getChildren().addAll(logoView, new Label(), copyright);

    // Right panel with login form
    VBox rightPanel = new VBox();
    rightPanel.setStyle("-fx-background-color: #FFFFFF;");
    rightPanel.setAlignment(Pos.CENTER);
    rightPanel.setPadding(new Insets(20));
    rightPanel.setSpacing(10);

    Label loginTitle = new Label("LOGIN");
    loginTitle.setFont(Font.font("Arial", 20));

    TextField loginField = new TextField();
    loginField.setPromptText("Mobile Number");

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Password");

    // Country code ComboBox
    ComboBox<String> countryCodeComboBox = new ComboBox<>();
    countryCodeComboBox.getItems().addAll("+1", "+91", "+44", "+61"); // Add your desired country codes
    countryCodeComboBox.getSelectionModel().selectFirst(); // Select the first country code by default
    HBox countryCodeBox = new HBox();
    countryCodeBox.setAlignment(Pos.CENTER_LEFT);
    countryCodeBox.setSpacing(5);
    countryCodeBox.getChildren().add(countryCodeComboBox);
    countryCodeBox.setVisible(true); // Initially shown

    // Radio buttons for login options
    RadioButton mobileNumberOption = new RadioButton("Login with Mobile Number");
    RadioButton emailOption = new RadioButton("Login with Email");
    ToggleGroup loginOptions = new ToggleGroup(); // Group radio buttons
    mobileNumberOption.setToggleGroup(loginOptions);
    emailOption.setToggleGroup(loginOptions);
    mobileNumberOption.setSelected(true); // Default selection
    HBox radioButtons = new HBox(10);
    radioButtons.getChildren().addAll(mobileNumberOption, emailOption);

    // Show/hide country code box based on selection
    emailOption.setOnAction(e -> {
        countryCodeBox.setVisible(false);
        loginField.setPromptText("Email");
        sendOTPButton.setVisible(false); // Hide OTP button for email login
        // Clear fields when switching to email login
        loginField.clear();
        passwordField.clear();
    });
    mobileNumberOption.setOnAction(e -> {
        countryCodeBox.setVisible(true);
        loginField.setPromptText("Mobile Number");
        sendOTPButton.setVisible(true); // Show OTP button for mobile number login
        // Clear fields when switching to mobile number login
        loginField.clear();
        passwordField.clear();
    });

    Button loginButton = new Button("Login");
    loginButton.setStyle("-fx-background-color: black; -fx-text-fill: gold;");
    loginButton.setOnAction(e -> {
        String username = loginField.getText();
        String password = passwordField.getText();

        // Depending on the selected login option, handle login accordingly
        if (mobileNumberOption.isSelected()) {
            String countryCode = countryCodeComboBox.getSelectionModel().getSelectedItem();
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please enter Mobile Number and OTP.");
            } else {
                loginWithMobileNumber(countryCode, username, password);
            }
        } else {
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please enter Email and Password.");
            } else {
                loginWithEmail(username, password);
            }
        }
    });

    // Send OTP button
    sendOTPButton = new Button("Send OTP");
    sendOTPButton.setOnAction(e -> {
        String countryCode = countryCodeComboBox.getSelectionModel().getSelectedItem();
        String mobileNumber = loginField.getText();
        if (!mobileNumber.isEmpty()) {
            // Generate and send OTP
            String otp = generateOTP();
            sendOTPWithTwilio(countryCode, mobileNumber, otp, sendOTPButton);
        } else {
            showAlert("Error", "Please enter Mobile Number.");
        }
    });

    Button signUpLink = new Button("Don't Have An Account? Sign Up");
    signUpLink.setTextFill(Color.BLUE);
    signUpLink.setBorder(Border.EMPTY);
    signUpLink.setBackground(Background.EMPTY);
    signUpLink.setOnAction(e -> showSignUpDialog(primaryStage));

    

    rightPanel.getChildren().addAll(loginTitle, radioButtons, countryCodeBox, loginField, passwordField, loginButton, sendOTPButton, signUpLink);

    // Center panel with login and branding
    HBox centerPanel = new HBox(leftPanel, rightPanel);
    centerPanel.setAlignment(Pos.CENTER);
    mainLayout.setCenter(centerPanel);

    // Scene setup
    Scene loginScene = new Scene(mainLayout, 600, 400);
    primaryStage.setScene(loginScene);
    primaryStage.setTitle("Login");
    primaryStage.show();

    // Center the login dialog on the screen
    primaryStage.centerOnScreen();
}



        // Method to handle login with mobile number and OTP verification
    private void loginWithMobileNumber(String countryCode, String mobileNumber, String OTP) {
        // Directly verify the entered OTP with the temporarily stored OTP
        if (generatedOTP != null && OTP.equals(generatedOTP)) {
            // OTP is correct, proceed with login
            System.out.println("Logging in with mobile number: " + countryCode + mobileNumber);
            System.out.println("OTP: " + OTP);
            openFlightBookingGUI(); // Open FlightBookingGUI upon successful login
        } else {
            // OTP is incorrect, show error message and prevent login
            showAlert("Error", "Incorrect OTP. Please enter the correct OTP.");
        }
    }

            // Method to handle login with email
    private void loginWithEmail(String email, String password) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "SELECT * FROM signup WHERE email = ? AND password = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // Insert email, password, and login type into the 'login' table
                saveLoginDetails(email, password, "email");
                showAlert("Login Successful", "Welcome back, " + email + "!");
                // Proceed to the next screen or action upon successful login
                openFlightBookingGUI();
            } else {
                showAlert("Login Failed", "Incorrect email or password. Please try again.");
            }
            conn.close();
        } catch (Exception e) {
            System.out.println("Error logging in with email: " + e.getMessage());
            showAlert("Error", "An error occurred while logging in. Please try again later.");
        }
    }



    // Method to save login details to the database
    private void saveLoginDetails(String email, String password, String loginType) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String sql = "INSERT INTO login (`OTP/Password`, `Mobile_No/EmailID`, `Type`) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, password); // Note: Password is stored in OTP/Password column
            statement.setString(2, email);    // Note: Email is stored in Mobile_No/EmailID column
            statement.setString(3, loginType);
            statement.executeUpdate();
            conn.close();
        } catch (Exception e) {
            System.out.println("Error saving login details to database: " + e.getMessage());
        }
    }



    // Method to generate a random OTP
    private String generateOTP() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    // Method to show an alert dialog
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    

     // Method to open the FlightBookingGUI window
    private void openFlightBookingGUI() {
        // Code to open the FlightBookingGUI window goes here
        FlightBookingGUI flightBookingGUI = new FlightBookingGUI();
        Stage stage = new Stage();
        stage.setFullScreen(true); // Open in full screen
        flightBookingGUI.start(stage); // Assuming FlightBookingGUI extends Application and has a start() method
    }


    public static void main(String[] args) {
        launch(args);
    }
}
