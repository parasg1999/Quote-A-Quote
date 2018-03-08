package com.parasg1999.www.quoteaquote;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    TextView quoteView;
    TextView authorView;

    private static final String QUOTE_REQUEST_URL =
            "http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quoteView = (TextView) findViewById(R.id.quote_text_view);
        authorView = (TextView) findViewById(R.id.author_text_view);

        Button newQuoteButton = (Button) findViewById(R.id.new_quote_button);
        newQuoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QuoteTask().execute(QUOTE_REQUEST_URL);
            }
        });
    }

    private void newQuote() {

    }

    public class QuoteTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection connection = null;
            String response = "";
            InputStream stream = null;

            try {
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                stream = connection.getInputStream();

                response = readFromStream(stream);


            } catch (MalformedURLException e) {
                Log.e("MainActivity", "URL problem", e);
            } catch (IOException e) {
                Log.e("MainActivity", "InputStream", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (stream != null) {
                    // Closing the input stream could throw an IOException, which is why
                    // the makeHttpRequest(URL url) method signature specifies than an IOException
                    // could be thrown.
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e("MainActivity", "InputStream", e);
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                authorView.setText(s);
                extractFromJson(s);
            } catch (JSONException e) {
                Log.e("extractFromJson", "JSON EXCEPTION" , e);
            }
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private void extractFromJson(String s) throws JSONException {
            JSONArray baseRespose = new JSONArray(s);
            JSONObject quoteObject = baseRespose.getJSONObject(0);
            String title = quoteObject.getString("title");
            String content = quoteObject.getString("content").replace("<p>","").replace("</p>","");
            authorView.setText(title);
            quoteView.setText(content);

        }
    }
}