package com.example.bettergym.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bettergym.R;
import com.example.bettergym.helper.DateHelper;
import com.example.bettergym.model.Visita;

import java.util.ArrayList;
import java.util.List;

public class VisitasAdapter extends BaseAdapter {

    public interface AbrirListener {
        void abrir(Visita visita);
    }

    private final List<Visita> visitas = new ArrayList<>();
    private final AbrirListener abrirListener;

    public VisitasAdapter(AbrirListener abrirListener) {
        this.abrirListener = abrirListener;
    }

    public void atualizar(List<Visita> lista) {
        visitas.clear();
        if (lista != null) {
            visitas.addAll(lista);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return visitas.size();
    }

    @Override
    public Visita getItem(int position) {
        return visitas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView == null ? LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visita, parent, false) : convertView;

        Visita visita = getItem(position);

        String nome = visita.getNome_ginasio();
        if (nome == null || nome.trim().isEmpty()) {
            nome = "Ginásio Visitado";
        }

        ((TextView) row.findViewById(R.id.txtVisitaNome)).setText(nome);
        ((TextView) row.findViewById(R.id.txtVisitaData)).setText(DateHelper.formatarVisita(visita.getData_visita()));

        row.setOnClickListener(v -> {
            if (abrirListener != null) {
                abrirListener.abrir(visita);
            }
        });

        return row;
    }
}
