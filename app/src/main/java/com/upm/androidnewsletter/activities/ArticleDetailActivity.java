package com.upm.androidnewsletter.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewAbstract = findViewById(R.id.textViewAbstract);
        webViewBody = findViewById(R.id.webViewBody);
        imageViewArticle = findViewById(R.id.imageViewArticle);
        buttonUploadImage = findViewById(R.id.buttonUploadImage);

        imageViewArticle = findViewById(R.id.imageViewArticle);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        int articleId = Integer.parseInt(intent.getStringExtra("id"));
        modelManager = LoginActivity.getModelManager();
        new GetArticleTask().execute(articleId);

        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });
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
                jsonImage.put("data", Utils.createScaledStrImage(base64Image,250,250));
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
                Toast.makeText(ArticleDetailActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
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
                // Handle the error, e.g., log it or display an error message
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Article article) {
            if (article != null) {
                try {
                    textViewTitle.setText(article.getTitleText());

                    textViewAbstract.setText(article.getAbstractText());
                    webViewBody.loadDataWithBaseURL(null, article.getBodyText(), "text/html", "UTF-8", null);
                    imageViewArticle.setImageBitmap(article.getImage().getBitmapImage());

                    loadingIndicator.setVisibility(View.GONE);
                    textViewTitle.setVisibility(View.VISIBLE);
                    textViewAbstract.setVisibility(View.VISIBLE);
                    webViewBody.setVisibility(View.VISIBLE);
                    imageViewArticle.setVisibility(View.VISIBLE);

                    buttonUploadImage.setVisibility(View.VISIBLE);

                    articleToDisplay = article;

                    try {
                        imageViewArticle.setImageBitmap(article.getImage().getBitmapImage());
                    } catch (ServerCommunicationError e) {
                        throw new RuntimeException(e);
                    }
                } catch (ServerCommunicationError e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

