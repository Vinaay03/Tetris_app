package com.example.tetris;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class TetrisManagerFiles extends TetrisManager{

    String gameStateFile = "gameState";
    String bestScoreFile = "bestScore";

    TetrisManagerFiles(Context context){
        super(context);
    }

    @Override
    void saveGameState(String s) throws IOException {
        OutputStream writer=myContext.openFileOutput(
                gameStateFile,
                Context.MODE_PRIVATE
        );
        writer.write(s.getBytes(StandardCharsets.UTF_8));
        writer.close();

        File file=new File(myContext.getFilesDir()+"/"+gameStateFile);
    }

    @Override
    String getGameState() throws FileNotFoundException {
        File file=new File(myContext.getFilesDir()+"/"+gameStateFile);
        Scanner scanner=new Scanner(file);
        String input="";
        while (scanner.hasNext()) {
            if(input != "")
                input += "\n";
            input+=scanner.nextLine();
        }
        return input;
    }

    @Override
    boolean hasGameState() {
        File file=new File(myContext.getFilesDir()+"/"+gameStateFile);
        return file.exists();
    }

    @Override
    void deleteGameState() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.deleteIfExists(
                    Paths.get(myContext.getFilesDir()+"/"+gameStateFile));
        }
    }

    @Override
    void saveBestScore(String s) throws IOException {
        OutputStream writer=myContext.openFileOutput(
                bestScoreFile,
                Context.MODE_PRIVATE
        );
        writer.write(s.getBytes(StandardCharsets.UTF_8));
        writer.close();

        File file=new File(myContext.getFilesDir()+"/"+bestScoreFile);
    }

    @Override
    String getBestScore() throws FileNotFoundException {
        File file=new File(myContext.getFilesDir()+"/"+bestScoreFile);
        Scanner scanner=new Scanner(file);
        String input="";
        while (scanner.hasNext()) {
            input+=scanner.nextLine();
        }
        return input;
    }

    @Override
    boolean hasBestScore() {
        File file=new File(myContext.getFilesDir()+"/"+bestScoreFile);
        return file.exists();
    }

    @Override
    void deleteBestScore() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.deleteIfExists(
                    Paths.get(myContext.getFilesDir()+"/"+bestScoreFile));
        }
    }
}
