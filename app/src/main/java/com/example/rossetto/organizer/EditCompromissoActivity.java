package com.example.rossetto.organizer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class EditCompromissoActivity extends AppCompatActivity {

    private TextView mId;
    private EditText mCompromisso;
    private static EditText mData;
    private static EditText mHora;
    private EditText mLocal;
    private Button mButton;
    private View mProgressView;
    private View mCompromissoFormView;
    private EditaCompromissoTask mCriaCompromisso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_compromisso);
        setupActionBar();

        mId = (TextView) findViewById(R.id.textedit_id);
        mCompromisso = (EditText) findViewById(R.id.textedit_compromisso);
        mData = (EditText) findViewById(R.id.textedit_data);
        mHora = (EditText) findViewById(R.id.textedit_hora);
        mLocal = (EditText) findViewById(R.id.textedit_local);
        mButton = (Button) findViewById(R.id.edit_button);
        mCompromissoFormView = findViewById(R.id.edit_form);
        mProgressView = findViewById(R.id.edit_progress);

        Intent intent = EditCompromissoActivity.this.getIntent();
        if (intent != null && intent.hasExtra("id")) {
            String id = intent.getStringExtra("id");
            mId.setText(id);
        }
        if (intent != null && intent.hasExtra("compromisso")) {
            String compromisso = intent.getStringExtra("compromisso");
            mCompromisso.setText(compromisso);
        }
        if (intent != null && intent.hasExtra("data")) {
            String data = intent.getStringExtra("data");
            mData.setText(data);
        }
        if (intent != null && intent.hasExtra("hora")) {
            String hora = intent.getStringExtra("hora");
            mHora.setText(hora);
        }
        if (intent != null && intent.hasExtra("local")) {
            String local = intent.getStringExtra("local");
            mLocal.setText(local);
        }

        mData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate =Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    mcurrentDate.setTime(sdf.parse(mData.getText().toString()));
                }catch (ParseException e){
                    e.printStackTrace();
                }
                int year=mcurrentDate.get(Calendar.YEAR);
                int month=mcurrentDate.get(Calendar.MONTH);
                int day=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(EditCompromissoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar cal = new GregorianCalendar(selectedyear, selectedmonth, selectedday);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                        mData.setText(dateFormat.format(cal.getTime()));
                    }
                },year, month, day);
                mDatePicker.setTitle("Selecione a data");
                mDatePicker.show();
            }
        });

        mHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate =Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    mcurrentDate.setTime(sdf.parse(mHora.getText().toString()));
                }catch (ParseException e){
                    e.printStackTrace();
                }
                int hour=mcurrentDate.get(Calendar.HOUR_OF_DAY);
                int minute=mcurrentDate.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker=new TimePickerDialog(EditCompromissoActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker timepicker, int selectedhour, int selectedminute) {
                        mHora.setText(""+String.format("%02d", selectedhour) + ":" + String.format("%02d", selectedminute));
                    }
                },hour, minute, true);
                mTimePicker.setTitle("Selecione a hora");
                mTimePicker.show();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = mId.getText().toString();
                String compromisso = mCompromisso.getText().toString();
                String data = mData.getText().toString();
                String hora = mHora.getText().toString();
                String local = mLocal.getText().toString();

                if (TextUtils.isEmpty(compromisso)) {
                    mCompromisso.setError(getString(R.string.error_field_required));
                    mCompromisso.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(data)) {
                    mData.setError(getString(R.string.error_field_required));
                    mData.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(hora)) {
                    mHora.setError(getString(R.string.error_field_required));
                    mHora.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(local)) {
                    mLocal.setError(getString(R.string.error_field_required));
                    mLocal.requestFocus();
                    return;
                }

                showProgress(true);
                CompromissoItem c = new CompromissoItem(id, compromisso, data, hora, local);
                mCriaCompromisso = new EditaCompromissoTask();
                mCriaCompromisso.execute(c);

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

    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCompromissoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCompromissoFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCompromissoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCompromissoFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public class EditaCompromissoTask extends AsyncTask<CompromissoItem, Void, Boolean> {

        private final String LOG_TAG = EditaCompromissoTask.class.getSimpleName();

        @Override
        protected Boolean doInBackground(CompromissoItem... params) {

            String jsonStr = null;
            SharedPreferences prefs = MainActivityFragment.prefs;
            int userId = prefs.getInt(MainActivityFragment.userKey, 0);
            if (userId == 0){
                return false;
            }
            String uId = Integer.toString(userId);

            CompromissoItem mCompromisso = params[0];
            String id = mCompromisso.getId();
            String compromisso = mCompromisso.getCompromisso();
            String data = mCompromisso.getData();
            String hora = mCompromisso.getHora();
            String local = mCompromisso.getLocal();

            HashMap<String, String> pars = new HashMap<String, String>();
            pars.put("compromisso", compromisso);
            pars.put("data", data);
            pars.put("hora", hora);
            pars.put("local", local);
            pars.put("id_usuario", uId);

            String url = "http://52.203.161.136/compromissos/"+id;

            try {
                HttpTools tools = new HttpTools(url, pars);
                jsonStr = tools.doPut();
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

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mCriaCompromisso = null;
            if (success) {
                finish();
            } else {
                showProgress(false);
                Toast.makeText(EditCompromissoActivity.this, "Ocorreu um erro ao editar o compromisso", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
