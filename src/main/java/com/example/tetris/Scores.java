package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class Scores extends AppCompatActivity {
    TextView bestScoreTxt;
    TextView bestSocreTitleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_score);

        bestSocreTitleTxt = ((TextView) findViewById(R.id._bestSocreTitleTv));
        bestSocreTitleTxt.setPaintFlags(bestSocreTitleTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        bestScoreTxt = ((TextView) findViewById(R.id._bestScoreTv));
        if(((DataManager)getApplication()).hasBestScore()){
            try {
                String bestScoreSaved = ((DataManager)getApplication()).getBestScore();
                bestScoreTxt.setText(bestScoreSaved + "pts");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            bestScoreTxt.setText("0pts");
        }
    }
}