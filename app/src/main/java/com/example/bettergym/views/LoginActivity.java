package com.example.bettergym.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bettergym.R;
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

        // Inicializar o Controller
        authService = new AuthService();

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
            authService.fazerLogin(email, password, new AuthService.AuthCallback() {
                @Override
                public void onSucesso(String userId) { // Se o teu serviço já devolver o Model User, mudas aqui para (User user) e usas user.getId()
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");

                    // Guardar nas SharedPreferences
                    getSharedPreferences("BetterGymApp", MODE_PRIVATE)
                            .edit()
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