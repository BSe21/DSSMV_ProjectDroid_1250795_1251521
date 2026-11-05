package com.example.bettergym.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bettergym.R;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.service.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private MaterialButton btnLogin;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        findViewById(R.id.txtRegistar).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString();

            btnLogin.setEnabled(false);
            btnLogin.setText("A entrar...");

            authService.fazerLogin(email, password, new AuthService.AuthCallback() {
                @Override
                public void onSucesso(String userId) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");

                    getSharedPreferences("BetterGymApp", MODE_PRIVATE)
                            .edit()
                            .putString("user_id", userId)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login efetuado com sucesso!", Toast.LENGTH_LONG).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
                @Override
                public void onErro(Exception erro) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    String msg = erro.getMessage();
                    if (erro instanceof InvalidDataException) {
                        msg = ((InvalidDataException) erro).what();
                    }
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
