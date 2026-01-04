package com.example.demo;

import com.example.demo.controleur.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger le fichier FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/converter/view/main.fxml"));
        Parent root = loader.load();

        // Créer la scène
        Scene scene = new Scene(root, 1000, 750);

        // Appliquer les styles CSS
        scene.getStylesheets().add(getClass().getResource("/com/example/demo/converter/view/style.css").toExternalForm());

        // Configurer la fenêtre principale
        primaryStage.setTitle("XML/JSON Converter - Avec/Sans API");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);

        // Passer la référence du stage au contrôleur
        MainController controller = loader.getController();
        if (controller instanceof MainController) {
          ////  ((MainController) controller).setStage(primaryStage);
        }

        // Afficher la fenêtre
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Lancer l'application JavaFX
        launch(args);
    }
}