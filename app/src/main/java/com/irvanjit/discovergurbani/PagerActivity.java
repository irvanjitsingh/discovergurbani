package com.irvanjit.discovergurbani;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class PagerActivity extends AppCompatActivity {
    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new HomeFragmentPagerAdapter(getSupportFragmentManager(), PagerActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        hideKeyboard();
                    }
                });
        prefs = getSharedPreferences("com.irvanjit.discovergurbani", MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("first_run", true)) {
            prefs.edit().putBoolean("first_run", false).apply();
            DialogFragment aboutDialog = new AboutDialogFragment();
            aboutDialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pager, menu);
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

    public void launchSearchView(View view) {
        Intent in = new Intent(this, SearchActivity.class);
        in.putExtra("displayMode", 0);
        startActivity(in);
    }

    public void launchRaagList(View view) {
        Intent in = new Intent(this, RaagListActivity.class);
        startActivity(in);
    }

    private void hideKeyboard() {
        if(getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void checkFirstRun() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = preferences.getBoolean(SettingsActivity.KEY_PREF_IS_FIRST_RUN, true);

        if (isFirstRun) {
            DialogFragment aboutDialog = new AboutDialogFragment();
            aboutDialog.show(getSupportFragmentManager(), null);
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 4;
        private final String[] tabTitles = new String[] { "Read", "Search", "Hukam", "Raags" };
        private Context context;

        public HomeFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ReadPageFragment.newInstance();
                case 1:
                    return SearchPageFragment.newInstance();
                case 2:
                    return HukamPageFragment.newInstance();
                case 3:
                    return RaagPageFragment.newInstance();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    public static class AboutDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setPositiveButton(R.string.ok_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setTitle(R.string.welcome_dialog_title);
            View displayView = inflater.inflate(R.layout.fragment_page_about, null);
            builder.setView(displayView);
            // Create the AlertDialog object and return it
            return builder.create();
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
            return inflater.inflate(R.layout.fragment_page_search, container, false);
        }
    }

    public static class RaagPageFragment extends Fragment {

        public static RaagPageFragment newInstance() {
            RaagPageFragment fragment = new RaagPageFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        public RaagPageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_page_raag, container, false);
        }
    }

}
