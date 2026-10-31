package com.example.bettergym.handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.bettergym.dto.PlaceGymDTO;
import com.example.bettergym.dto.PerfilDTO;
import com.example.bettergym.dto.FavoritoDTO;
import com.example.bettergym.dto.AvaliacaoDTO;
import com.example.bettergym.dto.VisitaDTO;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class JsonHandler {

    public static List<PlaceGymDTO> deSerializeJson2ListPlaceGymDTO(String resp) throws JSONException {
        List<PlaceGymDTO> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(resp);

        if (!jsonObject.has("places")) return list;

        JSONArray jsonResponse = jsonObject.getJSONArray("places");

        for(int i = 0; i < jsonResponse.length(); i++){
            JSONObject jsonChildNode = jsonResponse.getJSONObject(i);

            String id = jsonChildNode.optString("id", "");
            String name = jsonChildNode.has("displayName") ? jsonChildNode.getJSONObject("displayName").optString("text", "Sem Nome") : "Sem Nome";

            double lat = 0.0, lng = 0.0;
            if (jsonChildNode.has("location")) {
                lat = jsonChildNode.getJSONObject("location").optDouble("latitude", 0.0);
                lng = jsonChildNode.getJSONObject("location").optDouble("longitude", 0.0);
            }

            double rating = jsonChildNode.optDouble("rating", 0.0);
            String morada = jsonChildNode.optString("formattedAddress", "");
            int numeroAvaliacoes = jsonChildNode.optInt("userRatingCount", 0);
            List<String> listaFotos = new ArrayList<>();
            String referenciaFoto = "";
            if (jsonChildNode.has("photos")) {
                JSONArray photosArray = jsonChildNode.getJSONArray("photos");
                for (int p = 0; p < photosArray.length(); p++) {
                    String nomeFoto = photosArray.getJSONObject(p).optString("name", "");
                    if (!nomeFoto.isEmpty()) {
                        listaFotos.add(nomeFoto);
                    }
                }
                if (!listaFotos.isEmpty()) {
                    referenciaFoto = listaFotos.get(0);
                }
            }

            // --- MUDANÇA AQUI: Navegar pelas "caixas" corretas da Google ---
            boolean isOpenNow = false;
            if (jsonChildNode.has("regularOpeningHours")) {
                isOpenNow = jsonChildNode.getJSONObject("regularOpeningHours").optBoolean("openNow", false);
            } else if (jsonChildNode.has("currentOpeningHours")) {
                isOpenNow = jsonChildNode.getJSONObject("currentOpeningHours").optBoolean("openNow", false);
            }
            // ----------------------------------------------------------------

            List<AvaliacaoDTO> listaAvaliacoes = new ArrayList<>();
            if (jsonChildNode.has("reviews")) {
                JSONArray reviewsArray = jsonChildNode.getJSONArray("reviews");

                for (int j = 0; j < reviewsArray.length(); j++) {
                    JSONObject reviewObj = reviewsArray.getJSONObject(j);
                    AvaliacaoDTO avaliacaoDTO = new AvaliacaoDTO();

                    avaliacaoDTO.setGinasio_id(id); // Associar ao ginásio atual

                    // Obter Nome do Autor
                    if (reviewObj.has("authorAttribution")) {
                        avaliacaoDTO.setUser_id(reviewObj.getJSONObject("authorAttribution").optString("displayName", "Utilizador Anónimo"));
                    }

                    // Obter Nota (Rating do Comentário)
                    avaliacaoDTO.setNota(String.valueOf(reviewObj.optInt("rating", 0)));

                    // Obter Texto do Comentário
                    if (reviewObj.has("text")) {
                        avaliacaoDTO.setDescricao(reviewObj.getJSONObject("text").optString("text", ""));
                    }

                    listaAvaliacoes.add(avaliacaoDTO);
                }
            }

            PlaceGymDTO gymDTO = new PlaceGymDTO(id, name, lat, lng, rating, isOpenNow, morada, numeroAvaliacoes, referenciaFoto);
            gymDTO.setAvaliacoes(listaAvaliacoes);
            gymDTO.setFotos(listaFotos);

            Log.d("JsonHandler", "Ginásio: " + gymDTO.getNome() + " - Avaliações: " + listaAvaliacoes);
            list.add(gymDTO);
        }
        return list;
    }


    // ----------------------------------------------------------------------------------
    // DESERIALIZAR O PERFIL DO FIREBASE (REST API)
    // ----------------------------------------------------------------------------------
    public static PerfilDTO deSerializeFirebaseJson2PerfilDTO(String json) {
        try {
            JSONObject doc = new JSONObject(json);

            // O Firebase envia os dados dentro da gaveta "fields"
            if (doc.has("fields")) {
                JSONObject fields = doc.getJSONObject("fields");

                PerfilDTO dto = new PerfilDTO();

                if (fields.has("nome_completo")) {
                    dto.setNome_completo(fields.getJSONObject("nome_completo").optString("stringValue", ""));
                }

                if (fields.has("username")) {
                    dto.setUsername(fields.getJSONObject("username").optString("stringValue", ""));
                }

                if (fields.has("email")) {
                    dto.setEmail(fields.getJSONObject("email").optString("stringValue", ""));
                }

                if (fields.has("numero_telemovel")) {
                    dto.setNumero_telemovel(fields.getJSONObject("numero_telemovel").optString("stringValue", ""));
                }

                if (fields.has("data_nascimento")) {
                    dto.setData_nascimento(fields.getJSONObject("data_nascimento").optString("stringValue", ""));
                }

                if (fields.has("data_registo")) {
                    dto.setData_registo(fields.getJSONObject("data_registo").optString("stringValue", ""));
                }

                if (fields.has("foto_url")) {
                    dto.setFoto_url(fields.getJSONObject("foto_url").optString("stringValue", ""));
                }

                if (fields.has("historico_ginasios_visitados")) {
                    JSONObject histObj = fields.optJSONObject("historico_ginasios_visitados");
                    if (histObj != null && histObj.has("arrayValue")) {
                        JSONObject arrayVal = histObj.optJSONObject("arrayValue");
                        if (arrayVal != null && arrayVal.has("values")) {
                            JSONArray valArray = arrayVal.optJSONArray("values");
                            if (valArray != null) {
                                List<VisitaDTO> visitas = new ArrayList<>();
                                for (int i = 0; i < valArray.length(); i++) {
                                    JSONObject item = valArray.optJSONObject(i);
                                    if (item != null && item.has("mapValue")) {
                                        JSONObject mapVal = item.optJSONObject("mapValue");
                                        if (mapVal != null && mapVal.has("fields")) {
                                            JSONObject vFields = mapVal.optJSONObject("fields");
                                            if (vFields != null) {
                                                String gId = "";
                                                String dVisita = "";
                                                String nGinasio = "";
                                                if (vFields.has("ginasio_id")) {
                                                    gId = vFields.getJSONObject("ginasio_id").optString("stringValue", "");
                                                }
                                                if (vFields.has("data_visita")) {
                                                    dVisita = vFields.getJSONObject("data_visita").optString("stringValue", "");
                                                }
                                                if (vFields.has("nome_ginasio")) {
                                                    nGinasio = vFields.getJSONObject("nome_ginasio").optString("stringValue", "");
                                                } else if (vFields.has("nome")) {
                                                    nGinasio = vFields.getJSONObject("nome").optString("stringValue", "");
                                                }
                                                visitas.add(new VisitaDTO(gId, dVisita, nGinasio));
                                            }
                                        }
                                    }
                                }
                                dto.setHistorico_ginasios_visitados(visitas);
                            }
                        }
                    }
                }

                return dto;
            }
        } catch (Exception e) {
            android.util.Log.e("JSON_ERRO", "Erro ao ler Perfil do Firebase: " + e.getMessage());
        }
        return null;
    }

    // ----------------------------------------------------------------------------------
    // SERIALIZAR O HISTÓRICO DE VISITAS PARA FIREBASE (REST API)
    // ----------------------------------------------------------------------------------
    public static String serializeHistoricoVisitasFirebaseJson(List<VisitaDTO> visitas) throws JSONException {
        JSONObject root = new JSONObject();
        JSONObject fields = new JSONObject();
        JSONObject histObj = new JSONObject();
        JSONObject arrayValue = new JSONObject();
        JSONArray values = new JSONArray();

        if (visitas != null) {
            for (VisitaDTO v : visitas) {
                JSONObject item = new JSONObject();
                JSONObject mapValue = new JSONObject();
                JSONObject itemFields = new JSONObject();

                JSONObject gIdObj = new JSONObject();
                gIdObj.put("stringValue", v.getGinasio_id() != null ? v.getGinasio_id() : "");
                itemFields.put("ginasio_id", gIdObj);

                JSONObject dVisitaObj = new JSONObject();
                dVisitaObj.put("stringValue", v.getData_visita() != null ? v.getData_visita() : "");
                itemFields.put("data_visita", dVisitaObj);

                if (v.getNome_ginasio() != null && !v.getNome_ginasio().isEmpty()) {
                    JSONObject nObj = new JSONObject();
                    nObj.put("stringValue", v.getNome_ginasio());
                    itemFields.put("nome_ginasio", nObj);
                }

                mapValue.put("fields", itemFields);
                item.put("mapValue", mapValue);
                values.put(item);
            }
        }

        arrayValue.put("values", values);
        histObj.put("arrayValue", arrayValue);
        fields.put("historico_ginasios_visitados", histObj);
        root.put("fields", fields);

        return root.toString();
    }

    // ----------------------------------------------------------------------------------
    // DESERIALIZAR A LISTA DE FAVORITOS DO FIREBASE (REST API)
    // ----------------------------------------------------------------------------------
    public static List<FavoritoDTO> deSerializeFirebaseJson2ListFavoritoDTO(String json) {
        List<FavoritoDTO> lista = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                if (obj.has("document")) {
                    JSONObject doc = obj.getJSONObject("document");

                    String namePath = doc.optString("name", "");
                    String docId = namePath.substring(namePath.lastIndexOf("/") + 1);

                    JSONObject fields = doc.getJSONObject("fields");

                    FavoritoDTO dto = new FavoritoDTO();
                    dto.setId(docId);

                    if (fields.has("user_id")) {
                        dto.setUser_id(fields.getJSONObject("user_id").optString("stringValue", ""));
                    }
                    if (fields.has("ginasio_id")) {
                        dto.setGinasio_id(fields.getJSONObject("ginasio_id").optString("stringValue", ""));
                    }
                    if (fields.has("data_adicionado")) {
                        dto.setData_adicionado(fields.getJSONObject("data_adicionado").optString("stringValue", ""));
                    }

                    // AQUI ESTAVA O ERRO!
                    // Agora vamos garantir que criamos SEMPRE um Ginasio, mesmo para dados antigos!
                    String nomeGinasio = "Ginásio Guardado"; // Nome por defeito se faltar na base de dados

                    if (fields.has("nome")) {
                        nomeGinasio = fields.getJSONObject("nome").optString("stringValue", "Ginásio Guardado");
                    }

                    // Criamos sempre o DTO para não dar NullPointerException no Adapter do teu amigo
                    PlaceGymDTO gymDto = new PlaceGymDTO();
                    gymDto.setId(dto.getGinasio_id());
                    gymDto.setNome(nomeGinasio);
                    // (Opcional) Podemos pôr uma morada vazia para evitar outro erro
                    gymDto.setMorada("");

                    dto.setGinasio(gymDto);

                    lista.add(dto);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("JSON_ERRO", "Erro ao ler Favoritos: " + e.getMessage());
        }
        return lista;
    }
}
