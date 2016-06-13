/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.rossetto.organizer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class MainActivityFragment extends Fragment {

    private CompromissoAdapter mAdapter;
    public static final String userKey = "userId";
    public static SharedPreferences prefs;
    int userId;


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        userId = prefs.getInt(userKey, 0);
        if(userId == 0){
            Intent login = new Intent(getActivity(), LoginActivity.class);
            startActivity(login);
        }else{
            FetchCompromissoTask task = new FetchCompromissoTask();
            task.execute(userId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayList<CompromissoItem> objects = new ArrayList<CompromissoItem>();
        mAdapter = new CompromissoAdapter(
                getActivity(),
                R.layout.list_item_compromisso,
                objects);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_compromisso);
        listView.setAdapter(mAdapter);

        return rootView;
    }

    public class FetchCompromissoTask extends AsyncTask<Integer, Void, CompromissoItem[]> {

        private final String LOG_TAG = FetchCompromissoTask.class.getSimpleName();

        private CompromissoItem[] getCompromissosFromJson(String jsonStr)
                throws JSONException {

            final String COMPROMISSOS = "compromissos";
            final String TITULO = "compromisso";
            final String DATA = "data";
            final String HORA = "hora";
            final String LOCAL = "local";

            JSONObject json = new JSONObject(jsonStr);
            JSONArray compromissos = json.getJSONArray(COMPROMISSOS);

            CompromissoItem[] resultObjects = new CompromissoItem[compromissos.length()];
            for(int i = 0; i < compromissos.length(); i++) {
                String titulo;
                String data;
                String hora;
                String local;

                JSONObject compromisso = compromissos.getJSONObject(i);
                titulo = compromisso.getString(TITULO);
                data = compromisso.getString(DATA);
                hora = compromisso.getString(HORA);
                local = compromisso.getString(LOCAL);

                resultObjects[i] = new CompromissoItem(titulo, data, hora, local);
                Log.v(LOG_TAG, titulo + " - " + data + " - " + hora + " - " + local);
            }
            return resultObjects;

        }

        @Override
        protected CompromissoItem[] doInBackground(Integer... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;
            int uId = params[0];
            String userId = Integer.toString(uId);

            try {
                final String BASE_URL =
                        "http://52.203.161.136/usuarios/"+userId+"/compromissos?";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon().build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getCompromissosFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(CompromissoItem[] result) {
            if (result != null) {
                mAdapter.clear();
                for(CompromissoItem compromisso : result) {
                    mAdapter.add(compromisso);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}