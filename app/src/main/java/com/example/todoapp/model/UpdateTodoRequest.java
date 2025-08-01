package com.example.todoapp.model;

/**
 * 更新笔记请求数据模型
 */
public class UpdateTodoRequest {
    private String text;
    private String priority;
    private Boolean isCompleted;
    
    public UpdateTodoRequest() {
    }
    
    public UpdateTodoRequest(String text, String priority, Boolean isCompleted) {
        this.text = text;
        this.priority = priority;
        this.isCompleted = isCompleted;
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
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}