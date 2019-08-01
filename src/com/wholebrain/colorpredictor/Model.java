package com.wholebrain.colorpredictor;

import com.wholebrain.colorpredictor.NeuralNetwork.ColorNeuralNetwork;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * This class is manipulating all the usable datas that can be stored.
 */
public class Model {
    // Functionability attributes
    private ColorNeuralNetwork colorBrain = new ColorNeuralNetwork();
    private SimpleBooleanProperty learnMode = new SimpleBooleanProperty(true),
            checkedAutotrain = new SimpleBooleanProperty(false);
    private SimpleIntegerProperty rgbThreashold= new SimpleIntegerProperty(384),
            trainingTimes=new SimpleIntegerProperty(100),
            selectedOperator=new SimpleIntegerProperty(0);
    private int maxTrainingTimes = 65000000,
    maxThreashold = 767;
    // Communication attributes
    private List<String> operators = new ArrayList<>(Arrays.asList(">=","<"));
    private List<FxObserver> observers = new ArrayList<>();
    // Configuration attributes
    private File CONFIG = new File("config.nnc");
    private SimpleStringProperty currentName = new SimpleStringProperty();
    private File currentFile = null;
    private List<File> recentFiles = new ArrayList<>();
    private boolean isFileModified = false;

    Model(){

        selectedOperator.addListener((observable, oldValue, newValue) -> {
            notifyOperatorSelectionIndex();
            setFileModified();
        });
        trainingTimes.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue()>maxTrainingTimes)
                trainingTimes.set(maxTrainingTimes);
            setFileModified();
        });
        rgbThreashold.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue()>maxThreashold)
                rgbThreashold.set(maxThreashold);
            setFileModified();
        });

        ObjectInputStream ois = null;

        try {
            if (CONFIG.createNewFile())
                saveConfig();
            else {
                ois = new ObjectInputStream(
                        new BufferedInputStream(
                                new FileInputStream(CONFIG)
                        )
                );
                setCurrentFile((File) ois.readObject(), ois.readBoolean());
//                System.out.println("Current file = " + currentFile.getName());
                int nbRecentFiles = ois.readInt();
                for (int i = 0; i < nbRecentFiles; i++)
                    recentFiles.add((File)ois.readObject());
                loadData(ois);
                if(isFileModified) {
                    modifyName();
                    System.out.println("File was modified at last exit.");
                }else{
                    System.out.println("File was saved at last exit");
                }

            }
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        } catch (IncorrectFileException e) {
            e.printStackTrace();
            setCurrentFile(null);
        } finally{
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Closing the model means saving its configuration inside a stored config file.
     */
    void close(){
        /*try {
            CONFIG.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        saveConfig();
    }

    /**
     * Change the current read {@link File file} and notify the window's {@link Controller controller }
     * to change its title.
     * @param file The {@link File file} that is now being used to store the {@link ColorNeuralNetwork color neural network}.
     * @param isModified Specifies if the file was modified at its last closure, compared to the last time it was saved.
     */
    private void setCurrentFile(File file, boolean isModified){
        this.currentFile = file;
        this.isFileModified = isModified;
        modifyName();
    }

    /**
     * Same as @see #setCurrentFile(File,boolean) with the boolean as false, to inform that the file wasn't modified.
     * @param file The {@link File file} that is now being used to store the {@link ColorNeuralNetwork color neural network}.
     */
    private void setCurrentFile(File file){
        setCurrentFile(file, false);
    }

    /**
     * Changes the name and notifies the window's {@link Controller controller} to show the new informed name.
     */
    private void modifyName(){
        String s = currentFile == null ? "New":currentFile.getName();
        if(isFileModified)
            s=s.concat("*");
        currentName.set(s);
    }

    /**
     * Informs that the {@link File file} has been modified compared to the state of when it was loaded,
     * and notifies the window's {@link Controller controller} to show the new informed name.
     */
    void setFileModified(){
        this.isFileModified =true;
        modifyName();
    }

    /**
     * Tells if the {@link File file} has been modified compared to the state of when it was loaded.
     * @return {@link boolean Boolean} true if it has been modified.
     */
    boolean isNotModified(){
        return !isFileModified;
    }

    /**
     * Stores all the needed datas of the {@link Model model} inside the config file.
     */
    private void saveConfig(){
        ObjectOutputStream oos = null;
        try{
            oos = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(CONFIG)
                    )
            );
            oos.writeObject(currentFile);
            oos.writeBoolean(isFileModified);
            oos.writeInt(recentFiles.size());
            for (File file : recentFiles)
                oos.writeObject(file);
            saveData(oos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(oos!=null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds an {@link FxObserver observer} to notify for specific changes.
     * Those changes are :
     * - 1 : The {@link List<File> list of files} recently accessed.
     * - 2 : The inequality chosen to set the autotraining of the {@link ColorNeuralNetwork color neural network}.
     * @param observer This {@link FxObserver observer} is the controller that needs to be notify of changes
     *                 that can't be transmitted by classic binding.
     */
    void addFxObserver(FxObserver observer){
        observers.add(observer);
        notifyRecentFiles();
        notifyOperatorSelectionIndex();
    }

    /**
     * Removes the {@link FxObserver observer} from the list of observers to notify.
     * @param observer This {@link FxObserver observer} is a class that no longer needs to be notify of changes.
     *                 It might be helpful for future improvisations of the view.
     */
    public void removeFxObserver(FxObserver observer){
        observers.remove(observer);
    }

    /**
     * Notifies all the {@link FxObserver observers} of a change inside the {@link List<File> list of recent files} accessed.
     */
    private void notifyRecentFiles(){
        for (FxObserver observer : observers)
            observer.updateRecentFiles(recentFiles);
    }

    /**
     * Notifies all the {@link FxObserver observers} of the change of inequality used
     * to autotrain the {@link ColorNeuralNetwork color neural network}.
     */
    private void notifyOperatorSelectionIndex(){
        for (FxObserver observer : observers)
            observer.updateSelectedOperator(selectedOperator.get());
    }

    /**
     * Returns a link to the current {@link ColorNeuralNetwork color neural network}.
     * @return The current {@link ColorNeuralNetwork color neural network}.
     */
    ColorNeuralNetwork getColorbrain(){
        return colorBrain;
    }

    /**
     * Creates a new {@link ColorNeuralNetwork color neural network} and changes the current {@link File file} to a new
     * empty ({@link null}) one
     */
    void generateNew(){
        colorBrain = new ColorNeuralNetwork();
        setCurrentFile(null);
    }

    /**
     * Switches the mode between "Predict" and "Learn".
     * - "Predict" corresponds to a guess from the {@link ColorNeuralNetwork color neural network}.
     * - "Learn" corresponds to a backpropagation inside the {@link ColorNeuralNetwork color neural network}.
     * @return A {@link boolean} which indicates the mode after the switch.
     * - True = "Learn" mode.
     * - false = "Predict" mode.
     */
    boolean toggleMode(){
        learnMode.set(!learnMode.get());
        return learnMode.get();
    }

    /**
     * Binded {@link boolean} which expresses the actual mode.
     * @return A {@link BooleanProperty property} which holds a boolean that is binded with the view.
     * - True = "Learn" mode.
     * - false = "Predict" mode.
     */
    BooleanProperty getLearnMode(){
        return learnMode;
    }

    /**
     * Binded {@link boolean} which expresses if the autotrain option is currently checked.
     * @return A {@link BooleanProperty property} which holds a boolean that is binded with the view.
     * - True = Autotrain is checked.
     * - false = Autotrain is not checked.
     */
    BooleanProperty getCheckedAutotrain(){
        return checkedAutotrain;
    }

    /**
     * Binded {@link Integer integer} that indicates which operator is currently selected.
     * @return A {@link SimpleIntegerProperty property} which holds the index of the currently selected inequality
     * in the {@link List<String> list of possible operators}.
     */
    SimpleIntegerProperty getSelectedOperator(){
        return selectedOperator;
    }

    /**
     * Binded {@link Integer integer} that indicated the current threashold for the inequality in regards to the RGB value.
     * @return {@link SimpleIntegerProperty Integer Property}.
     */
    SimpleIntegerProperty getRgbThreashold(){
        return rgbThreashold;
    }

    /**
     * Binded {@link Integer integer} that indicated of many instance of autotraining is currently set.
     * @return {@link SimpleIntegerProperty Integer Property}.
     */
    SimpleIntegerProperty getTrainingTimes(){
        return trainingTimes;
    }

    /**
     * Returns the maximum possible number of automatic training at at one time.
     * @return {@link int}
     */
    int getMaxTrainingTimes(){
        return maxTrainingTimes;
    }

    /**
     * Returns the maximum possible value of the RGB threashold for the autotraining.
     * @return {@link int}
     */
    int getMaxThreashold(){
        return maxThreashold;
    }

    /**
     * Binded {@link String text} representing the name that has to be showed in the window.
     * @return {@link StringProperty String Property}.
     */
    StringProperty getCurrentName(){
        return currentName;
    }

    /**
     * Currently accessed {@link File file}.
     * @return A {@link File file}.
     */
    File getCurrentFile(){
        return currentFile;
    }

    /**
     * Update the {@link List<File> recent files list} and notifies the view.
     * The latter accessed {@link File file} will be at the beginning of the list.
     * @param file {@link File Current file} that needs to be on top of the {@link List<File> recent files list}.
     */
    private void updateRecentFiles(File file){
        System.out.println("Updating Recent Files");
        Iterator<File> it = recentFiles.iterator();
        HashSet<String> collector = new HashSet<>();
        while(it.hasNext()) {
            File nextFile = it.next();
            if (collector.contains(nextFile.getPath()) || file.getPath().equals(nextFile.getPath())) {
                System.out.println("\tRemoving `" + file.getPath() + "` from recent files");
                it.remove();
            }
            collector.add(nextFile.getPath());
        }
        recentFiles.add(0,file);
        System.out.println("Recent Files now = "+recentFiles.toString());
        notifyRecentFiles();
    }

    /**
     * Reads the informed {@link File file} and sets the attributes.
     * @param file A {@link File}.
     */
    void loadData(File file) {
        if(file == null) {
            System.out.println("Trying to LOAD a null file.");
            return;
        }
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(
                    new BufferedInputStream(
                            new FileInputStream(file)));
            loadData(ois);

            System.out.println("    Loading ::");
            System.out.println("Mode = "+(learnMode.get()? "Learn":"Predict"));
            System.out.println("Autotrain = "+checkedAutotrain.get());
            System.out.println("Operator = "+operators.get(selectedOperator.get()));
            System.out.println("ThreashHold = "+rgbThreashold.get());
            System.out.println("Training "+trainingTimes.get()+" times\n");


            setCurrentFile(file);
            System.out.println("Current File Path = "+currentFile.getPath());
            updateRecentFiles(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IncorrectFileException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unable to read the file.");
            alert.setHeaderText("File \""+file.toString()+"\" is corrupted or unreadable.");
            alert.setContentText("It will be removed from the recent files list.");
            recentFiles.remove(file);
            notifyRecentFiles();
            alert.showAndWait();
        } finally {
            if(ois!=null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * Reads a {@link ObjectInputStream} and sets the attributes.
     * @param ois {@link ObjectInputStream}
     * @throws IncorrectFileException Custom Exception about the access of the nns {@link File files}.
     */
    private void loadData(ObjectInputStream ois) throws IncorrectFileException {
        try {
            colorBrain=(ColorNeuralNetwork)ois.readObject();
            learnMode.set(ois.readBoolean());
            checkedAutotrain.set(ois.readBoolean());
            rgbThreashold.set(ois.readInt());
            trainingTimes.set(ois.readInt());
//            selectedOperator.setValue(ois.readInt());
            selectedOperator.set(ois.readInt());
            int nbOperators = ois.readInt();
            operators = new ArrayList<>();
            for (int i = 0; i<nbOperators; i++)
                operators.add((String)ois.readObject());
        } catch (Exception e) {
            throw new IncorrectFileException();
        }
    }

    /**
     * Saved the current attributes inside the {@link File file}.
     * @param file {@link File} to store the datas into.
     */
    void saveData(File file){

        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(file)));
            saveData(oos);

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if (oos != null){
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        setCurrentFile(file);
        updateRecentFiles(file);
        System.out.println("Path = "+currentFile.getPath());
    }

    /**
     * Saves the current attributes via an {@link ObjectOutputStream}.
     * @param oos {@link ObjectOutputStream}
     * @throws IOException Exception thrown when writing via the {@link ObjectOutputStream}.
     */
    private void saveData(ObjectOutputStream oos) throws IOException {
        System.out.println("    Saving ::");
        oos.writeObject(colorBrain);
        oos.writeBoolean(learnMode.get());
        System.out.println("Mode = "+(learnMode.get()? "Learn":"Predict"));
        oos.writeBoolean(checkedAutotrain.get());
        System.out.println("Autotrain = "+checkedAutotrain.get());
        oos.writeInt(rgbThreashold.get());
        oos.writeInt(trainingTimes.get());
        oos.writeInt(selectedOperator.getValue());
        int nbOperators = operators.size();
        oos.writeInt(nbOperators);
        for (String operator : operators) oos.writeObject(operator);
        System.out.println("Operator = "+operators.get(selectedOperator.get()));
        System.out.println("ThreashHold = "+rgbThreashold.get());
        System.out.println("Training "+trainingTimes.get()+" times\n");
    }

    /**
     * @see #saveData(File)
     * Saves inside the currently accessed {@link File file}.
     */
    void saveData(){
        saveData(currentFile);
    }

    /**
     * Returns the {@link List<String> list of possible operators}.
     * @return A {@link List<String>}.
     */
    List<String> getOperators(){
        return operators;
    }


}
