package com.example.bettergym.views; // MUDADO PARA ui

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.bettergym.R;
import com.example.bettergym.model.Ginasio;
import com.example.bettergym.service.PlacesService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesService placesService;
    private FusedLocationProviderClient fusedLocationClient;
    private OnBackPressedCallback voltarDoPerfil;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    ativarLocalizacaoECentrar();
                } else {
                    Toast.makeText(this, "Permissão de GPS recusada", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        voltarDoPerfil = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                esconderPerfil();
                bottomNav.setSelectedItemId(R.id.nav_mapa);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, voltarDoPerfil);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_perfil) {
                mostrarPerfil();
                return true;
            }

            esconderPerfil();
            if (itemId == R.id.nav_mapa) {
                return true;
            } else if (itemId == R.id.nav_ginasios) {
                return true;
            } else if (itemId == R.id.nav_favoritos) {
                return true;
            }
            return false;
        });
    }

    private void mostrarPerfil() {
        findViewById(R.id.containerPerfil).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerPerfil, new PerfilFragment()) // Não te esqueças de meter o PerfilFragment na pasta 'ui' também!
                .commit();
        voltarDoPerfil.setEnabled(true);
    }

    private void esconderPerfil() {
        Fragment atual = getSupportFragmentManager().findFragmentById(R.id.containerPerfil);
        if (atual != null) {
            getSupportFragmentManager().beginTransaction().remove(atual).commit();
        }
        findViewById(R.id.containerPerfil).setVisibility(View.GONE);
        voltarDoPerfil.setEnabled(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Aplica o estilo visual diretamente na View
        aplicarEstiloLimpo();
        ajustarEspacamento();

        verificarPermissaoGPS();
    }

    private void aplicarEstiloLimpo() {
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajustarEspacamento() {
        int paddingBottom = (int) (65 * getResources().getDisplayMetrics().density);
        mMap.setPadding(0, 0, 0, paddingBottom);
    }

    private void verificarPermissaoGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacaoECentrar();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void ativarLocalizacaoECentrar() {
        if (mMap == null) return;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 14.5f));

                // 4. Pede os dados ao PlacesService (agora focado apenas na API)
                placesService.pesquisarGinasiosProximos(minhaPosicao, new PlacesService.OnGymsLoadedListener() {
                    @Override
                    public void onSuccess(List<Ginasio> ginasios) {
                        runOnUiThread(() -> desenharMarcadores(ginasios));
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                Toast.makeText(this, "Aguardando sinal de GPS...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void desenharMarcadores(List<Ginasio> ginasios) {
        if (mMap == null) return;
        mMap.clear();

        for (Ginasio g : ginasios) {
            LatLng posicao = new LatLng(g.getLatitude(), g.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .title(g.getNome())
                    .snippet("Rating: " + g.getRating())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }
}