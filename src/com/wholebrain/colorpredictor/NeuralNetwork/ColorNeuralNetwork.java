package com.wholebrain.colorpredictor.NeuralNetwork;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A class specifically design to use a {@link NeuralNetwork neural network} to guess if
 * a text is more readable in black or in white, onto a specific colour.
 */
public class ColorNeuralNetwork implements Serializable {
    private NeuralNetwork brain;
    private static int[] architecture = {16};
    private static double[] blackOutput = {1.0,0.0},
            whiteOutput = {0.0,1.0};
    public enum BWColor{
        BLACK,
        WHITE
    }

    /**
     * Creates a new NeuralNetwork that is designed to learn
     * a specific binary anwser for a rgb color.
     */
    public ColorNeuralNetwork(){

        brain = new NeuralNetwork(3,architecture,2 ,0.5);
    }
//
//    public void setAutotrainOptions(int[] autotrainOptions) {
//        this.autotrainOptions = autotrainOptions;
//    }
//    public int[] getAutotrainOptions(){
//        return this.autotrainOptions;
//    }

    /**
     * Trains the {@link NeuralNetwork neural network} once.
     * @param r Red component of the colour.
     * @param g Green component of the colour.
     * @param b Blue component of the colour.
     * @param bw The expected {@link BWColor black or white colour}.
     */
    public void train(int r, int g, int b, BWColor bw){
        try {
            brain.back(rgbToInputs(r,g,b),bw==BWColor.BLACK?blackOutput:whiteOutput);
        } catch (NonMatchingSizesException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asks the {@link NeuralNetwork neural network} what {@link BWColor colour} the correct answer is more likely to be.
     * @param r Red component of the colour.
     * @param g Green component of the colour.
     * @param b Blue component of the colour.
     * @return A {@link double[] array} representing the probability of the correct answer to be black and to be white.
     */
    public double[] guess(int r, int g, int b){
        double[] outputs= new double[2];
        try {
            outputs = brain.forward(rgbToInputs(r,g,b));
        } catch (NonMatchingSizesException e) {
            e.printStackTrace();
        }
        int higherIndex = outputs[0]>outputs[1]?0:1;
        double[] ret = new double[2];
        ret[0] = higherIndex;
        ret[1] = outputs[higherIndex]/(Matrix.sum(outputs));
        System.out.println("ColorNN : "+ Arrays.toString(ret));
        return ret;
    }

    /**
     * Convert a rgb colour to an {@link double[] array} that can be used by the {@link NeuralNetwork neural network}.
     * @param r Red component of the colour.
     * @param g Green component of the colour.
     * @param b Blue component of the colour.
     * @return {@link double[] Array} representing each basic colour's intensity.
     */
    private double[] rgbToInputs(int r, int g, int b){
        double[] inputs = new double[3];
        inputs[0] = r/256.0;
        inputs[1] = g/256.0;
        inputs[2] = b/256.0;
        return inputs;
    }

}
