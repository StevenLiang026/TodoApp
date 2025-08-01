package com.example.todoapp.api;

import com.example.todoapp.model.LoginRequest;
import com.example.todoapp.model.LoginResponse;
import com.example.todoapp.model.RegisterRequest;
import com.example.todoapp.model.RegisterResponse;
import com.example.todoapp.model.TodoRequest;
import com.example.todoapp.model.TodoResponse;
import com.example.todoapp.model.TodoListResponse;
import com.example.todoapp.model.UpdateTodoRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API 服务接口定义
 */
public interface ApiService {
    
    /**
     * 用户注册
     */
    @POST("api/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
    
    /**
     * 用户登录
     */
    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    
    /**
     * 新增笔记
     */
    @POST("api/todos")
    Call<TodoResponse> createTodo(
            @Header("Authorization") String token,
            @Body TodoRequest request
    );
    
    /**
     * 查询笔记列表
     */
    @GET("api/todos")
    Call<TodoListResponse> getTodos(
            @Header("Authorization") String token,
            @Query("completed") Boolean completed,
            @Query("priority") String priority,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );
    
    /**
     * 更新笔记
     */
    @PUT("api/todos/{id}")
    Call<TodoResponse> updateTodo(
            @Header("Authorization") String token,
            @Path("id") int todoId,
            @Body UpdateTodoRequest request
    );
    
    /**
     * 删除笔记
     */
    @DELETE("api/todos/{id}")
    Call<Void> deleteTodo(
            @Header("Authorization") String token,
            @Path("id") int todoId
    );
}