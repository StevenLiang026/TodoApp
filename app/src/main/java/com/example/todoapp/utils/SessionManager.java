package com.example.todoapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户会话管理类
 * 用于管理用户登录状态和 Token
 */
public class SessionManager {
    private static final String PREF_NAME = "TodoAppSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    /**
     * 保存用户登录信息
     */
    public void saveLoginSession(String token, int userId, String username, String email) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }
    
    /**
     * 获取用户 Token
     */
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }
    
    /**
     * 获取用户 ID
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * 获取用户名
     */
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }
    
    /**
     * 获取用户邮箱
     */
    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }
    
    /**
     * 检查用户是否已登录
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 清除用户会话（退出登录）
     */
    public void logout() {
        editor.clear();
        editor.commit();
    }
    
    /**
     * 获取格式化的 Token（带 Bearer 前缀）
     */
    public String getFormattedToken() {
        String token = getToken();
        if (token != null && !token.startsWith("Bearer ")) {
            return "Bearer " + token;
        }
        return token;
    }
}