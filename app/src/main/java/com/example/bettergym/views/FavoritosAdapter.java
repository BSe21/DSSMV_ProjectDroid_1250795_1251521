package com.example.bettergym.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.bettergym.R;
import com.example.bettergym.model.Favorito;
import com.example.bettergym.model.Ginasio;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FavoritosAdapter extends BaseAdapter {
    interface RemoverListener { void remover(Favorito favorito); }
    interface AbrirListener { void abrir(Favorito favorito); }
    private final List<Favorito> favoritos = new ArrayList<>();
    private final Set<String> aRemover = new HashSet<>();
    private final RemoverListener listener;
    private final AbrirListener abrirListener;

    FavoritosAdapter(RemoverListener listener, AbrirListener abrirListener) {
        this.listener = listener;
        this.abrirListener = abrirListener;
    }

    void atualizar(List<Favorito> lista) {
        favoritos.clear();
        favoritos.addAll(lista);
        notifyDataSetChanged();
    }

    void definirOcupado(String id, boolean ocupado) {
        if (ocupado) aRemover.add(id);
        else aRemover.remove(id);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return favoritos.size(); }
    @Override public Favorito getItem(int position) { return favoritos.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorito, parent, false) : convertView;
        Favorito favorito = getItem(position);
        Ginasio ginasio = favorito.getGinasio();
        row.setOnClickListener(v -> abrirListener.abrir(favorito));
        ((TextView) row.findViewById(R.id.txtFavoritoNome)).setText(ginasio.getNome());
        TextView morada = row.findViewById(R.id.txtFavoritoMorada);
        morada.setText(ginasio.getMorada() == null || ginasio.getMorada().isEmpty()
                ? parent.getContext().getString(R.string.favorites_address_unavailable) : ginasio.getMorada());
        View remover = row.findViewById(R.id.btnRemoverFavorito);
        remover.setEnabled(!aRemover.contains(favorito.getGinasio_id()));
        remover.setContentDescription(parent.getContext().getString(
                R.string.favorites_remove_named, ginasio.getNome()));
        remover.setOnClickListener(v -> listener.remover(favorito));
        return row;
    }
}
