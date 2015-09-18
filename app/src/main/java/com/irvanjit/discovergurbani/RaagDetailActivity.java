package com.irvanjit.discovergurbani;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class RaagDetailActivity extends AppCompatActivity {

    private String nameEng;
    private String nameGurmukhi;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raag_detail);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //setup views
        TextView nameText = (TextView) findViewById(R.id.raag_name);
        TextView gurmukhiText = (TextView) findViewById(R.id.raag_gurmukhi);
        TextView descriptionText = (TextView) findViewById(R.id.raag_description );

        //get shabad info
        Intent intent = getIntent();
        nameEng = intent.getStringExtra(ShabadActivity.TAG_RAAG_NAME);
        nameGurmukhi = intent.getStringExtra(ShabadActivity.TAG_RAAG_GURMUKHI);
        description = intent.getStringExtra(ShabadActivity.TAG_RAAG_DESCRIPTION);

        nameText.setText(nameEng);
        gurmukhiText.setText(nameGurmukhi);
        descriptionText.setText(description);
        Typeface anmol = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
        gurmukhiText.setTypeface(anmol);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_raag_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent settingsIntent = new Intent(getApplicationContext(), AboutPageActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
