package com.example.tetris;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TetrisManagerPreferences extends TetrisManager{
    SharedPreferences preferences;

    String stateName = "gameState";



    TetrisManagerPreferences(Context context){
        super(context);
        preferences=context.getSharedPreferences("gamestate",
                context.MODE_PRIVATE);
    }

    @Override
    void saveGameState(String s) throws IOException {
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(stateName, s);
        editor.commit();
    }

    @Override
    String getGameState() throws FileNotFoundException {
        return preferences.getString(stateName,"");
    }

    @Override
    boolean hasGameState() {
        return preferences.contains(stateName);
    }

    @Override
    void deleteGameState() throws IOException {
        SharedPreferences.Editor editor=preferences.edit();
        editor.remove(stateName);
        editor.commit();
    }

    @Override
    void saveBestScore(String s) throws IOException {

    }

    @Override
    String getBestScore() throws FileNotFoundException {
        return null;
    }

    @Override
    boolean hasBestScore() {
        return false;
    }

    @Override
    void deleteBestScore() throws IOException {

    }
}
