package com.example.bettergym.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.example.bettergym.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

public class mapController {

    private final Context context;
    private GoogleMap mMap;
    private final FusedLocationProviderClient fusedLocationClient;

    public mapController(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void setMap(GoogleMap googleMap) {
        this.mMap = googleMap;
        aplicarEstiloLimpo();
        ajustarEspacamento();
    }

    private void aplicarEstiloLimpo() {
        if (mMap == null) return;
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ajustarEspacamento() {
        if (mMap == null) return;
        int paddingBottom = (int) (65 * context.getResources().getDisplayMetrics().density);
        mMap.setPadding(0, 0, 0, paddingBottom);
    }

    @SuppressLint("MissingPermission")
    public void ativarLocalizacaoECentrar() {
        if (mMap == null) return;

        // Ativa o ponto azul e o botão para recentrar
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 15.0f));
            } else {
                Toast.makeText(context, "Aguardando sinal de GPS...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public GoogleMap getMap() {
        return mMap;
    }
}