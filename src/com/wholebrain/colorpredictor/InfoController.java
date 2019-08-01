package com.wholebrain.colorpredictor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class InfoController implements Initializable {
    @FXML public VBox info_vbox;
    @FXML public Label welcome_label;
    @FXML public BorderPane info_pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final String welcomeText = "Welcome to ColorPredictor !",
                ideaText = "Made with Intellij Idea Community 2019.1.2", ideaLink = "https://www.jetbrains.com/idea/",
                codingTrainSeriesText  ="Inspired by Coding Train's series on neural networks",codingTrainSeriesLink="https://www.youtube.com/user/shiffman/playlists?view=50&sort=dd&shelf_id=16",
                codingTrainColorText = "and particularly by the project \"Color Predictor\"", codingTrainColorLink ="https://www.youtube.com/watch?v=KtPpoMThKUs",
                codingTrainGitHubText = "Coding Train GitHub", codingTrainGitHubLink="https://github.com/CodingTrain/website/",
                gitHubText = "My GitHub", gitHubLink ="https://github.com/Whole-Brain",
                contactText = "Contact me at", contactLink = "g.wael@outlook.fr";

        welcome_label.setText(welcomeText);

        addTextWithHyperlink(ideaText, ideaLink);
        addTextWithHyperlink(codingTrainSeriesText, codingTrainSeriesLink);
        addTextWithHyperlink(codingTrainColorText, codingTrainColorLink);
        addTextWithHyperlink(codingTrainGitHubText, codingTrainGitHubLink);
        addTextWithHyperlink(gitHubText, gitHubLink);
        addTextWithHyperlink(contactText, contactLink);

    }

    /**
     * Adds inside the main {@link VBox layout} a {@link TextFlow}.
     * @param description Description of the link.
     * @param link Link that will be clickable to read it inside the default web browser.
     */
    private void addTextWithHyperlink(String description, String link){
        Hyperlink hyperlink = new Hyperlink(link);
        makeHtmlClickable(hyperlink);
        TextFlow textFlow = new TextFlow(new Text(description +" : "));
        textFlow.getChildren().add(hyperlink);
        textFlow.setTextAlignment(TextAlignment.CENTER);
        info_vbox.getChildren().add(textFlow);
    }

    /**
     * Makes a {@link Hyperlink hyper link} clickable.
     * When clicked, they will open the default web browser of the system.
     * @param hyperlink {@link Hyperlink} to be made clickable.
     */
    private void makeHtmlClickable(Hyperlink hyperlink){
        hyperlink.setOnAction(e -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(hyperlink.getText()));
                } catch (IOException | URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /**
     * Calls the closure of the current main {@link Stage window}.
     */
    @FXML public void info_action() {
        ((Stage)info_pane.getScene().getWindow()).close();
    }
}
