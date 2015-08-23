package com.irvanjit.discovergurbani;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AwesomeSpinner {
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;

    public final ArrayList<Integer> disabledTranslationIndices;
    public final ArrayList<Integer> disabledTransliterationIndices;
    private final ArrayList<Integer> disabledValues;

    public AwesomeSpinner(Context context, View view, int spinnerId, int arrayId, int spinnerType) {

        spinner = (Spinner)view.findViewById(spinnerId);
        List<String> stringList = Arrays.asList(context.getResources().getStringArray(arrayId));
        CharSequence[] charList = stringList.toArray(new CharSequence[stringList.size()]);

        //load preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String translationId = preferences.getString(SettingsActivity.KEY_PREF_SEARCH_LANGUAGE, ShabadActivity.DEFAULT_TRANSLATION_ID);
        String transliterationId = preferences.getString(SettingsActivity.KEY_PREF_SEARCH_SCRIPT, ShabadActivity.DEFAULT_TRANSLITERATION_ID);

        int translationIndex = Integer.parseInt(translationId) - 1;
        int transliterationIndex = Integer.parseInt(transliterationId) - 55;
        int defaultValue;

        disabledTranslationIndices = new ArrayList<Integer>();
        disabledTransliterationIndices = new ArrayList<Integer>();

        //disabled translations
        disabledTranslationIndices.add(0);
        disabledTranslationIndices.add(1);
        disabledTranslationIndices.add(3);
        disabledTranslationIndices.add(4);
        disabledTranslationIndices.add(5);
        disabledTranslationIndices.add(8);
        disabledTranslationIndices.add(10);
        disabledTranslationIndices.add(13);
        disabledTranslationIndices.add(15);
        disabledTranslationIndices.add(17);
        disabledTranslationIndices.add(20);
        disabledTranslationIndices.add(23);
        disabledTranslationIndices.add(24);
        disabledTranslationIndices.add(30);
        disabledTranslationIndices.add(31);
        disabledTranslationIndices.add(32);
        disabledTranslationIndices.add(33);
        disabledTranslationIndices.add(34);
        disabledTranslationIndices.add(35);
        disabledTranslationIndices.add(41);
        disabledTranslationIndices.add(42);
        disabledTranslationIndices.add(43);
        disabledTranslationIndices.add(44);
        disabledTranslationIndices.add(45);
        disabledTranslationIndices.add(51);
        disabledTranslationIndices.add(52);

        if (spinnerType == 0) {
            disabledValues = disabledTranslationIndices;
            defaultValue = translationIndex;
        } else if (spinnerType == 1) {
            disabledValues = disabledTransliterationIndices;
            defaultValue = transliterationIndex;
        } else {
            disabledValues = null;
            defaultValue = 0;
        }

        if (spinnerType != 2) {
            adapter = new ArrayAdapter<CharSequence>(context,
                    R.layout.support_simple_spinner_dropdown_item, charList) {
                // Disable click item < month current
                @Override
                public boolean isEnabled(int position) {
                    return !disabledValues.contains(position);
                }
                // Change color item
                @Override
                public View getDropDownView(int position, View convertView,
                                            ViewGroup parent) {
                    View mView = super.getDropDownView(position, convertView, parent);
                    TextView mTextView = (TextView) mView;
                    if (disabledValues.contains(position)) {
                        mTextView.setTextColor(Color.GRAY);
                    } else {
                        mTextView.setTextColor(Color.WHITE);
                    }
                    return mView;
                }
            };
        } else {
            adapter = ArrayAdapter.createFromResource(context, arrayId, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(defaultValue);
    }

    public Spinner getAwesomeSpinner() {
        return spinner;
    }
}
