package com.example.todoapp.model;

/**
 * Todo 响应数据模型
 */
public class TodoResponse {
    private boolean success;
    private String message;
    private TodoData data;
    
    public TodoResponse() {
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
    
    public TodoData getData() {
        return data;
    }
    
    public void setData(TodoData data) {
        this.data = data;
    }
    
    public static class TodoData {
        private int id;
        private String text;
        private String priority;
        private String priorityColor;
        private boolean isCompleted;
        private String createTime;
        private String completeTime;
        
        public TodoData() {
            super();
            // 默认构造函数
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getPriority() {
            return priority;
        }
        
        public void setPriority(String priority) {
            this.priority = priority;
        }
        
        public String getPriorityColor() {
            return priorityColor;
        }
        
        public void setPriorityColor(String priorityColor) {
            this.priorityColor = priorityColor;
        }
        
        public boolean isCompleted() {
            return isCompleted;
        }
        
        public void setCompleted(boolean completed) {
            isCompleted = completed;
        }
        
        public String getCreateTime() {
            return createTime;
        }
        
        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }
        
        public String getCompleteTime() {
            return completeTime;
        }
        
        public void setCompleteTime(String completeTime) {
            this.completeTime = completeTime;
        }
    }
}