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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class ShabadActivity extends ActionBarActivity {
    private static final String DEBUG_TAG = "HttpDebug";

    private static final String apiBase = "http://api.sikher.com/";
    //    private static final String apiBase = "http://10.0.0.195:8000/";
    private TextView textView;
    private TextView translationText;
    private ListView listView;
    private Toast toast;
    private String translationId;
    private String transliterationId;
    ArrayList<HashMap<String, String>> shabadList;
    ProgressDialog loading;
    private DialogFragment displayOptions;
    private ListAdapter shabadDisplayAdapter;
    private boolean hideTranslation = false;
    private static int viewWidth;
    private static int viewHeight;

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
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_shabad);
        textView = (TextView) findViewById(R.id.result);
        Intent intent = getIntent();
        String shabadId = intent.getStringExtra(TAG_SHABAD);
        String pangtiId = intent.getStringExtra(TAG_PANGTI_ID);
        translationId = intent.getStringExtra(TAG_TRANSLATION);
        transliterationId = intent.getStringExtra(TAG_TRANSLITERATION);
        shabadList = new ArrayList<HashMap<String, String>>();
        listView = (ListView) findViewById(R.id.shabadview);
        viewWidth = listView.getWidth();
        viewHeight = listView.getHeight();
        displayOptions = new DisplayOptionsFragment();
        //setup error toast
        Context context = getApplicationContext();
        CharSequence connError = "Not connected";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, connError, duration);

        if (isConnected()) {
            new DisplayShabadTask().execute(shabadId);
        } else {
            toast.show();
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
            displayOptions.show(getSupportFragmentManager(), "missiles");
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_tweaks) {
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
        }

        @Override
        public void onStart() {
            super.onStart();
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            setDialogPosition();
        }

        private void setDialogPosition() {
            Window window = getDialog().getWindow();
            window.setGravity(Gravity.BOTTOM);

            WindowManager.LayoutParams params = window.getAttributes();
            int dialogWidth = params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.width = dialogWidth;
            window.setAttributes(params);
        }
    }

    public class ShabadDisplayAdapter extends SimpleAdapter {
        private ArrayList<HashMap<String, String>> results;

        public ShabadDisplayAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.results = data;
        }

        public View getView(int position, View view, ViewGroup parent) {
            Typeface anmolBaniBold = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
            View v = view;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.search_item, null);
            }
            TextView pangti = (TextView) v.findViewById(R.id.pangti);
            TextView translation = (TextView) v.findViewById(R.id.translation);
            TextView transliteration = (TextView) v.findViewById(R.id.transliteration);

            pangti.setText(results.get(position).get(TAG_PANGTI));
            pangti.setTypeface(anmolBaniBold);
            translation.setText(results.get(position).get(TAG_TRANSLATION));
            transliteration.setText(results.get(position).get(TAG_TRANSLITERATION));
            return v;
        }

        public boolean isEnabled(int position) {
            return false;
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
            textView.setText(result);
            shabadDisplayAdapter = new ShabadDisplayAdapter(
                    ShabadActivity.this, shabadList,
                    R.layout.shabad_item, new String[]
                    {TAG_PANGTI, TAG_TRANSLATION, TAG_TRANSLITERATION},
                    new int[] { R.id.pangti, R.id.translation, R.id.transliteration});

            listView.setAdapter(shabadDisplayAdapter);
//            listView.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.pangti_highlighted));
            loading.dismiss();
        }
    }

//    public void toggleTranslation(View view) {
//        hideTranslation = !hideTranslation;
//        shabadDisplayAdapter.notify();
//    }

    public String queryBuilder(String shabadId) {
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

            // Convert the InputStream into a string
            String contentAsString = readJson(is);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readJson(InputStream in) throws IOException, UnsupportedEncodingException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        int pangti_id = -1;
        String pangti = "Shabad";
        int author = -1;
        String raag = "Raag";
        int shabad_id = -1;
        String translation = "Translation";
        String transliteration = "Transliteration";
        String result = "String: ";
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                HashMap<String, String> shabad = new HashMap<String, String>();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals(TAG_PANGTI)) {
                        pangti = reader.nextString();
                        result += pangti;
//                    } else if (name.equals(TAG_AUTHOR)) {
//                        author = reader.nextInt();
//                    } else if (name.equals(TAG_SHABAD)) {
//                        shabad_id = reader.nextInt();
                    } else if (name.equals(TAG_RAAG)) {
                        raag = reader.nextString();
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
