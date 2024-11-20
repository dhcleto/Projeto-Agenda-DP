package com.example.projetoagendafirebase;

public class Tarefa {
    private int id; // Identificador único da tarefa
    private String nome; // Nome da tarefa
    private String descricao; // Descrição da tarefa
    private int userId; // ID do usuário associado à tarefa
    private String data; // Data da tarefa no formato "dd-MM-yyyy"
    private String hora; // Hora da tarefa no formato "HH:mm"

    // Construtor vazio (necessário para Retrofit e frameworks similares)
    public Tarefa() {}

    // Construtor completo
    public Tarefa(String nome, String descricao, int userId, String data, String hora) {
        this.nome = nome;
        this.descricao = descricao;
        this.userId = userId;
        this.data = data;
        this.hora = hora;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
