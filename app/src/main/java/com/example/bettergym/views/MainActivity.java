package com.example.bettergym.views; // Mantive o teu nome de package para não dar erro

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


import com.example.bettergym.R;
import com.example.bettergym.helper.MapHelper;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.service.PlacesService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesService placesService;
    private FusedLocationProviderClient fusedLocationClient;
    private OnBackPressedCallback voltarDoConteudo;
    private View ecraCarregamento;
    private TextView txtEtapa, txtPercentagem;
    private LinearProgressIndicator barraCarregamento;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private CancellationTokenSource pedidoLocalizacao;
    private int tentativaCarregamento;
    private boolean carregamentoAtivo = true;
    private final Runnable limiteCarregamento = () -> mostrarErroCarregamento(R.string.loading_timeout);

    // Elementos do Cartão
    private CardView cardDetalhes;
    private TextView txtCardNome, txtCardAberto;
    private MaterialButton btnVerDetalhes;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissoes -> {
                if (!carregamentoAtivo) return;
                uiHandler.postDelayed(limiteCarregamento, 30000);
                if (Boolean.TRUE.equals(permissoes.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(permissoes.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                    ativarLocalizacaoECentrar();
                } else {
                    mostrarErroCarregamento(R.string.loading_permission);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        prepararEcraCarregamento();

        // Ligar as views do cartão aos IDs do teu XML
        cardDetalhes = findViewById(R.id.cardDetalhesGinasio);
        txtCardNome = findViewById(R.id.txtCardNome);
        txtCardAberto = findViewById(R.id.txtCardAberto);
        btnVerDetalhes = findViewById(R.id.btnVerDetalhes);

        // 1. Instancia o Serviço e o Cliente de GPS
        placesService = new PlacesService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Prepara o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaPrincipal);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 3. Barra de navegação inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        voltarDoConteudo = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                esconderConteudo();
                bottomNav.setSelectedItemId(R.id.nav_mapa);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, voltarDoConteudo);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_perfil) {
                mostrarConteudo(new PerfilFragment());
                return true;
            }

            if (itemId == R.id.nav_favoritos) {
                mostrarConteudo(new FavoritosFragment());
                return true;
            }

            esconderConteudo();
            if (itemId == R.id.nav_mapa) {
                return true;
            } else if (itemId == R.id.nav_ginasios) {
                return true;
            }
            return false;
        });
        if (savedInstanceState != null) {
            bottomNav.setSelectedItemId(savedInstanceState.getInt("separador", R.id.nav_mapa));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("separador", ((BottomNavigationView) findViewById(R.id.bottomNavigation)).getSelectedItemId());
        super.onSaveInstanceState(outState);
    }

    private void mostrarConteudo(Fragment fragment) {
        // O contentor é partilhado pelos ecrãs de perfil e favoritos.
        cardDetalhes.setVisibility(View.GONE);
        findViewById(R.id.containerPerfil).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerPerfil, fragment)
                .commit();
        voltarDoConteudo.setEnabled(true);
    }

    private void esconderConteudo() {
        Fragment atual = getSupportFragmentManager().findFragmentById(R.id.containerPerfil);
        if (atual != null) {
            getSupportFragmentManager().beginTransaction().remove(atual).commit();
        }
        findViewById(R.id.containerPerfil).setVisibility(View.GONE);
        voltarDoConteudo.setEnabled(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Delega o estilo visual ao MapHelper
        MapHelper.aplicarEstilo(this, mMap);
        ajustarEspacamento();

        // Ligar a lógica do cartão interativo ao mapa!
        configurarCliquesMapa();

        if (!carregamentoAtivo) return;
        atualizarProgresso(30, R.string.loading_location);
        verificarPermissaoGPS();
    }

    private void prepararEcraCarregamento() {
        ecraCarregamento = findViewById(R.id.ecraCarregamento);
        txtEtapa = findViewById(R.id.txtEtapaCarregamento);
        txtPercentagem = findViewById(R.id.txtPercentagemCarregamento);
        barraCarregamento = findViewById(R.id.barraCarregamento);
        findViewById(R.id.btnRepetirCarregamento).setOnClickListener(v -> {
            tentativaCarregamento++;
            carregamentoAtivo = true;
            barraCarregamento.setProgress(0);
            findViewById(R.id.btnRepetirCarregamento).setVisibility(View.GONE);
            findViewById(R.id.btnContinuarCarregamento).setVisibility(View.GONE);
            uiHandler.removeCallbacks(limiteCarregamento);
            uiHandler.postDelayed(limiteCarregamento, 30000);
            atualizarProgresso(mMap == null ? 10 : 30,
                    mMap == null ? R.string.loading_map : R.string.loading_location);
            if (mMap != null) verificarPermissaoGPS();
        });
        findViewById(R.id.btnContinuarCarregamento).setOnClickListener(v -> {
            carregamentoAtivo = false;
            tentativaCarregamento++;
            if (pedidoLocalizacao != null) pedidoLocalizacao.cancel();
            uiHandler.removeCallbacks(limiteCarregamento);
            ecraCarregamento.setVisibility(View.GONE);
        });
        atualizarProgresso(10, R.string.loading_map);
        uiHandler.postDelayed(limiteCarregamento, 30000);
    }

    // Apenas a UI Thread altera as Views. As percentagens representam etapas concluídas,
    // e não um temporizador nem a percentagem de bytes descarregados.
    private void atualizarProgresso(int percentagem, int mensagem) {
        if (isFinishing() || isDestroyed() || !carregamentoAtivo) return;
        barraCarregamento.setProgress(percentagem);
        txtPercentagem.setText(getString(R.string.loading_percent, percentagem));
        txtEtapa.setText(mensagem);
    }

    private void mostrarErroCarregamento(int mensagem) {
        if (isFinishing() || isDestroyed() || !carregamentoAtivo) return;
        carregamentoAtivo = false;
        tentativaCarregamento++;
        if (pedidoLocalizacao != null) pedidoLocalizacao.cancel();
        uiHandler.removeCallbacks(limiteCarregamento);
        txtEtapa.setText(mensagem);
        findViewById(R.id.btnRepetirCarregamento).setVisibility(View.VISIBLE);
        findViewById(R.id.btnContinuarCarregamento).setVisibility(View.VISIBLE);
    }

    private boolean tentativaAtual(int tentativa) {
        return !isFinishing() && !isDestroyed() && carregamentoAtivo
                && tentativa == tentativaCarregamento;
    }

    private void ajustarEspacamento() {
        // Corrigido o erro de sintaxe do asterisco
        int paddingBottom = (int) (65 * getResources().getDisplayMetrics().density);
        mMap.setPadding(0, 0, 0, paddingBottom);
    }

    private void configurarCliquesMapa() {
        // Esconder o cartão ao clicar num sítio vazio do mapa
        mMap.setOnMapClickListener(latLng -> cardDetalhes.setVisibility(View.GONE));

        // Mostrar o cartão ao clicar num marcador
        mMap.setOnMarkerClickListener(marker -> {
            Ginasio ginasio = (Ginasio) marker.getTag(); // Vai buscar o objeto guardado

            if (ginasio != null) {
                txtCardNome.setText(ginasio.getNome());

                if (ginasio.isAbertoAgora()) {
                    txtCardAberto.setText("🟢 Aberto agora");
                    txtCardAberto.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    txtCardAberto.setText("🔴 Fechado");
                    txtCardAberto.setTextColor(Color.parseColor("#F44336"));
                }

                // Abre a ficha completa do ginásio selecionado.
                btnVerDetalhes.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, DetalheGinasioActivity.class);
                    intent.putExtra(DetalheGinasioActivity.EXTRA_GINASIO_ID, ginasio.getId());
                    startActivity(intent);
                });

                cardDetalhes.setVisibility(View.VISIBLE);

                // Centra a câmara no pino quando clicas nele
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            }

            return true; // TRUE significa que tratamos do clique e a janela antiga não abre
        });
    }

    private void verificarPermissaoGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacaoECentrar();
        } else {
            // O tempo de decisão do utilizador não conta como falha de carregamento.
            uiHandler.removeCallbacks(limiteCarregamento);
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    @SuppressLint("MissingPermission")
    private void ativarLocalizacaoECentrar() {
        if (mMap == null || !carregamentoAtivo) return;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsAtivo = lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!gpsAtivo) {
            Toast.makeText(this, "Por favor, ativa a Localização (GPS) nas definições do telemóvel", Toast.LENGTH_LONG).show();
            mostrarErroCarregamento(R.string.loading_no_location);
            return;
        }

        final int tentativa = ++tentativaCarregamento;
        if (pedidoLocalizacao != null) pedidoLocalizacao.cancel();
        pedidoLocalizacao = new CancellationTokenSource();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, cachedLocation -> {
            if (!tentativaAtual(tentativa)) return;
            if (cachedLocation != null) {
                processarLocalizacaoEncontrada(cachedLocation, tentativa);
            } else {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, pedidoLocalizacao.getToken())
                        .addOnSuccessListener(this, freshLocation -> {
                            if (!tentativaAtual(tentativa)) return;
                            if (freshLocation != null) {
                                processarLocalizacaoEncontrada(freshLocation, tentativa);
                            } else {
                                mostrarErroCarregamento(R.string.loading_no_location);
                            }
                        })
                        .addOnFailureListener(this, e -> {
                            if (tentativaAtual(tentativa)) mostrarErroCarregamento(R.string.loading_no_location);
                        });
            }
        });
    }

    private void processarLocalizacaoEncontrada(Location location, int tentativa) {
        atualizarProgresso(50, R.string.loading_search);
        LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 14.5f));

        placesService.pesquisarGinasiosProximos(minhaPosicao, new PlacesService.OnGymsLoadedListener() {
            @Override
            public void onProgress(int percentagem) {
                runOnUiThread(() -> {
                    if (!tentativaAtual(tentativa)) return;
                    atualizarProgresso(percentagem, percentagem == 70
                            ? R.string.loading_received : R.string.loading_markers);
                });
            }

            @Override
            public void onSuccess(List<Ginasio> ginasios) {
                runOnUiThread(() -> {
                    if (!tentativaAtual(tentativa)) return;
                    desenharMarcadores(ginasios);
                    atualizarProgresso(100, R.string.loading_done);
                    carregamentoAtivo = false;
                    uiHandler.removeCallbacks(limiteCarregamento);
                    ecraCarregamento.postOnAnimation(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            ecraCarregamento.setVisibility(View.GONE);
                        }
                    });
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (tentativaAtual(tentativa)) mostrarErroCarregamento(R.string.loading_network_error);
                });
            }
        });
    }

    private void desenharMarcadores(List<Ginasio> ginasios) {
        if (mMap == null) return;
        mMap.clear();

        for (Ginasio g : ginasios) {
            LatLng posicao = new LatLng(g.getLatitude(), g.getLongitude());

            Marker marcador = mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .title(g.getNome())
                    .anchor(0.5f, 0.5f)
                    .icon(MapHelper.criarMarcadorGinasio(this, g.isAbertoAgora())));

            if (marcador != null) {
                marcador.setTag(g);
            }
        }
    }

    @Override
    protected void onDestroy() {
        carregamentoAtivo = false;
        tentativaCarregamento++;
        uiHandler.removeCallbacksAndMessages(null);
        if (pedidoLocalizacao != null) pedidoLocalizacao.cancel();
        super.onDestroy();
    }
}
