package com.virtupet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class ChatBalloonApp extends Application {

    private VBox chatContainer;
    private TextField inputField;
    private Button sendButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Main container - keeping your StackPane approach but adding chat functionality
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #f0f0f0;");

        // Chat area setup
        chatContainer = new VBox(10);
        chatContainer.setStyle("-fx-padding: 10;");

        ScrollPane scrollPane = new ScrollPane(chatContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Input area - similar to your circle setup but for chat
        HBox inputBox = new HBox(10);
        inputBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        inputField = new TextField();
        inputField.setPromptText("Type your message here...");
        inputField.setStyle("-fx-background-radius: 15; -fx-pref-height: 30;");

        sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-radius: 15; -fx-pref-height: 30; -fx-background-color: #4CAF50; -fx-text-fill: white;");

        inputBox.getChildren().addAll(inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // Layout - using BorderPane instead of StackPane for better chat layout
        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(scrollPane);
        mainPane.setBottom(inputBox);

        // Add a circle that can be clicked to show it's active (keeping your original concept)
        Circle activeIndicator = new Circle(10, Color.GREEN);
        activeIndicator.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);");

        root.getChildren().addAll(mainPane, activeIndicator);
        StackPane.setAlignment(activeIndicator, Pos.TOP_RIGHT);
        StackPane.setMargin(activeIndicator, new Insets(10));

        // Event handlers
        sendButton.setOnAction(e -> sendMessage());
        activeIndicator.setOnMouseClicked(e -> {
            activeIndicator.setFill(activeIndicator.getFill() == Color.GREEN ? Color.RED : Color.GREEN);
        });

        Scene scene = new Scene(root, 400, 600);
        primaryStage.setTitle("ChatBalloon App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            addMessage(message, true);
            inputField.clear();

            new Thread(() -> {
                try {
                    GeminiClient client = new GeminiClient();
                    String response = client.enviarMensagem(message);
                    String cleanResponse = client.parseResponse(response);

                    Platform.runLater(() -> addMessage(cleanResponse, false));
                } catch (Exception e) {
                    Platform.runLater(() -> addMessage("Erro: " + e.getMessage(), false));
                    }
                }).start();
            }
        }

    private void addMessage(String text, boolean isUser) {
        HBox messageBubble = new HBox();
        messageBubble.setStyle(
                "-fx-background-color: " + (isUser ? "#DCF8C6" : "#FFFFFF") + ";" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 10;" +
                        "-fx-spacing: 5;" +
                        "-fx-alignment: " + (isUser ? "center-right" : "center-left") + ";"
        );

        // Add arrow/triangle pointer
        StackPane pointer = new StackPane();
        pointer.setStyle(
                "-fx-background-color: " + (isUser ? "#DCF8C6" : "#FFFFFF") + ";" +
                        "-fx-shape: \"M0 0 L10 0 L10 10 Z\";" +
                        "-fx-rotate: " + (isUser ? "0" : "180") + ";" +
                        "-fx-pref-width: 10;" +
                        "-fx-pref-height: 10;"
        );

        Label messageText = new Label(text);
        messageText.setWrapText(true);
        messageText.setMaxWidth(250);
        messageText.setStyle("-fx-font-size: 14;");

        if (isUser) {
            messageBubble.getChildren().addAll(pointer, messageText);
            messageBubble.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBubble.getChildren().addAll(messageText, pointer);
            messageBubble.setAlignment(Pos.CENTER_LEFT);
        }

        chatContainer.getChildren().add(messageBubble);
    }
}