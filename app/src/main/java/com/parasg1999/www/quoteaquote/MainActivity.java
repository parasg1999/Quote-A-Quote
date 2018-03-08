package com.parasg1999.www.quoteaquote;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

    private static final String QUOTE_REQUEST_URL =
            "http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=1";
    ProgressBar progressBar;
    private TextView quoteView;
    private TextView authorView;
    private ImageView shareButton, newQuoteButton;
    private String currentQuote, currentAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        quoteView = findViewById(R.id.quote_text_view);
        authorView = findViewById(R.id.author_text_view);

        shareButton = findViewById(R.id.share_button);
        newQuoteButton = findViewById(R.id.new_quote_button);
        newQuoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorView.setVisibility(View.GONE);
                quoteView.setVisibility(View.GONE);
                shareButton.setVisibility(View.GONE);
                newQuoteButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                new QuoteTask().execute(QUOTE_REQUEST_URL);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentAuthor != null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, currentQuote + "\n-" + currentAuthor);
                    intent.setType("text/plain");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });

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
            currentAuthor = quoteObject.getString("title");
            currentQuote = quoteObject.getString("content").replace("<p>", "").replace("</p>", "").replace("<br />", "").replace("&#8217;", "'").replace("&#8211;", "–").replace("&#038;", "&").replace("&#8220;", "“").replace("&#8221;", "”").replace("&#8216;", "‘");
            quoteView.setText(currentQuote);
            authorView.setText("- " + currentAuthor);
            progressBar.setVisibility(View.GONE);
            if (shareButton.getVisibility() != View.VISIBLE) {
                shareButton.setVisibility(View.VISIBLE);
            }
            quoteView.setVisibility(View.VISIBLE);
            authorView.setVisibility(View.VISIBLE);
            newQuoteButton.setVisibility(View.VISIBLE);

        }
    }
}