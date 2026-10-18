package com.example.bettergym.views; // MUDAR PARA ui

import android.annotation.SuppressLint;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesService placesService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mapa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        // 3. Liga o GPS e pede os ginásios ao serviço
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

    @SuppressLint("MissingPermission")
    private void ativarLocalizacaoECentrar() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 14.5f));

                // 4. O Fragment pede os dados ao Serviço
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

    // 5. Método isolado apenas para gerir os marcadores visuais
    private void desenharMarcadores(List<Ginasio> ginasios) {
        if (mMap == null) return;

        mMap.clear(); // Limpa pesquisas antigas

        for (Ginasio g : ginasios) {
            LatLng posicao = new LatLng(g.getLatitude(), g.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .title(g.getNome())
                    .snippet("Rating: " + g.getRating()) // Mostra o rating no clique!
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }
}