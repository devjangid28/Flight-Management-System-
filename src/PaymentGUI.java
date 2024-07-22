import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Callback;
import javafx.util.Duration;
import java.time.LocalDate;

public class PaymentGUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Flight Payment");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Card Number
        Label cardNumberLabel = new Label("Card Number:");
        GridPane.setConstraints(cardNumberLabel, 0, 0);
        TextField cardNumberInput = new TextField();
        cardNumberInput.setPromptText("Enter your card number");
        cardNumberInput.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("\\d{0,16}")) ? change : null));
        GridPane.setConstraints(cardNumberInput, 1, 0);

        // Expiry Date
        Label expiryDateLabel = new Label("Expiry Date:");
        GridPane.setConstraints(expiryDateLabel, 0, 1);
        DatePicker expiryDatePicker = new DatePicker(); // Use DatePicker instead of TextField
        expiryDatePicker.setValue(LocalDate.now().plusDays(1)); // Set minimum selectable date to tomorrow
        expiryDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now())); // Disable past dates
            }
        });
        GridPane.setConstraints(expiryDatePicker, 1, 1);

        // CVV
        Label cvvLabel = new Label("CVV:");
        GridPane.setConstraints(cvvLabel, 0, 2);
        TextField cvvInput = new TextField();
        cvvInput.setPromptText("CVV");
        cvvInput.setTextFormatter(new TextFormatter<>(change ->
                (change.getControlNewText().matches("\\d{0,3}")) ? change : null));
        GridPane.setConstraints(cvvInput, 1, 2);

        // Cardholder Name
        Label cardholderNameLabel = new Label("Cardholder Name:");
        GridPane.setConstraints(cardholderNameLabel, 0, 3);
        TextField cardholderNameInput = new TextField();
        cardholderNameInput.setPromptText("Enter your name");
        GridPane.setConstraints(cardholderNameInput, 1, 3);

        // Payment Button
        Button payButton = new Button("Pay Now");
        GridPane.setConstraints(payButton, 1, 4);

        // Add everything to grid
        grid.getChildren().addAll(cardNumberLabel, cardNumberInput, expiryDateLabel, expiryDatePicker,
                cvvLabel, cvvInput, cardholderNameLabel, cardholderNameInput, payButton);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Set action for Pay Now button
        payButton.setOnAction(e -> {
            // Show loading GIF
            Stage loadingStage = new Stage();
            loadingStage.setTitle("Processing Payment");
            ImageView imageView = new ImageView();
            try {
                Image gifImage = new Image("file:///C:/Users/asus_/OneDrive/Desktop/project/13.gif");
                imageView.setImage(gifImage);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load GIF", "An error occurred while loading the GIF: " + ex.getMessage());
                return; // Exit method if loading the image fails
            }
            loadingStage.setScene(new Scene(new Group(imageView)));
            loadingStage.show();

            // Simulate payment process
            simulatePayment(() -> {
                // Hide loading stage after payment completion
                loadingStage.close();
                // Show payment success message
                showAlert(Alert.AlertType.INFORMATION, "Payment Success", "Payment Successful", "Your payment was successful!");
            });
        });
    }

    // Simulate payment process
    private void simulatePayment(Runnable onComplete) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(2), ae -> onComplete.run())
        );
        timeline.play();
    }

    // Method to show alert
    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
