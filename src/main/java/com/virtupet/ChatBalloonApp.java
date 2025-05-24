package com.virtupet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ChatBalloonApp extends Application {
    private VBox chatContainer;
    private TextField inputField;
    private GeminiClient geminiClient;
    private ReminderScheduler reminderSystem;
    private final Map<String, ReminderType> presetReminders = Map.of(
            "morning", new ReminderType(LocalTime.of(8, 0), "Exercise", "cmd:start stretch_alert.mp3"),
            "lunch", new ReminderType(LocalTime.of(12, 30), "Lunch", "gemini:generate_meal_reminder")
    );

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        geminiClient = new GeminiClient(System.getenv("GEMINI_API_KEY"));

        // Main UI Container
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-container");

        // Chat History
        chatContainer = new VBox(8);
        chatContainer.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(chatContainer);
        scrollPane.setFitToWidth(true);

        // Input Area Setup
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #404040;");

        inputField = new TextField();
        inputField.setPromptText("Type commands to your pet...");
        inputField.getStyleClass().add("command-input");

        Button sendButton = new Button("▶");
        sendButton.getStyleClass().add("send-button");
        sendButton.setOnAction(e -> sendMessage());

        // Layout Assembly
        inputBox.getChildren().addAll(inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        root.setCenter(scrollPane);
        root.setBottom(inputBox);

        // Stage Configuration
        stage.setTitle("VirtuPet - AI Companion");
        stage.setScene(new Scene(root, 400, 600));
        stage.getScene().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());        stage.show();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            addMessage(message, true);
            inputField.clear();

            new Thread(() -> {
                try {
                    String rawResponse = geminiClient.sendPrompt(message);
                    String[] parsed = parseResponse(rawResponse);

                    Platform.runLater(() -> {
                        addMessage(parsed[0], false); // AI response
                        if (!parsed[1].isEmpty()) {
                            addCommandExecution(parsed[1]); // System command
                        }
                    });
                } catch (RuntimeException e) { // Catch specific exceptions
                    Platform.runLater(() ->
                            addMessage("Error: " + e.getMessage(), false));
                } catch (Exception e) {
                    Platform.runLater(() ->
                            addMessage("Critical Error: " + e.getMessage(), false));
                }
            }).start();
        }
    }

    private String[] parseResponse(String response) {
        // Format: [SAFE]Message[CMD]command
        String[] parts = new String[2];
        int safeIndex = response.indexOf("[SAFE]");
        int cmdIndex = response.indexOf("[CMD]");

        if (safeIndex != -1 && cmdIndex != -1) {
            parts[0] = response.substring(safeIndex + 6, cmdIndex).trim();
            parts[1] = response.substring(cmdIndex + 5).trim();
        } else {
            parts[0] = response;
        }
        return parts;
    }

    private void addMessage(String text, boolean isUser) {
        HBox container = new HBox();
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label message = new Label(text);
        message.setWrapText(true);  // Correct method name
        message.setMaxWidth(300.0); // Must be double

        // Correct style class addition
        message.getStyleClass().add(isUser ? "chat-bubble-user" : "chat-bubble-bot");

        container.getChildren().add(message); // Label IS a Node subclass
        chatContainer.getChildren().add(container);
    }

    private void addCommandExecution(String command) {
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);

        Label cmdLabel = new Label("Executing: " + command);
        cmdLabel.getStyleClass().add("command-execution");

        container.getChildren().add(cmdLabel);
        chatContainer.getChildren().add(container);

        // Execute sanitized command
        new SafeCommandExecutor().execute(command);
    }

     private void addSystemAlert(String message) { // Remove 'static'
        HBox container = new HBox();
        Label alert = new Label("⚠️ " + message);
        alert.getStyleClass().add("system-alert");
        container.getChildren().add(alert);
        chatContainer.getChildren().add(container); // Now accesses instance field
    }

    private void initializePresetReminders() {
        presetReminders.forEach((id, reminder) -> {
            reminderSystem.scheduleDailyReminder(id, reminder.time(), () -> {
                String response = null;
                try {
                    response = geminiClient.generateReminder(reminder.message());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String command = parseResponse(response)[1];
                String finalResponse = response;
                Platform.runLater(() -> {
                    addMessage(finalResponse, false);
                    if (!command.isEmpty()) new SafeCommandExecutor().execute(command);
                });
            });
        });
    }
    private class SafeCommandExecutor {
        private final Set<String> ALLOWED_CMDS = Set.of(  // Removed 'static'
                "echo", "timeout", "start", "explorer"
        );

        public void execute(String command) {  // Non-static
            if (!isAllowed(command)) {
                Platform.runLater(() ->
                        addSystemAlert("Blocked: " + command));
                return;
            }

            try {
                new ProcessBuilder("cmd", "/c", command)
                        .redirectErrorStream(true)
                        .start();
            } catch (IOException e) {
                Platform.runLater(() ->
                        addSystemAlert("Failed: " + e.getMessage()));
            }
        }

        private boolean isAllowed(String cmd) {  // Non-static
            return ALLOWED_CMDS.stream().anyMatch(cmd::contains);
        }
    }
}




