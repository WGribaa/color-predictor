package com.wholebrain.colorpredictor;

import com.wholebrain.colorpredictor.NeuralNetwork.ColorNeuralNetwork;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;


public class Controller implements Initializable, FxObserver {
    public AnchorPane autotrain_pane;
    public Pane color_pane;
    public Circle black_circle, white_circle;
    public Label training_label, certainty_label;
    public Button learn_button, predict_button, autotrain_button;
    public CheckBox autotrain_checkbox;
    public ComboBox<String> infsup_combobox;
    public TextField rgbthreshold_textfield, trainingtimes_textfield;
    public MenuItem save_menu, load_menu, new_menu;
    public Menu recent_menu;

    private Model model;

    private Stage primaryStage;

    private int r, g,b;
    private boolean isGuessedBlack = true;
    private String trainingText;

    /**
     * Toggle between "Predict" and "Learn" modes.
     */
    @FXML void on_toggle_mode() {
        boolean learnMode = model.toggleMode();
        guess();
        updateAnwers(!learnMode);
        model.setFileModified();
    }

    /**
     * Action depends on the mode.
     * In "Learn" mode : backpropagate the targets inside the {@link ColorNeuralNetwork color neural network},
     * depending on the selected {@link ColorNeuralNetwork.BWColor black or white colour}.
     * @param mouseEvent Click on the coloured panel.
     */
    @FXML
    protected void on_colorpane_clicked(MouseEvent mouseEvent) {
        double x = mouseEvent.getSceneX();
        pickNextColor();
        if(model.getLearnMode().get()) {
            // Supervised training.
            if (x <= color_pane.getWidth() / 2.0) {
                updateInfos("ColorPane clicked as Black : " + x);
                learn(ColorNeuralNetwork.BWColor.BLACK);
            } else {
                updateInfos("ColorPane clicked as White : " + x);
                learn(ColorNeuralNetwork.BWColor.WHITE);
            }

            model.setFileModified();
        }else{
            guess();
        }
    }

    /**
     * Fires when the mouse is over the coloured pane. Displays the circle which is in the same side of it.
     * @param mouseEvent Mouse cursor over.
     */
    @FXML
    public void on_colorpane_over(MouseEvent mouseEvent) {
        if (model.getLearnMode().get()){
            boolean isBlack = mouseEvent.getSceneX()<=color_pane.getWidth()/2.0;
            black_circle.setVisible(isBlack);
            white_circle.setVisible(!isBlack);
        }
    }

    /**
     * Both the circles are hided.
     */
    @FXML
    public void on_colorpane_exit() {
        if( model.getLearnMode().get()) {
            black_circle.setVisible(false);
            white_circle.setVisible(false);
        }
    }

    /**
     * Launches the Autotrain in another thread.
     */
    @FXML
    public void on_autotrain_launch() {
        CustomProgress train = new CustomProgress(model.getTrainingTimes().get());
        Thread trainer = new Thread(train);
        trainer.start();
        model.setFileModified();
    }

    /**
     * Tells the model to generate a new {@link ColorNeuralNetwork color neural networkk},
     * then picks a new colour.
     */
    @FXML
    public void on_new() {
        if (!checkModificationAndAsk("Generating a new Neural Network"))
            return;
        model.generateNew();
        pickNextColor();
            guess();
    }

    /**
     * Tells the model to store datas in the current {@link File file}.
     * If the File isn't set, ask the user where to save it.
     */
    @FXML
    public void on_save() {
        if(model.getCurrentFile()==null)
            on_save_as();
        else {
            model.saveData();
        }
    }

    /**
     * Shows a dialog so that the user choose a path where to store the {@link File file}.
     */
    @FXML
    public void on_save_as() {
        FileChooser fileChooser = fileDialog();
        File currentFile = model.getCurrentFile();
        fileChooser.setInitialDirectory(currentFile!= null ? currentFile.getParentFile():null);
        File file = fileChooser.showSaveDialog(primaryStage);

        if(file != null)
            model.saveData(file);
    }

    /**
     * Displays a dialog to choose a {@link File file} to load, then loads them.
     */
    @FXML
    public void on_load() {

        if (!checkModificationAndAsk("Loading a file"))
            return;

        FileChooser fileChooser = fileDialog();
        File currentFile = model.getCurrentFile();
        fileChooser.setInitialDirectory(currentFile!= null ? currentFile.getParentFile():null);
        File file = fileChooser.showOpenDialog(primaryStage);

        if(file != null)
            model.loadData(file);

            guess();
    }

    @FXML
    public void on_operator_chosen() {
    }

    /**
     * Toggles between the "Learn" and "Predict" modes.
     */
    @FXML
    protected void on_toggle_autotrain() {
        model.setFileModified();
        guess();
    }

    /**
     * Initialisation is required to set a {@link TextFormatter text formatter} to the {@link TextField fields}.
     * @param location Unused but required.
     * @param resources Unused but required.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pickNextColor();

        DecimalFormat format = new DecimalFormat("###,###");
        rgbthreshold_textfield.setTextFormatter(new TextFormatter<>(
                c -> {
                    if (c.getControlNewText().isEmpty())
                        return c;
                    ParsePosition position = new ParsePosition(0);
                    Number n = format.parse(c.getControlNewText(), position);
                    if (n == null || position.getIndex() < c.getControlNewText().length())
                        return null;
                    else{
                        if(n.intValue() > model.getMaxThreashold()){
                            c.setRange(0, c.getRangeEnd());
                            c.setText(String.valueOf(model.getMaxThreashold()));
                        }
                        return c;
                    }
                }
        ));

        DecimalFormat format2 = new DecimalFormat("###,###");
        trainingtimes_textfield.setTextFormatter(new TextFormatter<>(
                c -> {
                    if (c.getControlNewText().isEmpty())
                        return c;
                    ParsePosition position = new ParsePosition(0);
                    Number n = format2.parse(c.getControlNewText(), position);
                    if (n == null || position.getIndex() < c.getControlNewText().length())
                        return null;
                    else{
                        if(n.intValue() > model.getMaxTrainingTimes()){
                            c.setRange(0, c.getRangeEnd());
                            c.setText(String.valueOf(model.getMaxTrainingTimes()));
                        }
                        return c;
                    }
                }
        ));
    }

    /**
     * Custom initialisation method to link the {@link Controller controller} to the {@link Model model}.
     * This link is required to bind all the {@link javafx.beans.property.Property properties}.
     * @param model {@link Model} that has all the storable datas related to the main {@link Stage window}.
     */
    void initModel(Model model){
        if(this.model != null)
            throw new IllegalStateException("Model already initialized.");

        this.model= model;

        learn_button.disableProperty().bind(model.getLearnMode());
        predict_button.disableProperty().bind(model.getLearnMode().not());
        certainty_label.visibleProperty().bind(model.getLearnMode().not());


        autotrain_pane.visibleProperty().bind(autotrain_checkbox.selectedProperty()
                .and(autotrain_checkbox.disableProperty().not()));
        autotrain_checkbox.selectedProperty().bindBidirectional(model.getCheckedAutotrain());
        autotrain_checkbox.disableProperty().bind(model.getLearnMode().not());
        infsup_combobox.getItems().setAll(model.getOperators());
        trainingtimes_textfield.textProperty().bindBidirectional(model.getTrainingTimes(), new NumberStringConverter("###,###"));
        rgbthreshold_textfield.textProperty().bindBidirectional(model.getRgbThreashold(), new NumberStringConverter("###,###"));
        infsup_combobox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                    model.getSelectedOperator().set(newValue.intValue());
            System.out.println("New selected index from model = "+model.getSelectedOperator().get());
                });
        guess();

    }

    /**
     * Custom {@link FileChooser} to load or save datas inside specific nns files.
     * @return {@link FileChooser} that can latter be displayed to retrieve the desired {@link File file} location from the user.
     */
    private FileChooser fileDialog(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("NN save files (*.nns)","*.nns");
        fileChooser.getExtensionFilters().add(fileFilter);
        return fileChooser;
    }

    /**
     * Picks and stores a random colour.
     * @param updateColorPane If this {@link boolean} is true, the colour panel will be updated
     *                       to be filled with this new colour.
     */
    private void pickNextColor(boolean updateColorPane){

        r = new Random().nextInt(256);
        g = new Random().nextInt(256);
        b = new Random().nextInt(256);

        if(updateColorPane) {
            String colorString = intToHexString(r)+intToHexString(g)+intToHexString(b);
            color_pane.setStyle("-fx-background-color: #"+colorString);
        }
    }

    /**
     * Calls the same method with the boolean set as true.
     * @see #pickNextColor(boolean)
     */
    private void pickNextColor(){
        pickNextColor(true);
    }

    /**
     * Chooses if and which {@link Circle circle} is displayed as a answer (to the "guess").
     * @param b The {@link Circle circles} won't be displayed if false.
     */
    private void updateAnwers(boolean b) {
        white_circle.setVisible(b&&!isGuessedBlack);
        black_circle.setVisible(b&&isGuessedBlack);
    }

    /**
     * Transform an {@link int} within the range [0;255] to a {@link String string} representing its hexadecimal value.
     * @param n The {@link int integer} to convert.
     * @return A {@link String string}.
     */
    private String intToHexString(int n){
        String ret = Integer.toHexString(n).toUpperCase();
        if(ret.length()==1)
            ret = "0"+ret;
        return ret;
    }

    /**
     * Shows infos inside the console. For debugging.
     * @param s Message to show.
     */
    private void updateInfos(String s){
        System.out.println("Controller infos : "+s);
    }

    /**
     * Orders the {@link ColorNeuralNetwork color neural network} to backpropagate.
     * The inputs are the currently displayed colour RGB values. The target is informed as parameter of the method.
     * @param color Target {@link ColorNeuralNetwork.BWColor colour}.
     */
    private void learn(ColorNeuralNetwork.BWColor color){
        model.getColorbrain().train(r,g,b,color);
    }

    /**
     * Orders the {@link ColorNeuralNetwork color neural network} to predict an estimated {@link ColorNeuralNetwork.BWColor colour}.
     * Then shows the result as a {@link Circle circle} displayed over the colour panel.
     */
    private void guess(){
        if(model.getLearnMode().get())
            return;
        double[] answer = model.getColorbrain().guess(r,g,b);
        isGuessedBlack = (answer[0] == 0);
        double certainty = Math.round(answer[1]*10000)/100.0;
        certainty_label.setText("Certainty = "+certainty+"%");
        updateAnwers(true);
        updateInfos("Guessed that ["+r+","+g+","+b+"] (= "+(r+g+b)+") is "+(isGuessedBlack?"black.":"white.")+" with "+certainty+"% certainty.");
    }

    /**
     * The relevant {@link javafx.scene.Node elements} will be hidden if the {@link boolean parameter} is true.
     * @param hide True = hide.
     */
    private void hideAll(boolean hide){
        color_pane.setDisable(hide);
        if(hide) {
            predict_button.disableProperty().unbind();
            autotrain_checkbox.disableProperty().unbind();
            predict_button.setDisable(true);
            autotrain_checkbox.setDisable(true);
        }else{
            predict_button.setDisable(false);
            autotrain_checkbox.setDisable(false);
            predict_button.disableProperty().bind(model.getLearnMode().not());
            autotrain_checkbox.disableProperty().bind(model.getLearnMode().not());
        }
        infsup_combobox.setDisable(hide);
        trainingtimes_textfield.setDisable(hide);
        rgbthreshold_textfield.setDisable(hide);
        autotrain_button.setDisable(hide);
        Platform.runLater(()->autotrain_button.setText(hide?"Autotraining...":"Launch AutoTrain"));
    }

    /**
     * Strores a link to the {@link Stage primary stage} to be able to close it
     * or to display modal {@link Dialog dialogs} over it.
     * @param primaryStage Main {@link Stage window}.
     */
    void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Update the displayed {@link File recent files} inside the menu.
     * @param files Recently accessed {@link File files}.
     */
    @Override
    public void updateRecentFiles(List<File> files) {
        recent_menu.getItems().clear();
        System.out.println("Menu items of recent files updating with files : "+files.toString());
        if(files.size()==0) return;
        List<MenuItem> items = new ArrayList<>();
        for (File file : files) {
            MenuItem menuItem = new MenuItem(file.getName());
            menuItem.addEventHandler(EventType.ROOT, event ->
                    {
                        if(checkModificationAndAsk("Loading a file")) {
                            model.loadData(file);
                            guess();
                        }
                    }

            );
            items.add(menuItem);

        }

        recent_menu.getItems().addAll(items);
    }

    /**
     * Orders the view to show the currently seclected inequality.
     * @param index {@link int Integer} representing the index of the operator in the possible list.
     */
    @Override
    public void updateSelectedOperator(int index) {
        infsup_combobox.getSelectionModel().select(index);
    }

    /**
     * Checks if the {@link File current file} datas were modified since its last access.
     * If so, shows a {@link Dialog alert dialog} to inform that the modifications won't be stored.
     * @param contextDescription Main message to show on the dialog.
     * @return True if "Yes" is clicked, false otherwise.
     */
    private boolean checkModificationAndAsk(String contextDescription){
        if(model.isNotModified())
            return true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(contextDescription);
        alert.setHeaderText("The current file "+(model.getCurrentFile()==null?"":"\""+model.getCurrentFile().getName()+"\" ")+"has unsaved modifications.");
        alert.setContentText("Are you sure you want to close it ?");
        ButtonType yesButtonType = new ButtonType("Yes");
        ButtonType saveButton = new ButtonType("Save");
        ButtonType cancelButton = new ButtonType("Cancel");
        alert.getButtonTypes().setAll(yesButtonType, saveButton, cancelButton);
        alert.showAndWait();

        if (alert.getResult().equals(yesButtonType))
            return true;
        else if(alert.getResult().equals(saveButton))
            on_save();
        return model.isNotModified();
    }

    /**
     * Closes the application via the Main method, so that the model can store its configuration.
     */
    public void on_close() {
        primaryStage.close();
    }

    /**
     * Displays a information dialog with various content about this project.
     */
    public void on_about() {
        try {
            Main.getInstance().showInfoStage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link Runnable} used to update the {@link Label label} that describes the current progression
     * of the autotraining.
     */
    private class CustomProgress implements Runnable {
        private int iterations;
        CustomProgress(int n){
            this.iterations=n;
        }

        @Override
        public void run() {
            training_label.setVisible(true);
            hideAll(true);
            ColorNeuralNetwork.BWColor[] answers = new ColorNeuralNetwork.BWColor[2];
            int c = model.getSelectedOperator().get();
            answers[c] = ColorNeuralNetwork.BWColor.BLACK;
            answers[(c+1)%2] = ColorNeuralNetwork.BWColor.WHITE;
            int trainingPerLoop = iterations /200,
                    nextRefresh = trainingPerLoop;
            for (int i =0; i<iterations; i++) {
                ColorNeuralNetwork.BWColor trainedColor = (r + g + b >= 384) ?
                        answers[0] : answers[1];
                learn(trainedColor);
                if(i>nextRefresh) {
                    trainingText = String.format("Training... %,d / %,d (%d%%)",i,iterations,(i*100L/iterations));
                    Platform.runLater(() -> training_label.setText(trainingText));
                    nextRefresh+=trainingPerLoop;
                }
//                updateInfos("Autotrained as  : " + trainedColor);
                pickNextColor(false);
            }
            hideAll(false);
            training_label.setVisible(false);
        }
    }

}
