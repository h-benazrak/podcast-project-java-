package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class PodcastSchedulerApp extends Application {

    private EpisodeRepository repo = new EpisodeRepository();
    private ListView<Episode> listView = new ListView<>();

    @Override
    public void start(Stage stage) {

        TextField titleField = new TextField();
        titleField.setPromptText("Episode title");

        TextField durationField = new TextField();
        durationField.setPromptText("Duration (minutes)");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Regular", "Bonus");
        typeBox.setValue("Regular");

        DatePicker datePicker = new DatePicker();

        TextField timeField = new TextField();
        timeField.setPromptText("HH:MM");

        // Buttons
        Button createBtn = new Button("Create & Schedule");
        Button publishBtn = new Button("Publish Selected");
        Button saveBtn = new Button("Save Episodes");

        //  Button actions
        createBtn.setOnAction(e -> {
            try {

                Episode ep = repo.createEpisode(
                        typeBox.getValue(),
                        titleField.getText(),
                        Integer.parseInt(durationField.getText())
                );

                LocalDateTime dt = LocalDateTime.of(
                        datePicker.getValue(),
                        LocalTime.parse(timeField.getText())
                );


                repo.scheduleEpisode(ep, dt);
                listView.getItems().setAll(repo.getEpisodes());

                titleField.clear();
                durationField.clear();
                timeField.clear();

            } catch (Exception ex) {
                showAlert("Error", "Invalid input or schedule conflict");
            }
        });

        publishBtn.setOnAction(e -> {

            Episode ep = listView.getSelectionModel().getSelectedItem();

            // If no episode is selected
            if (ep == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Episode Selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select an episode to publish.");
                alert.showAndWait();
                return;
            }

            // If episode can be published
            if (ep.canPublish(LocalDateTime.now())) {
                ep.publish(LocalDateTime.now());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Published");
                alert.setHeaderText(null);
                alert.setContentText("Episode published successfully!");
                alert.showAndWait();

                listView.refresh();
            }
            // If episode cannot be published yet
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Cannot Publish");
                alert.setHeaderText(null);
                alert.setContentText(
                        "This episode cannot be published yet.\n" +
                                "Please wait until the scheduled time."
                );
                alert.showAndWait();
            }
        });


        saveBtn.setOnAction(e -> {

            Episode ep = listView.getSelectionModel().getSelectedItem();

            // If no episode is selected
            if (ep == null) {
                showAlert("No Selection", "Please select an episode before saving.");
                return;
            }

            try {
                repo.saveToFile();
                showAlert("Saved", "Episodes saved successfully");
            } catch (EpisodePersistenceException ex) {
                showAlert("Error", ex.getMessage());
            }
        });


        //  Layout
        VBox inputs = new VBox(10,
                titleField,
                durationField,
                typeBox,
                datePicker,
                timeField,
                createBtn,
                publishBtn,
                saveBtn
        );
        inputs.setPadding(new Insets(10));

        // Colors
        createBtn.setStyle("-fx-background-color: #814caf; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        publishBtn.setStyle("-fx-background-color: #7f21f3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        saveBtn.setStyle("-fx-background-color: #5e00ff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

        BorderPane root = new BorderPane();
        root.setLeft(inputs);
        root.setCenter(listView);

        stage.setTitle("Podcast Episode Scheduler & Publisher");
        stage.setScene(new Scene(root, 800, 450));
        stage.show();


        try {
            repo.loadFromFile();
            listView.getItems().setAll(repo.getEpisodes());
        } catch (EpisodePersistenceException ignored) {}
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
