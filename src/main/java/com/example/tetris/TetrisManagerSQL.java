package com.example.tetris;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;

public class TetrisManagerSQL extends TetrisManager{
    SQLiteDatabase db;

    int stateID = 1;

    TetrisManagerSQL(Context context){
        super(context);
        db=context.openOrCreateDatabase("tetris.sql", context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS tetrisstate "+"(id INT NOT NULL PRIMARY KEY,state TEXT NOT NULL)");
    }

    @Override
    void saveGameState(String s) throws IOException {
        db.execSQL("REPLACE INTO tetrisstate VALUES ("+ stateID +",'"+s+"')");
    }

    @Override
    String getGameState() throws FileNotFoundException {
        Cursor cursor=db.rawQuery("SELECT state FROM tetrisstate WHERE id="+ stateID,null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    @Override
    boolean hasGameState() {
        Cursor cursor=db.rawQuery("SELECT state FROM tetrisstate WHERE id="+ stateID,null);
        return cursor.getCount()>=1;
    }

    @Override
    void deleteGameState() throws IOException {
        db.execSQL("DELETE FROM tetrisstate");
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
