package com.example.bettergym.views;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bettergym.R;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.helper.Utils;
import com.example.bettergym.model.Avaliacao;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.service.FavoritosService;
import com.example.bettergym.service.PlacesService;
import com.example.bettergym.service.VisitasService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetalheGinasioActivity extends AppCompatActivity {

    public static final String EXTRA_GINASIO_ID = "extra_ginasio";

    private ViewPager2 viewPagerFotos;
    private TextView txtIndicadorFotos;
    private FotosAdapter fotosAdapter;
    private TextView txtSemFoto;
    private AppCompatImageButton btnFavorito;
    private MaterialButton btnRegistarVisita;
    private Ginasio ginasio;
    private FavoritosService favoritosService;
    private PlacesService placesService;
    private VisitasService visitasService;
    private boolean favorito, estadoConhecido, aGuardar;
    private Snackbar erroFavorito;

    private final ActivityResultLauncher<ScanOptions> qrScanLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result == null || result.getContents() == null) {
                    Toast.makeText(this, "Leitura de QR Code cancelada.", Toast.LENGTH_SHORT).show();
                } else {
                    processarResultadoQr(result.getContents());
                }
            }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    iniciarLeituraQr();
                } else {
                    Toast.makeText(this, "Permissão da câmara necessária para ler o QR Code.", Toast.LENGTH_LONG).show();
                }
            }
    );

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
        visitasService = new VisitasService();

        btnFavorito = findViewById(R.id.btnFavorito);
        btnFavorito.setOnClickListener(v -> alternarFavorito());

        btnRegistarVisita = findViewById(R.id.btnRegistarVisita);
        btnRegistarVisita.setEnabled(false);
        btnRegistarVisita.setOnClickListener(v -> verificarPermissaoEIniciarQr());

        // Apanhar o ID diretamente do Intent
        String ginasioId = getIntent().getStringExtra(EXTRA_GINASIO_ID);

        // Se temos ID, vamos SEMPRE à Google buscar os dados atualizados (incluindo as reviews)
        if (ginasioId != null && !ginasioId.isEmpty()) {
            carregarDetalhesDaGoogle(ginasioId);
        } else {
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

    private void verificarPermissaoEIniciarQr() {
        // Validação da regra de negócio delegada ao VisitasService
        try {
            visitasService.validarRegrasVisita(ginasio);
        } catch (InvalidDataException erro) {
            Toast.makeText(this, erro.what(), Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarLeituraQr();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarLeituraQr() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Aponte a câmara para o código QR do ginásio");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setBarcodeImageEnabled(false);
        qrScanLauncher.launch(options);
    }

    private void processarResultadoQr(String conteudo) {
        // Validação do QR e regras de negócio delegadas ao VisitasService
        try {
            visitasService.validarQrCodeGinasio(ginasio, conteudo);
            registarVisitaNoServidor();
        } catch (InvalidDataException erro) {
            Toast.makeText(this, erro.what(), Toast.LENGTH_LONG).show();
        }
    }

    private void registarVisitaNoServidor() {
        btnRegistarVisita.setEnabled(false);
        Toast.makeText(this, "A registar visita...", Toast.LENGTH_SHORT).show();

        // Passa o modelo Ginasio completo para o VisitasService aplicar as regras
        visitasService.registarVisita(ginasio, new VisitasService.EscritaCallback() {
            @Override
            public void onSucesso() {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    btnRegistarVisita.setEnabled(true);
                    Toast.makeText(DetalheGinasioActivity.this, "Visita registada com sucesso! 🏋️‍♂️", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onErro(Exception erro) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    btnRegistarVisita.setEnabled(true);
                    String msg = "Erro ao registar visita.";
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
        });
    }

    private void observarFavorito() {
        estadoConhecido = false;
        atualizarEstrela();

        favoritosService.verificarEstado(ginasio.getId(),
                new FavoritosService.EstadoCallback() {
                    @Override
                    public void onResultado(boolean guardado) {
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
                runOnUiThread(() -> {
                    aGuardar = false;
                    if (isFinishing() || isDestroyed()) return;

                    favorito = adicionar;
                    atualizarEstrela();
                    Toast.makeText(DetalheGinasioActivity.this, adicionar
                            ? R.string.favorites_added : R.string.favorites_removed, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onErro(Exception erro) {
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
        if (erroFavorito != null) erroFavorito.dismiss();
        super.onStop();
    }

    private void preencherDados(Ginasio ginasio) {
        ((TextView) findViewById(R.id.txtNomeGinasio)).setText(ginasio.getNome());
        ((TextView) findViewById(R.id.txtMoradaGinasio)).setText(Utils.valorOuPadrao(ginasio.getMorada(), "Morada não disponível"));

        TextView txtEstado = findViewById(R.id.txtEstadoGinasio);
        if (ginasio.isAbertoAgora()) {
            txtEstado.setText("Aberto agora");
            txtEstado.setTextColor(getColor(R.color.estado_aberto));
            btnRegistarVisita.setText("Registar Visita (QR Code)");
            btnRegistarVisita.setAlpha(1.0f);
        } else {
            txtEstado.setText("Fechado neste momento");
            txtEstado.setTextColor(getColor(R.color.estado_fechado));
            btnRegistarVisita.setText("Registar Visita (Ginásio Fechado)");
            btnRegistarVisita.setAlpha(0.65f);
        }

        TextView txtRating = findViewById(R.id.txtRatingGinasio);
        txtRating.setText(Utils.formatarRating(ginasio.getRating(), ginasio.getNumeroAvaliacoes()));

        btnRegistarVisita.setEnabled(true);

        configurarFotos(ginasio);
        ListView listViewAvaliacoes = findViewById(R.id.listViewAvaliacoes);
        List<Avaliacao> listaDeAvaliacoes = ginasio.getAvaliacoes();
        Log.d("DetalheGinasioActivity", "Avaliações: " + (listaDeAvaliacoes != null ? listaDeAvaliacoes.size() : 0));

        if (listaDeAvaliacoes != null && !listaDeAvaliacoes.isEmpty()) {
            AvaliacaoAdapter adapter = new AvaliacaoAdapter(this, listaDeAvaliacoes);
            listViewAvaliacoes.setAdapter(adapter);
            listViewAvaliacoes.setVisibility(View.VISIBLE);
        } else {
            listViewAvaliacoes.setVisibility(View.GONE);
        }
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
