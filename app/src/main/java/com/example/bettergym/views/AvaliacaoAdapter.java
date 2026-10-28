package com.example.bettergym.views; // Ajusta o package se necessário

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.bettergym.R;
import com.example.bettergym.model.Avaliacao;
import java.util.List;

public class AvaliacaoAdapter extends BaseAdapter {
    private Context context;
    private List<Avaliacao> avaliacoes;

    public AvaliacaoAdapter(Context context, List<Avaliacao> avaliacoes) {
        this.context = context;
        this.avaliacoes = avaliacoes;
    }

    @Override
    public int getCount() {
        return avaliacoes != null ? avaliacoes.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return avaliacoes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_avaliacao, parent, false);
        }

        TextView txtNome = convertView.findViewById(R.id.txtNomeAutor);
        TextView txtNota = convertView.findViewById(R.id.txtNota);
        TextView txtDescricao = convertView.findViewById(R.id.txtDescricao);

        Avaliacao avaliacaoAtual = avaliacoes.get(position);

        txtNome.setText(avaliacaoAtual.getUser_id());
        txtNota.setText("★ " + avaliacaoAtual.getNota());
        txtDescricao.setText(avaliacaoAtual.getDescricao());

        return convertView;
    }
}