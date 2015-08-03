package com.irvanjit.discovergurbani;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView translation;
    private TextView transliteration;
    private ListView shabadView;
    private String shabadId;
    private String translationId;
    private String transliterationId;
    private ArrayList<HashMap<String, String>> shabadList;
    private ProgressDialog loading;
    private DialogFragment displayOptions;
    private ListAdapter shabadDisplayAdapter;
    private boolean highlightPangti;
    private boolean firstLoad = true;
    private int targetPangti;
    private int pangtiPosition;
    private int displayMode;
    private int displayModeShabad = 0;
    private int displayModeAng = 1;
    private int displayModeHukamnama = 2;
    private int angId = 1;
    private Toast errorToast;

    private int pangtiFontSize;
    private int translationFontSize;
    private int transliterationFontSize;

    private int pangtiVisibility;
    private int translationVisibility;
    private int transliterationVisibility;

    //JSON Nodes
    private static final String TAG_PANGTI_ID = "id";
    private static final String TAG_PANGTI = "text";
    private static final String TAG_SHABAD = "hymn";
    private static final String TAG_TRANSLATION = "translation";
    private static final String TAG_TRANSLITERATION = "transliteration";
    private static final String TAG_ANG = "page";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setContentView(R.layout.activity_shabad);

        Intent intent = getIntent();
        shabadId = intent.getStringExtra(TAG_SHABAD);
        targetPangti = intent.getIntExtra("id", targetPangti);
        translationId = intent.getStringExtra(TAG_TRANSLATION);
        transliterationId = intent.getStringExtra(TAG_TRANSLITERATION);
        displayMode = intent.getIntExtra("displayMode", displayMode);
        shabadList = new ArrayList<HashMap<String, String>>();
        shabadView = (ListView) findViewById(R.id.shabadview);
        displayOptions = new DisplayOptionsFragment();
        errorMessage = (TextView) findViewById(R.id.shabadError);
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
        if (isConnected()) {
            new DisplayShabadTask().execute(shabadId);
        } else {
            errorToast.show();
        }
        firstLoad = false;
//        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
            displayOptions.show(getSupportFragmentManager(), null);
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


    public static class DisplayOptionsFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View displayView = inflater.inflate(R.layout.shabad_display_options, null);
            builder.setView(displayView);
            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            window.setAttributes(params);
            setDialogPosition();
            Dialog dialogView = getDialog();
            TextView gurmukhiToggleLabel = (TextView) dialogView.findViewById(R.id.gurmukhiFontLabel);
            Typeface anmolBani = Typeface.createFromAsset((getActivity()).getAssets(), "fonts/AnmolUniBani-Bold.ttf");
            gurmukhiToggleLabel.setTypeface(anmolBani);
        }

        private void setDialogPosition() {
            Window window = getDialog().getWindow();
            window.setGravity(Gravity.BOTTOM);

            WindowManager.LayoutParams params = window.getAttributes();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
    }

    public void toggleGurmukhiSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseGurmukhiSize) {
            if (pangtiFontSize > 15) {
                pangtiFontSize -= 2;
            }
        } else if (id == R.id.increaseGurmukhiSize) {
            if (pangtiFontSize < 30) {
                pangtiFontSize += 2;
            }
        }
        ((SimpleAdapter) shabadView.getAdapter()).notifyDataSetChanged();
    }

    public void toggleTranslationSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseTranslationSize) {
            if (translationFontSize > 15) {
                translationFontSize -= 2;
            }
        } else if (id == R.id.increaseTranslationSize) {
            if (translationFontSize < 30) {
                translationFontSize += 2;
            }
        }
        ((SimpleAdapter) shabadView.getAdapter()).notifyDataSetChanged();
    }

    public void toggleTransliterationSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseTransliterationSize) {
            if (transliterationFontSize > 15) {
                transliterationFontSize -= 2;
            }
        } else if (id == R.id.increaseTransliterationSize) {
            if (transliterationFontSize < 30) {
                transliterationFontSize += 2;
            }
        }
        ((SimpleAdapter) shabadView.getAdapter()).notifyDataSetChanged();
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
        } else if (id == R.id.translationSwitch) {
            translationVisibility = setVisible;
        } else if (id == R.id.transliterationSwitch) {
            transliterationVisibility = setVisible;
        }
        ((SimpleAdapter) shabadView.getAdapter()).notifyDataSetChanged();
    }

    private class DisplayShabadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                return "Could not load the shabad.";
            }
        }
        @Override
        protected void onPostExecute(String result) {

            //set shabad display defaults
            pangtiFontSize = 25;
            pangtiVisibility = View.VISIBLE;

            translationFontSize = 20;
            translationVisibility = View.VISIBLE;

            transliterationFontSize = 20;
            transliterationVisibility = View.VISIBLE;


            errorMessage.setText(result);
            shabadDisplayAdapter = new ShabadDisplayAdapter(
                    ShabadActivity.this, shabadList,
                    R.layout.shabad_item, new String[]
                    {TAG_PANGTI, TAG_TRANSLATION, TAG_TRANSLITERATION},
                    new int[] { R.id.pangti, R.id.translation, R.id.transliteration});
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
                v = vi.inflate(R.layout.search_item, null);
            }
            pangti = (TextView) v.findViewById(R.id.pangti);
            translation = (TextView) v.findViewById(R.id.translation);
            transliteration = (TextView) v.findViewById(R.id.transliteration);

            pangti.setText(results.get(position).get(TAG_PANGTI));
            pangti.setTypeface(anmolBani);
            pangti.setTextSize(TypedValue.COMPLEX_UNIT_SP, pangtiFontSize);
            pangti.setVisibility(pangtiVisibility);

            translation.setText(results.get(position).get(TAG_TRANSLATION));
            translation.setTextSize(TypedValue.COMPLEX_UNIT_SP, translationFontSize);
            translation.setVisibility(translationVisibility);


            transliteration.setText(results.get(position).get(TAG_TRANSLITERATION));
            transliteration.setTextSize(TypedValue.COMPLEX_UNIT_SP, transliterationFontSize);
            transliteration.setVisibility(transliterationVisibility);

            if (highlightPangti && position == pangtiPosition) {
                pangti.setTypeface(anmolBaniBold);
            }
            if (!highlightPangti) {
                pangti.setTypeface(anmolBani);
            }
            return v;
        }

        public boolean isEnabled(int position) {
            return false;
        }
    }

//    String queryBuilder(String shabadId) {
//        String urlString = "";
//        try {
//            urlString = apiBase+""+shabadId+"/"+translationId+"/"+transliterationId;
//        } catch (Exception e) {
//            Log.d(DEBUG_TAG, e.toString());
//        }
//        return urlString;
//    }
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

    // Reads an InputStream and converts it to a String.
    public String readJson(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        pangtiPosition = 0;
        String pangti = "Shabad";
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
                    } else if (displayMode == displayModeHukamnama && name.equals(TAG_ANG)) {
                        shabadId = String.valueOf(reader.nextInt());
                        displayMode = displayModeAng;
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
