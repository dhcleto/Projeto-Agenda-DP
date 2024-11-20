package com.example.projetoagendafirebase;

import java.util.List;

public class TarefaResponse {
    private boolean success; // Indica o sucesso ou falha da operação
    private String message; // Mensagem de retorno do servidor
    private List<Tarefa> tarefas; // Lista de tarefas, se aplicável

    // Getter e Setter para o campo de sucesso
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Getter e Setter para a mensagem
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter e Setter para a lista de tarefas
    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }
}
