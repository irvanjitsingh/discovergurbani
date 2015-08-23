package com.irvanjit.discovergurbani;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class HukamPageFragment extends Fragment {

    private String translationId;
    private String transliterationId;

    public static HukamPageFragment newInstance() {
        HukamPageFragment fragment = new HukamPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public HukamPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_hukam, container, false);
        setupSpinners(rootView);
        setupButton(rootView);
        return rootView;
    }

    private void setupButton(View view) {
        Button button = (Button)view.findViewById(R.id.hukam_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), ShabadActivity.class);
                in.putExtra("hymn", "1");
                in.putExtra("id", -1);
                in.putExtra("translation", translationId);
                in.putExtra("transliteration", transliterationId);
                in.putExtra("displayMode", 2);
                startActivity(in);
            }
        });
    }

    private void setupSpinners(View view) {
        Spinner translationSpinner = new AwesomeSpinner(getActivity(), view,
                R.id.translation_spinner, R.array.translation_strings, 0).getAwesomeSpinner();
        Spinner transliterationSpinner = new AwesomeSpinner(getActivity(), view,
                R.id.transliteration_spinner, R.array.transliteration_strings, 1).getAwesomeSpinner();
        translationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String langName = "lang_" + parent.getItemAtPosition(position).toString().replaceAll(" ", "_");
                int strId = getResources().getIdentifier(langName, "string", getActivity().getPackageName());
                translationId = getString(strId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        transliterationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String scriptName = "script_" + parent.getItemAtPosition(position).toString();
                int strId = getResources().getIdentifier(scriptName, "string", getActivity().getPackageName());
                transliterationId = getString(strId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
