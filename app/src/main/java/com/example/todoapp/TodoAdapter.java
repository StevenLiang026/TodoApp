package com.example.todoapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends BaseAdapter {
    
    public interface OnTodoItemClickListener {
        void onDeleteClick(int position);
        void onCheckboxClick(int position, boolean isChecked);
    }
    
    private Context context;
    private List<TodoItem> todoItems;
    private OnTodoItemClickListener listener;
    private SimpleDateFormat dateFormat;

    public TodoAdapter(Context context, List<TodoItem> todoItems) {
        this.context = context;
        this.todoItems = todoItems;
        this.dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    }
    
    public void setOnTodoItemClickListener(OnTodoItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return todoItems.size();
    }

    @Override
    public Object getItem(int position) {
        return todoItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return todoItems.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.todo_item, parent, false);
            holder = new ViewHolder();
            holder.checkBoxCompleted = convertView.findViewById(R.id.checkBoxCompleted);
            holder.textViewTodo = convertView.findViewById(R.id.textViewTodo);
            holder.textViewPriority = convertView.findViewById(R.id.textViewPriority);
            holder.textViewTime = convertView.findViewById(R.id.textViewTime);
            holder.buttonDelete = convertView.findViewById(R.id.buttonDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final TodoItem item = todoItems.get(position);
        
        // 设置复选框状态
        holder.checkBoxCompleted.setChecked(item.isCompleted());
        holder.checkBoxCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCheckboxClick(position, ((CheckBox) v).isChecked());
                }
            }
        });
        
        // 设置任务文本
        holder.textViewTodo.setText(item.getText());
        
        // 根据完成状态设置文本样式
        if (item.isCompleted()) {
            holder.textViewTodo.setPaintFlags(holder.textViewTodo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewTodo.setTextColor(Color.GRAY);
        } else {
            holder.textViewTodo.setPaintFlags(holder.textViewTodo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textViewTodo.setTextColor(Color.BLACK);
        }
        
        // 设置优先级显示
        holder.textViewPriority.setText(item.getPriority().getDisplayName());
        holder.textViewPriority.setTextColor(Color.parseColor(item.getPriorityColor()));
        
        // 设置时间显示
        String timeText = "创建: " + dateFormat.format(item.getCreateTime());
        if (item.isCompleted() && item.getCompleteTime() != null) {
            timeText += " | 完成: " + dateFormat.format(item.getCompleteTime());
        }
        holder.textViewTime.setText(timeText);
        
        // 设置删除按钮
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });
        
        return convertView;
    }
    
    private static class ViewHolder {
        CheckBox checkBoxCompleted;
        TextView textViewTodo;
        TextView textViewPriority;
        TextView textViewTime;
        Button buttonDelete;
        
        public ViewHolder() {
            super();
        }
    }
}