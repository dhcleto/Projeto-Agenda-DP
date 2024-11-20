package com.example.projetoagendafirebase;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // Rota para login
    @POST("/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);

    // Rota para registrar novo usuário
    @POST("/register")
    Call<ResponseBody> register(@Body RegisterRequest registerRequest);

    // Rota para listar tarefas de um usuário
    @GET("tarefas/{userId}")
    Call<TarefaResponse> listarTarefas(@Path("userId") int userId);

    // Rota para cadastrar uma nova tarefa
    @POST("tarefas")
    Call<TarefaResponse> cadastrarTarefa(@Body Tarefa tarefa);

    // Rota para buscar o userId pelo email
    @GET("/usuarios/{email}")
    Call<UserResponse> buscarUserIdPorEmail(@Path("email") String email);
}
