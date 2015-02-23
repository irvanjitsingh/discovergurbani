package com.irvanjit.discovergurbani;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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


public class SearchActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private static final String DEBUG_TAG = "HttpDebug";

    private SearchView searchView;
    private EditText query;
    private TextView resultMessage;
    private Toast toast;
    ProgressDialog loading;

    private ListView shabadsListView;
    ArrayList<HashMap<String, String>> shabadList;
    ArrayList<String> shabadIdList;
    ArrayList<String> pangtiIdList;
    String[] shabadListKeys;
    int[] shabadListValues;


    //default settings
    public String translationId = "13";
    public String transliterationId = "69";
    private int searchMode = 0;
    private static final String apiBase = "http://api.sikher.com/";
//    private static final String apiBase = "http://10.0.0.195:8000/";
//    private static final String apiBase = "http://127.0.0.1:8000/";

    //JSON Nodes
    private static final String TAG_PANGTI_ID = "id";
    private static final String TAG_PANGTI = "text";
    private static final String TAG_SHABAD = "hymn";
    private static final String TAG_ANG = "page";
    private static final String TAG_SECTION = "section";
    private static final String TAG_RAAG = "melody";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_TRANSLATION = "translation";
    private static final String TAG_TRANSLITERATION = "transliteration";
    private static final String TAG_META = "meta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //search page setup
        query = (EditText) findViewById(R.id.query);
        resultMessage = (TextView) findViewById(R.id.result);

        //Setup Shabad Results list
        setupShabadsListView();

//        Intent intent = getIntent();
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            String query = intent.getStringExtra(SearchManager.QUERY);
//            doSearch(query);
//        }

        //keyboard search button
        query.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getQuery(query);
                    handled = true;
                }
                return handled;
            }
        });

        //setup error toast
        Context context = getApplicationContext();
        CharSequence connError = "Not connected";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, connError, duration);

        //misc. setup
        loading = new ProgressDialog(SearchActivity.this);
        setupSpinners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        setupSearchView(menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        boolean queryExists = query.getText().length() > 0;
        if (parent.getId() == R.id.transliteration_spinner) {
            String scriptName = "script_" + parent.getItemAtPosition(pos).toString();
            int strId = getResources().getIdentifier(scriptName, "string", getPackageName());
            transliterationId = getString(strId);
            if (queryExists) {
                getQuery(query);
            }
        } else if (parent.getId() == R.id.translation_spinner) {
            Log.d("TRANSLATION STRING", translationId);
            String langName = "lang_" + parent.getItemAtPosition(pos).toString().replaceAll(" ", "_");
            int strId = getResources().getIdentifier(langName, "string", getPackageName());
            translationId = getString(strId);
            if (queryExists) {
                getQuery(query);
            }
        } else if (parent.getId() == R.id.searchmode_spinner) {
            searchMode = pos;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void setupSearchView(Menu searchActivityMenu) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchActivityMenu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(searchQueryListener);
    }

    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextChange(String newText) {
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            String stringUrl = query.toString();
            if (isConnected()) {
                new SearchShabadTask().execute(stringUrl);
            } else {
                toast.show();
            }
            return true;
        }
    };

    public void setupSpinner(Spinner spinner, ArrayAdapter<CharSequence> spinnerAdapter, int defaultValue) {
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(defaultValue);
        spinner.setOnItemSelectedListener(this);
    }

    public void setupSpinners() {
        //SPINNERS
        Spinner translationSpinner = (Spinner) findViewById(R.id.translation_spinner);
        Spinner transliterationSpinner = (Spinner) findViewById(R.id.transliteration_spinner);
        Spinner searchModeSpinner = (Spinner) findViewById(R.id.searchmode_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> translationAdapter = ArrayAdapter.createFromResource(this,
                R.array.translation_strings, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> transliterationAdapter = ArrayAdapter.createFromResource(this,
                R.array.transliteration_strings, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> searchModeAdapter = ArrayAdapter.createFromResource(this,
                R.array.search_modes, android.R.layout.simple_spinner_item);
        setupSpinner(translationSpinner, translationAdapter, 12);
        setupSpinner(transliterationSpinner, transliterationAdapter, 14);
        setupSpinner(searchModeSpinner, searchModeAdapter, searchMode);
    }

    public void setupLoadingDialog(ProgressDialog loading) {
            loading.setMessage("Searching");
            loading.setCancelable(true);
            loading.setCanceledOnTouchOutside(false);
            loading.setIndeterminate(false);
    }

    public void setupShabadsListView() {
        shabadList = new ArrayList<HashMap<String, String>>();
        shabadIdList = new ArrayList<String>();
        pangtiIdList = new ArrayList<String>();

        shabadsListView = (ListView) findViewById(R.id.searchresults);
        shabadsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get shabad metadata
                String shabadId = shabadIdList.get(position);
                String pangtiId = pangtiIdList.get(position);

                // Start shabad activitiy
                Intent in = new Intent(getApplicationContext(),
                        ShabadActivity.class);
                in.putExtra(TAG_SHABAD, shabadId);
                in.putExtra(TAG_PANGTI_ID, pangtiId);
                in.putExtra(TAG_TRANSLATION, translationId);
                in.putExtra(TAG_TRANSLITERATION, transliterationId);
                startActivity(in);
            }
        });
    }

    public void setupShabadListAdapter() {
        shabadListKeys = new String[] {TAG_PANGTI, TAG_TRANSLATION, TAG_TRANSLITERATION, TAG_META};
        shabadListValues = new int[] { R.id.pangti, R.id.translation, R.id.transliteration, R.id.meta};

        ListAdapter shabadListAdapter = new ShabadListAdapter(
                SearchActivity.this, shabadList, R.layout.search_item, shabadListKeys, shabadListValues);
        shabadsListView.setAdapter(shabadListAdapter);
    }

    public class ShabadListAdapter extends SimpleAdapter {

        private ArrayList<HashMap<String, String>> results;

        public ShabadListAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.results = data;
        }

        public View getView(int position, View view, ViewGroup parent) {
            Typeface anmolBaniBold = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani-Bold.ttf");
            View v = view;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.search_item, null);
            }
            TextView pangti = (TextView) v.findViewById(R.id.pangti);
            TextView translation = (TextView) v.findViewById(R.id.translation);
            TextView transliteration = (TextView) v.findViewById(R.id.transliteration);
            TextView meta = (TextView) v.findViewById(R.id.meta);
//            TextView ang = (TextView) v.findViewById(R.id.ang);
//            TextView author = (TextView) v.findViewById(R.id.author);
//            TextView section = (TextView) v.findViewById(R.id.section);

            pangti.setText(results.get(position).get(TAG_PANGTI));
            pangti.setTypeface(anmolBaniBold);
            translation.setText(results.get(position).get(TAG_TRANSLATION));
            transliteration.setText(results.get(position).get(TAG_TRANSLITERATION));
            meta.setText(results.get(position).get(TAG_META));
            return v;
        }
    }

//    public void doSearch(String query) {
//        if (isConnected()) {
//            new SearchShabadTask().execute(query);
//        } else {
//            toast.show();
//        }
//    }

    public void getQuery(View view) {
        String stringUrl = query.getText().toString();
        if (isConnected()) {
            new SearchShabadTask().execute(stringUrl);
        } else {
            toast.show();
        }
    }

    private class SearchShabadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            shabadList.clear();
            hideKeyboard();
            setupLoadingDialog(loading);
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
                return "No shabads found, check your query";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            resultMessage.setText(result);
            setupShabadListAdapter();
            loading.dismiss();
        }
    }

    public String queryBuilder(String rawQuery) throws IOException {
        String urlString;
        try {
            String query = URLEncoder.encode(rawQuery, "utf-8");
            String searchModeString = "search/"+searchMode;
            if (searchMode == 4) {
                searchModeString = "page";
            }
            urlString = apiBase+searchModeString+"/"+query+"/"+translationId+"/"+transliterationId;
        } catch (IOException e) {
            Log.d(DEBUG_TAG, e.toString());
            return null;
        }
        return urlString;
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

            // Convert the InputStream into a string
            String contentAsString = parseQueryJson(is);
            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String parseQueryJson(InputStream in) throws IOException, UnsupportedEncodingException {
        shabadIdList.clear();
        pangtiIdList.clear();
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        int pangti_id = -1;
        int shabad_id = -1;
        int ang = 0;
        String pangti = "Shabad";
        String author = "Author";
        String section = "Section";
        String translation = "Translation";
        String transliteration = "Transliteration";
        String meta;

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                HashMap<String, String> shabad = new HashMap<String, String>();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals(TAG_PANGTI_ID)) {
                        pangti_id = reader.nextInt();
                    } else if (name.equals(TAG_PANGTI)) {
                        pangti = reader.nextString();
                    } else if (name.equals(TAG_SHABAD)) {
                        shabad_id = reader.nextInt();
                    } else if (name.equals(TAG_ANG)) {
                        ang = reader.nextInt();
                    } else if (name.equals(TAG_SECTION)) {
                        section = reader.nextString();
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
                    } else if (name.equals(TAG_AUTHOR)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(TAG_AUTHOR)) {
                                author = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else {
                        reader.skipValue();
                    }
                }
                shabadIdList.add(String.valueOf(shabad_id));
                pangtiIdList.add(String.valueOf(pangti_id));
                shabad.put(TAG_PANGTI, pangti);
                shabad.put(TAG_TRANSLATION, translation);
                shabad.put(TAG_TRANSLITERATION, transliteration);
                meta = author+" Ji, "+"Ang "+String.valueOf(ang)+ ", "+section;
                shabad.put(TAG_META, meta);
//                shabad.put(TAG_SECTION, section);
//                shabad.put(TAG_AUTHOR, author);
                shabadList.add(shabad);
                reader.endObject();
            }
            reader.endArray();
        } finally {
            reader.close();
        }
        return "Your search returned "+String.valueOf(shabadIdList.size())+" shabad(s)";
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void hideKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}