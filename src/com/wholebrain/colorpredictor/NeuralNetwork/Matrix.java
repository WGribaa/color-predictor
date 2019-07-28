package com.wholebrain.colorpredictor.NeuralNetwork;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

abstract class Matrix {

    /**
     * Returns the matrix product of two matrices.
     * The number of columns of the first matrix
     *   must be equal to the number of rows of the second matrix.
     * @param matrix1 First matrix (# of columns is important).
     * @param matrix2 Sedonc matrix (# of rows is important).
     * @return Matrix product as a double[#rows1][#columns2];
     */
    static double[][] matrixProduct(double[][] matrix1, double[][] matrix2) throws NonMatchingSizesException {
        if(matrix1[0].length!= matrix2.length)
            throw new NonMatchingSizesException("MATRIX PRODUCT(matrix1, matrix2) : matrix 1 has "+matrix1[0].length
                    +" columns while matrix 2 has "+matrix2.length+" rows.");


        double[][] ret = new double[matrix1.length][matrix2[0].length];
        double cell;
        for (int i = 0; i< ret.length; i++){
            for (int j = 0; j< ret[0].length;j++){
                // for each cell :
                cell=0.0;
                for (int x=0; x<matrix1[0].length;x++)
                    cell+= matrix1[i][x] * matrix2[x][j];
                ret[i][j] = cell;
            }
        }
        return ret;
    }

    /**
     * Returns the matrix product of an array by a matrix.
     * The size of the array must equal the number of columns of the matrix.
     * @param array Array to multiply as a double[].
     * @param matrix as a double[][].
     * @return Matrix product as a double[#columnsMatrix].
     */
    static double[] matrixProduct(double[] array, double[][] matrix) throws NonMatchingSizesException {
        if(array.length!=matrix[0].length)
            throw new NonMatchingSizesException("MATRIX PRODUCT(array, matrix) : The array has "+array.length
                    +" columns while the matrix has "+matrix[0].length+" rows.");


        double[] ret = new double[matrix.length];
        for (int i = 0; i< matrix.length; i++){
            for (int j = 0; j<array.length; j++){
                ret[i] += array[j]*matrix[i][j];
            }
        }
        return ret;

    }

    /**
     * Returns the matrix product of an array by another array.
     * The first array wil be transposed during the process.
     * The result is necessarily a matrix of size [array1.length][array2.length].
     * @param array1 Array to multiply as a double[].
     * @param array2 Array to multiply as a double[].
     * @return Matrix product as a double[][].
     */
    static double[][] matrixProduct(double[] array1, double[] array2){

        double[][] ret = new double[array1.length][array2.length];
        for (int i =0; i<array1.length; i++)
            for (int j = 0; j<array2.length; j++)
                ret[i][j] += array1[i]*array2[j];
        return ret;

    }

    /**
     * Returns the matrix product of an array by another array.
     * The array wil be transposed during the process.
     * The result is necessarily an array of size [matrix.rows].
     * @param matrix Matrix to multiply as a double[][].
     * @param array Array to multiply as a double[].
     * @return Matrix product as a double[].
     */
    static double[] matrixProduct(double[][] matrix, double[] array) throws NonMatchingSizesException {
        if(array.length!=matrix[0].length)
            throw new NonMatchingSizesException("MATRIX PRODUCT(matrix, array) : The matrix has "+matrix[0].length
                    +" columns while the array has "+array.length+" \"rows\".");


        double[] ret = new double[matrix.length];

        for (int i =0; i< matrix.length; i++)
            for (int j = 0; j<matrix[0].length; j++)
                ret[i]+=array[j] * matrix[i][j];
            return ret;

    }

    /**
     * Return the term by term addition of two matrices.
     * The two matrices must have the both same sizes.
     * @param matrix1 double[][]
     * @param matrix2 double[][]
     * @return double[][] of the addition.
     */
    static double[][] add(double[][] matrix1, double[][] matrix2) throws NonMatchingSizesException {
        if(matrix1.length!=matrix2.length || matrix1[0].length != matrix2[0].length)
            throw new NonMatchingSizesException("MATRIX ADD(matrix1, matrix2) : Matrix 1 has size ["+matrix1.length+"]["+matrix1[0].length
                    +"] while Matrix 2 has size ["+matrix2.length+"]["+matrix2[0].length+"]");


        double[][] ret = new double[matrix2.length][matrix1[0].length];
        for (int i =0; i< matrix1.length; i++)
            for (int j = 0; j< matrix2[0].length; j++)
                ret[i][j] = matrix1[i][j] + matrix2[i][j];
        return ret;
    }

    /**
     * Return the term by term addition of two arrays.
     * Both arrays must have the same length.
     * @param array1 double[]
     * @param array2 double[]
     * @return double[] of the addition.
     */
    static double[] add(double[] array1, double[] array2) throws NonMatchingSizesException {
        if(array1.length!=array2.length)
            throw new NonMatchingSizesException("MATRIX ADD(array1, array2) : Array1 has size "+array1.length
                    +" while Array2 has size "+array2.length+".");


        double[] ret = new double[array1.length];
        for (int i =0; i< array1.length; i++)
            ret[i] = array1[i]+ array2[i];
        return ret;
    }

    /**
     * Return the term by term subtraction of two arrays.
     * Both arrays must have the same length.
     * @param array1 double[]
     * @param array2 double[]
     * @return double[] of the subtraction array1 - array2.
     */
    static double[] substract(double[] array1, double[] array2) throws NonMatchingSizesException {
        if(array1.length!=array2.length)
            throw new NonMatchingSizesException("MATRIX SUBSTRACT(array1, array2) : Array1 has size "+array1.length
                    +" while Array2 has size "+array2.length+".");

        double[] ret = new double[array1.length];
        for (int i =0; i< array1.length; i++)
            ret[i] = array1[i]-array2[i];
        return ret;
    }

    /**
     * Return the term by term multiplication of two arrays.
     * Both arrays must have the same length.
     * @param array1 double[]
     * @param array2 double[]
     * @return double[] of the multiplication array1 * array2.
     */
    static double[] multiply(double[] array1, double[] array2) throws NonMatchingSizesException {
        if(array1.length!=array2.length)
            throw new NonMatchingSizesException("MATRIX MULTIPLY(array1, array2) : Array1 has size "+array1.length
                    +" while Array2 has size "+array2.length+".");

        double[] ret = new double[array1.length];
        for (int i =0; i< array1.length; i++)
            ret[i] = array1[i]*array2[i];
        return ret;
    }

    static double[] externalProduct(double[] array, double factor){
        double[] ret = new double[array.length];
        for (int i=0; i< ret.length; i++)
            ret[i] = array[i] * factor;
        return ret;
    }

    /**
     * Apply a function to all number in an array.
     * @param array Array, as a double[], to compute.
     * @param f Function as a DoubleUnaryOperator.
     * @return array as a double[].
     */
    static double[] map (double[] array, DoubleUnaryOperator f){
        double[] ret = new double[array.length];
        for (int i = 0; i<array.length; i++){
            ret[i] = f.applyAsDouble(array[i]);
        }
        return ret;
    }

    static double[][] transpose(double[][] matrix) {
        double[][] ret = new double[matrix[0].length][matrix.length];
        for (int  i=0; i< matrix[0].length; i++)
            for (int j = 0; j< matrix.length; j++)
                ret[i][j] = matrix[j][i];
        return ret;
    }

    static double sum(double[]array){
        double ret = 0.0;
        for (double d : array)
            ret+=d;
        return ret;
    }

    static String toString(double[][] matrix){
        StringBuilder sb = new StringBuilder();
        for(double[] ds : matrix){
            for(double d : ds)
                sb.append(d).append("\t");
            sb.append("\n");
        }
        return sb.toString();
    }

    static String toString(double[] array){
        return Arrays.toString(array);
    }

}
