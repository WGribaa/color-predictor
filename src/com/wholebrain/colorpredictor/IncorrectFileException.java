package com.wholebrain.colorpredictor;

import java.io.File;

public class IncorrectFileException extends Exception{
    public IncorrectFileException(File file){
        System.err.println("`"+file.getPath()+"` is not a readable file.");
    }
    IncorrectFileException(){}
}
