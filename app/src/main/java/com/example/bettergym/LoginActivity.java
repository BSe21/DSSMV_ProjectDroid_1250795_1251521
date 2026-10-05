package com.example.bettergym;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Ligar as variáveis ao teu ecrã (XML)
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 2. Ação do botão
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preenche o email e a password!", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("A entrar...");

            fazerLogin(email, password);
        });
    }

    private void fazerLogin(String email, String password) {
        try {

            JSONObject dadosLogin = new JSONObject();
            dadosLogin.put("email", email);
            dadosLogin.put("password", password);


            SupabaseHelper.fazerPedido("POST", "auth/v1/token?grant_type=password", dadosLogin, new SupabaseHelper.ApiCallback() {

                @Override
                public void onSuccess(int statusCode, String responseJson) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");

                    try {

                        JSONObject resposta = new JSONObject(responseJson);
                        String token = resposta.getString("access_token");
                        String userId = resposta.getJSONObject("user").getString("id");


                        getSharedPreferences("BetterGymApp", MODE_PRIVATE)
                                .edit()
                                .putString("token", token)
                                .putString("user_id", userId)
                                .apply();

                        Toast.makeText(LoginActivity.this, "Login efetuado com sucesso!", Toast.LENGTH_LONG).show();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                        finish();

                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Erro ao processar a resposta.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    String msg="Email ou palavra-passe incorretos!";
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Entrar");
                    Toast.makeText(LoginActivity.this, msg , Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            btnLogin.setEnabled(true);
            btnLogin.setText("Entrar");
        }
    }
}