package com.irvanjit.discovergurbani;

import android.support.v4.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutPageFragment extends Fragment {

    public static AboutPageFragment newInstance() {
        AboutPageFragment fragment = new AboutPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AboutPageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
