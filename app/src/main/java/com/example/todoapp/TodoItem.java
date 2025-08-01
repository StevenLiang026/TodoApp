package com.example.todoapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.todoapp.model.TodoResponse;

public class TodoItem {
    public enum Priority {
        LOW("低"), NORMAL("普通"), HIGH("高"), URGENT("紧急");
        
        private String displayName;
        
        Priority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static Priority fromString(String priority) {
            if (priority == null) return NORMAL;
            switch (priority.toLowerCase()) {
                case "low": return LOW;
                case "high": return HIGH;
                case "urgent": return URGENT;
                case "normal":
                default: return NORMAL;
            }
        }
        
        public String toApiString() {
            return this.name().toLowerCase();
        }
    }
    
    private String text;
    private boolean isCompleted;
    private long id;
    private Priority priority;
    private Date createTime;
    private Date completeTime;
    private String category;
    
    private static final SimpleDateFormat API_DATE_FORMAT = 
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    public TodoItem(String text) {
        this.text = text;
        this.isCompleted = false;
        this.id = System.currentTimeMillis();
        this.priority = Priority.NORMAL;
        this.createTime = new Date();
        this.category = "默认";
    }
    
    public TodoItem(String text, Priority priority) {
        this.text = text;
        this.isCompleted = false;
        this.id = System.currentTimeMillis();
        this.priority = priority;
        this.createTime = new Date();
        this.category = "默认";
    }
    
    /**
     * 从服务器响应数据创建 TodoItem
     */
    public static TodoItem fromApiResponse(TodoResponse.TodoData data) {
        TodoItem item = new TodoItem(data.getText());
        item.setId(data.getId());
        item.setPriority(Priority.fromString(data.getPriority()));
        item.setCompleted(data.isCompleted());
        
        // 解析创建时间
        if (data.getCreateTime() != null) {
            try {
                item.setCreateTime(API_DATE_FORMAT.parse(data.getCreateTime()));
            } catch (ParseException e) {
                item.setCreateTime(new Date());
            }
        }
        
        // 解析完成时间
        if (data.getCompleteTime() != null) {
            try {
                item.setCompleteTime(API_DATE_FORMAT.parse(data.getCompleteTime()));
            } catch (ParseException e) {
                // 忽略解析错误
            }
        }
        
        return item;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed && completeTime == null) {
            this.completeTime = new Date();
        } else if (!completed) {
            this.completeTime = null;
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getCompleteTime() {
        return completeTime;
    }
    
    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getPriorityColor() {
        switch (priority) {
            case URGENT:
                return "#F44336"; // 红色
            case HIGH:
                return "#FF9800"; // 橙色
            case NORMAL:
                return "#4CAF50"; // 绿色
            case LOW:
                return "#9E9E9E"; // 灰色
            default:
                return "#4CAF50";
        }
    }
}