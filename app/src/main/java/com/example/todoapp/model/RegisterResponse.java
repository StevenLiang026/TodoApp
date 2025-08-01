package com.example.todoapp.model;

/**
 * 注册响应数据模型
 */
public class RegisterResponse {
    private boolean success;
    private String message;
    private RegisterData data;
    
    public RegisterResponse() {
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
    
    public RegisterData getData() {
        return data;
    }
    
    public void setData(RegisterData data) {
        this.data = data;
    }
    
    public static class RegisterData {
        private int userId;
        private String username;
        private String email;
        
        public RegisterData() {
            super();
            // 默认构造函数
        }
        
        public int getUserId() {
            return userId;
        }
        
        public void setUserId(int userId) {
            this.userId = userId;
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