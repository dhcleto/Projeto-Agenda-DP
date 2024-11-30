package com.example.projetoagendafirebase;

public class UserResponse {
    private boolean success;
    private User user;

    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }

    public class User {
        private int id;
        private String nome;
        private String email;

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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
