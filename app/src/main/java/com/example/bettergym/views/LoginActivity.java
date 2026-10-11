package com.example.bettergym.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bettergym.R;
import com.example.bettergym.controllers.authController;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private MaterialButton btnLogin;
    private authController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar o Controller
        authController = new authController();

        // Ligar as variáveis ao ecrã (XML)
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Link para o ecrã de registo
        findViewById(R.id.txtRegistar).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Ação do botão de login
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            btnLogin.setEnabled(false);
            btnLogin.setText("A entrar...");

            // Chamar o Controller
            authController.fazerLogin(email, password, new authController.AuthCallback() {
                @Override
                public void onSucesso(String resultado) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");

                    // O resultado traz "token|userId" separado por pipe
                    String[] partes = resultado.split("\\|");
                    String token = partes[0];
                    String userId = partes[1];

                    // Guardar nas SharedPreferences
                    getSharedPreferences("BetterGymApp", MODE_PRIVATE)
                            .edit()
                            .putString("token", token)
                            .putString("user_id", userId)
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login efetuado com sucesso!", Toast.LENGTH_LONG).show();

                    // Ir para a MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onErro(String erro) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    Toast.makeText(LoginActivity.this, erro, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}