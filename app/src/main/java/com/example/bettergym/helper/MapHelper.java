package com.example.bettergym.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.example.bettergym.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;

public class MapHelper {

    /**
     * Aplica o estilo personalizado (Clean Fitness) ao GoogleMap.
     */
    public static void aplicarEstilo(Context context, GoogleMap map) {
        if (map == null) return;
        try {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));
        } catch (Exception e) {
            Log.e("MapHelper", "Erro ao carregar estilo do mapa: " + e.getMessage());
        }
    }

    /**
     * Gera o marcador personalizado com o ícone de haltere da BetterGym.
     */
    public static BitmapDescriptor criarMarcadorGinasio(Context context, boolean aberto) {
        float densidade = context.getResources().getDisplayMetrics().density;
        int tamanhoPx = (int) (44 * densidade);
        Bitmap bitmap = Bitmap.createBitmap(tamanhoPx, tamanhoPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        float raio = (tamanhoPx / 2f) - (3f * densidade);

        // 0. Sombra suave para dar destaque tridimensional sobre o mapa claro
        Paint paintSombra = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSombra.setColor(Color.argb(50, 0, 0, 0));
        paintSombra.setStyle(Paint.Style.FILL);
        canvas.drawCircle(tamanhoPx / 2f, (tamanhoPx / 2f) + (1.5f * densidade), raio, paintSombra);

        // 1. Fundo Circular (Verde lima se aberto, Vermelho se fechado)
        Paint paintFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFundo.setColor(ContextCompat.getColor(context, aberto ? R.color.secondary : R.color.estado_fechado));
        paintFundo.setStyle(Paint.Style.FILL);
        canvas.drawCircle(tamanhoPx / 2f, tamanhoPx / 2f, raio, paintFundo);

        // 2. Borda branca de contraste
        Paint paintBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBorda.setColor(Color.WHITE);
        paintBorda.setStyle(Paint.Style.STROKE);
        paintBorda.setStrokeWidth(2.5f * densidade);
        canvas.drawCircle(tamanhoPx / 2f, tamanhoPx / 2f, raio, paintBorda);

        // 3. Haltere no centro
        Drawable dumbbell = ContextCompat.getDrawable(context, R.drawable.ic_dumbbell);
        if (dumbbell != null) {
            int padding = (int) (11 * densidade);
            dumbbell.setBounds(padding, padding, tamanhoPx - padding, tamanhoPx - padding);
            int corIcone = aberto ? ContextCompat.getColor(context, R.color.primary) : Color.WHITE;
            dumbbell.setTint(corIcone);
            dumbbell.draw(canvas);
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
