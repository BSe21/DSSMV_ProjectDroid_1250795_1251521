package com.example.bettergym.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatImageButton;
import com.example.bettergym.service.FavoritosService;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bettergym.R;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.service.PlacesService;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class DetalheGinasioActivity extends AppCompatActivity {

    public static final String EXTRA_GINASIO = "extra_ginasio";

    private ImageView imgGinasio;
    private TextView txtSemFoto;
    private AppCompatImageButton btnFavorito;
    private Ginasio ginasio;
    private FavoritosService favoritosService;
    private ListenerRegistration observacaoFavorito;
    private boolean favorito, estadoConhecido, aGuardar;
    private Snackbar erroFavorito;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_ginasio);

        imgGinasio = findViewById(R.id.imgGinasio);
        txtSemFoto = findViewById(R.id.txtSemFoto);
        MaterialButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        ginasio = (Ginasio) getIntent().getSerializableExtra(EXTRA_GINASIO);
        if (ginasio == null) {
            finish();
            return;
        }

        favoritosService = new FavoritosService();
        btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setOnClickListener(v -> alternarFavorito());
        preencherDados(ginasio);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ginasio != null) observarFavorito();
    }

    private void observarFavorito() {
        if (observacaoFavorito != null) observacaoFavorito.remove();
        estadoConhecido = false;
        atualizarEstrela();
        observacaoFavorito = favoritosService.observarEstado(ginasio.getId(),
                new FavoritosService.EstadoCallback() {
                    @Override
                    public void onResultado(boolean guardado) {
                        if (isFinishing() || isDestroyed()) return;
                        favorito = guardado;
                        estadoConhecido = true;
                        if (erroFavorito != null) erroFavorito.dismiss();
                        atualizarEstrela();
                    }

                    @Override
                    public void onErro(Exception erro) {
                        if (isFinishing() || isDestroyed()) return;
                        estadoConhecido = false;
                        atualizarEstrela();
                        erroFavorito = Snackbar.make(btnFavorito, R.string.favorites_error,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.loading_retry, v -> observarFavorito());
                        erroFavorito.show();
                    }
                });
    }

    private void alternarFavorito() {
        if (!estadoConhecido || aGuardar) return;
        boolean adicionar = !favorito;
        aGuardar = true;
        atualizarEstrela();
        FavoritosService.EscritaCallback callback = new FavoritosService.EscritaCallback() {
            @Override
            public void onSucesso() {
                aGuardar = false;
                if (isFinishing() || isDestroyed()) return;
                atualizarEstrela();
                Toast.makeText(DetalheGinasioActivity.this, adicionar
                        ? R.string.favorites_added : R.string.favorites_removed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onErro(Exception erro) {
                aGuardar = false;
                if (isFinishing() || isDestroyed()) return;
                atualizarEstrela();
                Toast.makeText(DetalheGinasioActivity.this,
                        R.string.favorites_write_error, Toast.LENGTH_LONG).show();
            }
        };
        if (adicionar) favoritosService.guardar(ginasio, callback);
        else favoritosService.remover(ginasio.getId(), callback);
    }

    private void atualizarEstrela() {
        btnFavorito.setImageResource(favorito ? R.drawable.ic_favorito_preenchido
                : R.drawable.ic_favorito_contorno);
        btnFavorito.setContentDescription(getString(favorito
                ? R.string.favorites_remove : R.string.favorites_add));
        btnFavorito.setEnabled(estadoConhecido && !aGuardar);
    }

    @Override
    protected void onStop() {
        if (observacaoFavorito != null) observacaoFavorito.remove();
        observacaoFavorito = null;
        if (erroFavorito != null) erroFavorito.dismiss();
        super.onStop();
    }

    private void preencherDados(Ginasio ginasio) {
        ((TextView) findViewById(R.id.txtNomeGinasio)).setText(ginasio.getNome());
        ((TextView) findViewById(R.id.txtMoradaGinasio)).setText(valorOuIndisponivel(ginasio.getMorada(), "Morada não disponível"));

        TextView txtEstado = findViewById(R.id.txtEstadoGinasio);
        if (ginasio.isAbertoAgora()) {
            txtEstado.setText("Aberto agora");
            txtEstado.setTextColor(getColor(R.color.estado_aberto));
        } else {
            txtEstado.setText("Fechado neste momento");
            txtEstado.setTextColor(getColor(R.color.estado_fechado));
        }

        TextView txtRating = findViewById(R.id.txtRatingGinasio);
        if (ginasio.getNumeroAvaliacoes() > 0) {
            txtRating.setText(String.format(Locale.getDefault(), "★ %.1f (%d avaliações)",
                    ginasio.getRating(), ginasio.getNumeroAvaliacoes()));
        } else {
            txtRating.setText("Ainda sem avaliações públicas");
        }

        carregarFoto(ginasio.getReferenciaFoto());
    }

    private String valorOuIndisponivel(String valor, String alternativa) {
        return valor == null || valor.trim().isEmpty() ? alternativa : valor;
    }

    private void carregarFoto(String referenciaFoto) {
        String urlFoto = PlacesService.obterUrlFoto(referenciaFoto);
        if (urlFoto == null) return;

        new Thread(() -> {
            try {
                Bitmap bitmap = NetworkHandler.downloadBitmap(urlFoto);
                if (bitmap != null) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        imgGinasio.setImageBitmap(bitmap);
                        imgGinasio.setVisibility(View.VISIBLE);
                        txtSemFoto.setVisibility(View.GONE);
                    });
                }
            } catch (Exception ignored) {
                // Mantém o estado sem foto quando a API não devolve uma imagem utilizável.
            }
        }).start();
    }
}
