package com.upm.androidnewsletter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.upm.androidnewsletter.R;
import com.upm.androidnewsletter.exceptions.AuthenticationError;
import com.upm.androidnewsletter.model.ModelManager;

import java.util.Properties;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private SharedPreferences sharedPreferences;
    private static ModelManager modelManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // components
        usernameEditText = findViewById(R.id.main_username);
        passwordEditText = findViewById(R.id.main_password);
        loginButton = findViewById(R.id.main_btn_log_in);

        // SharedPreferences to save API key
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        if (modelManager == null) {
            Properties prop = new Properties();
            prop.setProperty("ATTR_SERVICE_URL", "https://sanger.dia.fi.upm.es/pui-rest-news/");
            prop.setProperty("ATTR_REQUIRE_SELF_CERT", "TRUE");

            try {
                modelManager = new ModelManager(prop);
            } catch (Exception e) {
                Log.e("LoginActivity", "Authentication failed: " + e.toString());
            }
        }

        // Login button
        loginButton.setOnClickListener(view -> {
            String username = "DEV_TEAM_09";//usernameEditText.getText().toString();
            String password = "123456@09";//passwordEditText.getText().toString();
            new LoginTask().execute(username, password);
        });
    }

    public static ModelManager getModelManager() {
        return modelManager;
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            try {
                modelManager.login(username, password);
                return true;
            } catch (AuthenticationError e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("apikey", modelManager.getAuthTokenHeader());
                editor.apply();

                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                // Go to MainActivity
                Intent intent = new Intent(LoginActivity.this, ShowArticlesActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
