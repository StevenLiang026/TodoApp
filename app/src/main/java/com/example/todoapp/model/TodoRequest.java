package com.example.todoapp.model;

/**
 * 新增笔记请求数据模型
 */
public class TodoRequest {
    private String text;
    private String priority;
    
    public TodoRequest(String text, String priority) {
        this.text = text;
        this.priority = priority;
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
}