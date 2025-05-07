package com.virtupet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ChatBalloonApp extends Application {
    private final GeminiClient gemini = new GeminiClient();

    @Override
    public void start(Stage stage) {
        VBox chatBox = new VBox(10);

        TextArea display = new TextArea();
        display.setEditable(false);
        TextField input = new TextField();

        input.setOnAction(_ -> {
            Text text = new Text("prompt");
            text.setFont(new Font(14));
            String userInput = input.getText();
            display.appendText("Você: " + userInput + "\n");
            input.clear();

            new Thread(() -> {
                try {
                    String resposta = gemini.enviarMensagem(userInput);
                    Platform.runLater(() ->
                            display.appendText("IA: " + resposta)
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        chatBox.getChildren().addAll(display, input);
        Scene scene = new Scene(chatBox);
        stage.setScene(scene);
        stage.setTitle("Amigo Virtual");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
