package com.upm.androidnewsletter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.upm.androidnewsletter.R;
import com.upm.androidnewsletter.exceptions.ServerCommunicationError;
import com.upm.androidnewsletter.model.Article;
import com.upm.androidnewsletter.model.Image;
import com.upm.androidnewsletter.model.ModelManager;
import com.upm.androidnewsletter.model.Utils;
import com.upm.androidnewsletter.model.json.JSONObject;

import java.io.IOException;

public class ArticleDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView textViewTitle, textViewAbstract;
    private WebView webViewBody;
    private ImageView imageViewArticle;
    private Button buttonUploadImage;
    private Article articleToDisplay;
    private ModelManager modelManager;
    private ProgressBar loadingIndicator;
    private int articleId;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_article_detail);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewAbstract = findViewById(R.id.textViewAbstract);
        webViewBody = findViewById(R.id.webViewBody);
        imageViewArticle = findViewById(R.id.imageViewArticle);
        buttonUploadImage = findViewById(R.id.buttonUploadImage);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
        modelManager = LoginActivity.getModelManager();
        articleId = Integer.parseInt(intent.getStringExtra("id"));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        new GetArticleTask().execute(articleId);

        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageViewArticle.setImageBitmap(bitmap); // Display the selected image
                new UploadImageTask().execute(bitmap);
                loadingIndicator.setVisibility(View.VISIBLE);
                textViewTitle.setVisibility(View.GONE);
                textViewAbstract.setVisibility(View.GONE);
                webViewBody.setVisibility(View.GONE);
                imageViewArticle.setVisibility(View.GONE);
                buttonUploadImage.setVisibility(View.GONE);
                Toast.makeText(ArticleDetailActivity.this, "Image is being uploaded...", Toast.LENGTH_SHORT).show();
                new GetArticleTask().execute(articleId);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UploadImageTask extends AsyncTask<Bitmap, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            try {
                String base64Image = Utils.imgToBase64String(bitmaps[0]);
                JSONObject jsonImage = new JSONObject();
                jsonImage.put("id", -1);
                jsonImage.put("id_article", articleToDisplay.getId());
                jsonImage.put("order", "0");
                jsonImage.put("description", "User uploaded image");
                jsonImage.put("data", Utils.createScaledStrImage(base64Image,1000,750));
                Image imageToUpload = new Image(modelManager, jsonImage);
                modelManager.save(imageToUpload);
                return true;
            } catch (ServerCommunicationError e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(ArticleDetailActivity.this, "Image uploaded successfully! The article will be refreshed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ArticleDetailActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetArticleTask extends AsyncTask<Integer, Void, Article> {

        @Override
        protected Article doInBackground(Integer... params) {
            int articleId = params[0];
            try {
                return modelManager.getArticle(articleId);
            } catch (ServerCommunicationError e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Article article) {
            if (article != null) {
                try {
                    textViewTitle.setText(Html.fromHtml(article.getTitleText()));
                    textViewAbstract.setText(Html.fromHtml(article.getAbstractText()));
                    webViewBody.loadDataWithBaseURL(null, article.getBodyText(), "text/html", "UTF-8", null);
                    webViewBody.setBackgroundColor(Color.parseColor("#FAFAFA"));
                    imageViewArticle.setImageBitmap(article.getImage().getBitmapImage());
                    loadingIndicator.setVisibility(View.GONE);
                    textViewTitle.setVisibility(View.VISIBLE);
                    textViewAbstract.setVisibility(View.VISIBLE);
                    webViewBody.setVisibility(View.VISIBLE);
                    imageViewArticle.setVisibility(View.VISIBLE);
                    buttonUploadImage.setVisibility(View.VISIBLE);
                    scrollView = findViewById(R.id.scrollViewArticleDetail);
                    scrollView.smoothScrollTo(0,0);
                    articleToDisplay = article;
                } catch (ServerCommunicationError e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

