package com.example.projetoagendafirebase;

import java.util.List;

public class TarefaResponse {
    private boolean success;
    private List<Tarefa> tarefas;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }
}
