package com.example.bettergym.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bettergym.R;
import com.example.bettergym.handler.NetworkHandler;
import com.example.bettergym.service.PlacesService;
import java.util.ArrayList;
import java.util.List;

public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {

    private final Context context;
    private final List<String> referenciasFotos = new ArrayList<>();

    public FotosAdapter(Context context, List<String> fotos) {
        this.context = context;
        if (fotos != null) {
            this.referenciasFotos.addAll(fotos);
        }
    }

    public void atualizarFotos(List<String> novasFotos) {
        this.referenciasFotos.clear();
        if (novasFotos != null) {
            this.referenciasFotos.addAll(novasFotos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_foto_ginasio, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String referencia = referenciasFotos.get(position);
        holder.bind(referencia);
    }

    @Override
    public int getItemCount() {
        return referenciasFotos.size();
    }

    class FotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgFoto;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
        }

        public void bind(String referencia) {
            imgFoto.setImageDrawable(null);
            imgFoto.setTag(referencia);
            if (referencia == null || referencia.isEmpty()) return;

            String url = PlacesService.obterUrlFoto(referencia);
            if (url == null) return;

            new Thread(() -> {
                try {
                    Bitmap bitmap = NetworkHandler.downloadBitmap(url);
                    if (bitmap != null && context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            if (referencia.equals(imgFoto.getTag())) {
                                imgFoto.setImageBitmap(bitmap);
                            }
                        });
                    }
                } catch (Exception ignored) {
                }
            }).start();
        }
    }
}

