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
    private EditText editNome, editEmail, editSenha;
    private Button btCadastrar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);

        getSupportActionBar().hide();
        iniciarComponentes();

        // Configure Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wn8tzr-3000.csb.app") // Update to your server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        btCadastrar.setOnClickListener(v -> {
            String nome = editNome.getText().toString();
            String email = editEmail.getText().toString();
            String senha = editSenha.getText().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                mostrarSnackBar(v, "Preencha todos os campos");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mostrarSnackBar(v, "Insira um e-mail válido");
                return;
            }

            try {
                // Encrypt the password before sending
                String senhaCriptografada = AESHelper.encrypt(senha);
                RegisterRequest registerRequest = new RegisterRequest(nome, email, senhaCriptografada);
                cadastrarUsuario(registerRequest);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao criptografar a senha", e);
                mostrarSnackBar(v, "Erro ao criptografar a senha");
            }
        });
    }

    private void cadastrarUsuario(RegisterRequest registerRequest) {
        apiService.register(registerRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Cadastro realizado com sucesso");
                    mostrarSnackBar(btCadastrar, "Cadastro realizado com sucesso");
                    startActivity(new Intent(getApplicationContext(), FormLogin.class));
                } else {
                    Log.e(TAG, "Erro ao cadastrar usuário: " + response.message());
                    mostrarSnackBar(btCadastrar, "Erro ao cadastrar usuário");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Erro de conexão", t);
                mostrarSnackBar(btCadastrar, "Erro de conexão");
            }
        });
    }

    private void iniciarComponentes() {
        editNome = findViewById(R.id.edit_nome);
        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        btCadastrar = findViewById(R.id.bt_cadastrar);
    }

    private void mostrarSnackBar(View v, String mensagem) {
        Snackbar snackbar = Snackbar.make(v, mensagem, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();
    }
}
