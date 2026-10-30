package com.example.bettergym.views;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.viewpager2.widget.ViewPager2;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.exceptions.InvalidDataException;
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

import com.example.bettergym.model.Avaliacao;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.ListView;
import android.util.Log;

public class DetalheGinasioActivity extends AppCompatActivity {

    public static final String EXTRA_GINASIO_ID = "extra_ginasio";

    private ViewPager2 viewPagerFotos;
    private TextView txtIndicadorFotos;
    private FotosAdapter fotosAdapter;
    private TextView txtSemFoto;
    private AppCompatImageButton btnFavorito;
    private Ginasio ginasio;
    private FavoritosService favoritosService;
    private PlacesService placesService;
    private boolean favorito, estadoConhecido, aGuardar;
    private Snackbar erroFavorito;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_ginasio);

        viewPagerFotos = findViewById(R.id.viewPagerFotos);
        txtIndicadorFotos = findViewById(R.id.txtIndicadorFotos);
        txtSemFoto = findViewById(R.id.txtSemFoto);
        fotosAdapter = new FotosAdapter(this, new ArrayList<>());
        viewPagerFotos.setAdapter(fotosAdapter);
        viewPagerFotos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int total = fotosAdapter.getItemCount();
                if (total > 1) {
                    txtIndicadorFotos.setText((position + 1) + " / " + total);
                    txtIndicadorFotos.setVisibility(View.VISIBLE);
                } else {
                    txtIndicadorFotos.setVisibility(View.GONE);
                }
            }
        });

        MaterialButton btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> finish());

        favoritosService = new FavoritosService();
        placesService = new PlacesService();
        btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setOnClickListener(v -> alternarFavorito());

        // Apanhar o ID diretamente do Intent
        String ginasioId = getIntent().getStringExtra(EXTRA_GINASIO_ID);

        // Se temos ID, vamos SEMPRE à Google buscar os dados atualizados (incluindo as reviews)
        if (ginasioId != null && !ginasioId.isEmpty()) {
            carregarDetalhesDaGoogle(ginasioId);
        } else {
            // Se não chegou ID nenhum, avisa e fecha
            InvalidDataException erro = new InvalidDataException("Erro: ID do ginásio não encontrado.");
            Toast.makeText(this, erro.what(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void carregarDetalhesDaGoogle(String placeId) {

        placesService.obterDetalhesGinasio(placeId, new PlacesService.OnGymDetailsLoadedListener() {


            @Override
            public void onSuccess(Ginasio ginasioReal) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    ginasio = ginasioReal;
                    preencherDados(ginasio);
                    // Assim que os dados chegam, verifica o estado do favorito
                    observarFavorito();
                });
            }

            @Override
            public void onError(Exception erro) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Log.e("DetalheGinasio", "Erro ao carregar detalhes", erro);
                    String msg = erro != null && erro.getMessage() != null ? erro.getMessage() : "Erro ao carregar detalhes.";
                    Toast.makeText(DetalheGinasioActivity.this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void observarFavorito() {
        // APAGADO: if (observacaoFavorito != null) observacaoFavorito.remove();
        estadoConhecido = false;
        atualizarEstrela();

        // MUDOU PARA verificarEstado E SEM VARIÁVEL
        favoritosService.verificarEstado(ginasio.getId(),
                new FavoritosService.EstadoCallback() {
                    @Override
                    public void onResultado(boolean guardado) {
                        // ENVOLVER COM runOnUiThread
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            favorito = guardado;
                            estadoConhecido = true;
                            if (erroFavorito != null) erroFavorito.dismiss();
                            atualizarEstrela();
                        });
                    }

                    @Override
                    public void onErro(Exception erro) {
                        // ENVOLVER COM runOnUiThread
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            estadoConhecido = false;
                            atualizarEstrela();
                            erroFavorito = Snackbar.make(btnFavorito, R.string.favorites_error,
                                            Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.loading_retry, v -> observarFavorito());
                            erroFavorito.show();
                        });
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
                // ENVOLVER COM runOnUiThread
                runOnUiThread(() -> {
                    aGuardar = false;
                    if (isFinishing() || isDestroyed()) return;

                    // NOVA LINHA CRUCIAL (Como não há escuta em tempo real, atualizamos manualmente)
                    favorito = adicionar;

                    atualizarEstrela();
                    Toast.makeText(DetalheGinasioActivity.this, adicionar
                            ? R.string.favorites_added : R.string.favorites_removed, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onErro(Exception erro) {
                // ENVOLVER COM runOnUiThread
                runOnUiThread(() -> {
                    aGuardar = false;
                    if (isFinishing() || isDestroyed()) return;
                    atualizarEstrela();
                    String msg = getString(R.string.favorites_write_error);
                    if (erro instanceof InvalidDataException) {
                        msg = ((InvalidDataException) erro).what();
                    } else if (erro instanceof DataConsistencyException) {
                        msg = ((DataConsistencyException) erro).what();
                    } else if (erro != null && erro.getMessage() != null) {
                        msg = erro.getMessage();
                    }
                    Toast.makeText(DetalheGinasioActivity.this, msg, Toast.LENGTH_LONG).show();
                });
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
        // APAGADO: if (observacaoFavorito != null) observacaoFavorito.remove();


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

        configurarFotos(ginasio);
        ListView listViewAvaliacoes = findViewById(R.id.listViewAvaliacoes);
        List<Avaliacao> listaDeAvaliacoes = ginasio.getAvaliacoes();
        Log.d("DetalheGinasioActivity", "Avaliações: " + listaDeAvaliacoes.size());

        if (listaDeAvaliacoes != null && !listaDeAvaliacoes.isEmpty()) {
            AvaliacaoAdapter adapter = new AvaliacaoAdapter(this, listaDeAvaliacoes);
            listViewAvaliacoes.setAdapter(adapter);
            listViewAvaliacoes.setVisibility(View.VISIBLE); // Garante que fica visível
        } else {
            listViewAvaliacoes.setVisibility(View.GONE); // Esconde a lista se não houver comentários
        }

    }

    private String valorOuIndisponivel(String valor, String alternativa) {
        return valor == null || valor.trim().isEmpty() ? alternativa : valor;
    }

    private void configurarFotos(Ginasio ginasio) {
        List<String> listaFotos = new ArrayList<>();
        if (ginasio.getFotos() != null && !ginasio.getFotos().isEmpty()) {
            listaFotos.addAll(ginasio.getFotos());
        } else if (ginasio.getReferenciaFoto() != null && !ginasio.getReferenciaFoto().isEmpty()) {
            listaFotos.add(ginasio.getReferenciaFoto());
        }

        if (!listaFotos.isEmpty()) {
            fotosAdapter.atualizarFotos(listaFotos);
            viewPagerFotos.setVisibility(View.VISIBLE);
            txtSemFoto.setVisibility(View.GONE);
            if (listaFotos.size() > 1) {
                txtIndicadorFotos.setText("1 / " + listaFotos.size());
                txtIndicadorFotos.setVisibility(View.VISIBLE);
            } else {
                txtIndicadorFotos.setVisibility(View.GONE);
            }
        } else {
            viewPagerFotos.setVisibility(View.GONE);
            txtIndicadorFotos.setVisibility(View.GONE);
            txtSemFoto.setVisibility(View.VISIBLE);
        }
    }
}
