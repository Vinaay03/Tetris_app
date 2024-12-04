package com.example.tetris;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;

abstract public class TetrisManager {

    Context myContext;

    TetrisManager(Context context){
        myContext = context;
    }

    TetrisState ts;

    abstract void saveGameState(String s) throws IOException;

    abstract String getGameState() throws FileNotFoundException;

    abstract boolean hasGameState();

    abstract void deleteGameState() throws IOException;

    abstract void saveBestScore(String s) throws IOException;

    abstract String getBestScore() throws FileNotFoundException;

    abstract boolean hasBestScore();

    abstract void deleteBestScore() throws IOException;
}
