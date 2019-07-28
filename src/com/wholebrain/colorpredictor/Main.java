package com.wholebrain.colorpredictor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {


    private static Main instance;
    private Model model;
    private SimpleStringProperty title = new SimpleStringProperty("Color Predictor - ");


    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.getIcons().add(new Image("file:colorpredictoricon.png"));
        primaryStage.show();

        model = new Model();

        Controller controller = loader.getController();
        controller.setStage(primaryStage);
        controller.initModel(model);
        model.addFxObserver(controller);

        primaryStage.titleProperty().bind(title.concat(model.getCurrentName()));

        closeStageAfterDelay(showInfoStage(), 5000);

        instance = this;
    }

    static Main getInstance(){
        return instance;
    }


    @Override
    public void init() throws Exception {
        super.init();

    }

    /**
     * Closes the application after saving the model configurations.
     * @throws Exception Inherited.
     */
    @Override
    public void stop() throws Exception {
        model.close();
        super.stop();
    }

    /**
     * Displays the information window.
     * @return The {@link Stage window} that is returned for further manipulations (like hide).
     * @throws IOException See {@see FXMLLoader#load()}
     */
    Stage showInfoStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("infoPanel.fxml"));
        Parent root = loader.load();
        Stage infoStage = new Stage();
        infoStage.initModality(Modality.APPLICATION_MODAL);
        infoStage.initStyle(StageStyle.UNDECORATED);
        infoStage.setScene(new Scene(root));
        infoStage.setResizable(false);
        infoStage.centerOnScreen();
        infoStage.show();
        return infoStage;
    }

    /**
     * Closes the specified {@link Stage window} after a informed delay.
     * @param stage Window to be closed.
     * @param delayInMs Delay of closure in milliseconds.
     */
    private void closeStageAfterDelay(Stage stage, int delayInMs){

        new Thread(() -> {
            try {
                Thread.sleep(delayInMs);
                Platform.runLater(stage::hide);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
