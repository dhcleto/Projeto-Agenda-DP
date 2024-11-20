package com.example.projetoagendafirebase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
                        calendarioTextView.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime()));
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
        Tarefa tarefa = new Tarefa(taskName, descricao, userId, selectedDate, selectedTime);

        Call<TarefaResponse> call = apiService.cadastrarTarefa(tarefa);
        call.enqueue(new Callback<TarefaResponse>() {
            @Override
            public void onResponse(Call<TarefaResponse> call, Response<TarefaResponse> response) {
                if (response.isSuccessful()) {
                    enviarNotificacaoPorEmail(taskName, selectedDate, selectedTime);
                    Toast.makeText(AdicionarActivity.this, "Tarefa cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdicionarActivity.this, "Erro ao cadastrar tarefa.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TarefaResponse> call, Throwable t) {
                Toast.makeText(AdicionarActivity.this, "Falha de conexão ao cadastrar tarefa.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarNotificacaoPorEmail(String taskName, String date, String time) {
        String assunto = "Nova Tarefa: " + taskName;
        String mensagem = "Detalhes da tarefa:\n\n" +
                "Nome: " + taskName + "\n" +
                "Data: " + date + "\n" +
                "Hora: " + time;

        for (String email : participantes) {
            new Thread(() -> {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");

                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication("suagendapreferida@gmail.com", "drwk bxsv epgm goco");
                        }
                    });

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress("agendafirebase1@gmail.com"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    message.setSubject(assunto);
                    message.setText(mensagem);

                    Transport.send(message);
                    Log.d("Email", "Notificação enviada para " + email);
                } catch (Exception e) {
                    Log.e("Email", "Erro ao enviar e-mail para " + email, e);
                }
            }).start();
        }
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
    }
}
