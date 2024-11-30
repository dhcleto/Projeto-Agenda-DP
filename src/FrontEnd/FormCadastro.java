package com.example.projetoagendafirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FormCadastro extends AppCompatActivity {

    private static final String TAG = "FormCadastro";
    private EditText edit_nome, edit_email, edit_senha;
    private Button bt_cadastrar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);

        getSupportActionBar().hide();
        iniciarComponentes();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://xqlj3s-3000.csb.app") // URL do servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        bt_cadastrar.setOnClickListener(v -> {
            String nome = edit_nome.getText().toString();
            String email = edit_email.getText().toString();
            String senha = edit_senha.getText().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Snackbar.make(v, "Preencha todos os campos", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Verificar formato do e-mail
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(v, "Insira um e-mail válido", Snackbar.LENGTH_SHORT).show();
                return;
            }

            try {
                // Criptografar a senha antes de enviar ao servidor
                String senhaCriptografada = AESHelper.encrypt(senha);
                RegisterRequest registerRequest = new RegisterRequest(nome, email, senhaCriptografada);
                cadastrarUsuario(registerRequest);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao criptografar a senha", e);
                Snackbar.make(v, "Erro ao criptografar a senha", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void cadastrarUsuario(RegisterRequest registerRequest) {
        apiService.register(registerRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Cadastro realizado com sucesso");
                    Snackbar.make(bt_cadastrar, "Cadastro realizado com sucesso", Snackbar.LENGTH_SHORT).show();
                    Intent mudarTela = new Intent(getApplicationContext(), FormLogin.class);
                    startActivity(mudarTela);
                } else {
                    Log.e(TAG, "Erro ao cadastrar usuário: " + response.message());
                    Snackbar.make(bt_cadastrar, "Erro ao cadastrar usuário", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Erro de conexão", t);
                Snackbar.make(bt_cadastrar, "Erro de conexão", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarComponentes() {
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_cadastrar = findViewById(R.id.bt_cadastrar);
    }
}
