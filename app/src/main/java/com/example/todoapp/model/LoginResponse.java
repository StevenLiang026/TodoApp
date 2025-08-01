package com.example.todoapp.model;

/**
 * 登录响应数据模型 - 匹配后端API格式
 */
public class LoginResponse {
    private String message;
    private String token;
    private User user;
    
    public LoginResponse() {
        super();
        // 默认构造函数
    }
    
    // 为了兼容现有代码，添加isSuccess方法
    public boolean isSuccess() {
        return message != null && (message.contains("成功") || message.contains("success"));
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
    
    // 为了兼容现有代码，添加getData方法
    public LoginData getData() {
        if (token != null && user != null) {
            LoginData data = new LoginData();
            data.setToken(token);
            data.setUser(user);
            return data;
        }
        return null;
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
