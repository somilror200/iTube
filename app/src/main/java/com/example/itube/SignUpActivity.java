package com.example.itube;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signUpButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = DatabaseHelper.getInstance(this);
        nameEditText = findViewById(R.id.editTextName);
        usernameEditText = findViewById(R.id.editTextUsernameSignUp);
        passwordEditText = findViewById(R.id.editTextPasswordSignUp);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        signUpButton = findViewById(R.id.buttonSignUpUser);

        signUpButton.setOnClickListener(v -> attemptSignUp());
    }

    private void attemptSignUp() {
        clearErrors();

        String name = nameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (name.length() < 2) {
            nameEditText.setError("Enter your name");
            nameEditText.requestFocus();
            return;
        }

        if (!username.matches("[A-Za-z0-9_]{3,24}")) {
            usernameEditText.setError("Use 3–24 letters, numbers or underscores");
            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8 || password.trim().isEmpty()) {
            passwordEditText.setError("Use at least 8 non-blank characters");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        signUpButton.setEnabled(false);

        AppExecutors.database().execute(() -> {
            boolean alreadyExists = dbHelper.usernameExists(username);
            long newRowId = alreadyExists ? -1 : dbHelper.addUser(name, username, password);

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                signUpButton.setEnabled(true);

                if (alreadyExists || newRowId < 0) {
                    usernameEditText.setError("That username is already taken");
                    usernameEditText.requestFocus();
                    return;
                }

                Toast.makeText(this, "Account created — sign in to continue", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void clearErrors() {
        nameEditText.setError(null);
        usernameEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);
    }
}
