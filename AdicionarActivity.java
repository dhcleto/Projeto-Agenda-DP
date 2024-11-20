package com.example.projetoagendafirebase;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdicionarActivity extends AppCompatActivity {

    private EditText nomeTarefa;
    private Button selectDateButton, definirHorarioButton, adicionarParticipanteButton, adicionarTarefa;
    private TextView horarioTextView, calendarioTextView, listaParticipantes;
    private Calendar calendar;
    private List<String> participantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar);

        iniciarComponentes();

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

        adicionarParticipanteButton.setOnClickListener(v -> abrirDialogAdicionarParticipante());

        adicionarTarefa.setOnClickListener(v -> {
            String taskName = nomeTarefa.getText().toString().trim();
            String selectedDate = calendarioTextView.getText().toString();
            String selectedTime = horarioTextView.getText().toString();

            if (taskName.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tarefa adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                // Salve os dados da tarefa e participantes
            }
        });
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
