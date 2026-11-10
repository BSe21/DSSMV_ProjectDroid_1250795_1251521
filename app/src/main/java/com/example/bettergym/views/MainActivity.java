package com.example.bettergym.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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

    // --- Mapa e localização ---
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesService placesService;

    // --- Ecrã de carregamento ---
    private View ecraCarregamento;
    private TextView txtEtapa, txtPercentagem;
    private LinearProgressIndicator barraCarregamento;

    // --- Card de detalhes do marcador ---
    private CardView cardDetalhes;
    private TextView txtCardNome, txtCardAberto;
    private MaterialButton btnVerDetalhes;

    // --- Navegação ---
    private OnBackPressedCallback voltarDoConteudo;

    // --- Pedido de permissão de localização ---
    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissoes -> {
                boolean aceite = Boolean.TRUE.equals(permissoes.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(permissoes.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (aceite) {
                    obterLocalizacaoECarregarGinasios();
                } else {
                    mostrarErro(R.string.loading_permission);
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        inicializarCarregamento();
        inicializarCardDetalhes();
        inicializarNavegacao();

        placesService = new PlacesService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Inicia o carregamento do mapa — quando estiver pronto, chama onMapReady()
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaPrincipal);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (savedInstanceState != null) {
            ((BottomNavigationView) findViewById(R.id.bottomNavigation))
                    .setSelectedItemId(savedInstanceState.getInt("separador", R.id.nav_mapa));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("separador", ((BottomNavigationView) findViewById(R.id.bottomNavigation)).getSelectedItemId());
        super.onSaveInstanceState(outState);
    }

    // -------------------------------------------------------------------------
    // Callback do Google Maps — chamado quando o mapa está pronto
    // -------------------------------------------------------------------------

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        MapHelper.aplicarEstilo(this, mMap);


        int paddingBottom = (int) (65 * getResources().getDisplayMetrics().density);
        mMap.setPadding(0, 0, 0, paddingBottom);

        configurarCliquesMapa();
        verificarPermissaoEObterLocalizacao();
    }

    // -------------------------------------------------------------------------
    // Localização e carregamento de ginásios
    // -------------------------------------------------------------------------

    private void verificarPermissaoEObterLocalizacao() {
        atualizarProgresso(30, R.string.loading_location);

        boolean temPermissao =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (temPermissao) {
            obterLocalizacaoECarregarGinasios();
        } else {
            // Pede as permissões ao utilizador — resultado chega no requestPermissionLauncher
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void obterLocalizacaoECarregarGinasios() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        atualizarProgresso(50, R.string.loading_search);

        // Obtém a localização atual do utilizador via GPS
        CancellationTokenSource cancelToken = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelToken.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        mostrarErro(R.string.loading_no_location);
                        return;
                    }

                    LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 14.5f));

                    // Pesquisa ginásios próximos usando a Google Places API
                    placesService.pesquisarGinasiosProximos(minhaPosicao, new PlacesService.OnGymsLoadedListener() {
                        @Override
                        public void onProgress(int percentagem) {
                            runOnUiThread(() -> atualizarProgresso(percentagem,
                                    percentagem == 70 ? R.string.loading_received : R.string.loading_markers));
                        }

                        @Override
                        public void onSuccess(List<Ginasio> ginasios) {
                            runOnUiThread(() -> {
                                desenharMarcadores(ginasios);
                                atualizarProgresso(100, R.string.loading_done);
                                ecraCarregamento.postOnAnimation(() -> ecraCarregamento.setVisibility(View.GONE));
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> mostrarErro(R.string.loading_network_error));
                        }
                    });
                })
                .addOnFailureListener(this, e -> mostrarErro(R.string.loading_no_location));
    }

    // -------------------------------------------------------------------------
    // Marcadores no mapa
    // -------------------------------------------------------------------------

    private void desenharMarcadores(List<Ginasio> ginasios) {
        mMap.clear();
        for (Ginasio g : ginasios) {
            LatLng posicao = new LatLng(g.getLatitude(), g.getLongitude());
            Marker marcador = mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .title(g.getNome())
                    .anchor(0.5f, 0.5f)
                    .icon(MapHelper.criarMarcadorGinasio(this, g.isAbertoAgora())));
            if (marcador != null) {
                marcador.setTag(g); // guarda o objeto Ginasio no marcador para uso posterior
            }
        }
    }

    private void configurarCliquesMapa() {
        // Clique no mapa (fundo) → esconde o card
        mMap.setOnMapClickListener(latLng -> cardDetalhes.setVisibility(View.GONE));

        // Clique num marcador → mostra o card com o nome e estado do ginásio
        mMap.setOnMarkerClickListener(marker -> {
            Ginasio ginasio = (Ginasio) marker.getTag();
            if (ginasio == null) return true;

            txtCardNome.setText(ginasio.getNome());

            if (ginasio.isAbertoAgora()) {
                txtCardAberto.setText("🟢 Aberto agora");
                txtCardAberto.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                txtCardAberto.setText("🔴 Fechado");
                txtCardAberto.setTextColor(Color.parseColor("#F44336"));
            }

            btnVerDetalhes.setOnClickListener(v -> {
                Intent intent = new Intent(this, DetalheGinasioActivity.class);
                intent.putExtra(DetalheGinasioActivity.EXTRA_GINASIO_ID, ginasio.getId());
                startActivity(intent);
            });

            cardDetalhes.setVisibility(View.VISIBLE);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            return true; // impede a info window padrão do Google Maps
        });
    }

    // -------------------------------------------------------------------------
    // Ecrã de carregamento
    // -------------------------------------------------------------------------

    private void inicializarCarregamento() {
        ecraCarregamento = findViewById(R.id.ecraCarregamento);
        txtEtapa = findViewById(R.id.txtEtapaCarregamento);
        txtPercentagem = findViewById(R.id.txtPercentagemCarregamento);
        barraCarregamento = findViewById(R.id.barraCarregamento);

        atualizarProgresso(10, R.string.loading_map);

        // Botão "Repetir" — tenta carregar novamente do início
        findViewById(R.id.btnRepetirCarregamento).setOnClickListener(v -> {
            esconderBotoesErro();
            atualizarProgresso(mMap == null ? 10 : 30,
                    mMap == null ? R.string.loading_map : R.string.loading_location);
            if (mMap != null) verificarPermissaoEObterLocalizacao();
        });

        // Botão "Continuar" — ignora o erro e entra na app sem ginásios
        findViewById(R.id.btnContinuarCarregamento).setOnClickListener(v -> {
            ecraCarregamento.setVisibility(View.GONE);
        });
    }

    private void atualizarProgresso(int percentagem, int mensagem) {
        barraCarregamento.setProgress(percentagem);
        txtPercentagem.setText(getString(R.string.loading_percent, percentagem));
        txtEtapa.setText(mensagem);
    }

    private void mostrarErro(int mensagem) {
        txtEtapa.setText(mensagem);
        findViewById(R.id.btnRepetirCarregamento).setVisibility(View.VISIBLE);
        findViewById(R.id.btnContinuarCarregamento).setVisibility(View.VISIBLE);
    }

    private void esconderBotoesErro() {
        barraCarregamento.setProgress(0);
        findViewById(R.id.btnRepetirCarregamento).setVisibility(View.GONE);
        findViewById(R.id.btnContinuarCarregamento).setVisibility(View.GONE);
    }

    // -------------------------------------------------------------------------
    // Card de detalhes do ginásio (aparece ao clicar num marcador)
    // -------------------------------------------------------------------------

    private void inicializarCardDetalhes() {
        cardDetalhes = findViewById(R.id.cardDetalhesGinasio);
        txtCardNome = findViewById(R.id.txtCardNome);
        txtCardAberto = findViewById(R.id.txtCardAberto);
        btnVerDetalhes = findViewById(R.id.btnVerDetalhes);
    }

    // -------------------------------------------------------------------------
    // Navegação (BottomNavigationView + botão Voltar)
    // -------------------------------------------------------------------------

    private void inicializarNavegacao() {
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
            if (itemId == R.id.nav_ginasios) {
                startActivity(new Intent(this, HistoricoVisitasActivity.class));
                return false;
            }

            // nav_mapa
            esconderConteudo();
            return true;
        });
    }

    private void mostrarConteudo(Fragment fragment) {
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
}
