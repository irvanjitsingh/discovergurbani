package com.irvanjit.discovergurbani;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class RaagListActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = "HttpDebug";

    private ListView raagListView;

    private ArrayList<HashMap<String, String>> raagList;
    private String[] raagListKeys;
    private int[] raagListValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raag_list);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Setup Shabad Results list
        setupShabadsListView();

            if (isConnected()) {
            new LoadRaagsTask().execute("melody");
        } else {
            Toast.makeText(this, "Not connected to network", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

        return super.onOptionsItemSelected(item);
    }

    private void setupShabadsListView() {
        raagList = new ArrayList<HashMap<String, String>>();
        raagListView = (ListView) findViewById(R.id.raag_list);
        raagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // launch raag activity
                Intent in = new Intent(getApplicationContext(), RaagDetailActivity.class);
                in.putExtra(ShabadActivity.TAG_RAAG_NAME, raagList.get(position).get(ShabadActivity.TAG_RAAG_NAME));
                in.putExtra(ShabadActivity.TAG_RAAG_GURMUKHI, raagList.get(position).get(ShabadActivity.TAG_RAAG_GURMUKHI));
                in.putExtra(ShabadActivity.TAG_RAAG_DESCRIPTION, raagList.get(position).get(ShabadActivity.TAG_RAAG_DESCRIPTION));
                startActivity(in);
            }
        });
    }

    private void setupShabadListAdapter() {
        raagListKeys = new String[] {ShabadActivity.TAG_RAAG_NAME, ShabadActivity.TAG_RAAG_GURMUKHI};
        raagListValues = new int[] {R.id.raag_name, R.id.raag_gurmukhi};

        ListAdapter shabadListAdapter = new RaagListAdapter(
                RaagListActivity.this, raagList, R.layout.raag_item, raagListKeys, raagListValues);
        raagListView.setAdapter(shabadListAdapter);
    }

    public class RaagListAdapter extends SimpleAdapter {

        private final ArrayList<HashMap<String, String>> results;

        public RaagListAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.results = data;
        }

        public View getView(int position, View view, ViewGroup parent) {
            Typeface anmolBani = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
            View v = view;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.raag_item, null);
            }
            TextView name_eng = (TextView) v.findViewById(R.id.raag_name);
            TextView name_gurmukhi = (TextView) v.findViewById(R.id.raag_gurmukhi);

            name_eng.setText(results.get(position).get(ShabadActivity.TAG_RAAG_NAME));
            name_gurmukhi.setText(results.get(position).get(ShabadActivity.TAG_RAAG_GURMUKHI));
            name_gurmukhi.setTypeface(anmolBani);
            return v;
        }
    }

    private class LoadRaagsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            raagList.clear();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return getData(urls[0]);
            } catch (IOException e) {
                return "An error occurred. Please try again.";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            setupShabadListAdapter();
        }
    }

    private String queryBuilder(String query) {
        return ShabadActivity.URL_API_BASE+"/"+query;
    }

    //HTTP Request
    private String getData(String query) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(queryBuilder(query));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();
            return parseQueryJson(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    private String parseQueryJson(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        int raag_id = -1;
        String raag_eng = "Raag English";
        String raag_gurmukhi = "Raag Gurmukhi";
        String description = "Description";
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                HashMap<String, String> shabad = new HashMap<String, String>();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals(ShabadActivity.TAG_RAAG_ID)) {
                        raag_id = reader.nextInt();
                    } else if (name.equals(ShabadActivity.TAG_RAAG_NAME)) {
                        raag_eng = reader.nextString();
                    } else if (name.equals(ShabadActivity.TAG_RAAG_GURMUKHI)) {
                        raag_gurmukhi = reader.nextString();
                    } else if (name.equals(ShabadActivity.TAG_RAAG_DESCRIPTION)) {
                        description = reader.nextString();
                    } else {
                        reader.skipValue();
                    }
                }
                if (!raag_eng.equals("None")) {
                    shabad.put(ShabadActivity.TAG_RAAG_NAME, raag_eng);
                    shabad.put(ShabadActivity.TAG_RAAG_GURMUKHI, raag_gurmukhi);
                    shabad.put(ShabadActivity.TAG_RAAG_DESCRIPTION, description);
                    raagList.add(shabad);
                }
                reader.endObject();
            }
            reader.endArray();
        } finally {
            reader.close();
        }
        return "";
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}