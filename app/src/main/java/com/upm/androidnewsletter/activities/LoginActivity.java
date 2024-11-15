package com.upm.androidnewsletter.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.upm.androidnewsletter.R;
import com.upm.androidnewsletter.exceptions.AuthenticationError;
import com.upm.androidnewsletter.model.ModelManager;
import java.util.Properties;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private static ModelManager modelManager;
    private String usernameString;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.main_username);
        passwordEditText = findViewById(R.id.main_password);
        loginButton = findViewById(R.id.main_btn_log_in);
        saveLoginCheckBox = findViewById(R.id.saveLoginCheckBox);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (modelManager == null) {
            Properties prop = new Properties();
            prop.setProperty("ATTR_SERVICE_URL", "https://sanger.dia.fi.upm.es/pui-rest-news/");
            prop.setProperty("ATTR_REQUIRE_SELF_CERT", "TRUE");

            try {
                modelManager = new ModelManager(prop);
            } catch (Exception e) {
                Log.e("LoginActivity", "Authentication failed: " + e);
            }
        }

        loginButton.setOnClickListener(view -> {
            if (saveLoginCheckBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("username", usernameEditText.getText().toString());
                loginPrefsEditor.putString("password", passwordEditText.getText().toString());
                loginPrefsEditor.commit();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }
            String username = usernameEditText.getText().toString();
            usernameString = username;
            String password = passwordEditText.getText().toString();
            new LoginTask().execute(username, password);
        });

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            usernameEditText.setText(loginPreferences.getString("username", ""));
            passwordEditText.setText(loginPreferences.getString("password", ""));
            saveLoginCheckBox.setChecked(true);
        }
    }

    public static ModelManager getModelManager() {
        return modelManager;
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

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

        protected void onPostExecute(Boolean success) {
            if (success) {
                SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("apikey", modelManager.getAuthTokenHeader());
                editor.apply();

                Toast.makeText(LoginActivity.this, "Login successful! \nWelcome " + usernameString + "!" , Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, ShowArticlesActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}
