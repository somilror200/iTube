package com.example.itube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

        loginButton.setOnClickListener(v -> attemptLogin());
        signupButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SignUpActivity.class)));
    }

    private void attemptLogin() {
        usernameEditText.setError(null);
        passwordEditText.setError(null);

        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        loginButton.setEnabled(false);

        AppExecutors.database().execute(() -> {
            long userId = dbHelper.authenticateUser(username, password);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                loginButton.setEnabled(true);

                if (userId < 0) {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveUserId(userId);
                passwordEditText.setText("");
                startActivity(new Intent(this, MainPageActivity.class));
                finish();
            });
        });
    }

    private void saveUserId(long userId) {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.session_preferences), MODE_PRIVATE);
        preferences.edit()
                .putLong(getString(R.string.key_current_user_id), userId)
                .apply();
    }
}
