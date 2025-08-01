package com.example.todoapp.model;

/**
 * 登录响应数据模型
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private LoginData data;
    
    public LoginResponse() {
        super();
        // 默认构造函数
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LoginData getData() {
        return data;
    }
    
    public void setData(LoginData data) {
        this.data = data;
    }
    
    public static class LoginData {
        private String token;
        private User user;
        
        public LoginData() {
            super();
            // 默认构造函数
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        public User getUser() {
            return user;
        }
        
        public void setUser(User user) {
            this.user = user;
        }
    }
    
    public static class User {
        private int id;
        private String username;
        private String email;
        
        public User() {
            super();
            // 默认构造函数
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}