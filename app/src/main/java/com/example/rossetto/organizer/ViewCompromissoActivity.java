package com.example.rossetto.organizer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ViewCompromissoActivity extends AppCompatActivity {

    private TextView mCompromisso;
    private TextView mData;
    private TextView mHora;
    private TextView mLocal;
    private TextView mId;
    private Button mEditButton;
    private Button mDeleteButton;
    private View mProgressView;
    private View mFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_compromisso);
        setupActionBar();

        mId = (TextView) findViewById(R.id.textview_id);
        mCompromisso = (TextView) findViewById(R.id.textview_compromisso);
        mData = (TextView) findViewById(R.id.textview_data);
        mHora = (TextView) findViewById(R.id.textview_hora);
        mLocal = (TextView) findViewById(R.id.textview_local);
        mEditButton = (Button) findViewById(R.id.compromisso_edit_button);
        mDeleteButton = (Button) findViewById(R.id.compromisso_delete_button);
        mFormView = findViewById(R.id.view_form);
        mProgressView = findViewById(R.id.view_progress);

        Intent intent = ViewCompromissoActivity.this.getIntent();
        if (intent != null && intent.hasExtra("id")) {
            String id = intent.getStringExtra("id");
            mId.setText(id);
        }

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mId.getText().toString();
                String compromisso = mCompromisso.getText().toString();
                String data = mData.getText().toString();
                String hora = mHora.getText().toString();
                String local = mLocal.getText().toString();
                Intent intent = new Intent(ViewCompromissoActivity.this, EditCompromissoActivity.class)
                        .putExtra("id", id)
                        .putExtra("compromisso", compromisso)
                        .putExtra("data", data)
                        .putExtra("hora", hora)
                        .putExtra("local", local);
                startActivity(intent);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ViewCompromissoActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Excluir Compromisso")
                        .setMessage("Deseja realmente excluir esse compromisso?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String id = mId.getText().toString();
                                ExcluiCompromisso exclui = new ExcluiCompromisso();
                                exclui.execute(id);
                            }

                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
        });

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String id = mId.getText().toString();
        showProgress(true);
        FetchCompromisso task = new FetchCompromisso();
        task.execute(id);
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class ExcluiCompromisso extends AsyncTask<String, Void, Boolean> {

        private final String LOG_TAG = ExcluiCompromisso.class.getSimpleName();

        @Override
        protected Boolean doInBackground(String... params) {

            String jsonStr = null;
            String cId = params[0];
            String url = "http://52.203.161.136/compromissos/"+cId;

            try {
                HttpTools tools = new HttpTools(url);
                jsonStr = tools.doDelete();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            try {
                JSONObject c = new JSONObject(jsonStr);
                if (c.has("success")) {
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            showProgress(false);
            if (success) {
                finish();
            } else {
                Toast.makeText(ViewCompromissoActivity.this, "Ocorreu um erro ao excluir o compromisso", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class FetchCompromisso extends AsyncTask<String, Void, CompromissoItem> {

        private final String LOG_TAG = FetchCompromisso.class.getSimpleName();

        @Override
        protected CompromissoItem doInBackground(String... params) {

            String jsonStr = null;
            String id = params[0];
            String url = "http://52.203.161.136/compromissos/"+id;

            try {
                HttpTools tools = new HttpTools(url);
                jsonStr = tools.doGet();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            try {
                JSONObject c = new JSONObject(jsonStr);
                int compId = c.getInt("id");
                if(compId == 0){
                    return null;
                }else{
                    String cId = c.getString("id");
                    String cTitulo = c.getString("compromisso");
                    String cData = c.getString("data");
                    String cHora = c.getString("hora");
                    String cLocal = c.getString("local");
                    CompromissoItem comp = new CompromissoItem(cId, cTitulo, cData, cHora, cLocal);
                    return comp;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(CompromissoItem comp) {
            showProgress(false);
            if (comp != null) {
                mId.setText(comp.getId());
                mCompromisso.setText(comp.getCompromisso());
                mData.setText(comp.getData());
                mHora.setText(comp.getHora());
                mLocal.setText(comp.getLocal());
            } else {
                Toast.makeText(ViewCompromissoActivity.this, "Ocorreu um erro ao carregar o compromisso", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
