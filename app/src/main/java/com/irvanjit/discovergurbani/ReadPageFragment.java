package com.irvanjit.discovergurbani;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ReadPageFragment extends Fragment {

    private String translationId;
    private String transliterationId;
    private String angNumber;
    private EditText angField;
    private Toast toast;

    public static ReadPageFragment newInstance() {
        ReadPageFragment fragment = new ReadPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ReadPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_read, container, false);

        //setup error toast
        Context context = getActivity().getApplicationContext();
        CharSequence connError = "Please enter a valid ang number";
        int duration = Toast.LENGTH_SHORT;
        toast = Toast.makeText(context, connError, duration);

        setupSpinners(rootView);
        setupButton(rootView);
        return rootView;
    }

    private void setupButton(View view) {
        angField = (EditText)view.findViewById(R.id.ang_number);
        angField.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        Button button = (Button)view.findViewById(R.id.read_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                angNumber = angField.getText().toString();
                if (angNumber.isEmpty()) {
                    angNumber = "1";
                }
                int ang = Integer.parseInt(angNumber);
                if (ang < 1 || ang > 1430) {
                    toast.show();
                } else {
                    getActivity().getWindow().getDecorView().requestFocus();
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    Intent in = new Intent(getActivity(), ShabadActivity.class);
                    in.putExtra("hymn", angNumber);
                    in.putExtra("id", -1);
                    in.putExtra("translation", translationId);
                    in.putExtra("transliteration", transliterationId);
                    in.putExtra("displayMode", 1);
                    startActivity(in);
                }
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