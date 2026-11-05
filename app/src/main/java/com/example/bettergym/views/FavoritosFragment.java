package com.example.bettergym.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bettergym.R;
import com.example.bettergym.exceptions.DataConsistencyException;
import com.example.bettergym.exceptions.InvalidDataException;
import com.example.bettergym.model.Favorito;
import com.example.bettergym.service.FavoritosService;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class FavoritosFragment extends Fragment {
    private FavoritosService service;
    private FavoritosAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favoritos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        service = new FavoritosService();
        adapter = new FavoritosAdapter(this::remover, this::abrirDetalhes);
        ListView lista = view.findViewById(R.id.listaFavoritos);
        lista.setAdapter(adapter);
        view.findViewById(R.id.btnRepetirFavoritos).setOnClickListener(v -> carregar());
    }

    @Override public void onStart() {
        super.onStart();
        carregar();
    }

    private void carregar() {
        mostrarEstado(R.string.favorites_loading, true, false, false);

        service.obterLista(new FavoritosService.ListaCallback() {
            @Override public void onResultado(List<Favorito> favoritos) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    View view = getView();
                    if (view == null) return;
                    adapter.atualizar(favoritos);
                    if (favoritos.isEmpty()) {
                        mostrarEstado(R.string.favorites_empty_title, false, true, false);
                    } else {
                        view.findViewById(R.id.estadoFavoritos).setVisibility(View.GONE);
                        view.findViewById(R.id.listaFavoritos).setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override public void onErro(Exception erro) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    mostrarEstado(R.string.favorites_error, false, false, true);
                });
            }
        });
    }

    private void mostrarEstado(int mensagem, boolean carregar, boolean vazio, boolean erro) {
        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.listaFavoritos).setVisibility(View.GONE);
        view.findViewById(R.id.estadoFavoritos).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.txtEstadoFavoritos)).setText(mensagem);
        view.findViewById(R.id.progressoFavoritos).setVisibility(carregar ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.txtAjudaFavoritos).setVisibility(vazio ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.btnRepetirFavoritos).setVisibility(erro ? View.VISIBLE : View.GONE);
    }

    private void abrirDetalhes(Favorito favorito) {
        Intent intent = new Intent(requireContext(), DetalheGinasioActivity.class);
        intent.putExtra(DetalheGinasioActivity.EXTRA_GINASIO_ID, favorito.getGinasio_id());
        startActivity(intent);
    }

    private void remover(Favorito favorito) {
        FavoritosAdapter atual = adapter;
        atual.definirOcupado(favorito.getGinasio_id(), true);
        service.remover(favorito.getGinasio_id(), new FavoritosService.EscritaCallback() {
            @Override public void onSucesso() {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    atual.definirOcupado(favorito.getGinasio_id(), false);
                    if (getView() != null) Toast.makeText(requireContext(),
                            R.string.favorites_removed, Toast.LENGTH_SHORT).show();

                    carregar();
                });
            }

            @Override public void onErro(Exception erro) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    atual.definirOcupado(favorito.getGinasio_id(), false);
                    String msg = getString(R.string.favorites_write_error);
                    if (erro instanceof InvalidDataException) {
                        msg = ((InvalidDataException) erro).what();
                    } else if (erro instanceof DataConsistencyException) {
                        msg = ((DataConsistencyException) erro).what();
                    }
                    if (getView() != null) Toast.makeText(requireContext(),
                            msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    @Override public void onStop() {

        super.onStop();
    }
}
