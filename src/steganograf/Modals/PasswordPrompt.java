package steganograf.Modals;

import steganograf.Logic.Utils;
import steganograf.Types.PasswordType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;

public class PasswordPrompt {

    private static String errorLabelText;
    private static String password;
    private static File keyImage;
    private static boolean keyImageEnabled = false;

    public static String display(PasswordType mode) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Enter Password");
        window.setMinWidth(400);
        window.setMinHeight(120);
        window.setOnCloseRequest(e -> password = null);

        // Password
        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 0);
        PasswordField passText = new PasswordField();
        passText.setPrefWidth(230);
        GridPane.setConstraints(passText, 1, 0);

        // Confirm Password
        Label confirmPassLabel = new Label("Confirm Password:");
        GridPane.setConstraints(confirmPassLabel, 0, 1);
        PasswordField confirmPassText = new PasswordField();
        confirmPassText.setPrefWidth(230);
        GridPane.setConstraints(confirmPassText, 1, 1);

        // Error Label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        GridPane.setConstraints(errorLabel, 1, 2);

        // Buttons
        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        Button cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);

        // Image View
        ImageView keyImageView = new ImageView();
        keyImageView.setPreserveRatio(true);

        // HBox
        HBox base = new HBox(10);
        base.setPadding(new Insets(10));
        base.setAlignment(Pos.BASELINE_RIGHT);
        base.getChildren().addAll(okButton, cancelButton);

        // GridPane with 10px padding around edge
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 20, 10, 20));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.getChildren().addAll(passLabel, passText);
        if(mode == PasswordType.ENCRYPT) {
            grid.getChildren().addAll(confirmPassLabel, confirmPassText);
        }
        grid.getChildren().add(errorLabel);

        // BorderPane (Password Scene)
        BorderPane passwordPane = new BorderPane();
        passwordPane.setCenter(grid);
        passwordPane.setBottom(base);

        // Button ActionEvents
        okButton.setOnAction(e -> {
            if(keyImageEnabled){
                password = Utils.hashImage(keyImage);
                window.close();
            }
            else if(mode == PasswordType.ENCRYPT) {
                if (validatePassword(passText.getText(), confirmPassText.getText())) {
                    password = passText.getText();
                    window.close();
                }
                else {
                    password = null;
                    errorLabel.setText(errorLabelText);
                    passText.clear();
                    confirmPassText.clear();
                }
            }
            else if(mode == PasswordType.DECRYPT) {
                password = passText.getText();
                window.close();
            }
        });

        cancelButton.setOnAction(e -> {
            password = null;
            window.close();
        });

        Scene scene = new Scene(passwordPane);
        window.setScene(scene);
        window.showAndWait();
        return password;
    }

    /**
     * Checks if the encryption password isn't empty
     * or if the password and password confirmation fields are equal
     *
     * @param pass        value of the password box
     * @param confirmPass value of the confirm password box
     * @return            <code>true</code> if password is valid, <code>false</code> if password is invalid.
     */
    private static boolean validatePassword(String pass,String confirmPass) {

        boolean isValid = true;
        if(pass.equals("") || confirmPass.equals("")) {
            errorLabelText = "Both fields are required."; isValid = false;
        }
        else if(!pass.equals(confirmPass)){
            errorLabelText = "Passwords don't match!"; isValid = false;
        }
        return isValid;
    }

}
