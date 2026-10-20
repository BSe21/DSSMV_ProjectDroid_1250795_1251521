package com.example.bettergym.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_ginasio);

        imgGinasio = findViewById(R.id.imgGinasio);
        txtSemFoto = findViewById(R.id.txtSemFoto);
        MaterialButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        Ginasio ginasio = (Ginasio) getIntent().getSerializableExtra(EXTRA_GINASIO);
        if (ginasio == null) {
            finish();
            return;
        }

        preencherDados(ginasio);
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
