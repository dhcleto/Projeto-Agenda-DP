package com.example.projetoagendafirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView nomeUsuario, emailUsuario;

    private Button bt_criartarefa;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tarefasView;


    String usuarioID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();


        nomeUsuario = findViewById(R.id.textNomeUsuario);
        emailUsuario = findViewById(R.id.textEmailUsuario);

        bt_criartarefa = findViewById(R.id.bt_criartarefa);
        tarefasView = findViewById(R.id.tarefastodo);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bt_criartarefa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AdicionarActivity.class));
            }
        });

        loadTasks();
    }

    @Override
    protected void onStart() {
        super.onStart();

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null){
                    nomeUsuario.setText(documentSnapshot.getString("nome"));
                    emailUsuario.setText(email);
                }

            }
        });
    }

    private void loadTasks() {

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("Usuarios")
                    .document(userId)
                    .collection("Tarefas")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            StringBuilder tarefasBuilder = new StringBuilder();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String tarefa = document.getString("nomeTarefa");
                                tarefasBuilder.append("Tarefa: ").append(tarefa).append("\n");
                            }
                            tarefasView.setText(tarefasBuilder.toString());
                        }
                    });
        }
    }
}


