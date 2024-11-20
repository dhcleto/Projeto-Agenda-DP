package com.example.projetoagendafirebase;

public class Tarefa {
    private int id;
    private String nome;
    private String descricao;
    private int userId;
    private String data;
    private String hora;

    // Construtor vazio (necessário para algumas operações do Retrofit)
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
