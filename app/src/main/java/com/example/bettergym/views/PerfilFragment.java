package com.example.bettergym.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.bettergym.model.Perfil;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class PerfilFragment extends Fragment {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // Sem sessão iniciada não há perfil para mostrar: volta ao login
        FirebaseUser utilizador = mAuth.getCurrentUser();
        if (utilizador == null) {
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

        String uid = utilizador.getUid();
        carregarPerfil(uid);
        contar("visitas", txtVisitas, uid);
        contar("favoritos", txtFavoritos, uid);
        contar("avaliacoes", txtAvaliacoes, uid);
    }

    // Lê o documento perfis/{uid} do Firestore
    private void carregarPerfil(String uid) {
        db.collection("perfis").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    Perfil perfil = doc.toObject(Perfil.class);
                    if (perfil == null) {
                        txtNome.setText("Perfil não encontrado");
                        return;
                    }
                    mostrarPerfil(perfil);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    txtNome.setText("—");
                    Toast.makeText(requireContext(), "Não foi possível carregar o perfil.", Toast.LENGTH_LONG).show();
                });
    }

    private void mostrarPerfil(Perfil perfil) {
        txtNome.setText(valorOuTraco(perfil.getNome_completo()));
        txtUsername.setText(perfil.getUsername() == null || perfil.getUsername().isEmpty()
                ? "" : "@" + perfil.getUsername());
        txtEmail.setText(valorOuTraco(perfil.getEmail()));
        txtTelemovel.setText(valorOuTraco(perfil.getNumero_telemovel()));
        txtNascimento.setText(valorOuTraco(perfil.getData_nascimento()));
        txtMembroDesde.setText(formatarDataRegisto(perfil.getData_registo()));
        txtIniciais.setText(iniciais(perfil.getNome_completo()));

        String foto = perfil.getFoto_url();
        if (foto != null && foto.startsWith("http")) {
            carregarFoto(foto);
        }
    }

    // Conta quantos documentos do utilizador existem numa coleção (visitas, favoritos, avaliações)
    private void contar(String colecao, TextView destino, String uid) {
        db.collection(colecao).whereEqualTo("user_id", uid)
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener(resultado -> {
                    if (isAdded()) destino.setText(String.valueOf(resultado.getCount()));
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) destino.setText("0");
                });
    }

    // Descarrega a foto num thread à parte e mostra-a no círculo
    private void carregarFoto(String urlFoto) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(urlFoto).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);
                try (InputStream in = conn.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    if (bitmap != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            imgFotoPerfil.setImageBitmap(bitmap);
                            imgFotoPerfil.setVisibility(View.VISIBLE);
                            txtIniciais.setVisibility(View.GONE);
                        });
                    }
                }
            } catch (Exception e) {
                // Sem foto: ficam as iniciais
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void terminarSessao() {
        mAuth.signOut();
        requireActivity().getSharedPreferences("BetterGymApp", android.content.Context.MODE_PRIVATE)
                .edit().clear().apply();
        irParaLogin();
    }

    private void irParaLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // ------------------------------------------------------------ auxiliares

    private String valorOuTraco(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? "—" : valor;
    }

    // Primeira letra do primeiro e do último nome, em maiúsculas.
    private String iniciais(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) return "?";
        String[] partes = nomeCompleto.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        if (partes.length == 1) return primeira.toUpperCase(Locale.ROOT);
        String ultima = partes[partes.length - 1].substring(0, 1);
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    // Converte "2026-10-15T19:45:45Z" em "15/10/2026". Se não conseguir, mostra o texto original.
    private String formatarDataRegisto(String dataIso) {
        if (dataIso == null || dataIso.trim().isEmpty()) return "—";
        try {
            SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            entrada.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date data = entrada.parse(dataIso);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(data);
        } catch (Exception e) {
            return dataIso;
        }
    }
}
