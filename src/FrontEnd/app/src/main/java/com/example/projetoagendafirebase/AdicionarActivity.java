package com.example.projetoagendafirebase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdicionarActivity extends AppCompatActivity {

    private EditText nomeTarefa;
    private Button selectDateButton, definirHorarioButton, adicionarParticipanteButton, adicionarTarefa;
    private TextView horarioTextView, calendarioTextView, listaParticipantes;
    private Calendar calendar;
    private List<String> participantes;
    private ApiService apiService; // Retrofit API Service
    private int userId; // ID do usuário logado

    private String repeticaoSelecionada = "Nenhuma"; // Valor padrão


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar);

        iniciarComponentes();

        // Obter o userId do usuário logado
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Erro: ID do usuário não foi recebido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wn8tzr-3000.csb.app") // Substitua pelo seu servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Configurar o seletor de data
        selectDateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AdicionarActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                        calendarioTextView.setText(selectedDate);

                        // Verificar se a data selecionada é um feriado
                        verificarFeriado(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });


        // Configurar o seletor de horário
        definirHorarioButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AdicionarActivity.this,
                    (view, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        horarioTextView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Adicionar participantes
        adicionarParticipanteButton.setOnClickListener(v -> abrirDialogAdicionarParticipante());

        // Adicionar tarefa
        adicionarTarefa.setOnClickListener(v -> {
            String taskName = nomeTarefa.getText().toString().trim();
            String selectedDate = calendarioTextView.getText().toString();
            String selectedTime = horarioTextView.getText().toString();

            if (taskName.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty() || participantes.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos e adicione participantes.", Toast.LENGTH_SHORT).show();
            } else {
                cadastrarTarefa(taskName, "Descrição da Tarefa", userId, selectedDate, selectedTime);
            }
        });
    }

    private void cadastrarTarefa(String taskName, String descricao, int userId, String selectedDate, String selectedTime) {
        // Criação da tarefa com os dados recebidos
        Tarefa tarefa = new Tarefa(taskName, descricao, userId, selectedDate, selectedTime);

        // Adiciona o valor de repetição à tarefa
        tarefa.setRepeticao(repeticaoSelecionada);

        // Log para depuração, verificando se os dados estão corretos antes de enviar ao backend
        Log.d("AdicionarActivity", "Cadastrando tarefa: Nome=" + taskName
                + ", Data=" + selectedDate
                + ", Hora=" + selectedTime
                + ", Repetição=" + repeticaoSelecionada);

        Call<TarefaResponse> call = apiService.cadastrarTarefa(tarefa);

        call.enqueue(new Callback<TarefaResponse>() {
            @Override
            public void onResponse(Call<TarefaResponse> call, Response<TarefaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TarefaResponse tarefaResponse = response.body();

                    // Confirmação de sucesso
                    if (tarefaResponse.isSuccess()) {
                        enviarNotificacaoPorEmail(taskName, selectedDate, selectedTime);
                        Toast.makeText(AdicionarActivity.this, "Tarefa cadastrada com sucesso!", Toast.LENGTH_SHORT).show();

                        // Configurar repetição da tarefa, se aplicável
                        configurarRepeticaoTarefa(taskName, selectedDate, selectedTime, repeticaoSelecionada);

                        // Finaliza a activity e retorna à tela principal
                        finish();
                    } else {
                        // Tratamento de erro vindo do backend
                        Toast.makeText(AdicionarActivity.this, "Erro ao cadastrar tarefa no servidor.", Toast.LENGTH_SHORT).show();
                        Log.e("AdicionarActivity", "Erro do servidor: " + tarefaResponse.getMessage());
                    }
                } else {
                    // Tratamento de erro genérico
                    Toast.makeText(AdicionarActivity.this, "Erro ao cadastrar tarefa. Tente novamente.", Toast.LENGTH_SHORT).show();
                    Log.e("AdicionarActivity", "Erro na resposta: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<TarefaResponse> call, Throwable t) {
                // Tratamento de erro de conexão
                Toast.makeText(AdicionarActivity.this, "Falha de conexão ao cadastrar tarefa.", Toast.LENGTH_SHORT).show();
                Log.e("AdicionarActivity", "Erro de conexão: " + t.getMessage());
            }
        });
    }

    private void configurarRepeticaoTarefa(String taskName, String date, String time, String repeticao) {
        if (repeticao.equals("Nenhuma")) {
            return; // Sem repetição
        }

        String mensagemRepeticao = "Tarefa repetida: " + repeticao + "\n" +
                "Nome: " + taskName + "\n" +
                "Data: " + date + "\n" +
                "Hora: " + time;

        // Exemplo de log para depuração
        Log.d("Repetição", mensagemRepeticao);

        // Caso você queira usar AlarmManager ou WorkManager, configure aqui
    }



    private void enviarNotificacaoPorEmail(String taskName, String date, String time) {
        new Thread(() -> {
            try {
                // Configurações do servidor SMTP
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                // Autenticação do e-mail
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("suagendapreferida@gmail.com", "drwk bxsv epgm goco");
                    }
                });

                // Configuração da mensagem
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress("suagendapreferida@gmail.com"));
                message.setSubject("Nova Tarefa: " + taskName);
                message.setText("Detalhes da tarefa:\n\n"
                        + "Nome: " + taskName + "\n"
                        + "Data: " + date + "\n"
                        + "Hora: " + time);

                // Enviar para cada participante
                for (String email : participantes) {
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    Transport.send(message);
                    Log.d("Email", "Notificação enviada para " + email);
                }
            } catch (Exception e) {
                Log.e("Email", "Erro ao enviar e-mail", e);
            }
        }).start();
    }



    private void abrirDialogAdicionarParticipante() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Participante");

        final EditText input = new EditText(this);
        input.setHint("E-mail do participante");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        builder.setView(input);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "E-mail inválido.", Toast.LENGTH_SHORT).show();
            } else {
                participantes.add(email);
                listaParticipantes.setText("Participantes:\n" + String.join("\n", participantes));
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void iniciarComponentes() {
        nomeTarefa = findViewById(R.id.nomeTarefa);
        selectDateButton = findViewById(R.id.selecionarData);
        definirHorarioButton = findViewById(R.id.definirHorario);
        adicionarParticipanteButton = findViewById(R.id.adicionarParticipanteButton);
        adicionarTarefa = findViewById(R.id.adicionaTarefa);
        calendarioTextView = findViewById(R.id.calendarioTextView);
        horarioTextView = findViewById(R.id.horarioTextView);
        listaParticipantes = findViewById(R.id.listaParticipantes);
        calendar = Calendar.getInstance();
        participantes = new ArrayList<>();
        Spinner spinnerRepeticao = findViewById(R.id.spinnerRepeticao);

// Configurar Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeticao_opcoes,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepeticao.setAdapter(adapter);

// Listener para capturar a repetição selecionada
        spinnerRepeticao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeticaoSelecionada = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                repeticaoSelecionada = "Nenhuma"; // Valor padrão
            }
        });

    }
    private void verificarFeriado(String selectedDate) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://wn8tzr-3000.csb.app/feriados/2024"; // Atualize o ano conforme necessário

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    // Interpretar o JSON como um JSONObject
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // Verificar se o campo "success" está true
                    if (jsonResponse.getBoolean("success")) {
                        // Obter o array de feriados
                        JSONArray feriadosArray = jsonResponse.getJSONArray("feriados");
                        boolean isFeriado = false;
                        String nomeFeriado = "";

                        // Percorrer os feriados para encontrar correspondência com a data
                        for (int i = 0; i < feriadosArray.length(); i++) {
                            JSONObject feriado = feriadosArray.getJSONObject(i);
                            String data = feriado.getString("date");
                            String nome = feriado.getString("name");

                            if (data.equals(selectedDate)) {
                                isFeriado = true;
                                nomeFeriado = nome;
                                break;
                            }
                        }

                        // Atualizar a UI com a mensagem apropriada
                        if (isFeriado) {
                            String mensagem = "A data selecionada é um feriado: " + nomeFeriado;
                            runOnUiThread(() -> Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, "A data selecionada não é um feriado.", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao verificar feriados. Resposta inválida.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("Feriados", "Erro na API de feriados: " + response.code());
                    runOnUiThread(() -> Toast.makeText(this, "Erro ao verificar feriados. Tente novamente.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("Feriados", "Erro ao verificar feriado: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Erro ao verificar feriados. Tente novamente.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }



}
