package com.irvanjit.discovergurbani;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requests {
    public String query;

    public Requests(String q) {
        query = q;
    }
    //
//    public InputStream getData(String myurl) throws IOException {
//        return this.downloadUrl(myurl);
//    }
    public InputStream downloadUrl() throws IOException {
        InputStream is = null;
        try {
            String base = "http://api.sikher.com/search/ਕਕਗਗਅਨਜ";
            String q = query;
            URL url = new URL(base);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("HTTP Debug:", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
//            String contentAsString = readJson(is);
            return is;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                Log.d("HTTPDEbug", "input stream is null");
                is.close();
            }
        }
    }
}
