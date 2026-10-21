package com.example.bettergym.views;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.bettergym.R;
import com.example.bettergym.service.AuthService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class AlterarPasswordDialogFragment extends DialogFragment {
    private TextInputEditText atual, nova, confirmacao;
    private TextView erro;
    private MaterialButton guardar, cancelar;
    private boolean aGuardar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(),
                R.style.ThemeOverlay_BetterGym_PasswordDialog);
        View conteudo = LayoutInflater.from(builder.getContext())
                .inflate(R.layout.dialog_alterar_password, null);
        atual = conteudo.findViewById(R.id.editPasswordAtual);
        nova = conteudo.findViewById(R.id.editPasswordNova);
        confirmacao = conteudo.findViewById(R.id.editPasswordConfirmacao);
        erro = conteudo.findViewById(R.id.txtErroPassword);
        guardar = conteudo.findViewById(R.id.btnGuardarPassword);
        cancelar = conteudo.findViewById(R.id.btnCancelarPassword);
        AlertDialog dialog = builder
                .setView(conteudo)
                .create();
        guardar.setOnClickListener(v -> guardar(dialog));
        cancelar.setOnClickListener(v -> dismiss());
        confirmacao.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                guardar(dialog);
                return true;
            }
            return false;
        });
        return dialog;
    }

    private void guardar(AlertDialog dialog) {
        if (aGuardar) return;
        erro.setVisibility(View.GONE);
        definirOcupado(dialog, true);
        new AuthService().alterarPalavraPasse(texto(atual), texto(nova), texto(confirmacao),
                new AuthService.AuthCallback() {
                    @Override
                    public void onSucesso(String mensagem) {
                        if (!isAdded() || getDialog() != dialog) return;
                        limparCampos();
                        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
                        dismissAllowingStateLoss();
                    }

                    @Override
                    public void onErro(String mensagem) {
                        if (!isAdded() || getDialog() != dialog) return;
                        definirOcupado(dialog, false);
                        erro.setText(mensagem);
                        erro.setVisibility(View.VISIBLE);
                    }
                });
    }

    private String texto(TextInputEditText campo) {
        // Os espaços fazem parte da palavra-passe: não aplicar trim().
        return campo.getText() == null ? "" : campo.getText().toString();
    }

    private void definirOcupado(AlertDialog dialog, boolean ocupado) {
        aGuardar = ocupado;
        setCancelable(!ocupado);
        dialog.setCanceledOnTouchOutside(!ocupado);
        atual.setEnabled(!ocupado);
        nova.setEnabled(!ocupado);
        confirmacao.setEnabled(!ocupado);
        guardar.setEnabled(!ocupado);
        guardar.setText(ocupado
                ? R.string.password_saving : R.string.password_save);
        cancelar.setEnabled(!ocupado);
    }

    private void limparCampos() {
        if (atual != null && atual.getText() != null) atual.getText().clear();
        if (nova != null && nova.getText() != null) nova.getText().clear();
        if (confirmacao != null && confirmacao.getText() != null) confirmacao.getText().clear();
    }

    @Override
    public void onDestroyView() {
        limparCampos();
        atual = null;
        nova = null;
        confirmacao = null;
        erro = null;
        guardar = null;
        cancelar = null;
        super.onDestroyView();
    }
}
