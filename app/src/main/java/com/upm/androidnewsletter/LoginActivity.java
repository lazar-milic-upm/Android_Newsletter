package com.upm.androidnewsletter;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private SharedPreferences sharedPreferences;

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

        // Login button
        loginButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            new LoginTask().execute(username, password);
        });
    }

    // Login asynchronous
    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];

            // fake token
            if ("user".equals(username) && "password".equals(password)) {
                return "fakeApiKey123456";  // Return a fake API key if login is successful
            }
            return null;
        }

        @Override
        protected void onPostExecute(String apiKey) {
            if (apiKey != null) {
                // save API key in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("apikey", apiKey);
                editor.apply();

                // if Success
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                // startActivity(new Intent(LoginActivity.this, ShowArticlesActivity.class));
            } else {
                // if Error
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
