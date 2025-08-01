package com.example.todoapp.model;

/**
 * 注册响应数据模型 - 匹配后端API格式
 */
public class RegisterResponse {
    private String message;
    private String token;
    private User user;
    
    public RegisterResponse() {
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
