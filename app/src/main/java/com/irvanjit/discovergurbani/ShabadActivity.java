package com.irvanjit.discovergurbani;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ShabadActivity extends AppCompatActivity {

    private TextView errorMessage;
    private TextView pangti;
    private TextView laridaar;
    private TextView translation;
    private TextView transliteration;
    private TextView ang;
    private TextView raag;
    private TextView author;
    private RecyclerView shabadView;
    private String shabadId;
    private String translationId;
    private String transliterationId;
    private String angString;
    private String raagString;
    private String authorString;
    private ArrayList<ShabadList> shabadList;
    private ProgressDialog loading;
    private LinearLayout displayOptionsView;
    private MediaPlayer shabadAudio;
    private Toast errorToast;
    private View mDecorView;
    private boolean displayWakeMode;
    private boolean laridaarMode;
    private boolean highlightPangti;
    private boolean firstLoad = true;
    private boolean shabadError;
    private int targetPangti;
    private int pangtiPosition;
    private int displayMode;
    private int actionBarHeight = 10;

    private int pangtiFontSize;
    private int laridaarFontSize;
    private int translationFontSize;
    private int transliterationFontSize;

    private int pangtiVisibility;
    private int laridaarVisibility;
    private int translationVisibility;
    private int transliterationVisibility;

    private final int displayModeShabad = 0;
    private final int displayModeAng = 1;
    private final int displayModeHukamnama = 2;

    private final int defaultPangtiFontSize = 22;
    private final int defaultTransliterationFontSize = 16;
    private final int defaultTranslationFontSize = 14;

    private final int defaultPangtiVisibility = View.VISIBLE;
    private final int defaultLaridaarVisibility = View.GONE;
    private final int defaultTranslationVisibility = View.VISIBLE;
    private final int defaultTransliterationVisibility = View.VISIBLE;
    private final boolean defaultLaridaarEnabled = false;

    public static final String DEFAULT_TRANSLATION_ID = "13";
    public static final String DEFAULT_TRANSLITERATION_ID = "69";

    private SharedPreferences preferences;

    public static final String DEBUG_TAG = "HttpDebug";

    public static final String URL_API_BASE = "http://api.sikher.com";
    private static final String URL_AUDIO_BASE = "http://media.sikher.com/audio";

    //JSON Nodes
    public static final String TAG_PANGTI_ID = "id";
    public static final String TAG_PANGTI = "text";
    public static final String TAG_LARIDAAR = "original";
    public static final String TAG_SHABAD = "hymn";
    public static final String TAG_ANG = "page";
    public static final String TAG_SECTION = "section";
    public static final String TAG_AUTHOR = "author";
    public static final String TAG_TRANSLATION = "translation";
    public static final String TAG_TRANSLITERATION = "transliteration";
    public static final String TAG_SCRIPTURE = "scripture";
    public static final String TAG_META = "meta";
    public static final String TAG_RAAG_ID = "id";
    public static final String TAG_RAAG_NAME = "melody";
    public static final String TAG_RAAG_GURMUKHI = "gurmukhi";
    public static final String TAG_RAAG_DESCRIPTION = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_shabad);

        //get shabad info
        Intent intent = getIntent();
        shabadId = intent.getStringExtra(TAG_SHABAD);
        targetPangti = intent.getIntExtra(TAG_PANGTI_ID, targetPangti);
        translationId = intent.getStringExtra(TAG_TRANSLATION);
        transliterationId = intent.getStringExtra(TAG_TRANSLITERATION);
        displayMode = intent.getIntExtra("displayMode", displayMode);
        shabadList = new ArrayList<ShabadList>();
        shabadView = (RecyclerView) findViewById(R.id.shabadview);
        shabadView.hasFixedSize();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        shabadView.setLayoutManager(llm);

        //setup sirlekh header
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        //load preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        //load aesthetics
        pangtiFontSize = preferences.getInt(SettingsActivity.KEY_PREF_FONT_SIZE_GURMUKHI, defaultPangtiFontSize);
        laridaarFontSize = pangtiFontSize;
        transliterationFontSize = preferences.getInt(SettingsActivity.KEY_PREF_FONT_SIZE_TRANSLITERATION, defaultTransliterationFontSize);
        translationFontSize = preferences.getInt(SettingsActivity.KEY_PREF_FONT_SIZE_TRANSLATION, defaultTranslationFontSize);
        pangtiVisibility = preferences.getInt(SettingsActivity.KEY_PREF_VISIBILITY_GURMUKHI, defaultPangtiVisibility);
        laridaarVisibility = preferences.getInt(SettingsActivity.KEY_PREF_VISIBILITY_LARIDAAR, defaultLaridaarVisibility);
        transliterationVisibility = preferences.getInt(SettingsActivity.KEY_PREF_VISIBILITY_TRANSLITERATION, defaultTransliterationVisibility);
        translationVisibility = preferences.getInt(SettingsActivity.KEY_PREF_VISIBILITY_TRANSLATION, defaultTranslationVisibility);
        laridaarMode = preferences.getBoolean(SettingsActivity.KEY_PREF_ENABLE_LARIDAAR_MODE, defaultLaridaarEnabled);

        //setup display lock
        displayWakeMode = preferences.getBoolean(SettingsActivity.KEY_PREF_ENABLE_DISPLAY_WAKE, false);
        if (displayWakeMode) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        //save language prefs
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SettingsActivity.KEY_PREF_SEARCH_LANGUAGE, translationId);
        editor.putString(SettingsActivity.KEY_PREF_SEARCH_SCRIPT, transliterationId);
        editor.apply();

        //setup display options
        displayOptionsView = (LinearLayout) findViewById(R.id.display_options);
        displayOptionsView.setVisibility(View.GONE);
        TextView bottomText = (TextView) findViewById(R.id.dpoBottom);
        Typeface anmolRegular = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
        Typeface anmolBold = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani-Bold.ttf");
        bottomText.setTypeface(anmolRegular);
        int bottomBarHeight = 0;
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            bottomBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        bottomText.setHeight(bottomBarHeight + 2);
        TextView gurmukhiToggleLabel = (TextView) findViewById(R.id.gurmukhiFontLabel);
        gurmukhiToggleLabel.setTypeface(anmolBold);

        SwitchCompat gurmukhiSwitch = (SwitchCompat) findViewById(R.id.gurmukhiSwitch);
        SwitchCompat transliterationSwitch = (SwitchCompat) findViewById(R.id.transliterationSwitch);
        SwitchCompat translationSwitch = (SwitchCompat) findViewById(R.id.translationSwitch);
        gurmukhiSwitch.setChecked(pangtiVisibility == View.VISIBLE || laridaarVisibility == View.VISIBLE);
        transliterationSwitch.setChecked(transliterationVisibility == View.VISIBLE);
        translationSwitch.setChecked(translationVisibility == View.VISIBLE);

        //setup shabad scrolling
        mDecorView = getWindow().getDecorView();
        showSystemUI();
        shabadView.setOnTouchListener(new View.OnTouchListener() {
            float height;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect displayOptionsRect = new Rect();
                displayOptionsView.getHitRect(displayOptionsRect);
                if (!displayOptionsRect.contains((int) event.getX(), (int) event.getY())) {
                    displayOptionsView.setVisibility(View.GONE);
                }
                int action = event.getAction();
                float height = event.getY();
                if (action == MotionEvent.ACTION_DOWN) {
                    this.height = height;
                } else if (action == MotionEvent.ACTION_UP) {
                    if (this.height < height) {
                        //scroll up
                        showSystemUI();
                    } else if (this.height > height) {
                        //scroll down
                        hideSystemUI();
                    }
                }
                return false;
            }
        });

        //setup error toast
        CharSequence connError = "Check network connection";
        int duration = Toast.LENGTH_SHORT;
        errorToast = Toast.makeText(getApplicationContext(), connError, duration);

        highlightPangti = targetPangti != -1;

        //execute api call
        if (isConnected()) {
            new DisplayShabadTask().execute(shabadId);
        } else {
            errorToast.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAudioStream();

        //save preferences
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(SettingsActivity.KEY_PREF_ENABLE_LARIDAAR_MODE, laridaarMode);
        editor.putInt(SettingsActivity.KEY_PREF_FONT_SIZE_GURMUKHI, pangtiFontSize);
        editor.putInt(SettingsActivity.KEY_PREF_FONT_SIZE_TRANSLITERATION, transliterationFontSize);
        editor.putInt(SettingsActivity.KEY_PREF_FONT_SIZE_TRANSLATION, translationFontSize);
        editor.putInt(SettingsActivity.KEY_PREF_VISIBILITY_GURMUKHI, pangtiVisibility);
        editor.putInt(SettingsActivity.KEY_PREF_VISIBILITY_LARIDAAR, laridaarVisibility);
        editor.putInt(SettingsActivity.KEY_PREF_VISIBILITY_TRANSLITERATION, transliterationVisibility);
        editor.putInt(SettingsActivity.KEY_PREF_VISIBILITY_TRANSLATION, translationVisibility);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayWakeMode = preferences.getBoolean(SettingsActivity.KEY_PREF_ENABLE_DISPLAY_WAKE, false);
        if (displayWakeMode) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shabad, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem startAction = menu.findItem(R.id.action_audio_start);
        MenuItem stopAction = menu.findItem(R.id.action_audio_stop);
        MenuItem pauseAction = menu.findItem(R.id.action_audio_pause);

        if (shabadAudio != null) {
            if (shabadAudio.isPlaying()) {
                startAction.setEnabled(false);
                stopAction.setEnabled(true);
                pauseAction.setEnabled(true);
            } else {
                startAction.setEnabled(true);
                pauseAction.setEnabled(false);
            }
        } else {
            stopAction.setEnabled(false);
            pauseAction.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        if (id == R.id.action_display_options) {
            displayOptionsView.setVisibility(View.VISIBLE);
            return true;
        }
        if (id == R.id.action_laridaar) {
            toggleLaridaar();
            return true;
        }
        if (id == R.id.action_reset) {
            resetDisplaySettings();
            return true;
        }
        if (id == R.id.action_previous_button) {
            gotoNextShabad(shabadId, false);
            return true;
        }
        if (id == R.id.action_next_button) {
            gotoNextShabad(shabadId, true);
            return true;
        }
        if (id == R.id.action_audio_start) {
            if (shabadAudio != null) {
                resumeAudioStream();
            } else {
                startAudioStream();
            }
            return true;
        }
        if (id == R.id.action_audio_stop) {
            if (shabadAudio != null) {
                 stopAudioStream();
            }
            return true;
        }
        if (id == R.id.action_audio_pause) {
            if (shabadAudio != null) {
                if (shabadAudio.isPlaying()) {
                    pauseAudioStream();
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @TargetApi(16)
//    @SuppressWarnings("deprecation")
//    public void cleanAudio(MediaPlayer player) {
//        if (android.os.Build.VERSION.SDK_INT >= 16) {
//            noiseSuppressed = NoiseSuppressor.isAvailable();
//        } else if (android.os.Build.VERSION.SDK_INT < 16) {
//            noiseSuppressed = false;
//        }
//        if (noiseSuppressed) {
//            NoiseSuppressor ns = NoiseSuppressor.create(player.getAudioSessionId());
//            ns.setEnabled(true);
//        }
//        Equalizer equalizer = new Equalizer(0,player.getAudioSessionId());
//        equalizer.setEnabled(true);
//        //presets: index[Normal, Classical, Dance, Flat, Folk, Heavy Metal, Hip Hop, Jazz, Pop, Rock]
//        equalizer.usePreset((short) 6);
//    }

    private void startAudioStream() {
        shabadAudio = new MediaPlayer();
//        cleanAudio(shabadAudio);
        shabadAudio.setAudioStreamType(AudioManager.STREAM_MUSIC);
        String url = URL_AUDIO_BASE +"/sggsj/sggsj-"+angString+".mp3";
        Toast.makeText(this, "Starting audio stream", Toast.LENGTH_SHORT).show();
        try {
            shabadAudio.setDataSource(url);
            shabadAudio.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, "An error occurred with the stream", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        shabadAudio.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer player) {
                player.start();
            }
        });

        this.invalidateOptionsMenu();
    }

    private void stopAudioStream() {
        if (shabadAudio != null) {
            Toast.makeText(this, "Stopping playback", Toast.LENGTH_SHORT).show();
            shabadAudio.stop();
            shabadAudio.release();
            shabadAudio = null;
        }
        this.invalidateOptionsMenu();
    }

    private void pauseAudioStream() {
        if (shabadAudio != null) {
            if (shabadAudio.isPlaying()) {
                Toast.makeText(this, "Pausing playback", Toast.LENGTH_SHORT).show();
                shabadAudio.pause();
            }
        }
        this.invalidateOptionsMenu();
    }

    private void resumeAudioStream() {
        if (shabadAudio != null) {
            if (!shabadAudio.isPlaying()) {
                Toast.makeText(this, "Resuming playback", Toast.LENGTH_SHORT).show();
                shabadAudio.start();
            }
        }
        this.invalidateOptionsMenu();
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void toggleLaridaar() {
        laridaarMode = !laridaarMode;
        laridaarSetup();
        shabadView.getAdapter().notifyDataSetChanged();
    }

    private void laridaarSetup() {
        if (pangtiVisibility == View.VISIBLE || laridaarVisibility == View.VISIBLE) {
            if (laridaarMode) {
                pangtiVisibility = View.GONE;
                laridaarVisibility = View.VISIBLE;
            } else {
                pangtiVisibility = View.VISIBLE;
                laridaarVisibility = View.GONE;
            }
        }
    }

    private void resetDisplaySettings() {
        laridaarMode = defaultLaridaarEnabled;
        pangtiFontSize = defaultPangtiFontSize;
        laridaarFontSize = defaultPangtiFontSize;
        translationFontSize = defaultTranslationFontSize;
        transliterationFontSize = defaultTransliterationFontSize;
        pangtiVisibility = defaultPangtiVisibility;
        laridaarVisibility = defaultLaridaarVisibility;
        translationVisibility = defaultTranslationVisibility;
        transliterationVisibility = defaultTransliterationVisibility;

        shabadView.getAdapter().notifyDataSetChanged();

        SwitchCompat gurmukhiSwitch = (SwitchCompat) findViewById(R.id.gurmukhiSwitch);
        SwitchCompat transliterationSwitch = (SwitchCompat) findViewById(R.id.transliterationSwitch);
        SwitchCompat translationSwitch = (SwitchCompat) findViewById(R.id.translationSwitch);
        gurmukhiSwitch.setChecked(pangtiVisibility == View.VISIBLE || laridaarVisibility == View.VISIBLE);
        transliterationSwitch.setChecked(transliterationVisibility == View.VISIBLE);
        translationSwitch.setChecked(translationVisibility == View.VISIBLE);
    }

    public void toggleGurmukhiSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseGurmukhiSize) {
            if (pangtiFontSize > 15) {
                pangtiFontSize -= 2;
                laridaarFontSize -= 2;
            }
        } else if (id == R.id.increaseGurmukhiSize) {
            if (pangtiFontSize < 30) {
                pangtiFontSize += 2;
                laridaarFontSize += 2;
            }
        }
        shabadView.getAdapter().notifyDataSetChanged();

    }

    public void toggleTranslationSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseTranslationSize) {
            if (translationFontSize > 10) {
                translationFontSize -= 2;
            }
        } else if (id == R.id.increaseTranslationSize) {
            if (translationFontSize < 30) {
                translationFontSize += 2;
            }
        }
        shabadView.getAdapter().notifyDataSetChanged();

    }

    public void toggleTransliterationSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseTransliterationSize) {
            if (transliterationFontSize > 10) {
                transliterationFontSize -= 2;
            }
        } else if (id == R.id.increaseTransliterationSize) {
            if (transliterationFontSize < 30) {
                transliterationFontSize += 2;
            }
        }
        shabadView.getAdapter().notifyDataSetChanged();

    }

    public void toggleText(View view) {
        int id = view.getId();
        int setVisible;
        boolean switchOn = ((SwitchCompat) view).isChecked();
        if (switchOn) {
            setVisible = View.VISIBLE;
        } else {
            setVisible = View.GONE;
        }
        if (id == R.id.gurmukhiSwitch) {
            pangtiVisibility = setVisible;
            laridaarVisibility = setVisible;
            if (switchOn) {
                laridaarSetup();
            }
        } else if (id == R.id.translationSwitch) {
            translationVisibility = setVisible;
        } else if (id == R.id.transliterationSwitch) {
            transliterationVisibility = setVisible;
        }
        shabadView.getAdapter().notifyDataSetChanged();

    }

    private class DisplayShabadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //metadata header
            if (firstLoad) {
//                shabadHeader.setMinimumWidth(getActionBar().);
//                shabadView.addItemDecoration(new HeaderDecoration(shabadHeader));
            }
            errorMessage = (TextView)findViewById(R.id.shabadError);
//            errorMessage.setVisibility(View.GONE);
            shabadError = false;
            shabadList.clear();
            if (displayMode != displayModeShabad || !firstLoad) {
                highlightPangti = false;
            }
            loading = new ProgressDialog(ShabadActivity.this);
            loading.setMessage("Loading");
            loading.setCancelable(true);
            loading.setCanceledOnTouchOutside(false);
            loading.setIndeterminate(false);
            loading.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            try {
                return getData(urls[0]);
            } catch (IOException e) {
                shabadError = true;
                return "An error occurred. Could not load the shabad.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
//            if (shabadError) {
//                errorMessage.setVisibility(View.VISIBLE);
//                errorMessage.setText(result);
//            }
            ShabadAdapter shabadAdapter = new ShabadAdapter();
            shabadView.setAdapter(shabadAdapter);

            //set position to correct tukh
            if (highlightPangti) {
                shabadView.getLayoutManager().scrollToPosition(pangtiPosition-1);
            }
            loading.dismiss();
            firstLoad = false;
        }
    }

    public class ShabadList {
        protected String pangti;
        protected String laridaar;
        protected String translation;
        protected String transliteration;
    }

    public class ShabadAdapter extends RecyclerView.Adapter<ShabadAdapter.ShabadViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        Typeface anmolBani = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
        Typeface anmolBaniBold = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani-Bold.ttf");

        public ShabadAdapter() {
        }

        public class ShabadViewHolder extends RecyclerView.ViewHolder {
            protected TextView vPangti;
            protected TextView vLaridaar;
            protected TextView vTranslation;
            protected TextView vTransliteration;

//            protected TextView vRaag;
//            protected TextView vAuthor;
//            protected TextView vAng;
//            protected TextView vError;

            public ShabadViewHolder(View v) {
                super(v);
                vPangti =  (TextView) v.findViewById(R.id.gurmukhi);
                vLaridaar = (TextView) v.findViewById(R.id.laridaar);
                vTranslation = (TextView)  v.findViewById(R.id.translation);
                vTransliteration = (TextView)  v.findViewById(R.id.transliteration);
            }

//            public ShabadViewHolder(View v, boolean isHeader) {
//                super(v);
//                vRaag =  (TextView) v.findViewById(R.id.metaSection);
//                vAuthor = (TextView) v.findViewById(R.id.metaAuthor);
//                vAng = (TextView)  v.findViewById(R.id.metaAng);
//                vError = (TextView)  v.findViewById(R.id.shabadError);
//            }
        }

        @Override
        public int getItemCount() {
            return shabadList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }

        @Override
        public void onBindViewHolder(ShabadViewHolder shabadHolder, int position) {
            ShabadList item = shabadList.get(position);

            shabadHolder.vPangti.setText(item.pangti);
            shabadHolder.vPangti.setTypeface(anmolBani);
            shabadHolder.vPangti.setTextSize(TypedValue.COMPLEX_UNIT_SP, pangtiFontSize);
            shabadHolder.vPangti.setVisibility(pangtiVisibility);

            shabadHolder.vLaridaar.setText(item.laridaar);
            shabadHolder.vLaridaar.setTypeface(anmolBani);
            shabadHolder.vLaridaar.setTextSize(TypedValue.COMPLEX_UNIT_SP, laridaarFontSize);
            shabadHolder.vLaridaar.setVisibility(laridaarVisibility);

            shabadHolder.vTranslation.setText(item.translation);
            shabadHolder.vTranslation.setTextSize(TypedValue.COMPLEX_UNIT_SP, translationFontSize);
            shabadHolder.vTranslation.setVisibility(translationVisibility);

            shabadHolder.vTransliteration.setText(item.transliteration);
            shabadHolder.vTransliteration.setTextSize(TypedValue.COMPLEX_UNIT_SP, transliterationFontSize);
            shabadHolder.vTransliteration.setVisibility(transliterationVisibility);

            if (highlightPangti && position == pangtiPosition) {
                shabadHolder.vPangti.setTypeface(anmolBaniBold);
                shabadHolder.vLaridaar.setTypeface(anmolBaniBold);
            }
            if (!highlightPangti) {
                shabadHolder.vPangti.setTypeface(anmolBani);
                shabadHolder.vLaridaar.setTypeface(anmolBani);
                shabadHolder.vTranslation.setTypeface(null, Typeface.NORMAL);
                shabadHolder.vTransliteration.setTypeface(null, Typeface.NORMAL);
            }
        }

        @Override
        public ShabadViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View rowView;
            if (i == TYPE_HEADER) {
                rowView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_item, viewGroup, false);
                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(15, actionBarHeight + (actionBarHeight / 2) + 10, 15, 15);
                rowView.setLayoutParams(params);
            } else {
                rowView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shabad_item, viewGroup, false);
            }
            return new ShabadViewHolder(rowView);
        }
    }

    private void gotoNextShabad(String id, boolean forward) {
        stopAudioStream();
        int inc;
        if (forward) {
            inc = 1;
        } else {
            inc = -1;
        }
        int nextShabad = Integer.parseInt(id) + inc;
        int upperLimit;
        if (displayMode == displayModeShabad) {
            upperLimit = 3620;
        } else {
            upperLimit = 1430;
        }
        boolean enable = (forward && nextShabad <= upperLimit) || (!forward && nextShabad >= 1);
        if (enable) {
            if (isConnected()) {
                new DisplayShabadTask().execute(String.valueOf(nextShabad));
                try {
                    shabadId = String.valueOf(Integer.parseInt(shabadId) + inc);
                } catch (Exception e) {
                }
            } else {
                errorToast.show();
            }
        }
    }

    private String queryBuilder(String shabadId) throws IOException {
        String urlString;
        try {
            String displayModeString = "hymn/"+shabadId;
            if (displayMode == displayModeAng) {
                displayModeString = "page/"+shabadId;
            } else if (displayMode == displayModeHukamnama) {
                displayModeString = "random/1";
            }
            urlString = URL_API_BASE+"/"+displayModeString+"/"+translationId+"/"+transliterationId;
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.toString());
            return null;
        }
        return urlString;
    }

    private String getData(String shabadId) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(queryBuilder(shabadId));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            return readJson(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    //shabad json parser
    private String readJson(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        pangtiPosition = 1;
        String pangti = "Shabad";
        String laridaar = "Shabad-Laridaar";
        String translation = "Translation";
        String transliteration = "Transliteration";
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                ShabadList shabad = new ShabadList();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (highlightPangti && name.equals(TAG_PANGTI_ID)) {
                        if (reader.nextInt() < targetPangti) {
                            pangtiPosition ++;
                        }
                    } else if (name.equals(TAG_PANGTI)) {
                        pangti = reader.nextString();
                    } else if (name.equals(TAG_LARIDAAR)) {
                        laridaar = reader.nextString();
                    } else if (name.equals(TAG_ANG)) {
                        angString = String.valueOf(reader.nextInt());
                        if (displayMode == displayModeHukamnama) {
                            shabadId = angString;
                            displayMode = displayModeAng;
                        }
                    } else if (name.equals(TAG_SECTION)) {
                        raagString = reader.nextString();
                    } else if (name.equals(TAG_TRANSLATION)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(TAG_PANGTI)) {
                                translation = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else if (name.equals(TAG_TRANSLITERATION)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(TAG_PANGTI)) {
                                transliteration = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                if (shabadList.isEmpty()) {
                    ShabadList header = new ShabadList();
                    header.pangti = "";
                    header.laridaar = "";
                    header.translation = "Ang: "+angString;
                    header.transliteration = raagString;
                    shabadList.add(header);
                }
                shabad.pangti = pangti;
                shabad.laridaar = laridaar;
                shabad.translation = translation;
                shabad.transliteration = transliteration;
                shabadList.add(shabad);
                reader.endObject();
            }
            reader.endArray();
        } finally {
            reader.close();
        }
        return null;
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
