package com.example.brainalyzer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnSignIn).setOnClickListener(v -> startActivity(new Intent(this, SignInActivity.class)));
        findViewById(R.id.btnSignUp).setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}