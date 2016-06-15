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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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

public class MainActivityFragment extends Fragment {

    private CompromissoAdapter mAdapter;
    public static final String userKey = "userId";
    public static final String userToken = "token";
    public static final String exitKey = "exit";
    public static SharedPreferences prefs;
    private View mListView;
    private View mProgressView;

    int userId;
    Boolean exit = false;


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(exitKey, false);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        userId = prefs.getInt(userKey, 0);
        if(userId == 0){
            exit = prefs.getBoolean(exitKey, false);
            if(exit){
                getActivity().finish();
            }else {
                Intent login = new Intent(getActivity(), LoginActivity.class);
                startActivity(login);
            }
        }else{
            if (isNetworkAvailable()) {
                showProgress(true);
                FetchCompromissoTask task = new FetchCompromissoTask();
                task.execute(userId);
            }else {
                Toast.makeText(getActivity(), "Não foi possível acessar seus compromissos. Verifique sua conexão à internet.", Toast.LENGTH_SHORT).show();
            }
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

        mListView = rootView.findViewById(R.id.listview_compromisso);
        mProgressView = rootView.findViewById(R.id.main_progress);
        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_compromisso);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                CompromissoItem comp = mAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), ViewCompromissoActivity.class)
                        .putExtra("id", comp.getId());
                startActivity(intent);
            }
        });
        return rootView;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
            mListView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mListView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class FetchCompromissoTask extends AsyncTask<Integer, Void, CompromissoItem[]> {

        private final String LOG_TAG = FetchCompromissoTask.class.getSimpleName();

        private CompromissoItem[] getCompromissosFromJson(String jsonStr)
                throws JSONException {

            final String COMPROMISSOS = "compromissos";
            final String ID = "id";
            final String TITULO = "compromisso";
            final String DATA = "data";
            final String HORA = "hora";
            final String LOCAL = "local";

            JSONObject json = new JSONObject(jsonStr);
            JSONArray compromissos = json.getJSONArray(COMPROMISSOS);

            CompromissoItem[] resultObjects = new CompromissoItem[compromissos.length()];
            for(int i = 0; i < compromissos.length(); i++) {
                String id;
                String titulo;
                String data;
                String hora;
                String local;

                JSONObject compromisso = compromissos.getJSONObject(i);
                id = compromisso.getString(ID);
                titulo = compromisso.getString(TITULO);
                data = compromisso.getString(DATA);
                hora = compromisso.getString(HORA);
                local = compromisso.getString(LOCAL);

                resultObjects[i] = new CompromissoItem(id, titulo, data, hora, local);
            }
            return resultObjects;

        }

        @Override
        protected CompromissoItem[] doInBackground(Integer... params) {

            // Will contain the raw JSON response as a string.
            String jsonStr = null;
            int uId = params[0];
            String userId = Integer.toString(uId);
            String url = "http://52.203.161.136/usuarios/"+userId+"/compromissos";

            try {
                HttpTools tools = new HttpTools(url);
                jsonStr = tools.doGet();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                return getCompromissosFromJson(jsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(CompromissoItem[] result) {
            if (result != null) {
                mAdapter.clear();
                for(CompromissoItem compromisso : result) {
                    mAdapter.add(compromisso);
                }
            }
            showProgress(false);
        }
    }
}