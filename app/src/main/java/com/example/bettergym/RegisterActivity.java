package com.example.bettergym;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editNome, editEmail, editPassword, editConfirmPassword;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmLayout;
    private MaterialButton btnRegistar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmLayout = findViewById(R.id.confirmLayout);
        btnRegistar = findViewById(R.id.btnRegistar);

        // Voltar ao login
        findViewById(R.id.txtLogin).setOnClickListener(v -> finish());

        btnRegistar.setOnClickListener(v -> {
            String nome = editNome.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString();
            String confirmar = editConfirmPassword.getText().toString();

            if (!validar(nome, email, password, confirmar)) {
                return;
            }

            btnRegistar.setEnabled(false);
            btnRegistar.setText("A criar conta...");

            fazerRegisto(nome, email, password);
        });
    }

    private boolean validar(String nome, String email, String password, String confirmar) {
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmLayout.setError(null);

        boolean ok = true;

        if (nome.isEmpty()) {
            nameLayout.setError("Escreve o teu nome");
            ok = false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email inválido");
            ok = false;
        }
        if (password.length() < 6) {
            passwordLayout.setError("A password deve ter pelo menos 6 caracteres");
            ok = false;
        }
        if (!password.equals(confirmar)) {
            confirmLayout.setError("As passwords não coincidem");
            ok = false;
        }
        return ok;
    }

    // Passo 1: criar o utilizador no Supabase Auth
    private void fazerRegisto(String nome, String email, String password) {
        try {
            JSONObject dados = new JSONObject();
            dados.put("email", email);
            dados.put("password", password);

            JSONObject metadados = new JSONObject();
            metadados.put("nome_completo", nome);
            dados.put("data", metadados);

            SupabaseHelper.fazerPedido("POST", "auth/v1/signup", dados, new SupabaseHelper.ApiCallback() {

                @Override
                public void onSuccess(int statusCode, String responseJson) {
                    try {
                        JSONObject resposta = new JSONObject(responseJson);

                        // Com sessão o id vem dentro de "user"; sem sessão vem no topo da resposta.
                        String userId = resposta.has("user")
                                ? resposta.getJSONObject("user").getString("id")
                                : resposta.getString("id");

                        criarPerfil(userId, nome);

                    } catch (Exception e) {
                        restaurarBotao();
                        Toast.makeText(RegisterActivity.this, "Erro ao processar a resposta.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    restaurarBotao();

                    String msg;
                    if (errorMessage.contains("user_already_exists") || errorMessage.contains("already registered")) {
                        msg = "Já existe uma conta com esse email.";
                    } else if (errorMessage.contains("weak_password")) {
                        msg = "Password demasiado fraca.";
                    } else if (errorMessage.contains("email_address_invalid") || errorMessage.contains("validation_failed")) {
                        msg = "Email inválido.";
                    } else if (errorMessage.contains("over_email_send_rate_limit") || errorMessage.contains("429")) {
                        msg = "Demasiados pedidos. Tenta daqui a uns minutos.";
                    } else if (errorMessage.startsWith("Falha de ligação")) {
                        msg = "Sem ligação ao servidor. Verifica a internet.";
                    } else {
                        msg = "Não foi possível criar a conta.";
                    }
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            restaurarBotao();
        }
    }

    // Passo 2: inserir a linha na tabela perfis (id, nome_completo, data_registo)
    private void criarPerfil(String userId, String nome) {
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            iso.setTimeZone(TimeZone.getTimeZone("UTC"));

            JSONObject perfil = new JSONObject();
            perfil.put("id", userId);
            perfil.put("nome_completo", nome);
            perfil.put("data_registo", iso.format(new Date()));

            SupabaseHelper.fazerPedido("POST", "rest/v1/perfis", perfil, new SupabaseHelper.ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseJson) {
                    terminar("Conta criada com sucesso! Faz login para entrar.");
                }

                @Override
                public void onFailure(String errorMessage) {
                    // A conta já existe; só o perfil falhou. Não bloqueia o utilizador.
                    terminar("Conta criada, mas não foi possível guardar o perfil. Faz login para entrar.");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            terminar("Conta criada, mas não foi possível guardar o perfil. Faz login para entrar.");
        }
    }

    // Passo 3: voltar ao ecrã de login para o utilizador entrar
    private void terminar(String mensagem) {
        restaurarBotao();
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
        finish();
    }

    private void restaurarBotao() {
        btnRegistar.setEnabled(true);
        btnRegistar.setText("Criar conta");
    }
}