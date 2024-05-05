package com.example.itube;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText editTextName, editTextUsernameSignUp, editTextPasswordSignUp, editTextConfirmPassword;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        editTextName = findViewById(R.id.editTextName);
        editTextUsernameSignUp = findViewById(R.id.editTextUsernameSignUp);
        editTextPasswordSignUp = findViewById(R.id.editTextPasswordSignUp);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button signUpButton = findViewById(R.id.buttonSignUpUser);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String username = editTextUsernameSignUp.getText().toString();
                String password = editTextPasswordSignUp.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();

                if (password.equals(confirmPassword)) {
                    long newRowId = dbHelper.addUser(name, username, password);
                    if (newRowId != -1) {
                        Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close sign-up activity
                    } else {
                        Toast.makeText(SignUpActivity.this, "Error registering user", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
