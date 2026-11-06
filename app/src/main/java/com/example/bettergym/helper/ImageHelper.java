package com.example.bettergym.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageHelper {

    private static final int MAX_DIMENSAO = 600;
    private static final int QUALIDADE_JPEG = 75;

    public static String converterParaBase64(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        return "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
    }

    public static byte[] comprimirImagem(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is == null) return null;
                BitmapFactory.decodeStream(is, null, options);
            }

            int larguraOriginal = options.outWidth;
            int alturaOriginal = options.outHeight;
            if (larguraOriginal <= 0 || alturaOriginal <= 0) {
                return null;
            }

            int inSampleSize = 1;
            while ((larguraOriginal / inSampleSize) > MAX_DIMENSAO || (alturaOriginal / inSampleSize) > MAX_DIMENSAO) {
                inSampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;

            Bitmap bitmap;
            try (InputStream is = context.getContentResolver().openInputStream(uri)) {
                if (is == null) return null;
                bitmap = BitmapFactory.decodeStream(is, null, options);
            }

            if (bitmap == null) {
                return null;
            }

            int largura = bitmap.getWidth();
            int altura = bitmap.getHeight();
            if (largura > MAX_DIMENSAO || altura > MAX_DIMENSAO) {
                float ratio = Math.min((float) MAX_DIMENSAO / largura, (float) MAX_DIMENSAO / altura);
                int novaLargura = Math.round(largura * ratio);
                int novaAltura = Math.round(altura * ratio);
                Bitmap bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, novaLargura, novaAltura, true);
                if (bitmapRedimensionado != bitmap) {
                    bitmap.recycle();
                    bitmap = bitmapRedimensionado;
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, outputStream);
            bitmap.recycle();

            return outputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}

