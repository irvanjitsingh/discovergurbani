package com.irvanjit.discovergurbani;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    //language prefs
    public static final String KEY_PREF_SEARCH_LANGUAGE = "pref_language";
    public static final String KEY_PREF_SEARCH_SCRIPT = "pref_script";

    //display aesthetic prefs
    public static final String KEY_PREF_FONT_SIZE_GURMUKHI = "pref_font_size_gurmukhi";
    public static final String KEY_PREF_FONT_SIZE_TRANSLITERATION = "pref_font_size_transliteration";
    public static final String KEY_PREF_FONT_SIZE_TRANSLATION = "pref_font_size_translation";
    public static final String KEY_PREF_VISIBILITY_GURMUKHI = "pref_visibility_gurmukhi";
    public static final String KEY_PREF_VISIBILITY_LARIDAAR = "pref_visibility_laridaar";
    public static final String KEY_PREF_VISIBILITY_TRANSLITERATION = "pref_visibility_transliteration";
    public static final String KEY_PREF_VISIBILITY_TRANSLATION = "pref_visibility_translation";
    public static final String KEY_PREF_ENABLE_LARIDAAR_MODE = "pref_laridaar_mode";

    //misc prefs
    public static final String KEY_PREF_ENABLE_DISPLAY_WAKE = "pref_display_wake";
    public static final String KEY_PREF_ENABLE_AUTO_SEARCH = "pref_auto_search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferences())
                .commit();
    }

    public static class GeneralPreferences extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.general_preferences);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//            if (key.equals(KEY_PREF_DISPLAY_WAKE)) {
//                Preference displayWakePref = findPreference(key);
//            }
        }
    }
}
