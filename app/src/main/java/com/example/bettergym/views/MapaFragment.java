package com.example.bettergym.views; // Atualizado para a pasta ui

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesService placesService;

    // Elementos do Cartão
    private androidx.cardview.widget.CardView cardDetalhes;
    private android.widget.TextView txtCardNome, txtCardAberto;
    private com.google.android.material.button.MaterialButton btnVerDetalhes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ligar as views do cartão
        cardDetalhes = view.findViewById(R.id.cardDetalhesGinasio);
        txtCardNome = view.findViewById(R.id.txtCardNome);
        txtCardAberto = view.findViewById(R.id.txtCardAberto);
        btnVerDetalhes = view.findViewById(R.id.btnVerDetalhes);

        // 1. Inicializa o GPS e o Serviço
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        placesService = new PlacesService();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 2. Aplica o teu estilo antigo do mapa
        aplicarEstiloLimpo();
        ajustarEspacamento();

        // 3. Configura os cliques no mapa para o novo Cartão Interativo
        configurarCliquesMapa();

        // 4. Liga o GPS e pede os ginásios ao serviço
        ativarLocalizacaoECentrar();
    }

    private void aplicarEstiloLimpo() {
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajustarEspacamento() {
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


                btnVerDetalhes.setOnClickListener(v -> {
                    Toast.makeText(requireContext(), "Detalhes do " + ginasio.getNome() + " em breve!", Toast.LENGTH_SHORT).show();
                });

                cardDetalhes.setVisibility(View.VISIBLE);

                // Centra a câmara no pino quando clicas nele
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            }

            return true; // IMPORTANTE: TRUE significa que tratamos do clique e o mapa não abre a janela padrão
        });
    }

    @SuppressLint("MissingPermission")
    private void ativarLocalizacaoECentrar() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 14.5f));

                // O Fragment pede os dados ao Serviço
                placesService.pesquisarGinasiosProximos(minhaPosicao, new PlacesService.OnGymsLoadedListener() {
                    @Override
                    public void onSuccess(List<Ginasio> ginasios) {
                        // Quando chegam os dados, vai para a Main Thread para desenhar no mapa
                        requireActivity().runOnUiThread(() -> desenharMarcadores(ginasios));
                    }

                    @Override
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } else {
                Toast.makeText(getContext(), "Aguardar sinal de GPS...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método isolado apenas para gerir os marcadores visuais
    private void desenharMarcadores(List<Ginasio> ginasios) {
        if (mMap == null) return;

        mMap.clear(); // Limpa pesquisas antigas

        for (Ginasio g : ginasios) {
            LatLng posicao = new LatLng(g.getLatitude(), g.getLongitude());

            // Removido o .title() para evitar que a janela antiga apareça
            Marker marcador = mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // PASSO CRUCIAL: Guarda o objeto Ginásio dentro do marcador
            if (marcador != null) {
                marcador.setTag(g);
            }
        }
    }
}