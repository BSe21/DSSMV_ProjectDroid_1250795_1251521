package com.example.bettergym.views;

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
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.helper.Utils;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.model.Perfil;
import com.example.bettergym.service.PerfilService;
import com.google.android.material.button.MaterialButton;

public class PerfilFragment extends Fragment {

    private PerfilService perfilService;

    private ImageView imgFotoPerfil;
    private TextView txtIniciais, txtNome, txtUsername;
    private TextView txtVisitas, txtFavoritos, txtAvaliacoes;
    private TextView txtEmail, txtTelemovel, txtNascimento, txtMembroDesde;
    private MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Instancia o serviço e vai buscar o ID do utilizador atual
        perfilService = new PerfilService();
        String uid = perfilService.getCurrentUserId();

        if (uid == null) {
            irParaLogin();
            return;
        }

        inicializarViews(view);

        btnLogout.setOnClickListener(v -> terminarSessao());

        View cardVisitas = view.findViewById(R.id.cardVisitas);
        if (cardVisitas != null) {
            cardVisitas.setOnClickListener(v -> startActivity(new Intent(requireContext(), HistoricoVisitasActivity.class)));
        }

        view.findViewById(R.id.btnAlterarPassword).setOnClickListener(v -> {
            if (getChildFragmentManager().findFragmentByTag("alterar_password") == null) {
                new AlterarPasswordDialogFragment().show(getChildFragmentManager(), "alterar_password");
            }
        });

        // 2. Pede os dados ao Serviço
        perfilService.carregarPerfil(uid, new PerfilService.PerfilCallback() {
            @Override
            public void onSuccess(Perfil perfil) {
                // VOLTAR À THREAD DO ECRÃ!
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) mostrarPerfil(perfil);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                // VOLTAR À THREAD DO ECRÃ PARA MOSTRAR O TOAST E TEXTO!
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            txtNome.setText("—");
                            String msg = e instanceof DataConsistencyException ? ((DataConsistencyException) e).what() : e.getMessage();
                            Toast.makeText(requireContext(), "Erro ao carregar perfil: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        // 3. Pede as restantes estatísticas ao Serviço
        perfilService.contarEstatistica("favoritos", uid, contagem -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> { if (isAdded()) txtFavoritos.setText(String.valueOf(contagem)); });
            }
        });

        perfilService.contarEstatistica("avaliacoes", uid, contagem -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> { if (isAdded()) txtAvaliacoes.setText(String.valueOf(contagem)); });
            }
        });
    }

    private void inicializarViews(View view) {
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
    }

    private void mostrarPerfil(Perfil perfil) {
        // Usa o helper Utils para formatar o texto e proteger contra Nulos
        txtNome.setText(Utils.valorOuTraco(perfil.getNome_completo()));
        txtUsername.setText(perfil.getUsername() == null || perfil.getUsername().isEmpty() ? "" : "@" + perfil.getUsername());
        txtEmail.setText(Utils.valorOuTraco(perfil.getEmail()));
        txtTelemovel.setText(Utils.valorOuTraco(perfil.getNumero_telemovel()));
        txtNascimento.setText(Utils.valorOuTraco(perfil.getData_nascimento()));
        txtMembroDesde.setText(Utils.formatarDataRegisto(perfil.getData_registo()));
        txtIniciais.setText(Utils.iniciais(perfil.getNome_completo()));

        // Contagem de visitas a partir do historico_ginasios_visitados do perfil
        if (perfil.getHistorico_ginasios_visitados() != null) {
            txtVisitas.setText(String.valueOf(perfil.getHistorico_ginasios_visitados().size()));
        } else {
            txtVisitas.setText("0");
        }

        String foto = perfil.getFoto_url();
        if (foto != null && foto.startsWith("http")) {
            carregarFoto(foto);
        }
    }

    private void carregarFoto(String urlFoto) {
        new Thread(() -> {
            try {
                // Pede o download da foto diretamente ao NetworkHandler
                Bitmap bitmap = NetworkHandler.downloadBitmap(urlFoto);
                if (bitmap != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        imgFotoPerfil.setImageBitmap(bitmap);
                        imgFotoPerfil.setVisibility(View.VISIBLE);
                        txtIniciais.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                // Mantém as iniciais em caso de erro na rede
            }
        }).start();
    }

    private void terminarSessao() {
        perfilService.fazerLogout();
        requireActivity().getSharedPreferences("BetterGymApp", android.content.Context.MODE_PRIVATE).edit().clear().apply();
        irParaLogin();
    }

    private void irParaLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
