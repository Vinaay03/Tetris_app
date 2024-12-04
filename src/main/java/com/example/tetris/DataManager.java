package com.example.tetris;

import android.app.Application;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DataManager extends Application {

    TetrisManager tetrisManager;
    boolean hasTetrisManager;

    TetrisManager bestScoreManager;

    boolean saveActivated = false;

    void saveGameState(String s) throws IOException {
        System.out.println("Saving board");
        tetrisManager.saveGameState(s);
    }

    String getGameState() throws FileNotFoundException{
        return tetrisManager.getGameState();
    }

    boolean hasGameState() {
        return tetrisManager.hasGameState();
    }

    void deleteGameState() throws IOException{
        tetrisManager.deleteGameState();
    }

    void saveBestScore(String s) throws IOException {
        System.out.println("Saving best score");
        bestScoreManager.saveBestScore(s);
    }

    String getBestScore() throws FileNotFoundException{
        return bestScoreManager.getBestScore();
    }

    boolean hasBestScore() {
        return bestScoreManager.hasBestScore();
    }

    void deleteBestScore() throws IOException{
        bestScoreManager.deleteBestScore();
    }

    void initBestScoreManager(){
        bestScoreManager=new TetrisManagerFiles(this);
    }

    boolean tetrisManagerSelected(){
        boolean preferencesManager = tetrisManager instanceof TetrisManagerPreferences;
        boolean filesManager = tetrisManager instanceof TetrisManagerFiles;
        boolean SQLManager = tetrisManager instanceof TetrisManagerSQL;

        return preferencesManager || filesManager || SQLManager;
    }

    void setUserWantsToSave(boolean saveGame) {
        saveActivated = saveGame;
    }

    boolean userWantsToSave() {
        return saveActivated;
    }

    void setTetrisManager(int manager) {
        hasTetrisManager = true;
        switch (manager){
            case 0:
                Log.d("SELECTED MANAGER","Preferences");
                tetrisManager=new TetrisManagerPreferences(this);
                break;
            case 1:
                Log.d("SELECTED MANAGER","Files");
                tetrisManager=new TetrisManagerFiles(this);
                break;
            case 2:
                Log.d("SELECTED MANAGER","SQL");
                tetrisManager=new TetrisManagerSQL(this);
                break;
        }
    }

    @Override
    public void onCreate() {super.onCreate();}

}
