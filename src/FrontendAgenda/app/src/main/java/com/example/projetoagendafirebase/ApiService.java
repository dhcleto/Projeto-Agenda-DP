package com.example.projetoagendafirebase;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("/login")
    Call<UserResponse> login(@Body LoginRequest loginRequest);

    @POST("/register")
    Call<ResponseBody> register(@Body RegisterRequest registerRequest);

    @GET("/tarefas/{userId}")
    Call<TarefaResponse> listarTarefas(@Path("userId") int userId);

    @POST("/tarefas")
    Call<TarefaResponse> cadastrarTarefa(@Body Tarefa tarefa); // Ajustado para TarefaResponse
}
