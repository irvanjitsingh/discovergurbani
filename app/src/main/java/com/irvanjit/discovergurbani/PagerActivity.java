package com.irvanjit.discovergurbani;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class PagerActivity extends ActionBarActivity implements ActionBar.TabListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    public void launchSearchView(View view) {
        Intent in = new Intent(this, SearchActivity.class);
        in.putExtra("displayMode", 0);
        startActivity(in);
    }

    public void launchShabadHukamnama(View view) {
        Intent in = new Intent(this, ShabadActivity.class);
        in.putExtra("hymn", "1");
        in.putExtra("id", -1);
        in.putExtra("translation", "13");
        in.putExtra("transliteration", "69");
        in.putExtra("displayMode", 2);
        startActivity(in);
    }

    public void hideKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pager, menu);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return AboutPageFragment.newInstance();
                case 1:
                    return ReadPageFragment.newInstance();
                case 2:
                    return SearchPageFragment.newInstance();
                case 3:
                    return HukamPageFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }

    public static class AboutPageFragment extends Fragment {

        public static AboutPageFragment newInstance() {
            AboutPageFragment fragment = new AboutPageFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        public AboutPageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_page_about, container, false);
            Typeface anmolBani = Typeface.createFromAsset(getActivity().getAssets(), "fonts/AnmolUni.ttf");
            TextView welcomeText = (TextView)rootView.findViewById(R.id.about_page);
            welcomeText.setTypeface(anmolBani);
            return rootView;
        }
    }

    public static class ReadPageFragment extends Fragment {

        String translationId;
        String transliterationId;
        String angNumber;
        EditText angField;
        Toast toast;

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

        void setupButton(View view) {
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

        void setupSpinner(Spinner spinner, ArrayAdapter<CharSequence> spinnerAdapter, int defaultValue) {
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            spinner.setSelection(defaultValue);
        }

        void setupSpinners(View view) {
            //SPINNERS
            Spinner translationSpinner = (Spinner)view.findViewById(R.id.translation_spinner);
            Spinner transliterationSpinner = (Spinner)view.findViewById(R.id.transliteration_spinner);
            ArrayAdapter<CharSequence> translationAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.translation_strings, android.R.layout.simple_spinner_item);
            ArrayAdapter<CharSequence> transliterationAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.transliteration_strings, android.R.layout.simple_spinner_item);
            setupSpinner(translationSpinner, translationAdapter, 12);
            setupSpinner(transliterationSpinner, transliterationAdapter, 14);
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

    public static class SearchPageFragment extends Fragment {

        public static SearchPageFragment newInstance() {
            SearchPageFragment fragment = new SearchPageFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        public SearchPageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_page_search, container, false);
            return rootView;
        }
    }

    public static class HukamPageFragment extends Fragment {

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
            return rootView;
        }
    }
}
