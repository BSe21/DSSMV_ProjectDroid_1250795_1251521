package com.example.bettergym.views;

import android.Manifest;
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
import com.example.bettergym.controllers.mapController;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private mapController mapController;
    private OnBackPressedCallback voltarDoPerfil;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    mapController.ativarLocalizacaoECentrar();
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Instancia o controlador do mapa
        mapController = new mapController(this);

        // 2. Prepara o mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaPrincipal);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 3. Barra de navegação inferior
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Botão "voltar" no Perfil: regressa ao mapa em vez de sair da app
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

            esconderPerfil();   // qualquer outro separador volta a mostrar o mapa
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

    // Mostra o Perfil por cima do mapa, mantendo a barra de navegação em baixo
    private void mostrarPerfil() {
        findViewById(R.id.containerPerfil).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerPerfil, new PerfilFragment())
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
        mapController.setMap(googleMap);
        verificarPermissaoGPS();
    }

    private void verificarPermissaoGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mapController.ativarLocalizacaoECentrar();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
}