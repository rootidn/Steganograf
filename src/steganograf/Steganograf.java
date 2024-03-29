/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * Thanks to : Feres Gaaloul and Ilyes Hamrouni on Stegit github
 */
package Steganograf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class Steganograf extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("Resources/layout.fxml"));
        Image icon = new Image(Controller.class.getResource("Resources/stegano.png").toExternalForm(), false);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Steganograph App");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.getIcons().add(icon);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
