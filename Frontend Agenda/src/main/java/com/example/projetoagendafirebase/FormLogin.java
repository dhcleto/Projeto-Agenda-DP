package com.example.projetoagendafirebase;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FormLogin extends AppCompatActivity {

    private static final String TAG = "FormLogin";
    private static final String SERVER_URL = "https://6s33ws-8080.csb.app/login"; // URL do seu servidor

    private TextView text_tela_cadastro;
    private EditText edit_email, edit_senha;
    private Button bt_entrar;
    private ProgressBar progressBar;
    String[] mensagens = {"Preencha todos os campos", "Erro ao efetuar login. Verifique suas credenciais."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);
        getSupportActionBar().hide();
        IniciarComponentes();

        text_tela_cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FormLogin.this, FormCadastro.class);
                startActivity(intent);
            }
        });

        bt_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edit_email.getText().toString();
                String senha = edit_senha.getText().toString();

                if (email.isEmpty() || senha.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(v, mensagens[0], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                } else {
                    AutenticarUsuario(email, senha);
                }
            }
        });
    }

    private void AutenticarUsuario(final String email, final String senha) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000); // 10 segundos para conexão
                    conn.setReadTimeout(10000); // 10 segundos para leitura

                    // Criando JSON com credenciais
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("email", email);
                    jsonParam.put("senha", senha);

                    // Enviando a requisição
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                    os.close();

                    // Lendo a resposta
                    int responseCode = conn.getResponseCode();
                    StringBuilder response = new StringBuilder();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        // Analisando a resposta JSON
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        if (jsonResponse.getBoolean("success")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    MainActivity(); // Login bem-sucedido
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Snackbar snackbar = Snackbar.make(bt_entrar, mensagens[1], Snackbar.LENGTH_SHORT);
                                    snackbar.setBackgroundTint(Color.WHITE);
                                    snackbar.setTextColor(Color.BLACK);
                                    snackbar.show();
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Erro de resposta: Código " + responseCode);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                Snackbar snackbar = Snackbar.make(bt_entrar, "Erro de conexão. Código: " + responseCode, Snackbar.LENGTH_SHORT);
                                snackbar.setBackgroundTint(Color.WHITE);
                                snackbar.setTextColor(Color.BLACK);
                                snackbar.show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao autenticar usuário", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Snackbar snackbar = Snackbar.make(bt_entrar, "Erro de conexão. Verifique sua rede.", Snackbar.LENGTH_SHORT);
                            snackbar.setBackgroundTint(Color.WHITE);
                            snackbar.setTextColor(Color.BLACK);
                            snackbar.show();
                        }
                    });
                }
            }
        }).start();
    }

    private void MainActivity() {
        Intent intent = new Intent(FormLogin.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void IniciarComponentes() {
        text_tela_cadastro = findViewById(R.id.text_tela_cadastro);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_entrar = findViewById(R.id.bt_entrar);
        progressBar = findViewById(R.id.progressbar);
    }
}
