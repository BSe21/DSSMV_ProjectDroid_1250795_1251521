package com.example.bettergym.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bettergym.R;
import com.example.bettergym.model.Visita;
import com.example.bettergym.service.VisitasService;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HistoricoVisitasActivity extends AppCompatActivity {

    private VisitasService visitasService;
    private VisitasAdapter adapter;
    private ListView listaVisitas;
    private View estadoVisitas;
    private ProgressBar progressoVisitas;
    private TextView txtEstadoVisitas;
    private TextView txtAjudaVisitas;
    private MaterialButton btnRepetirVisitas;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_visitas);

        visitasService = new VisitasService();

        MaterialButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        listaVisitas = findViewById(R.id.listaVisitas);
        estadoVisitas = findViewById(R.id.estadoVisitas);
        progressoVisitas = findViewById(R.id.progressoVisitas);
        txtEstadoVisitas = findViewById(R.id.txtEstadoVisitas);
        txtAjudaVisitas = findViewById(R.id.txtAjudaVisitas);
        btnRepetirVisitas = findViewById(R.id.btnRepetirVisitas);

        adapter = new VisitasAdapter(this::abrirDetalhesGinasio);
        listaVisitas.setAdapter(adapter);

        btnRepetirVisitas.setOnClickListener(v -> carregarVisitas());
    }

    @Override
    protected void onStart() {
        super.onStart();
        carregarVisitas();
    }

    private void carregarVisitas() {
        mostrarEstado("A carregar histórico...", true, false, false);

        visitasService.obterVisitas(new VisitasService.VisitasCallback() {
            @Override
            public void onResultado(List<Visita> visitas) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    adapter.atualizar(visitas);
                    if (visitas == null || visitas.isEmpty()) {
                        mostrarEstado("Sem visitas registadas", false, true, false);
                    } else {
                        estadoVisitas.setVisibility(View.GONE);
                        listaVisitas.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onErro(Exception erro) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    mostrarEstado("Erro ao carregar histórico de visitas", false, false, true);
                });
            }
        });
    }

    private void mostrarEstado(String mensagem, boolean carregar, boolean vazio, boolean erro) {
        listaVisitas.setVisibility(View.GONE);
        estadoVisitas.setVisibility(View.VISIBLE);
        txtEstadoVisitas.setText(mensagem);
        progressoVisitas.setVisibility(carregar ? View.VISIBLE : View.GONE);
        txtAjudaVisitas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        btnRepetirVisitas.setVisibility(erro ? View.VISIBLE : View.GONE);
    }

    private void abrirDetalhesGinasio(Visita visita) {
        if (visita == null || visita.getGinasio_id() == null) return;
        Intent intent = new Intent(this, DetalheGinasioActivity.class);
        intent.putExtra(DetalheGinasioActivity.EXTRA_GINASIO_ID, visita.getGinasio_id());
        startActivity(intent);
    }
}

