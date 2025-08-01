package com.example.todoapp.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API 客户端管理类
 */
public class ApiClient {
    // Vercel 部署地址
    private static final String BASE_URL = "https://todo-app-backend-2gp1068s5-tavianliangs-projects.vercel.app/";
    
    // 本地开发地址（可选）
    // private static final String BASE_URL = "http://10.0.2.2:3000/"; // Android 模拟器
    // private static final String BASE_URL = "http://192.168.1.100:3000/"; // 真机测试（替换为你的电脑IP）
    
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    
    /**
     * 获取 Retrofit 实例
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // 创建日志拦截器
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // 创建 OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            
            // 创建 Retrofit 实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 获取 API 服务实例
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
    
    /**
     * 格式化 JWT Token
     */
    public static String formatToken(String token) {
        if (token != null && !token.startsWith("Bearer ")) {
            return "Bearer " + token;
        }
        return token;
    }
}