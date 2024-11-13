package com.upm.androidnewsletter.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.upm.androidnewsletter.R;
import com.upm.androidnewsletter.exceptions.ServerCommunicationError;
import com.upm.androidnewsletter.model.Article;
import com.upm.androidnewsletter.model.ArticleAdapter;
import com.upm.androidnewsletter.model.ModelManager;

import java.io.IOException;
import java.util.List;

public class ShowArticlesActivity extends AppCompatActivity {

    private ModelManager modelManager;
    private ListView listViewArticles;
    private ArticleAdapter articleAdapter;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_articles);

        modelManager = LoginActivity.getModelManager();
        listViewArticles = findViewById(R.id.listViewArticles);
        loadingIndicator = findViewById(R.id.progressBarLoading);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fetchArticlesIfConnected();
    }

    // Method to check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Trigger article fetch if network is available
    private void fetchArticlesIfConnected() {
        if (isNetworkAvailable()) {

            new FetchArticlesTask().execute();
        } else {
            Toast.makeText(this, "No internet connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    private class FetchArticlesTask extends AsyncTask<Void, Void, List<Article>> {
        private String errorMessage = null;

        @Override
        protected List<Article> doInBackground(Void... voids) {
            try {
                // Fetch articles from the server
                return modelManager.getArticles();
            } catch (ServerCommunicationError e) {
                errorMessage = e.getMessage(); // Server-specific error
                return null;
            } catch (Exception e) {
                errorMessage = "Network error. Please check your connection."; // General network error
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Article> articles) {
            if (articles != null) {
                // Set up the adapter with the fetched articles
                articleAdapter = new ArticleAdapter(ShowArticlesActivity.this, R.layout.item_article, articles);
                listViewArticles.setAdapter(articleAdapter);
                loadingIndicator.setVisibility(View.GONE);
                listViewArticles.setVisibility(View.VISIBLE);

                listViewArticles.setOnItemClickListener((parent, view, position, id) -> {
                    Article selectedArticle = articleAdapter.getItem(position);
                    if (selectedArticle != null) {
                        Intent intent = new Intent(ShowArticlesActivity.this, ArticleDetailActivity.class);
                        intent.putExtra("id", String.valueOf(selectedArticle.getId()));
                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(ShowArticlesActivity.this, "Failed to fetch articles: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
