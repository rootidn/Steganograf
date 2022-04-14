/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganograf;

import steganograf.Exceptions.CannotDecodeException;
import steganograf.Exceptions.CannotEncodeException;
import steganograf.Exceptions.UnsupportedImageTypeException;
import steganograf.Logic.AESEncryption;
import steganograf.Logic.BaseSteganography;
import steganograf.Logic.HiddenData;
import steganograf.Logic.ImageSteganography;
import steganograf.Logic.Utils;
import steganograf.Logic.ZLibCompression;
import steganograf.Modals.AlertBox;
import steganograf.Modals.PasswordPrompt;
import steganograf.Types.DataFormat;
import steganograf.Types.PasswordType;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Controller{

    // JavaFX Components
    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem newSecretDocument, newSecretImage, cutMenu, copyMenu, pasteMenu, undoMenu, redoMenu, selectAllMenu, deselectMenu, deleteMenu;
    @FXML
    private RadioMenuItem darkTheme, lightTheme;
    @FXML
    private ImageView coverImageView, steganographicImageView; //secretImageView,
    @FXML
    private TextArea secretMessage;
    @FXML
    private Button encodeDocument, decodeImage; //encodeImage,
    @FXML
    private Tab secretMessageTab, secretDocumentTab; //secretImageTab, 
    @FXML
    private VBox root, coverImagePane, steganographicImagePane; //, secretImagePane
    @FXML
    private ListView<String> secretDocumentContent;
    @FXML
    private CheckBox encryptMessage, encryptDocument, compressDocument, compressMessage;
    @FXML
    private ToggleGroup messagePixelsPerByte, documentPixelsPerByte, pixelsPerPixel;
    @FXML
    private HBox messagePixelsPerByteWrapper, documentPixelsPerByteWrapper;

    // Files;
    private File coverImage, secretDocument, steganographicImage, tempFile; //, secretImage
    // Password
    private String password;
    // Clipboard
    private Clipboard systemClipboard = Clipboard.getSystemClipboard();

    public void setCoverImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Cover Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        coverImage = fc.showOpenDialog(null);
        if (coverImage != null) {
            coverImagePane.setMinSize(0, 0);
            coverImageView.setImage(new Image("file:" + coverImage.getPath()));
            coverImageView.fitWidthProperty().bind(coverImagePane.widthProperty());
            coverImageView.fitHeightProperty().bind(coverImagePane.heightProperty());
            coverImagePane.setMaxSize(900, 900);
            secretMessageTab.setDisable(false);
            secretDocumentTab.setDisable(false);
        } else {
            AlertBox.error("Error while setting cover image", "Try again...");
        }
    }

    public void setSteganographicImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Steganographic Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.bmp", "*.jpg", "*.jpeg"));
        steganographicImage = fc.showOpenDialog(null);
        if (steganographicImage != null) {
            steganographicImagePane.setMinSize(0, 0);
            steganographicImageView.setImage(new Image("file:" + steganographicImage.getPath()));
            steganographicImageView.fitWidthProperty().bind(steganographicImagePane.widthProperty());
            steganographicImageView.fitHeightProperty().bind(steganographicImagePane.heightProperty());
            steganographicImagePane.setMaxSize(1440, 900);
            decodeImage.setDisable(false);
        } else {
            AlertBox.error("Error while setting steganographic image", "Try again...");
        }
    }

    public void setSecretDocument() {
        FileChooser fc = new FileChooser();
        fc.setTitle("New Secret Document...");
        secretDocument = fc.showOpenDialog(null);
        if (secretDocument != null) {
            encodeDocument.setDisable(false);
            try {
                getDocumentContent(secretDocumentContent, secretDocument);
            } catch (IOException e) {
                e.printStackTrace();
                AlertBox.error("Error while setting secret document", e.getMessage());
            }
        } else {
            AlertBox.error("Error while setting secret document", "Try again...");
        }
    }

    public void encodeMessageInImage() {
        String message = secretMessage.getText();
        byte[] secret = message.getBytes(StandardCharsets.UTF_8);
        if(compressMessage.isSelected())
            secret = ZLibCompression.compress(secret);
        if (encryptMessage.isSelected())
            secret = AESEncryption.encrypt(secret, password);
        String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
        imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Steganographic Image...");
        fc.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(
                        imageExtension.toUpperCase(),
                        "*." + imageExtension));
        steganographicImage = fc.showSaveDialog(null);
        if (steganographicImage != null) {
            BaseSteganography img;
            try {
                img = new ImageSteganography(coverImage, encryptMessage.isSelected(), compressMessage.isSelected(), getToggleGroupValue(messagePixelsPerByte));
                img.encode(secret, steganographicImage);
                AlertBox.information("Encoding Successful!", "Message encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
                e.printStackTrace();
                AlertBox.error("Error while encoding", e.getMessage());
            }
        }
    }

    public void encodeDocumentInImage() {
        String secretFileExtension = Utils.getFileExtension(secretDocument);
        try {
            if(compressDocument.isSelected() || encryptDocument.isSelected()) {tempFile = File.createTempFile("temp", "." + secretFileExtension); tempFile.deleteOnExit();}
            if(compressDocument.isSelected() && encryptDocument.isSelected()) {
                File auxFile = File.createTempFile("aux", "."+secretFileExtension); auxFile.deleteOnExit();
                ZLibCompression.compress(secretDocument, auxFile);
                AESEncryption.encrypt(auxFile, tempFile, password);
            }else{
                if(compressDocument.isSelected())
                    ZLibCompression.compress(secretDocument, tempFile);
                else if(encryptDocument.isSelected())
                    AESEncryption.encrypt(secretDocument, tempFile, password);
            }
            if(compressDocument.isSelected() || encryptDocument.isSelected()) { secretDocument = tempFile; }
            String imageExtension = Utils.getFileExtension(coverImage).toLowerCase();
            imageExtension = (imageExtension.matches("jpg|jpeg")) ? "png" : imageExtension;
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            imageExtension.toUpperCase(),
                            "*." + imageExtension));
            steganographicImage = fc.showSaveDialog(null);
            if (steganographicImage != null) {
                BaseSteganography img;
//                if (imageExtension.toLowerCase().equals("gif"))
//                    img = new GifSteganography(coverImage, encryptDocument.isSelected(), compressDocument.isSelected());
//                else
                img = new ImageSteganography(coverImage, encryptDocument.isSelected(), compressDocument.isSelected(), getToggleGroupValue(documentPixelsPerByte));
                img.encode(secretDocument, steganographicImage);
                AlertBox.information("Encoding Successful!", "Document encoded successfully in " + steganographicImage.getName() + ".", steganographicImage);
            }
        } catch (IOException | CannotEncodeException | UnsupportedImageTypeException e) {
            e.printStackTrace();
            AlertBox.error("Error while encoding", e.getMessage());
        }
    }

    public void decodeImage() {
        String imageExtension = Utils.getFileExtension(steganographicImage);
        HiddenData hiddenData;
        FileChooser fc = new FileChooser();
        File file;
        try {
            BaseSteganography img = new ImageSteganography(steganographicImage);
            hiddenData = new HiddenData(img.getHeader());
            fc.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter(
                            hiddenData.extension.toUpperCase(),
                            "*." + hiddenData.extension));

            if (hiddenData.format == DataFormat.MESSAGE) {
                tempFile = File.createTempFile("message", ".txt");
                img.decode(tempFile);
                byte[] secret = Files.readAllBytes(tempFile.toPath());
                String message;
                if (hiddenData.isEncrypted) {
                    password = PasswordPrompt.display(PasswordType.DECRYPT);
                    secret = AESEncryption.decrypt(secret, password);
                }
                if (hiddenData.isCompressed)
                    secret = ZLibCompression.decompress(secret);
                message = new String(secret, StandardCharsets.UTF_8);
                if (message.length() > 0)
                    AlertBox.information("Decoding successful!", "Here is the secret message:", message);
                tempFile.deleteOnExit();
            }

            else if(hiddenData.format == DataFormat.DOCUMENT) {
                file = fc.showSaveDialog(null);
                if (hiddenData.isCompressed || hiddenData.isEncrypted) {
                    tempFile = File.createTempFile("temp", "." + hiddenData.extension);
                    tempFile.deleteOnExit();
                    img.decode(tempFile);
                }
                if (hiddenData.isEncrypted) {
                    password = PasswordPrompt.display(PasswordType.DECRYPT);
                }
                if (hiddenData.isEncrypted && hiddenData.isCompressed) {
                    File auxFile = File.createTempFile("aux", "." + hiddenData.extension);
                    auxFile.deleteOnExit();
                    AESEncryption.decrypt(tempFile, auxFile, password);
                    ZLibCompression.decompress(auxFile, file);
                } else {
                    if (hiddenData.isCompressed)
                        ZLibCompression.decompress(tempFile, file);
                    else if (hiddenData.isEncrypted)
                        AESEncryption.decrypt(tempFile, file, password);
                    else
                        img.decode(file);
                }
                if (file != null && file.length() > 0)
                    AlertBox.information("Decoding Successful!", "Document decoded in " + file.getName(), file);
            }

        } catch (IOException | CannotDecodeException | UnsupportedImageTypeException e) {
            e.printStackTrace();
            AlertBox.error("Error while decoding", e.getMessage());
        }
    }


    public void getEncryptionPassword() {
        if (encryptMessage.isSelected() || encryptDocument.isSelected()) {
            if ((password = PasswordPrompt.display(PasswordType.ENCRYPT)) == null) {
                encryptMessage.setSelected(false);
                encryptDocument.setSelected(false);
            }
        } else {
            password = null;
        }
    }

    private byte getToggleGroupValue(ToggleGroup group){
        RadioButton selectedRadioButton = (RadioButton) group.getSelectedToggle();
        return (byte) Character.getNumericValue(selectedRadioButton.getText().charAt(0));
    }

    private static void getDocumentContent(ListView<String> documentView, File document) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(document));
        BufferedReader reader = new BufferedReader(streamReader);//reads the user file
        String line;
        documentView.getItems().clear();
        while ((line = reader.readLine()) != null)
            documentView.getItems().add(line);
    }

    public void undo() { secretMessage.undo(); }
    public void redo() { secretMessage.redo(); }
    public void cut() { secretMessage.cut(); }
    public void copy(){ secretMessage.copy();}
    public void paste(){ secretMessage.paste(); }
    public void delete(){ secretMessage.replaceSelection(""); }
    public void selectAll(){ secretMessage.selectAll(); }
    public void deselect() { secretMessage.deselect(); }

    public void quitApp() {
        System.exit(0);
    }

}
