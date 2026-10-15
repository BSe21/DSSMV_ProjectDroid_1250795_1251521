package com.example.bettergym.controllers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.example.bettergym.R;
import com.example.bettergym.dtos.PlaceGymDTO;
import com.example.bettergym.model.Ginasio;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class mapController {

    private final Context context;
    private GoogleMap mMap;
    private final FusedLocationProviderClient fusedLocationClient;

    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyCKax_upBuMHtXzfmrAE6ItVybCaS-c-ug";

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

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng minhaPosicao = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(minhaPosicao, 14.5f));

                pesquisarGinasiosProximos(minhaPosicao);
            } else {
                Toast.makeText(context, "Aguardando sinal de GPS...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void pesquisarGinasiosProximos(LatLng posicao) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String urlString = "https://places.googleapis.com/v1/places:searchNearby";

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                    conexao.setRequestMethod("POST");
                    conexao.setRequestProperty("Content-Type", "application/json");
                    conexao.setRequestProperty("X-Goog-Api-Key", GOOGLE_PLACES_API_KEY);
                    conexao.setRequestProperty("X-Goog-FieldMask",
                            "places.id,places.displayName,places.location,places.formattedAddress,places.rating,places.userRatingCount,places.currentOpeningHours.openNow");
                    conexao.setDoOutput(true);
                    conexao.setConnectTimeout(10000);
                    conexao.setReadTimeout(10000);

                    String jsonInputString = "{"
                            + "\"includedTypes\": [\"gym\"],"
                            + "\"maxResultCount\": 20,"
                            + "\"locationRestriction\": {"
                            + "  \"circle\": {"
                            + "    \"center\": {"
                            + "      \"latitude\": " + posicao.latitude + ","
                            + "      \"longitude\": " + posicao.longitude
                            + "    },"
                            + "    \"radius\": 5000.0"
                            + "  }"
                            + "}"
                            + "}";

                    try (OutputStream os = conexao.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                    StringBuilder resposta = new StringBuilder();
                    String linha;

                    while ((linha = reader.readLine()) != null) {
                        resposta.append(linha);
                    }
                    reader.close();
                    conexao.disconnect();

                    JSONObject json = new JSONObject(resposta.toString());
                    if (!json.has("places")) return;

                    JSONArray places = json.getJSONArray("places");
                    List<Ginasio> ginasios = new ArrayList<>();

                    // AQUI USAS O DTO: desserializa o JSON para DTO e converte para o Model de domínio
                    for (int i = 0; i < places.length(); i++) {
                        JSONObject placeJson = places.getJSONObject(i);
                        PlaceGymDTO dto = PlaceGymDTO.fromJson(placeJson);
                        ginasios.add(dto.toModel());
                    }

                    // Volta à UI Thread para desenhar no mapa
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                desenharMarcadoresGinasios(ginasios);
                            }
                        });
                    }

                } catch (Exception e) {
                    android.util.Log.e("PLACES_ERRO", "Erro no processamento com DTO: " + e.getMessage());
                }
            }
        }).start();
    }

    private void desenharMarcadoresGinasios(List<Ginasio> ginasios) {
        if (mMap == null) return;

        for (Ginasio g : ginasios) {
            LatLng posicao = new LatLng(g.getLatitude(), g.getLongitude());

            mMap.addMarker(new MarkerOptions()
                    .position(posicao)
                    .title(g.getNome())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    public GoogleMap getMap() {
        return mMap;
    }
}