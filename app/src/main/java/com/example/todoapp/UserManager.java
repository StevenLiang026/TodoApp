package com.example.todoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {
    private static final String PREF_NAME = "TodoAppUsers";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    
    public UserManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * 注册新用户
     */
    public boolean registerUser(String username, String password, String email) {
        // 检查用户名是否已存在
        if (isUserExists(username)) {
            return false;
        }
        
        // 验证输入
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
            return false;
        }
        
        // 加密密码
        String hashedPassword = hashPassword(password);
        
        // 保存用户信息
        editor.putString("user_" + username + "_password", hashedPassword);
        editor.putString("user_" + username + "_email", email);
        editor.apply();
        
        return true;
    }
    
    /**
     * 用户登录
     */
    public boolean loginUser(String username, String password) {
        if (!isUserExists(username)) {
            return false;
        }
        
        String storedPassword = sharedPreferences.getString("user_" + username + "_password", "");
        String hashedPassword = hashPassword(password);
        
        if (storedPassword.equals(hashedPassword)) {
            // 登录成功，保存当前用户状态
            editor.putString(KEY_CURRENT_USER, username);
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.apply();
            return true;
        }
        
        return false;
    }
    
    /**
     * 用户登出
     */
    public void logoutUser() {
        editor.remove(KEY_CURRENT_USER);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
    
    /**
     * 检查用户是否已登录
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * 获取当前登录用户
     */
    public String getCurrentUser() {
        return sharedPreferences.getString(KEY_CURRENT_USER, "");
    }
    
    /**
     * 检查用户是否存在
     */
    private boolean isUserExists(String username) {
        return sharedPreferences.contains("user_" + username + "_password");
    }
    
    /**
     * 密码哈希加密
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // 如果加密失败，返回原密码（不推荐在生产环境中使用）
        }
    }
    
    /**
     * 获取用户邮箱
     */
    public String getUserEmail(String username) {
        return sharedPreferences.getString("user_" + username + "_email", "");
    }
}