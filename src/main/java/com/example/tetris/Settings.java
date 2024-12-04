package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import java.io.IOException;

public class Settings extends AppCompatActivity {

    Button removeBestScoreBtn;
    Switch saveSwitch;
    Spinner managerDropdown;
    int selectedDataManager;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        boolean saveGame = saveSwitch.isChecked();
        ((DataManager) getApplication()).setUserWantsToSave(saveGame);

        selectedDataManager = managerDropdown.getSelectedItemPosition();
        if(saveGame){
            ((DataManager) getApplication()).setTetrisManager(selectedDataManager);
        }
    }

    private void showSelectedManagerOnCreate(){
        if(((DataManager) getApplication()).tetrisManager instanceof TetrisManagerPreferences){
            managerDropdown.setSelection(0);
        }
        else if(((DataManager) getApplication()).tetrisManager instanceof TetrisManagerFiles){
            managerDropdown.setSelection(1);
        }
        else if(((DataManager) getApplication()).tetrisManager instanceof TetrisManagerSQL){
            managerDropdown.setSelection(2);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        removeBestScoreBtn = ((Button) findViewById(R.id._removeBestScoreBtn));
        removeBestScoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((DataManager)getApplication()).hasBestScore()){
                    try {
                        ((DataManager)getApplication()).deleteBestScore();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        saveSwitch = ((Switch) findViewById(R.id._saveSwitch));
        boolean switchValue = ((DataManager) getApplication()).userWantsToSave();
        saveSwitch.setChecked(switchValue);
        saveSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                managerDropdown.setEnabled(saveSwitch.isChecked());
            }
        });

        String[] items = new String[]{"Preferences", "Files", "SQL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        managerDropdown = ((Spinner) findViewById(R.id._managerSpinner));
        managerDropdown.setEnabled(saveSwitch.isChecked());
        managerDropdown.setAdapter(adapter);
        showSelectedManagerOnCreate();
    }
}