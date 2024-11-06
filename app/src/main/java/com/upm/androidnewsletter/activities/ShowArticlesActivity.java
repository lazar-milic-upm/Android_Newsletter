package com.upm.androidnewsletter.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Properties;
import java.io.IOException;

import com.upm.androidnewsletter.R;
import com.upm.androidnewsletter.exceptions.AuthenticationError;
import com.upm.androidnewsletter.exceptions.ServerCommunicationError;
import com.upm.androidnewsletter.model.Article;
import com.upm.androidnewsletter.model.ArticleAdapter;
import com.upm.androidnewsletter.model.ModelManager;

public class ShowArticlesActivity extends AppCompatActivity {

    private ModelManager modelManager;
    private ListView listViewArticles;
    private ArticleAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_articles);

        modelManager = LoginActivity.getModelManager();

        listViewArticles = findViewById(R.id.listViewArticles);

        new FetchArticlesTask().execute();
    }

    private class FetchArticlesTask extends AsyncTask<Void, Void, List<Article>> {
        private String errorMessage = null;

        @Override
        protected List<Article> doInBackground(Void... voids) {
            try {
                // Fetch articles from the server
                return modelManager.getArticles(10, 2);
            } catch (ServerCommunicationError e) {
                // If there's an error, store the message
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Article> articles) {
            if (articles != null) {
                // Set up the adapter with the fetched articles
                articleAdapter = new ArticleAdapter(ShowArticlesActivity.this, R.layout.item_article, articles);
                listViewArticles.setAdapter(articleAdapter);
            } else {
                Toast.makeText(ShowArticlesActivity.this, "Failed to fetch articles: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
