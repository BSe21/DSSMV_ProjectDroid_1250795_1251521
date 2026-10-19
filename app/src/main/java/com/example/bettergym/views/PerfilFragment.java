package com.example.bettergym.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bettergym.R;
import com.example.bettergym.controllers.FormatadorPerfil;
import com.example.bettergym.controllers.PerfilController;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.model.Perfil;
import com.google.android.material.button.MaterialButton;

//Vista do perfil do utilizador, apresentada dentro da MainActivity (a barra de navegação mantém-se visível).
// A vista só apresenta dados: não fala com o Firebase. Pede tudo ao {@link PerfilController}.

public class PerfilFragment extends Fragment {

    private static final String FICHEIRO_PREFERENCIAS = "BetterGymApp";

    private PerfilController controlador;

    private ImageView imgFotoPerfil;
    private TextView txtIniciais, txtNome, txtUsername;
    private TextView txtVisitas, txtFavoritos, txtAvaliacoes;
    private TextView txtEmail, txtTelemovel, txtNascimento, txtMembroDesde;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controlador = new PerfilController();

        // Sem sessão iniciada não há perfil para mostrar: volta ao login
        if (!controlador.existeSessao()) {
            irParaLogin();
            return;
        }

        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        txtIniciais = view.findViewById(R.id.txtIniciais);
        txtNome = view.findViewById(R.id.txtNome);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtVisitas = view.findViewById(R.id.txtVisitas);
        txtFavoritos = view.findViewById(R.id.txtFavoritos);
        txtAvaliacoes = view.findViewById(R.id.txtAvaliacoes);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtTelemovel = view.findViewById(R.id.txtTelemovel);
        txtNascimento = view.findViewById(R.id.txtNascimento);
        txtMembroDesde = view.findViewById(R.id.txtMembroDesde);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> terminarSessao());

        carregarDados();
    }

    // Pede ao controlador o perfil e as estatísticas.
    private void carregarDados() {
        try {
            controlador.carregarPerfil(new PerfilController.PerfilCallback() {
                @Override
                public void onSucesso(Perfil perfil) {
                    if (isAdded()) {
                        mostrarPerfil(perfil);
                    }
                }

                @Override
                public void onErro(String erro) {
                    if (isAdded()) {
                        txtNome.setText(FormatadorPerfil.valorOuTraco(null));
                        Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show();
                    }
                }
            });

            controlador.contarVisitas(criarCallbackDeContagem(txtVisitas));
            controlador.contarFavoritos(criarCallbackDeContagem(txtFavoritos));
            controlador.contarAvaliacoes(criarCallbackDeContagem(txtAvaliacoes));

        } catch (InvalidDataException e) {
            Toast.makeText(requireContext(), e.what(), Toast.LENGTH_LONG).show();
            irParaLogin();
        }
    }

    // Cria o callback que escreve o total de contagens do ecrã.
    private PerfilController.ContagemCallback criarCallbackDeContagem(TextView destino) {
        return new PerfilController.ContagemCallback() {
            @Override
            public void onSucesso(long total) {
                if (isAdded()) {
                    destino.setText(String.valueOf(total));
                }
            }

            @Override
            public void onErro(String erro) {
                if (isAdded()) {
                    destino.setText("0");
                }
            }
        };
    }

    // Preenche o ecrã com os dados do perfil.
    private void mostrarPerfil(Perfil perfil) {
        txtNome.setText(FormatadorPerfil.valorOuTraco(perfil.getNome_completo()));
        txtUsername.setText(FormatadorPerfil.formatarNomeUtilizador(perfil.getUsername()));
        txtEmail.setText(FormatadorPerfil.valorOuTraco(perfil.getEmail()));
        txtTelemovel.setText(FormatadorPerfil.valorOuTraco(perfil.getNumero_telemovel()));
        txtNascimento.setText(FormatadorPerfil.valorOuTraco(perfil.getData_nascimento()));
        txtMembroDesde.setText(FormatadorPerfil.formatarDataRegisto(perfil.getData_registo()));
        txtIniciais.setText(FormatadorPerfil.calcularIniciais(perfil.getNome_completo()));

        try {
            controlador.carregarFoto(perfil.getFoto_url(), new PerfilController.FotoCallback() {
                @Override
                public void onSucesso(Bitmap foto) {
                    if (isAdded()) {
                        imgFotoPerfil.setImageBitmap(foto);
                        imgFotoPerfil.setVisibility(View.VISIBLE);
                        txtIniciais.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onErro(String erro) {
                    // Sem foto: ficam as iniciais
                }
            });
        } catch (InvalidDataException e) {
            // Ainda não há foto de perfil: ficam as iniciais
        }
    }

    private void terminarSessao() {
        controlador.terminarSessao();
        requireActivity().getSharedPreferences(FICHEIRO_PREFERENCIAS, Context.MODE_PRIVATE)
                .edit().clear().apply();
        irParaLogin();
    }

    private void irParaLogin() {
        Intent intencao = new Intent(requireActivity(), LoginActivity.class);
        intencao.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intencao);
        requireActivity().finish();
    }
}