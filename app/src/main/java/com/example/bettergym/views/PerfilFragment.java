package com.example.bettergym.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bettergym.R;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.helper.ImageHelper;
import com.example.bettergym.helper.Utils;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.model.Perfil;
import com.example.bettergym.service.PerfilService;
import com.google.android.material.button.MaterialButton;

public class PerfilFragment extends Fragment {

    private PerfilService perfilService;

    private ImageView imgFotoPerfil;
    private TextView txtIniciais, txtNome, txtUsername;
    private TextView txtVisitas, txtFavoritos;
    private TextView txtEmail, txtTelemovel, txtNascimento, txtMembroDesde;
    private MaterialButton btnLogout;
    private View layoutCarregamentoPerfil;
    private View scrollConteudoPerfil;

    private final ActivityResultLauncher<String> seletorFotoLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    atualizarFoto(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        perfilService.carregarPerfil(uid, new PerfilService.PerfilCallback() {
            @Override
            public void onSuccess(Perfil perfil) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) mostrarPerfil(perfil);
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            if (layoutCarregamentoPerfil != null) layoutCarregamentoPerfil.setVisibility(View.GONE);
                            if (scrollConteudoPerfil != null) scrollConteudoPerfil.setVisibility(View.VISIBLE);
                            txtNome.setText("—");
                            String msg = e instanceof DataConsistencyException ? ((DataConsistencyException) e).what() : e.getMessage();
                            Toast.makeText(requireContext(), "Erro ao carregar perfil: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        perfilService.contarEstatistica("favoritos", uid, contagem -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> { if (isAdded()) txtFavoritos.setText(String.valueOf(contagem)); });
            }
        });
    }

    private void inicializarViews(View view) {
        layoutCarregamentoPerfil = view.findViewById(R.id.layoutCarregamentoPerfil);
        scrollConteudoPerfil = view.findViewById(R.id.scrollConteudoPerfil);
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        txtIniciais = view.findViewById(R.id.txtIniciais);
        txtNome = view.findViewById(R.id.txtNome);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtVisitas = view.findViewById(R.id.txtVisitas);
        txtFavoritos = view.findViewById(R.id.txtFavoritos);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtTelemovel = view.findViewById(R.id.txtTelemovel);
        txtNascimento = view.findViewById(R.id.txtNascimento);
        txtMembroDesde = view.findViewById(R.id.txtMembroDesde);
        btnLogout = view.findViewById(R.id.btnLogout);

        View cardFotoPerfil = view.findViewById(R.id.cardFotoPerfil);
        if (cardFotoPerfil != null) {
            cardFotoPerfil.setOnClickListener(v -> seletorFotoLauncher.launch("image/*"));
        }
    }

    private void mostrarPerfil(Perfil perfil) {
        if (layoutCarregamentoPerfil != null) layoutCarregamentoPerfil.setVisibility(View.GONE);
        if (scrollConteudoPerfil != null) scrollConteudoPerfil.setVisibility(View.VISIBLE);

        txtNome.setText(Utils.valorOuTraco(perfil.getNome_completo()));
        txtUsername.setText(perfil.getUsername() == null || perfil.getUsername().isEmpty() ? "" : "@" + perfil.getUsername());
        txtEmail.setText(Utils.valorOuTraco(perfil.getEmail()));
        txtTelemovel.setText(Utils.valorOuTraco(perfil.getNumero_telemovel()));
        txtNascimento.setText(Utils.valorOuTraco(perfil.getData_nascimento()));
        txtMembroDesde.setText(Utils.formatarDataRegisto(perfil.getData_registo()));
        txtIniciais.setText(Utils.iniciais(perfil.getNome_completo()));

        if (perfil.getHistorico_ginasios_visitados() != null) {
            txtVisitas.setText(String.valueOf(perfil.getHistorico_ginasios_visitados().size()));
        } else {
            txtVisitas.setText("0");
        }

        exibirFoto(perfil.getFoto_url());
    }

    private void exibirFoto(String foto) {
        if (foto == null || foto.trim().isEmpty()) {
            imgFotoPerfil.setVisibility(View.GONE);
            txtIniciais.setVisibility(View.VISIBLE);
            return;
        }

        if (foto.startsWith("http")) {
            new Thread(() -> {
                try {
                    Bitmap bitmap = NetworkHandler.downloadBitmap(foto);
                    if (bitmap != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            imgFotoPerfil.setImageBitmap(bitmap);
                            imgFotoPerfil.setVisibility(View.VISIBLE);
                            txtIniciais.setVisibility(View.GONE);
                        });
                    }
                } catch (Exception ignored) {
                }
            }).start();
        } else {
            try {
                String base64 = foto;
                if (base64.contains(",")) {
                    base64 = base64.substring(base64.indexOf(",") + 1);
                }
                byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bitmap != null) {
                    imgFotoPerfil.setImageBitmap(bitmap);
                    imgFotoPerfil.setVisibility(View.VISIBLE);
                    txtIniciais.setVisibility(View.GONE);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void atualizarFoto(Uri uri) {
        String uid = perfilService.getCurrentUserId();
        if (uid == null) return;

        byte[] fotoBytes = ImageHelper.comprimirImagem(requireContext(), uri);
        if (fotoBytes == null || fotoBytes.length == 0) {
            Toast.makeText(requireContext(), "Não foi possível carregar a imagem selecionada.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "A atualizar foto de perfil...", Toast.LENGTH_SHORT).show();

        perfilService.atualizarFotoPerfil(uid, fotoBytes, new PerfilService.FotoCallback() {
            @Override
            public void onSuccess(String fotoUrl) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        exibirFoto(fotoUrl);
                        Toast.makeText(requireContext(), "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        String msg = e != null && e.getMessage() != null ? e.getMessage() : "Erro ao atualizar foto de perfil.";
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
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
