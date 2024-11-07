package com.upm.androidnewsletter.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.upm.androidnewsletter.R;
import com.upm.androidnewsletter.model.Article;
import com.upm.androidnewsletter.model.ModelManager;

import java.io.IOException;

public class ArticleDetailActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView textViewTitle, textViewAbstract, textViewBody;
    private ImageView imageViewArticle;
    private Button buttonUploadImage;
    private Article article;
    private ModelManager modelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        modelManager = LoginActivity.getModelManager();

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewAbstract = findViewById(R.id.textViewAbstract);
        textViewBody = findViewById(R.id.textViewBody);
        imageViewArticle = findViewById(R.id.imageViewArticle);
        buttonUploadImage = findViewById(R.id.buttonUploadImage);

        // Retrieve the article from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("article")) {
            article = (Article) intent.getSerializableExtra("article");
            displayArticleDetails();
        }

        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });
    }

    private void displayArticleDetails() {
        if (article != null) {
            textViewTitle.setText(article.getTitleText());
            textViewAbstract.setText(article.getAbstractText());
            textViewBody.setText(article.getBodyText());
            // Load article image if available
            // imageViewArticle.setImageBitmap(article.getImage().getBitmap()); // Update with actual loading logic
        }
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
                imageViewArticle.setImageBitmap(bitmap);
                // Upload image to server
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
            // Convert bitmap to Base64 and upload using modelManager
            // String base64Image = Utils.bitmapToBase64(bitmaps[0]);
            // return modelManager.uploadImage(article.getId(), base64Image);
            return true; // Placeholder
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
}

