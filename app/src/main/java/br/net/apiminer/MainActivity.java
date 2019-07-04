package br.net.apiminer;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView resultado;
    private EditText codigo;
    private String categoria;
    private int quantidadeProdutos = 0;
    private List<JSONArray> listaProdutos = new ArrayList<>();
    private List<String> produtos = new ArrayList<>();
    private String nomes = "\n\nProdutos: \n\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        resultado = findViewById(R.id.resultado);
        codigo = findViewById(R.id.codigo);
        Button btn = findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    buscarDados(codigo.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
                resultado.setText("Categoria: " + categoria + "\n\nQuantidade de JSONArrays de produtos: " + listaProdutos.size() + "\n\nTotal de produtos minerados da api: " + quantidadeProdutos + nomes);
            }
        });
    }

    private void buscarDados(String codigo) throws Exception {

        String token = "YTfQgI-6K0M4SPyeYLn_hQ";
        String endpoint = "https://api.cosmos.bluesoft.com.br";
        URL url = new URL(endpoint + "/gpcs/" + codigo);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setRequestProperty("X-Cosmos-Token", token);
        conexao.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
        String jsonString = br.readLine();
        processarDados(new JSONObject(jsonString), codigo);
        percorrerProdutos();
        conexao.disconnect();
    }

    private JSONArray atualizarPagina(String paginacao, String codigo) throws Exception {
        String token = "YTfQgI-6K0M4SPyeYLn_hQ";
        String endpoint = "https://api.cosmos.bluesoft.com.br";
        URL url = new URL(endpoint + "/gpcs/" + codigo + paginacao);
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setRequestProperty("X-Cosmos-Token", token);
        conexao.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
        String jsonString = br.readLine();
        JSONObject jo = new JSONObject(jsonString);
        JSONArray ja = (JSONArray) jo.get("products");
        conexao.disconnect();
        return ja;
    }

    private void percorrerProdutos() throws Exception{

        for (JSONArray ja: listaProdutos) {
            for (int i=1; i <= ja.length(); i++) {
                quantidadeProdutos = quantidadeProdutos + 1;
                JSONObject obj = (JSONObject) ja.get(i-1);
                produtos.add(obj.getString("description"));
            }
        }
        for (String a: produtos) {
            nomes = nomes + "\n" + a;
        }
    }

    private void processarDados(JSONObject jsonObject, String codigo) throws Exception {
        JSONArray jsonProdutos;
        int maxPaginas = Integer.parseInt(jsonObject.get("total_pages").toString());
        categoria = jsonObject.getString("portuguese");
        for (int paginaAtual=1 ; paginaAtual <= maxPaginas; paginaAtual++) {
            if (paginaAtual == 1) {
                jsonProdutos = (JSONArray) jsonObject.get("products");
            } else {
                String paginacao = "?page=" + paginaAtual;
                jsonProdutos = atualizarPagina(paginacao, codigo);
            }
            listaProdutos.add(jsonProdutos);
        }
    }
}
