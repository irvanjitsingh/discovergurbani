package com.irvanjit.discovergurbani;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;


public class MainSettingsActivity extends ActionBarActivity {

    private int mThemeId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);

        if(savedInstanceState != null && savedInstanceState.getInt("theme", -1) != -1) {
            mThemeId = savedInstanceState.getInt("theme");
            this.setTheme(mThemeId);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("theme", mThemeId );
    }

    public void onDarkSwitchClicked(View view) {
        boolean on = ((Switch) view).isChecked();
        if (on) {
        } else {
        }
    }

}
