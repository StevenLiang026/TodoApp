package com.example.todoapp.model;

import java.util.List;

/**
 * Todo 列表响应数据模型
 */
public class TodoListResponse {
    private boolean success;
    private String message;
    private TodoListData data;
    
    public TodoListResponse() {
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
    
    public TodoListData getData() {
        return data;
    }
    
    public void setData(TodoListData data) {
        this.data = data;
    }
    
    public static class TodoListData {
        private List<TodoResponse.TodoData> todos;
        private int total;
        private int page;
        private int limit;
        
        public TodoListData() {
            super();
            // 默认构造函数
        }
        
        public List<TodoResponse.TodoData> getTodos() {
            return todos;
        }
        
        public void setTodos(List<TodoResponse.TodoData> todos) {
            this.todos = todos;
        }
        
        public int getTotal() {
            return total;
        }
        
        public void setTotal(int total) {
            this.total = total;
        }
        
        public int getPage() {
            return page;
        }
        
        public void setPage(int page) {
            this.page = page;
        }
        
        public int getLimit() {
            return limit;
        }
        
        public void setLimit(int limit) {
            this.limit = limit;
        }
    }
}