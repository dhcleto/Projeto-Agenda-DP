package com.example.projetoagendafirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdicionarActivity extends AppCompatActivity {
    private EditText nomeTarefa;
    private Button selectDateButton, definirHorarioButton, adicionarTarefa;
    private Calendar calendar;

    // Elementos de data e hora
    private TextView horarioTextView, calendarioTextView;

    // Firebase Firestore e Auth
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar);

        // Inicializar os componentes da interface
        IniciarComponentes();

        // Inicializar o calendário
        calendar = Calendar.getInstance();

        // Inicializar Firebase Firestore e Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Diálogo para seleção de data
        selectDateButton.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AdicionarActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year1);
                        calendar.set(Calendar.MONTH, month1);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String selectedDate = sdf.format(calendar.getTime());
                        calendarioTextView.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Diálogo para seleção de horário
        definirHorarioButton.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AdicionarActivity.this,
                    (view, hourOfDay, minute1) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute1);

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String selectedTime = sdf.format(calendar.getTime());
                        horarioTextView.setText(selectedTime);
                    },
                    hour, minute, true);
            timePickerDialog.show();
        });

        // Ação de adicionar tarefa e salvar no Firestore
        adicionarTarefa.setOnClickListener(v -> {
            String taskName = nomeTarefa.getText().toString().trim();
            String selectedDate = calendarioTextView.getText().toString();
            String selectedTime = horarioTextView.getText().toString();

            if (!taskName.isEmpty() && !selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                // Obter o usuário atual
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    // Criar o mapa da tarefa para salvar no Firestore
                    Map<String, Object> taskData = new HashMap<>();
                    taskData.put("nomeTarefa", taskName);
                    taskData.put("data", selectedDate);
                    taskData.put("hora", selectedTime);

                    // Adicionar a tarefa ao Firestore no documento do usuário logado
                    db.collection("Usuarios")
                            .document(userId)
                            .collection("Tarefas")
                            .add(taskData)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AdicionarActivity.this, "Tarefa adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                                finish(); // Voltar para a tela principal
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AdicionarActivity.this, "Erro ao adicionar tarefa", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(AdicionarActivity.this, "Por favor, insira o nome, data e hora da tarefa", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void IniciarComponentes() { // Mudado para void
        nomeTarefa = findViewById(R.id.nomeTarefa);
        selectDateButton = findViewById(R.id.selecionarData);
        definirHorarioButton = findViewById(R.id.definirHorario);
        adicionarTarefa = findViewById(R.id.adicionaTarefa);
        calendarioTextView = findViewById(R.id.calendarioTextView);
        horarioTextView = findViewById(R.id.horarioTextView);
    }
}
