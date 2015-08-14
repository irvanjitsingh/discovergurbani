package com.irvanjit.discovergurbani;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class ShabadActivity extends ActionBarActivity{
    private static final String DEBUG_TAG = "HttpDebug";

    private static final String apiBase = "http://api.sikher.com";
    private TextView errorMessage;
    private TextView pangti;
    private TextView laridaar;
    private TextView translation;
    private TextView transliteration;
    private TextView ang;
    private TextView raag;
    private TextView author;
    private ListView shabadView;
    private String shabadId;
    private String translationId;
    private String transliterationId;
    private String angString;
    private String raagString;
    private String authorString;
    private ArrayList<HashMap<String, String>> shabadList;
    private ProgressDialog loading;
    private DialogFragment displayOptions;
    private LinearLayout displayOptionsView;
    private ListAdapter shabadDisplayAdapter;
    private Toast errorToast;
    private View mDecorView;
    private boolean laridaarMode;
    private boolean highlightPangti;
    private boolean firstLoad = true;
    private boolean shabadError;
    private int targetPangti;
    private int pangtiPosition;
    private int displayMode;
    private int displayModeShabad = 0;
    private int displayModeAng = 1;
    private int displayModeHukamnama = 2;
    private int actionBarHeight = 10;

    private int pangtiFontSize;
    private int laridaarFontSize;
    private int translationFontSize;
    private int transliterationFontSize;

    private int pangtiVisibility;
    private int laridaarVisibility;
    private int translationVisibility;
    private int transliterationVisibility;

    private final int defaultPangtiFontSize = 22;
    private final int defaultTransliterationFontSize = 16;
    private final int defaultTranslationFontSize = 14;

    private final int defaultPangtiVisibility = View.VISIBLE;
    private final int defaultLaridaarVisibility = View.GONE;
    private final int defaultTranslationVisibility = View.VISIBLE;;
    private final int defaultTransliterationVisibility = View.VISIBLE;;
    private final boolean defaultLaridaarEnabled = false;;


    public static final String PREFS_NAME = "DisplayPreferences";

    //JSON Nodes
    private static final String TAG_PANGTI_ID = "id";
    private static final String TAG_PANGTI = "text";
    private static final String TAG_LARIDAAR = "original";
    private static final String TAG_SHABAD = "hymn";
    private static final String TAG_TRANSLATION = "translation";
    private static final String TAG_TRANSLITERATION = "transliteration";
    private static final String TAG_ANG = "page";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_SECTION = "section";

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
        targetPangti = intent.getIntExtra("id", targetPangti);
        translationId = intent.getStringExtra(TAG_TRANSLATION);
        transliterationId = intent.getStringExtra(TAG_TRANSLITERATION);
        displayMode = intent.getIntExtra("displayMode", displayMode);
        shabadList = new ArrayList<HashMap<String, String>>();
        shabadView = (ListView) findViewById(R.id.shabadview);

        //setup sirlekh header
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        //setup display preferences preferences
        SharedPreferences displaySettings = getSharedPreferences(PREFS_NAME, 0);

        pangtiFontSize = displaySettings.getInt("pangtiFontSize", defaultPangtiFontSize);
        laridaarFontSize = pangtiFontSize;
        transliterationFontSize = displaySettings.getInt("transliterationFontSize", defaultTransliterationFontSize);
        translationFontSize = displaySettings.getInt("translationFontSize", defaultTranslationFontSize);

        pangtiVisibility = displaySettings.getInt("pangtiVisibility", defaultPangtiVisibility);
        laridaarVisibility = displaySettings.getInt("laridaarVisibility", defaultLaridaarVisibility);
        transliterationVisibility = displaySettings.getInt("transliterationVisibility", defaultTransliterationVisibility);
        translationVisibility = displaySettings.getInt("translationVisibility", defaultTranslationVisibility);
        laridaarMode = displaySettings.getBoolean("laridaarMode", defaultLaridaarEnabled);

        //setup display options
        //        displayOptions = new DisplayOptionsFragment();
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

        Switch gurmukhiSwitch = (Switch) findViewById(R.id.gurmukhiSwitch);
        Switch transliterationSwitch = (Switch) findViewById(R.id.transliterationSwitch);
        Switch translationSwitch = (Switch) findViewById(R.id.translationSwitch);
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
                if (!displayOptionsRect.contains((int)event.getX(), (int)event.getY())) {
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
                        Log.v("SCROLL", "Scrolled up");
                    } else if (this.height > height) {
                        //scroll down
                        hideSystemUI();
                        Log.v("SCROLL", "Scrolled down");
                    }
                }
                return false;
            }
        });

        //setup error toast
        Context context = getApplicationContext();
        CharSequence connError = "Check network connection";
        int duration = Toast.LENGTH_SHORT;
        errorToast = Toast.makeText(context, connError, duration);

        if (targetPangti == -1) {
            highlightPangti = false;
        } else {
            highlightPangti = true;
        }

        //execute api call
        if (isConnected()) {
            new DisplayShabadTask().execute(shabadId);
        } else {
            errorToast.show();
        }
        firstLoad = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        //save preferences
        SharedPreferences displaySettings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = displaySettings.edit();

        editor.putBoolean("laridaarMode", laridaarMode);
        editor.putInt("pangtiFontSize", pangtiFontSize);
        editor.putInt("transliterationFontSize", transliterationFontSize);
        editor.putInt("translationFontSize", translationFontSize);
        editor.putInt("pangtiVisibility", pangtiVisibility);
        editor.putInt("laridaarVisibility", laridaarVisibility);
        editor.putInt("transliterationVisibility", transliterationVisibility);
        editor.putInt("translationVisibility", translationVisibility);

        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shabad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            Intent settingsIntent = new Intent(getApplicationContext(), MainSettingsActivity.class);
//            startActivity(settingsIntent);
//            return true;
//        }
        if (id == R.id.action_display_options) {
            displayOptionsView.setVisibility(View.VISIBLE);
//            displayOptions.show(getSupportFragmentManager(), null);
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

        return super.onOptionsItemSelected(item);
    }

    // This snippet hides the system bars.
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

//    public static class DisplayOptionsFragment extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            LayoutInflater inflater = getActivity().getLayoutInflater();
//            View displayView = inflater.inflate(R.layout.shabad_display_options, null);
//            builder.setView(displayView);
//            return builder.create();
//        }
//
//        @Override
//        public void onStart() {
//            super.onStart();
//            Window window = getDialog().getWindow();
//            WindowManager.LayoutParams params = window.getAttributes();
//            window.setAttributes(params);
//            setDialogPosition();
//            Dialog dialogView = getDialog();
//            TextView gurmukhiToggleLabel = (TextView) dialogView.findViewById(R.id.gurmukhiFontLabel);
//            Typeface anmolBani = Typeface.createFromAsset((getActivity()).getAssets(), "fonts/AnmolUniBani-Bold.ttf");
//            gurmukhiToggleLabel.setTypeface(anmolBani);
//        }
//
//        private void setDialogPosition() {
//            Window window = getDialog().getWindow();
//            window.setGravity(Gravity.BOTTOM);
//
//            WindowManager.LayoutParams params = window.getAttributes();
//            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
//            window.setAttributes(params);
//        }
//    }

    public void toggleLaridaar() {
        laridaarMode = !laridaarMode;
        laridaarSetup();
        ((SimpleAdapter)((HeaderViewListAdapter)shabadView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
    }

    public void laridaarSetup() {
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

    public void resetDisplaySettings() {
        laridaarMode = defaultLaridaarEnabled;
        pangtiFontSize = defaultPangtiFontSize;
        laridaarFontSize = defaultPangtiFontSize;
        translationFontSize = defaultTranslationFontSize;
        transliterationFontSize = defaultTransliterationFontSize;
        pangtiVisibility = defaultPangtiVisibility;
        laridaarVisibility = defaultLaridaarVisibility;
        translationVisibility = defaultTranslationVisibility;
        transliterationVisibility = defaultTransliterationVisibility;
        ((SimpleAdapter)((HeaderViewListAdapter)shabadView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
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
//        ((SimpleAdapter) shabadView.getAdapter()).notifyDataSetChanged();
        ((SimpleAdapter)((HeaderViewListAdapter)shabadView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
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
        ((SimpleAdapter)((HeaderViewListAdapter)shabadView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
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
        ((SimpleAdapter)((HeaderViewListAdapter)shabadView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
    }

    public void toggleText(View view) {
        int id = view.getId();
        int setVisible;
        boolean switchOn = ((Switch) view).isChecked();
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
        ((SimpleAdapter)((HeaderViewListAdapter)shabadView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
    }

    private class DisplayShabadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //metadata header
            if (firstLoad) {
                View shabadHeader = getLayoutInflater().inflate(R.layout.shabad_header, null);
                shabadHeader.setPadding(15, actionBarHeight + (actionBarHeight / 2) + 10, 15, 15);
                shabadView.addHeaderView(shabadHeader);
            }
            errorMessage = (TextView)findViewById(R.id.shabadError);
            errorMessage.setVisibility(View.GONE);
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

            if (shabadError) {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(result);
            }

            //set header text
            ang = (TextView) findViewById(R.id.metaAng);
            raag = (TextView) findViewById(R.id.metaSection);
//            author = (TextView) findViewById(R.id.metaAuthor);
            ang.setText(angString);
            raag.setText(raagString);

            //init shabad adapter
            shabadDisplayAdapter = new ShabadDisplayAdapter(
                    ShabadActivity.this, shabadList,
                    R.layout.shabad_item, new String[]
                    {TAG_PANGTI, TAG_LARIDAAR, TAG_TRANSLATION, TAG_TRANSLITERATION},
                    new int[] { R.id.pangti, R.id.laridaar, R.id.translation, R.id.transliteration});
            shabadView.setAdapter(shabadDisplayAdapter);

            //set position to correct tukh
            shabadView.setSelection(pangtiPosition);
            loading.dismiss();
        }
    }

    public class ShabadDisplayAdapter extends SimpleAdapter {
        private ArrayList<HashMap<String, String>> results;

        public ShabadDisplayAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.results = data;
        }

        public View getView(int position, View view, ViewGroup parent) {
            Typeface anmolBani = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
            Typeface anmolBaniBold = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani-Bold.ttf");
            View v = view;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.shabad_item, null);
            }

            pangti = (TextView) v.findViewById(R.id.gurmukhi);
            laridaar = (TextView) v.findViewById(R.id.laridaar);
            translation = (TextView) v.findViewById(R.id.translation);
            transliteration = (TextView) v.findViewById(R.id.transliteration);

            pangti.setText(results.get(position).get(TAG_PANGTI));
            pangti.setTypeface(anmolBani);
            pangti.setTextSize(TypedValue.COMPLEX_UNIT_SP, pangtiFontSize);
            pangti.setVisibility(pangtiVisibility);
            laridaar.setText(results.get(position).get(TAG_LARIDAAR));
            laridaar.setTypeface(anmolBani);
            laridaar.setTextSize(TypedValue.COMPLEX_UNIT_SP, laridaarFontSize);
            laridaar.setVisibility(laridaarVisibility);

            translation.setText(results.get(position).get(TAG_TRANSLATION));
            translation.setTextSize(TypedValue.COMPLEX_UNIT_SP, translationFontSize);
            translation.setVisibility(translationVisibility);

            transliteration.setText(results.get(position).get(TAG_TRANSLITERATION));
            transliteration.setTextSize(TypedValue.COMPLEX_UNIT_SP, transliterationFontSize);
            transliteration.setVisibility(transliterationVisibility);

            if (highlightPangti && position == pangtiPosition) {
                pangti.setTypeface(anmolBaniBold);
                laridaar.setTypeface(anmolBaniBold);
            }
            if (!highlightPangti) {
                pangti.setTypeface(anmolBani);
                laridaar.setTypeface(anmolBani);
            }
            return v;
        }

        public boolean isEnabled(int position) {
            return false;
        }
    }

    private void gotoNextShabad(String id, boolean forward) {
        int inc;
        boolean enable = false;
        int nextShabad = Integer.parseInt(id);
        if (displayMode != displayModeShabad) {
            if ((forward && nextShabad < 1430) || (!forward && nextShabad > 1)) {
                enable = true;
            }
        } else if ((forward && nextShabad < 3620) || (!forward && nextShabad > 1)) {
            enable = true;
        } else {
            enable = false;
        }
        if (forward) {
            inc = 1;
        } else {
            inc = -1;
        }
        if (enable) {
            if (isConnected()) {
                new DisplayShabadTask().execute(String.valueOf(nextShabad + inc));
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
            urlString = apiBase+"/"+displayModeString+"/"+translationId+"/"+transliterationId;
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.toString());
            return null;
        }
        Log.d("URL ______", urlString);
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
    public String readJson(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        pangtiPosition = 0;
        String pangti = "Shabad";
        String laridaar = "Shabad-Laridaar";
        String translation = "Translation";
        String transliteration = "Transliteration";
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                HashMap<String, String> shabad = new HashMap<String, String>();
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
                shabad.put(TAG_PANGTI, pangti);
                shabad.put(TAG_LARIDAAR, laridaar);
                shabad.put(TAG_TRANSLATION, translation);
                shabad.put(TAG_TRANSLITERATION, transliteration);
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
