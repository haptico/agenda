package com.example.rossetto.organizer;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class HttpTools {

    private String url;
    private HashMap<String, String> params = new HashMap<>();
    private String method;

    public HttpTools(String url) {
        this.url = url;
    }

    public HttpTools(String url, HashMap<String, String> params) {
        this.url = url;
        this.params = params;
    }

    public String getQuery() throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(key), "UTF-8"));
        }

        return result.toString();
    }

    public String doDelete() throws IOException {
        method = "DELETE";
        return readConnection();
    }

    public String doPost() throws IOException {
        method = "POST";
        return readConnection();
    }

    public String doGet() throws IOException {
        method = "GET";
        return readConnection();
    }

    public String doPut() throws IOException {
        method = "PUT";
        return readConnection();
    }

    private String readConnection() throws IOException {

        Uri builtUri = Uri.parse(url).buildUpon().build();
        URL mUrl = new URL(builtUri.toString());

        HttpURLConnection urlConnection = (HttpURLConnection) mUrl.openConnection();
        urlConnection.setRequestMethod(method);
        if(!params.isEmpty()) {
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery());
            writer.flush();
            writer.close();
            os.close();
        }
        urlConnection.connect();

        // Read the input stream into a String
        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();
        if (inputStream == null) {
            // Nothing to do.
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        if (buffer.length() == 0) {
            // Stream was empty.  No point in parsing.
            return null;
        }

        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }

}
