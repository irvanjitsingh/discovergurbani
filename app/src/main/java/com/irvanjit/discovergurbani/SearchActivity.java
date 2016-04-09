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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String DEBUG_TAG = "HttpDebug";

    private SearchView searchView;
    private TextView resultMessage;
    private ProgressDialog loading;

//    private ArrayList<HashMap<String, String>> shabadList;
    private List<ShabadList> shabadList;
    private ArrayList<String> shabadIdList;
    private ArrayList<Integer> pangtiIdList;
    private boolean autoSearchEnabled;
    private boolean isAutoSearch;

    private String query;
    private String translationId;
    private String transliterationId;

//    private ListView shabadsListView;
    private RecyclerView shabadsListView;
    private SharedPreferences preferences;


    //default settings
    private int searchMode = 0;
    private final int searchTranslation = 3;
    private final int searchTransliteration = 4;
    private final int searchAng = 5;

    private HashMap<Character, Character> charMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //search page setup
        query = "";
        resultMessage = (TextView) findViewById(R.id.result);
        charMap = new GurmukhiCharMap().getMapping();

        //Setup Shabad Results list
        setupShabadsListView();

        //load preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        autoSearchEnabled = preferences.getBoolean(SettingsActivity.KEY_PREF_ENABLE_AUTO_SEARCH, false);


        loading = new ProgressDialog(SearchActivity.this);
        setupSpinners();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        autoSearchEnabled = preferences.getBoolean(SettingsActivity.KEY_PREF_ENABLE_AUTO_SEARCH, false);
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        boolean queryExists = !query.isEmpty();
        if (parent.getId() == R.id.transliteration_spinner) {
            String scriptName = "script_" + parent.getItemAtPosition(pos).toString();
            int strId = getResources().getIdentifier(scriptName, "string", getPackageName());
            transliterationId = getString(strId);
            if (queryExists) {
                searchForShabad(query);
            }
        } else if (parent.getId() == R.id.translation_spinner) {
            String langName = "lang_" + parent.getItemAtPosition(pos).toString().replaceAll(" ", "_");
            int strId = getResources().getIdentifier(langName, "string", getPackageName());
            translationId = getString(strId);
            if (queryExists) {
                searchForShabad(query);
            }
        } else if (parent.getId() == R.id.searchmode_spinner) {
            searchMode = pos;
            if (searchMode == searchAng) {
                searchView.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            } else {
                searchView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void setupSearchView(Menu searchActivityMenu) {
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchActivityMenu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocusFromTouch();
        searchView.setOnQueryTextListener(searchQueryListener);

        //appearance
        searchView.setQueryHint(getString(R.string.search_hint_english));
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        TextView searchText = (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        Typeface anmolBani = Typeface.createFromAsset(getAssets(), "fonts/AnmolUni.ttf");
        searchText.setTypeface(anmolBani);
    }

    private final SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        boolean queryReplaced;

        @Override
        public boolean onQueryTextChange(String newText) {
            if (searchMode != searchAng) {
                if (autoSearchEnabled) {
                    shabadList.clear();
                    resultMessage.setText("");
                    shabadsListView.requestFocus();
                    searchView.requestFocus();
                }

                query = searchView.getQuery().toString();

                if (autoSearchEnabled && query.length() > 2) {
                    resultMessage.setText("searchi2ng...");
                }

                isAutoSearch = true;
                StringBuilder gurmukhi = null;
                if (searchMode < 3) {
                    gurmukhi = new StringBuilder(query);
                    for (int i = 0; i < query.length(); i++) {
                        // Replace if a gurmukhi substitution is available
                        Character replacement = charMap.get(query.charAt(i));
                        if (replacement != null) {
                            queryReplaced = true;
                            gurmukhi.setCharAt(i, replacement);
                        }
                    }
                }
                if (queryReplaced && searchMode < 3) {
                    queryReplaced = false;
                    searchView.setQuery(gurmukhi, false);
                } else if (autoSearchEnabled && (query.length() > 2)) {
                    searchForShabad(query);
                }
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            isAutoSearch = false;
            hideKeyboard();

            if ((!(autoSearchEnabled && query.length() > 2))||(searchMode == searchAng)) {
                searchForShabad(query);
            } else {
                shabadsListView.requestFocus();
            }
            return true;
        }
    };

    private void setupSpinners() {
        Spinner translationSpinner = new AwesomeSpinner(this, getWindow().getDecorView(),
                R.id.translation_spinner, R.array.translation_strings, 0).getAwesomeSpinner();
        Spinner transliterationSpinner = new AwesomeSpinner(this, getWindow().getDecorView(),
                R.id.transliteration_spinner, R.array.transliteration_strings, 1).getAwesomeSpinner();
        Spinner searchModeSpinner = new AwesomeSpinner(this, getWindow().getDecorView(),
                R.id.searchmode_spinner, R.array.search_modes, 2).getAwesomeSpinner();
        translationSpinner.setOnItemSelectedListener(this);
        transliterationSpinner.setOnItemSelectedListener(this);
        searchModeSpinner.setOnItemSelectedListener(this);
    }

    private void setupLoadingDialog(ProgressDialog loading) {
            loading.setMessage("Searching");
            loading.setCancelable(true);
            loading.setCanceledOnTouchOutside(false);
            loading.setIndeterminate(false);
    }

    private void setupShabadsListView() {
        shabadList = new ArrayList<ShabadList>();
        shabadIdList = new ArrayList<String>();
        pangtiIdList = new ArrayList<Integer>();

        shabadsListView = (RecyclerView) findViewById(R.id.search_results);
        shabadsListView.hasFixedSize();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        shabadsListView.setLayoutManager(llm);

        shabadsListView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        if (isAutoSearch && autoSearchEnabled) {
                            shabadsListView.requestFocus();
                            hideKeyboard();
                        }

                        // Get shabad metadata
                        ShabadList list = shabadList.get(position);
                        String shabadId = String.valueOf(list.shabadId);
                        int pangtiId = list.pangtiId;

                        // Start shabad activity
                        Intent in = new Intent(getApplicationContext(),
                                ShabadActivity.class);
                        in.putExtra(ShabadActivity.TAG_SHABAD, shabadId);
                        in.putExtra(ShabadActivity.TAG_PANGTI_ID, pangtiId);
                        in.putExtra(ShabadActivity.TAG_TRANSLATION, translationId);
                        in.putExtra(ShabadActivity.TAG_TRANSLITERATION, transliterationId);
                        in.putExtra("displayMode", 0);
                        startActivity(in);
                    }
                })
        );
    }

    public class ShabadList {
        protected int shabadId;
        protected int pangtiId;
        protected String pangti;
        protected String translation;
        protected String transliteration;
        protected String meta;
    }

    public class ShabadAdapter extends RecyclerView.Adapter<ShabadAdapter.ShabadViewHolder> {

        public ShabadAdapter() {
        }

        @Override
        public int getItemCount() {
            return shabadList.size();
        }

        @Override
        public void onBindViewHolder(ShabadViewHolder shabadHolder, int i) {
            Typeface anmolBani = Typeface.createFromAsset(getAssets(), "fonts/AnmolUniBani.ttf");
            ShabadList ci = shabadList.get(i);
            shabadHolder.vPangti.setTypeface(anmolBani);
            shabadHolder.vPangti.setText(ci.pangti);
            shabadHolder.vTranslation.setText(ci.translation);
            shabadHolder.vTransliteration.setText(ci.transliteration);
            shabadHolder.vMeta.setText(ci.meta);
        }

        @Override
        public ShabadViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.search_item, viewGroup, false);

            return new ShabadViewHolder(itemView);
        }

        public class ShabadViewHolder extends RecyclerView.ViewHolder {
            protected TextView vPangti;
            protected TextView vTranslation;
            protected TextView vTransliteration;
            protected TextView vMeta;

            public ShabadViewHolder(View v) {
                super(v);
                vPangti =  (TextView) v.findViewById(R.id.pangti);
                vTranslation = (TextView)  v.findViewById(R.id.translation);
                vTransliteration = (TextView)  v.findViewById(R.id.transliteration);
                vMeta = (TextView) v.findViewById(R.id.meta);
            }
        }
    }

    private void searchForShabad(String query) {
        if (isConnected()) {
            new SearchShabadTask().execute(query);
        } else {
            Toast.makeText(this, "Not connected to network", Toast.LENGTH_SHORT).show();
        }
    }

    private class SearchShabadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isAutoSearch) {
                setupLoadingDialog(loading);
                loading.show();
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return getData(urls[0]);
            } catch (IOException e) {
                return "No shabads found, check your query";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            resultMessage.setText(result);

            ShabadAdapter shabadAdapter = new ShabadAdapter();
            shabadsListView.setAdapter(shabadAdapter);

            if (!isAutoSearch) {
                loading.dismiss();
                shabadsListView.requestFocus();
                resultMessage.setText("");
            }
        }
    }

    private String queryBuilder(String rawQuery) {
        String urlString;
        try {
            String query = URLEncoder.encode(rawQuery, "utf-8");
            String searchModeString = "search/"+searchMode;
            if (searchMode == searchAng) {
                searchModeString = "page";
            }
            urlString = ShabadActivity.URL_API_BASE+"/"+searchModeString+"/"+query+"/"+translationId+"/"+transliterationId;
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
            return parseQueryJson(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    private String parseQueryJson(InputStream in) throws IOException {
        shabadIdList.clear();
        pangtiIdList.clear();
        shabadList.clear();
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
//                HashMap<String, String> shabad = new HashMap<String, String>();
                ShabadList shabad = new ShabadList();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals(ShabadActivity.TAG_PANGTI_ID)) {
                        pangti_id = reader.nextInt();
                    } else if (name.equals(ShabadActivity.TAG_PANGTI)) {
                        if (searchMode == searchTranslation) {
                            translation = reader.nextString();
                        } else if (searchMode == searchTransliteration) {
                            transliteration = reader.nextString();
                        } else {
                            pangti = reader.nextString();
                        }
                    } else if (name.equals(ShabadActivity.TAG_SHABAD)) {
                        shabad_id = reader.nextInt();
                    } else if (name.equals(ShabadActivity.TAG_ANG)) {
                        ang = reader.nextInt();
                    } else if (name.equals(ShabadActivity.TAG_SECTION)) {
                        section = reader.nextString();
                    } else if (name.equals(ShabadActivity.TAG_TRANSLATION)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(ShabadActivity.TAG_PANGTI)) {
                                translation = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else if (name.equals(ShabadActivity.TAG_TRANSLITERATION)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(ShabadActivity.TAG_PANGTI)) {
                                transliteration = reader.nextString();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    } else if (name.equals(ShabadActivity.TAG_SCRIPTURE)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(ShabadActivity.TAG_PANGTI_ID)) {
                                pangti_id = reader.nextInt();
                            } else if (subName.equals(ShabadActivity.TAG_PANGTI)) {
                                pangti = reader.nextString();
                            } else if (subName.equals(ShabadActivity.TAG_SHABAD)) {
                                shabad_id = reader.nextInt();
                            } else if (subName.equals(ShabadActivity.TAG_ANG)) {
                                ang = reader.nextInt();
                            } else if (subName.equals(ShabadActivity.TAG_SECTION)) {
                                section = reader.nextString();
                            } else if (subName.equals(ShabadActivity.TAG_AUTHOR)) {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    String secondSubName = reader.nextName();
                                    if (secondSubName.equals(ShabadActivity.TAG_AUTHOR)) {
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
                        reader.endObject();
                    } else if (name.equals(ShabadActivity.TAG_AUTHOR)) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String subName = reader.nextName();
                            if (subName.equals(ShabadActivity.TAG_AUTHOR)) {
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
                if (!author.equals("None")) {
                    shabadIdList.add(String.valueOf(shabad_id));
                    pangtiIdList.add(pangti_id);
                    shabad.pangtiId = pangti_id;
                    shabad.shabadId = shabad_id;
                    shabad.pangti = pangti;
                    shabad.translation = translation;
                    shabad.transliteration = transliteration;
                    meta = author+" Ji | Ang "+String.valueOf(ang)+" | "+section;
                    shabad.meta = meta;
                    shabadList.add(shabad);
                }

                reader.endObject();
            }
            reader.endArray();
        } finally {
            reader.close();
        }
        int numberOfResults = shabadIdList.size();
        String resultsText = "Your search returned "+String.valueOf(numberOfResults)+" shabad";
        if (numberOfResults > 1) {
            resultsText += "s";
        }
        return resultsText;
    }

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void hideKeyboard() {
        if(getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
