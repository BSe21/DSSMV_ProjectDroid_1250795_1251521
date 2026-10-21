package com.example.bettergym.views;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bettergym.R;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.exceptions.DuplicatedDataException;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.service.AuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private Uri fotoPerfilUri = null;

    private TextInputEditText editNome, editUsername, editBirthDate, editPhone, editEmail, editPassword, editConfirmPassword;
    private MaterialButton btnRegistar;
    private AuthService authService;

    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    fotoPerfilUri = uri;
                    imgProfile.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new AuthService();

        imgProfile = findViewById(R.id.imgProfile);
        editNome = findViewById(R.id.editNome);
        editUsername = findViewById(R.id.editUsername);
        editBirthDate = findViewById(R.id.editBirthDate);
        editPhone = findViewById(R.id.editPhone);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegistar = findViewById(R.id.btnRegistar);

        imgProfile.setOnClickListener(v -> galeriaLauncher.launch("image/*"));
        findViewById(R.id.txtSelectPhoto).setOnClickListener(v -> galeriaLauncher.launch("image/*"));
        editBirthDate.setOnClickListener(v -> abrirSeletorData());
        findViewById(R.id.txtLogin).setOnClickListener(v -> finish());

        btnRegistar.setOnClickListener(v -> tentarRegisto());
    }

    private void abrirSeletorData() {
        Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR) - 18;
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    editBirthDate.setText(dataFormatada);
                },
                ano, mes, dia
        );
        datePickerDialog.show();
    }

    private void tentarRegisto() {
        String nome = editNome.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String dataNasc = editBirthDate.getText().toString().trim();
        String telefone = editPhone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirmar = editConfirmPassword.getText().toString();

        try {
            btnRegistar.setEnabled(false);
            btnRegistar.setText("A criar conta...");

            authService.registarNovaConta(nome, username, dataNasc, telefone, email, password, confirmar, new AuthService.AuthCallback() {
                @Override
                public void onSucesso(String mensagem) {
                    Toast.makeText(RegisterActivity.this, mensagem, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onErro(String erro) {
                    restaurarBotao();
                    Toast.makeText(RegisterActivity.this, erro, Toast.LENGTH_LONG).show();
                }
            });

        } catch (InvalidDataException e) {
            restaurarBotao();
            Toast.makeText(RegisterActivity.this, e.what(), Toast.LENGTH_LONG).show();
        } catch (DuplicatedDataException e) {
            restaurarBotao();
            Toast.makeText(RegisterActivity.this, e.what(), Toast.LENGTH_LONG).show();
        } catch (DataConsistencyException e) {
            restaurarBotao();
            Toast.makeText(RegisterActivity.this, e.what(), Toast.LENGTH_LONG).show();
        }
    }

    private void restaurarBotao() {
        btnRegistar.setEnabled(true);
        btnRegistar.setText("Criar conta");
    }
}