package com.irvanjit.discovergurbani;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class ShabadActivity extends ActionBarActivity {
    private static final String DEBUG_TAG = "HttpDebug";

    private static final String apiBase = "http://api.sikher.com/";
    private TextView textView;
    private TextView translationText;
    private TextView pangti;
    private TextView translation;
    private TextView transliteration;
    private ListView listView;
    private String translationId;
    private String transliterationId;
    private ArrayList<HashMap<String, String>> shabadList;
    private ProgressDialog loading;
    private DialogFragment displayOptions;
    private ListAdapter shabadDisplayAdapter;
    private Switch translationSwitch;
    private int targetPangti;
    private int pangtiPosition;

    private int pangtiFontSize;
    private int pangtiVisibility;

    //JSON Nodes
    private static final String TAG_PANGTI_ID = "id";
    private static final String TAG_PANGTI = "text";
    private static final String TAG_SHABAD = "hymn";
    private static final String TAG_RAAG = "section";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_TRANSLATION = "translation";
    private static final String TAG_TRANSLITERATION = "transliteration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setContentView(R.layout.activity_shabad);
        textView = (TextView) findViewById(R.id.result);
        Intent intent = getIntent();
        String shabadId = intent.getStringExtra(TAG_SHABAD);
        targetPangti = intent.getIntExtra("id", targetPangti);
        translationId = intent.getStringExtra(TAG_TRANSLATION);
        transliterationId = intent.getStringExtra(TAG_TRANSLITERATION);
        shabadList = new ArrayList<HashMap<String, String>>();
        listView = (ListView) findViewById(R.id.shabadview);
        displayOptions = new DisplayOptionsFragment();
        //setup error toast
        Context context = getApplicationContext();
        CharSequence connError = "Not connected";
        int duration = Toast.LENGTH_SHORT;
        Toast errorToast = Toast.makeText(context, connError, duration);

        if (isConnected()) {
            new DisplayShabadTask().execute(shabadId);
        } else {
            errorToast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shabad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            translation.setVisibility(View.GONE);
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_display_options) {
            displayOptions.show(getSupportFragmentManager(), null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static class DisplayOptionsFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View displayView = inflater.inflate(R.layout.shabad_display_options, null);
            builder.setView(displayView);
//            builder.setMessage("...").setTitle("Display Options");
//                    .setPositiveButton("set", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                        }
//                    })
//                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                        }
//                    });
            // Create the AlertDialog object and return it
            return builder.create();

//            translationSwitch = (Switch) findViewById(R.id.translation_switch);
        }

        @Override
        public void onStart() {
            super.onStart();
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            window.setAttributes(params);
            setDialogPosition();
        }

        private void setDialogPosition() {
            Window window = getDialog().getWindow();
            window.setGravity(Gravity.BOTTOM);

            WindowManager.LayoutParams params = window.getAttributes();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
    }

    public void toggleFontSize(View view) {
        int id = view.getId();
        if (id == R.id.decreaseGurmukhiSize) {
            if (pangtiFontSize > 15) {
                pangtiFontSize -= 2;
                ((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        } else if (id == R.id.increaseGurmukhiSize) {
            if (pangtiFontSize < 30) {
                pangtiFontSize += 2;
                ((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    private class DisplayShabadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
//                Request request = new Request(urls[0]);
//                String result = readJson(request.downloadUrl());
//                return readJson(request.downloadUrl());
                return getData(urls[0]);
            } catch (IOException e) {
                return "Could not display the shabad.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            pangtiFontSize = 25;
            pangtiVisibility = View.VISIBLE;
            textView.setText(result);
            shabadDisplayAdapter = new ShabadDisplayAdapter(
                    ShabadActivity.this, shabadList,
                    R.layout.shabad_item, new String[]
                    {TAG_PANGTI, TAG_TRANSLATION, TAG_TRANSLITERATION},
                    new int[] { R.id.pangti, R.id.translation, R.id.transliteration});
            listView.setAdapter(shabadDisplayAdapter);
            listView.setSelection(pangtiPosition);
            loading.dismiss();
//            listView.notify();
//            shabadDisplayAdapter.notify();
            //set pangti position
//            listView.setSelection(2);

//            listView.smoothScrollToPosition(2);
//            Log.d("FIRST PANGTI::", String.valueOf(firstPangti));
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
            translation.setText(results.get(position).get(TAG_TRANSLATION));
            transliteration.setText(results.get(position).get(TAG_TRANSLITERATION));
            pangti.setTypeface(anmolBani);
            pangti.setTextSize(TypedValue.COMPLEX_UNIT_SP, pangtiFontSize);
            pangti.setVisibility(pangtiVisibility);
            if (position == pangtiPosition) {
                pangti.setTypeface(anmolBaniBold);
                translation.setTypeface(null, Typeface.BOLD);
                transliteration.setTypeface(null, Typeface.BOLD);
            }
            return v;
        }

        public boolean isEnabled(int position) {
            return false;
        }
    }

//    public void toggleTranslation(View view) {
//        hideTranslation = !hideTranslation;
//        shabadDisplayAdapter.notify();
//    }

//        translationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    // The toggle is enabled
//                } else {
//                    // The toggle is disabled
//                }
//            }
//        };

    String queryBuilder(String shabadId) {
        String urlString = "";
        try {
            urlString = apiBase+"hymn/"+shabadId+"/"+translationId+"/"+transliterationId;
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.toString());
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
    public String readJson(InputStream in) throws IOException, UnsupportedEncodingException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        int pangti_id = 0;
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
                    if (name.equals(TAG_PANGTI_ID)) {
                        if (reader.nextInt() < targetPangti) {
                            pangtiPosition ++;
                        }
                    } else if (name.equals(TAG_PANGTI)) {
                        pangti = reader.nextString();
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
