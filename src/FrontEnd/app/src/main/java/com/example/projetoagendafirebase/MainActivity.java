package com.example.projetoagendafirebase;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView nomeUsuario, emailTextView, tarefasView;
    private Button btCriarTarefa;
    private ApiService apiService;  // Retrofit API Service
    private String email;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        nomeUsuario = findViewById(R.id.textNomeUsuario);
        emailTextView = findViewById(R.id.textEmailUsuario);
        tarefasView = findViewById(R.id.tarefastodo);
        btCriarTarefa = findViewById(R.id.bt_criartarefa);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wn8tzr--3000.csb.app/") // URL do servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Receber os dados passados da FormLogin
        userId = getIntent().getIntExtra("userId", -1);
        email = getIntent().getStringExtra("email");
        String nome = getIntent().getStringExtra("nome");

        // Validar se os dados foram recebidos corretamente
        if (userId == -1 || email == null || nome == null) {
            Log.e("MainActivity", "Erro: Dados do usuário não recebidos corretamente.");
            finish();  // Finaliza a Activity em caso de erro para evitar problemas
            return;
        }

        // Atualizar a interface com os dados do usuário
        nomeUsuario.setText(nome);
        emailTextView.setText(email);

        // Carregar as tarefas do usuário
        carregarTarefas(userId);

        // Ao clicar no botão de criar tarefa, abrir a activity de adicionar tarefa
        btCriarTarefa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdicionarActivity.class);
                intent.putExtra("userId", userId);  // Aqui passamos o userId
                startActivity(intent);
            }
        });
    }

    // Método para carregar as tarefas associadas ao usuário
    private void carregarTarefas(int userId) {
        Log.d("MainActivity", "Carregando tarefas para o userId: " + userId);
        Call<TarefaResponse> call = apiService.listarTarefas(userId); // Agora passamos o userId

        call.enqueue(new Callback<TarefaResponse>() {
            @Override
            public void onResponse(Call<TarefaResponse> call, Response<TarefaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TarefaResponse tarefaResponse = response.body();

                    // Verificar se o sucesso é verdadeiro e há tarefas
                    if (tarefaResponse.isSuccess()) {
                        List<Tarefa> tarefas = tarefaResponse.getTarefas();
                        if (tarefas != null && !tarefas.isEmpty()) {
                            StringBuilder tarefasBuilder = new StringBuilder();
                            for (Tarefa tarefa : tarefas) {
                                tarefasBuilder.append("Tarefa: ").append(tarefa.getNome())
                                        .append(" - Data: ").append(tarefa.getData())
                                        .append(" - Hora: ").append(tarefa.getHora())
                                        .append(" - Repetição: ").append(tarefa.getRepeticao()) // Incluindo repetição
                                        .append("\n");
                            }
                            tarefasView.setText(tarefasBuilder.toString()); // Exibir tarefas no TextView
                        } else {
                            tarefasView.setText("Nenhuma tarefa encontrada.");
                            Log.d("MainActivity", "Nenhuma tarefa foi encontrada.");
                        }

                    } else {
                        tarefasView.setText("Erro ao carregar tarefas.");
                        Log.e("MainActivity", "Erro: Sucesso falso na resposta.");
                    }
                } else {
                    tarefasView.setText("Erro ao carregar tarefas do servidor.");
                    Log.e("MainActivity", "Erro ao carregar tarefas. Resposta: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<TarefaResponse> call, Throwable t) {
                tarefasView.setText("Erro de conexão ao carregar tarefas.");
                Log.e("MainActivity", "Erro de conexão: " + t.getMessage());
            }
        });
    }
}
