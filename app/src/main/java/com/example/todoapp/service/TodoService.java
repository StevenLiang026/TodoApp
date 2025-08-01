package com.example.todoapp.service;

import android.content.Context;
import android.util.Log;

import com.example.todoapp.TodoItem;
import com.example.todoapp.api.ApiClient;
import com.example.todoapp.api.ApiService;
import com.example.todoapp.model.LoginRequest;
import com.example.todoapp.model.LoginResponse;
import com.example.todoapp.model.RegisterRequest;
import com.example.todoapp.model.RegisterResponse;
import com.example.todoapp.model.TodoListResponse;
import com.example.todoapp.model.TodoRequest;
import com.example.todoapp.model.TodoResponse;
import com.example.todoapp.model.UpdateTodoRequest;
import com.example.todoapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Todo 服务管理类
 * 处理所有与服务器的网络交互
 */
public class TodoService {
    private static final String TAG = "TodoService";
    
    private ApiService apiService;
    private SessionManager sessionManager;
    
    public TodoService(Context context) {
        super();
        this.apiService = ApiClient.getApiService();
        this.sessionManager = new SessionManager(context);
    }
    
    /**
     * 用户注册回调接口
     */
    public interface RegisterCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    /**
     * 用户登录回调接口
     */
    public interface LoginCallback {
        void onSuccess(LoginResponse.LoginData loginData);
        void onError(String error);
    }
    
    /**
     * Todo 操作回调接口
     */
    public interface TodoCallback {
        void onSuccess(TodoItem todoItem);
        void onError(String error);
    }
    
    /**
     * Todo 列表回调接口
     */
    public interface TodoListCallback {
        void onSuccess(List<TodoItem> todoItems);
        void onError(String error);
    }
    
    /**
     * 删除回调接口
     */
    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
    
    /**
     * 用户注册
     */
    public void register(String username, String email, String password, RegisterCallback callback) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        
        Log.d(TAG, "开始注册请求 - 用户名: " + username + ", 邮箱: " + email);
        
        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                Log.d(TAG, "注册响应 - HTTP状态码: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    Log.d(TAG, "注册响应内容 - message: " + registerResponse.getMessage() + 
                              ", token: " + (registerResponse.getToken() != null ? "存在" : "null") +
                              ", user: " + (registerResponse.getUser() != null ? "存在" : "null"));
                    
                    // 检查是否有token，如果有token说明注册成功
                    if (registerResponse.getToken() != null && !registerResponse.getToken().isEmpty()) {
                        callback.onSuccess(registerResponse.getMessage());
                    } else {
                        callback.onError(registerResponse.getMessage() != null ? registerResponse.getMessage() : "注册失败");
                    }
                } else {
                    Log.e(TAG, "注册失败 - HTTP状态码: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "无错误详情";
                        Log.e(TAG, "错误响应内容: " + errorBody);
                        callback.onError("注册失败：服务器错误 (状态码: " + response.code() + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "读取错误响应失败", e);
                        callback.onError("注册失败：服务器错误");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e(TAG, "注册请求失败", t);
                callback.onError("网络连接失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 用户登录
     */
    public void login(String username, String password, LoginCallback callback) {
        LoginRequest request = new LoginRequest(username, password);
        
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        LoginResponse.LoginData loginData = loginResponse.getData();
                        // 保存登录信息到本地
                        sessionManager.saveLoginSession(
                                loginData.getToken(),
                                loginData.getUser().getId(),
                                loginData.getUser().getUsername(),
                                loginData.getUser().getEmail()
                        );
                        callback.onSuccess(loginData);
                    } else {
                        callback.onError(loginResponse.getMessage());
                    }
                } else {
                    callback.onError("登录失败：服务器错误");
                }
            }
            
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "登录请求失败", t);
                callback.onError("网络连接失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 创建新的 Todo
     */
    public void createTodo(String text, TodoItem.Priority priority, TodoCallback callback) {
        String token = sessionManager.getFormattedToken();
        if (token == null) {
            callback.onError("用户未登录");
            return;
        }
        
        TodoRequest request = new TodoRequest(text, priority.toApiString());
        
        apiService.createTodo(token, request).enqueue(new Callback<TodoResponse>() {
            @Override
            public void onResponse(Call<TodoResponse> call, Response<TodoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TodoResponse todoResponse = response.body();
                    if (todoResponse.isSuccess()) {
                        TodoItem todoItem = TodoItem.fromApiResponse(todoResponse.getData());
                        callback.onSuccess(todoItem);
                    } else {
                        callback.onError(todoResponse.getMessage());
                    }
                } else {
                    callback.onError("创建笔记失败：服务器错误");
                }
            }
            
            @Override
            public void onFailure(Call<TodoResponse> call, Throwable t) {
                Log.e(TAG, "创建笔记请求失败", t);
                callback.onError("网络连接失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 获取 Todo 列表
     */
    public void getTodos(TodoListCallback callback) {
        String token = sessionManager.getFormattedToken();
        if (token == null) {
            callback.onError("用户未登录");
            return;
        }
        
        apiService.getTodos(token, null, null, null, null).enqueue(new Callback<TodoListResponse>() {
            @Override
            public void onResponse(Call<TodoListResponse> call, Response<TodoListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TodoListResponse listResponse = response.body();
                    if (listResponse.isSuccess()) {
                        List<TodoItem> todoItems = new ArrayList<>();
                        for (TodoResponse.TodoData data : listResponse.getData().getTodos()) {
                            todoItems.add(TodoItem.fromApiResponse(data));
                        }
                        callback.onSuccess(todoItems);
                    } else {
                        callback.onError(listResponse.getMessage());
                    }
                } else {
                    callback.onError("获取笔记列表失败：服务器错误");
                }
            }
            
            @Override
            public void onFailure(Call<TodoListResponse> call, Throwable t) {
                Log.e(TAG, "获取笔记列表请求失败", t);
                callback.onError("网络连接失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 更新 Todo
     */
    public void updateTodo(int todoId, String text, TodoItem.Priority priority, boolean isCompleted, TodoCallback callback) {
        String token = sessionManager.getFormattedToken();
        if (token == null) {
            callback.onError("用户未登录");
            return;
        }
        
        UpdateTodoRequest request = new UpdateTodoRequest(text, priority.toApiString(), isCompleted);
        
        apiService.updateTodo(token, todoId, request).enqueue(new Callback<TodoResponse>() {
            @Override
            public void onResponse(Call<TodoResponse> call, Response<TodoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TodoResponse todoResponse = response.body();
                    if (todoResponse.isSuccess()) {
                        TodoItem todoItem = TodoItem.fromApiResponse(todoResponse.getData());
                        callback.onSuccess(todoItem);
                    } else {
                        callback.onError(todoResponse.getMessage());
                    }
                } else {
                    callback.onError("更新笔记失败：服务器错误");
                }
            }
            
            @Override
            public void onFailure(Call<TodoResponse> call, Throwable t) {
                Log.e(TAG, "更新笔记请求失败", t);
                callback.onError("网络连接失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 删除 Todo
     */
    public void deleteTodo(int todoId, DeleteCallback callback) {
        String token = sessionManager.getFormattedToken();
        if (token == null) {
            callback.onError("用户未登录");
            return;
        }
        
        apiService.deleteTodo(token, todoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("删除笔记失败：服务器错误");
                }
            }
            
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "删除笔记请求失败", t);
                callback.onError("网络连接失败：" + t.getMessage());
            }
        });
    }
    
    /**
     * 用户退出登录
     */
    public void logout() {
        sessionManager.logout();
    }
    
    /**
     * 检查用户是否已登录
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
    
    /**
     * 获取当前用户名
     */
    public String getCurrentUsername() {
        return sessionManager.getUsername();
    }
}