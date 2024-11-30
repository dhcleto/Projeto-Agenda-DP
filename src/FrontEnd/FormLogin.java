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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormLogin extends AppCompatActivity {

    private static final String TAG = "FormLogin";
    private TextView textTelaCadastro;
    private EditText editEmail, editSenha;
    private Button btEntrar;
    private ProgressBar progressBar;
    private ApiService apiService;

    String[] mensagens = {"Preencha todos os campos", "Erro ao efetuar login. Verifique suas credenciais."};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_login);
        getSupportActionBar().hide();

        // Inicializar componentes
        iniciarComponentes();

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://xqlj3s-3000.csb.app") // Substitua pela URL do seu servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Redirecionar para a tela de cadastro
        textTelaCadastro.setOnClickListener(view -> {
            Intent intent = new Intent(FormLogin.this, FormCadastro.class);
            startActivity(intent);
        });

        // Ação do botão de login
        btEntrar.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                mostrarMensagemSnackBar(v, mensagens[0]);
            } else {
                autenticarUsuario(v, email, senha);
            }
        });
    }

    private void autenticarUsuario(View v, String email, String senha) {
        progressBar.setVisibility(View.VISIBLE);

        try {
            String senhaCriptografada = AESHelper.encrypt(senha); // Criptografar a senha com AES
            LoginRequest loginRequest = new LoginRequest(email, senhaCriptografada);

            apiService.login(loginRequest).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isSuccess()) {
                            iniciarMainActivity(response.body().getUser());
                        } else {
                            mostrarMensagemSnackBar(v, "Credenciais inválidas");
                        }
                    } else {
                        mostrarMensagemSnackBar(v, "Erro ao efetuar login");
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    mostrarMensagemSnackBar(v, "Erro de conexão");
                }
            });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            mostrarMensagemSnackBar(v, "Erro ao criptografar a senha");
        }
    }

    private void iniciarMainActivity(UserResponse.User user) {
        Intent intent = new Intent(FormLogin.this, MainActivity.class);
        intent.putExtra("userId", user.getId());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("nome", user.getNome());
        startActivity(intent);
        finish();
    }

    private void iniciarComponentes() {
        textTelaCadastro = findViewById(R.id.text_tela_cadastro);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        btEntrar = findViewById(R.id.bt_entrar);
        progressBar = findViewById(R.id.progressbar);
    }

    private void mostrarMensagemSnackBar(View v, String mensagem) {
        Snackbar snackbar = Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();
    }
}
