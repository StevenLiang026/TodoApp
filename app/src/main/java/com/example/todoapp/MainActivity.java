package com.example.todoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.todoapp.service.TodoService;
import com.example.todoapp.utils.SessionManager;

public class MainActivity extends Activity implements TodoAdapter.OnTodoItemClickListener {
    
    private EditText editTextTodo;
    private EditText editTextSearch;
    private Button buttonAdd;
    private Button buttonMenu;
    private Button buttonFilter;
    private Button buttonSort;
    private Button buttonSearch;
    private Button buttonStats;
    private Button buttonExport;
    private Spinner spinnerPriority;
    private ListView listViewTodos;
    private TextView textViewEmpty;
    private TextView textViewWelcome;
    private TextView textViewStats;
    
    private List<TodoItem> todoItems;
    private List<TodoItem> filteredTodoItems;
    private TodoAdapter adapter;
    private SessionManager sessionManager;
    private TodoService todoService;
    
    // 筛选和排序状态
    private String currentFilter = "全部";
    private String currentSort = "默认";
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        todoService = new TodoService(this);
        
        // 检查用户是否已登录
        if (!sessionManager.isLoggedIn()) {
            startLoginActivity();
            return;
        }
        
        setContentView(R.layout.activity_main);
        
        initViews();
        initData();
        setupListeners();
        updateWelcomeMessage();
        loadTodoItems();
        updateStats();
    }

    private void initViews() {
        editTextTodo = findViewById(R.id.editTextTodo);
        editTextSearch = findViewById(R.id.editTextSearch);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonMenu = findViewById(R.id.buttonMenu);
        buttonFilter = findViewById(R.id.buttonFilter);
        buttonSort = findViewById(R.id.buttonSort);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonStats = findViewById(R.id.buttonStats);
        buttonExport = findViewById(R.id.buttonExport);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        listViewTodos = findViewById(R.id.listViewTodos);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewStats = findViewById(R.id.textViewStats);
        
        // 设置优先级下拉框
        setupPrioritySpinner();
    }

    private void setupPrioritySpinner() {
        String[] priorities = {"普通", "低", "高", "紧急"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(adapter);
    }

    private void initData() {
        todoItems = new ArrayList<>();
        filteredTodoItems = new ArrayList<>();
        adapter = new TodoAdapter(this, filteredTodoItems);
        adapter.setOnTodoItemClickListener(this);
        listViewTodos.setAdapter(adapter);
        
        updateEmptyView();
    }

    private void setupListeners() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTodoItem();
            }
        });
        
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });
        
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterMenu();
            }
        });
        
        buttonSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu();
            }
        });
        
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });
        
        buttonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetailedStats();
            }
        });
        
        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExportMenu();
            }
        });
    }

    private void updateWelcomeMessage() {
        String currentUser = sessionManager.getUsername();
        if (textViewWelcome != null && !TextUtils.isEmpty(currentUser)) {
            textViewWelcome.setText("欢迎回来，" + currentUser + "！");
        }
    }

    private TodoItem.Priority getPriorityFromSpinner() {
        String selected = spinnerPriority.getSelectedItem().toString();
        if ("低".equals(selected)) {
            return TodoItem.Priority.LOW;
        } else if ("高".equals(selected)) {
            return TodoItem.Priority.HIGH;
        } else if ("紧急".equals(selected)) {
            return TodoItem.Priority.URGENT;
        } else {
            return TodoItem.Priority.NORMAL;
        }
    }

    private void addTodoItem() {
        String todoText = editTextTodo.getText().toString().trim();
        
        if (TextUtils.isEmpty(todoText)) {
            Toast.makeText(this, "请输入待办事项内容", Toast.LENGTH_SHORT).show();
            return;
        }
        
        TodoItem.Priority priority = getPriorityFromSpinner();
        
        todoService.createTodo(todoText, priority, new TodoService.TodoCallback() {
            @Override
            public void onSuccess(TodoItem todoItem) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        todoItems.add(todoItem);
                        editTextTodo.setText("");
                        spinnerPriority.setSelection(0);
                        applyFiltersAndSort();
                        updateStats();
                        Toast.makeText(MainActivity.this, "待办事项已添加", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "添加失败: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < filteredTodoItems.size()) {
            TodoItem itemToDelete = filteredTodoItems.get(position);
            String itemText = itemToDelete.getText();
            
            todoService.deleteTodo((int)itemToDelete.getId(), new TodoService.DeleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            todoItems.remove(itemToDelete);
                            applyFiltersAndSort();
                            updateStats();
                            Toast.makeText(MainActivity.this, "已删除: " + itemText, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "删除失败: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onCheckboxClick(int position, boolean isChecked) {
        if (position >= 0 && position < filteredTodoItems.size()) {
            TodoItem item = filteredTodoItems.get(position);
            
            todoService.updateTodo((int)item.getId(), item.getText(), item.getPriority(), isChecked, new TodoService.TodoCallback() {
                @Override
                public void onSuccess(TodoItem updatedItem) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            item.setCompleted(isChecked);
                            adapter.notifyDataSetChanged();
                            updateStats();
                            String message = isChecked ? "任务已完成" : "任务标记为未完成";
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "更新失败: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void showFilterMenu() {
        PopupMenu popup = new PopupMenu(this, buttonFilter);
        popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.filter_all) {
                    currentFilter = "全部";
                } else if (itemId == R.id.filter_completed) {
                    currentFilter = "已完成";
                } else if (itemId == R.id.filter_pending) {
                    currentFilter = "未完成";
                } else if (itemId == R.id.filter_high_priority) {
                    currentFilter = "高优先级";
                } else if (itemId == R.id.filter_urgent) {
                    currentFilter = "紧急";
                }
                
                buttonFilter.setText("筛选: " + currentFilter);
                applyFiltersAndSort();
                return true;
            }
        });
        
        popup.show();
    }

    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(this, buttonSort);
        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.sort_default) {
                    currentSort = "默认";
                } else if (itemId == R.id.sort_priority) {
                    currentSort = "优先级";
                } else if (itemId == R.id.sort_alphabetical) {
                    currentSort = "字母顺序";
                } else if (itemId == R.id.sort_completion) {
                    currentSort = "完成状态";
                }
                
                buttonSort.setText("排序: " + currentSort);
                applyFiltersAndSort();
                return true;
            }
        });
        
        popup.show();
    }

    private void performSearch() {
        currentSearch = editTextSearch.getText().toString().trim();
        applyFiltersAndSort();
        
        if (!currentSearch.isEmpty()) {
            Toast.makeText(this, "搜索: " + currentSearch, Toast.LENGTH_SHORT).show();
        }
    }

    private void applyFiltersAndSort() {
        filteredTodoItems.clear();
        
        // 应用筛选
        for (TodoItem item : todoItems) {
            boolean shouldInclude = false;
            
            if ("全部".equals(currentFilter)) {
                shouldInclude = true;
            } else if ("已完成".equals(currentFilter)) {
                shouldInclude = item.isCompleted();
            } else if ("未完成".equals(currentFilter)) {
                shouldInclude = !item.isCompleted();
            } else if ("高优先级".equals(currentFilter)) {
                shouldInclude = item.getPriority() == TodoItem.Priority.HIGH || 
                               item.getPriority() == TodoItem.Priority.URGENT;
            } else if ("紧急".equals(currentFilter)) {
                shouldInclude = item.getPriority() == TodoItem.Priority.URGENT;
            }
            
            // 应用搜索
            if (shouldInclude && !currentSearch.isEmpty()) {
                shouldInclude = item.getText().toLowerCase().contains(currentSearch.toLowerCase());
            }
            
            if (shouldInclude) {
                filteredTodoItems.add(item);
            }
        }
        
        // 应用排序
        if ("优先级".equals(currentSort)) {
            Collections.sort(filteredTodoItems, new Comparator<TodoItem>() {
                @Override
                public int compare(TodoItem o1, TodoItem o2) {
                    return Integer.compare(getPriorityValue(o2.getPriority()), 
                                         getPriorityValue(o1.getPriority()));
                }
            });
        } else if ("字母顺序".equals(currentSort)) {
            Collections.sort(filteredTodoItems, new Comparator<TodoItem>() {
                @Override
                public int compare(TodoItem o1, TodoItem o2) {
                    return o1.getText().compareToIgnoreCase(o2.getText());
                }
            });
        } else if ("完成状态".equals(currentSort)) {
            Collections.sort(filteredTodoItems, new Comparator<TodoItem>() {
                @Override
                public int compare(TodoItem o1, TodoItem o2) {
                    return Boolean.compare(o1.isCompleted(), o2.isCompleted());
                }
            });
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private int getPriorityValue(TodoItem.Priority priority) {
        switch (priority) {
            case URGENT: return 4;
            case HIGH: return 3;
            case NORMAL: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }

    private void updateStats() {
        int total = todoItems.size();
        int completed = 0;
        int urgent = 0;
        int high = 0;
        
        for (TodoItem item : todoItems) {
            if (item.isCompleted()) completed++;
            if (item.getPriority() == TodoItem.Priority.URGENT) urgent++;
            if (item.getPriority() == TodoItem.Priority.HIGH) high++;
        }
        
        int pending = total - completed;
        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;
        
        String statsText = String.format(Locale.getDefault(),
            "总计: %d | 已完成: %d | 待办: %d | 完成率: %.1f%% | 紧急: %d | 高优先级: %d",
            total, completed, pending, completionRate, urgent, high);
        
        if (textViewStats != null) {
            textViewStats.setText(statsText);
        }
    }

    private void showDetailedStats() {
        int total = todoItems.size();
        int completed = 0;
        int urgent = 0;
        int high = 0;
        int normal = 0;
        int low = 0;
        
        for (TodoItem item : todoItems) {
            if (item.isCompleted()) completed++;
            switch (item.getPriority()) {
                case URGENT: urgent++; break;
                case HIGH: high++; break;
                case NORMAL: normal++; break;
                case LOW: low++; break;
            }
        }
        
        int pending = total - completed;
        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;
        
        String message = String.format(Locale.getDefault(),
            "📊 详细统计信息\n\n" +
            "总任务数: %d\n" +
            "已完成: %d\n" +
            "待完成: %d\n" +
            "完成率: %.1f%%\n\n" +
            "📋 按优先级分类:\n" +
            "🔴 紧急: %d\n" +
            "🟠 高优先级: %d\n" +
            "🟡 普通: %d\n" +
            "🟢 低优先级: %d",
            total, completed, pending, completionRate, urgent, high, normal, low);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("统计信息");
        builder.setMessage(message);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    private void showExportMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("导出数据");
        
        String[] exportOptions = {"导出为文本文件", "导出为CSV文件", "导出统计报告"};
        
        builder.setItems(exportOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        exportToText();
                        break;
                    case 1:
                        exportToCSV();
                        break;
                    case 2:
                        exportStatsReport();
                        break;
                }
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void exportToText() {
        try {
            String fileName = "TodoApp_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileWriter writer = new FileWriter(file);
            writer.write("TodoApp 待办事项导出\n");
            writer.write("导出时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\n");
            writer.write("用户: " + sessionManager.getUsername() + "\n\n");
            
            for (int i = 0; i < todoItems.size(); i++) {
                TodoItem item = todoItems.get(i);
                writer.write(String.format("%d. %s [%s] [%s]\n", 
                    i + 1, 
                    item.getText(), 
                    getPriorityText(item.getPriority()),
                    item.isCompleted() ? "已完成" : "待完成"));
            }
            
            writer.close();
            Toast.makeText(this, "导出成功: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToCSV() {
        try {
            String fileName = "TodoApp_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileWriter writer = new FileWriter(file);
            writer.write("序号,内容,优先级,状态,创建时间\n");
            
            for (int i = 0; i < todoItems.size(); i++) {
                TodoItem item = todoItems.get(i);
                writer.write(String.format("%d,\"%s\",%s,%s,%s\n", 
                    i + 1, 
                    item.getText().replace("\"", "\"\""), 
                    getPriorityText(item.getPriority()),
                    item.isCompleted() ? "已完成" : "待完成",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())));
            }
            
            writer.close();
            Toast.makeText(this, "CSV导出成功: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "CSV导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportStatsReport() {
        try {
            String fileName = "TodoApp_Stats_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            
            FileWriter writer = new FileWriter(file);
            writer.write("TodoApp 统计报告\n");
            writer.write("===================\n");
            writer.write("生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\n");
            writer.write("用户: " + sessionManager.getUsername() + "\n\n");
            
            int total = todoItems.size();
            int completed = 0;
            int urgent = 0;
            int high = 0;
            int normal = 0;
            int low = 0;
            
            for (TodoItem item : todoItems) {
                if (item.isCompleted()) completed++;
                switch (item.getPriority()) {
                    case URGENT: urgent++; break;
                    case HIGH: high++; break;
                    case NORMAL: normal++; break;
                    case LOW: low++; break;
                }
            }
            
            int pending = total - completed;
            double completionRate = total > 0 ? (completed * 100.0 / total) : 0;
            
            writer.write("基本统计:\n");
            writer.write("总任务数: " + total + "\n");
            writer.write("已完成: " + completed + "\n");
            writer.write("待完成: " + pending + "\n");
            writer.write("完成率: " + String.format("%.1f%%", completionRate) + "\n\n");
            
            writer.write("优先级分布:\n");
            writer.write("紧急: " + urgent + "\n");
            writer.write("高优先级: " + high + "\n");
            writer.write("普通: " + normal + "\n");
            writer.write("低优先级: " + low + "\n\n");
            
            writer.write("任务列表:\n");
            writer.write("---------\n");
            for (int i = 0; i < todoItems.size(); i++) {
                TodoItem item = todoItems.get(i);
                writer.write(String.format("%d. %s [%s] [%s]\n", 
                    i + 1, 
                    item.getText(), 
                    getPriorityText(item.getPriority()),
                    item.isCompleted() ? "已完成" : "待完成"));
            }
            
            writer.close();
            Toast.makeText(this, "统计报告导出成功: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "统计报告导出失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getPriorityText(TodoItem.Priority priority) {
        if (priority == TodoItem.Priority.URGENT) {
            return "紧急";
        } else if (priority == TodoItem.Priority.HIGH) {
            return "高";
        } else if (priority == TodoItem.Priority.LOW) {
            return "低";
        } else {
            return "普通";
        }
    }

    private void updateEmptyView() {
        if (filteredTodoItems.isEmpty()) {
            listViewTodos.setVisibility(View.GONE);
            textViewEmpty.setVisibility(View.VISIBLE);
            
            if (!currentSearch.isEmpty()) {
                textViewEmpty.setText("没有找到匹配 \"" + currentSearch + "\" 的任务");
            } else if (!currentFilter.equals("全部")) {
                textViewEmpty.setText("当前筛选条件下没有任务");
            } else {
                textViewEmpty.setText("还没有待办事项，点击上方按钮添加一个吧！");
            }
        } else {
            listViewTodos.setVisibility(View.VISIBLE);
            textViewEmpty.setVisibility(View.GONE);
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("菜单");
        
        String[] menuItems = {"个人信息", "清空搜索", "重置筛选", "登出"};
        
        builder.setItems(menuItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        showUserInfo();
                        break;
                    case 1:
                        clearSearch();
                        break;
                    case 2:
                        resetFilters();
                        break;
                    case 3:
                        showLogoutDialog();
                        break;
                }
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void clearSearch() {
        editTextSearch.setText("");
        currentSearch = "";
        applyFiltersAndSort();
        Toast.makeText(this, "搜索已清空", Toast.LENGTH_SHORT).show();
    }

    private void resetFilters() {
        currentFilter = "全部";
        currentSort = "默认";
        currentSearch = "";
        editTextSearch.setText("");
        buttonFilter.setText("筛选");
        buttonSort.setText("排序");
        applyFiltersAndSort();
        Toast.makeText(this, "筛选和排序已重置", Toast.LENGTH_SHORT).show();
    }

    private void showUserInfo() {
        String currentUser = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        
        String message = "用户名: " + currentUser + "\n邮箱: " + email + "\n\n当前筛选: " + currentFilter + "\n当前排序: " + currentSort;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("个人信息");
        builder.setMessage(message);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认登出");
        builder.setMessage("确定要登出当前账号吗？");
        
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "已安全登出", Toast.LENGTH_SHORT).show();
        startLoginActivity();
    }

    private void saveTodoItems() {
        // 保存到本地缓存作为备用
        android.content.SharedPreferences prefs = getSharedPreferences("TodoApp_" + sessionManager.getUsername(), MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // 保存待办事项数量
        editor.putInt("todo_count", todoItems.size());
        
        // 保存每个待办事项
        for (int i = 0; i < todoItems.size(); i++) {
            TodoItem item = todoItems.get(i);
            editor.putString("todo_text_" + i, item.getText());
            editor.putString("todo_priority_" + i, item.getPriority().name());
            editor.putBoolean("todo_completed_" + i, item.isCompleted());
        }
        
        editor.apply();
    }

    private void loadTodoItems() {
        todoService.getTodos(new TodoService.TodoListCallback() {
            @Override
            public void onSuccess(List<TodoItem> items) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        todoItems.clear();
                        todoItems.addAll(items);
                        applyFiltersAndSort();
                        updateStats();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "加载数据失败: " + error, Toast.LENGTH_SHORT).show();
                        // 如果网络失败，可以尝试加载本地缓存
                        loadLocalCache();
                    }
                });
            }
        });
    }
    
    private void loadLocalCache() {
        // 从SharedPreferences加载缓存数据作为备用
        android.content.SharedPreferences prefs = getSharedPreferences("TodoApp_" + sessionManager.getUsername(), MODE_PRIVATE);
        
        int count = prefs.getInt("todo_count", 0);
        todoItems.clear();
        
        for (int i = 0; i < count; i++) {
            String text = prefs.getString("todo_text_" + i, "");
            String priorityName = prefs.getString("todo_priority_" + i, "NORMAL");
            boolean completed = prefs.getBoolean("todo_completed_" + i, false);
            
            if (!text.isEmpty()) {
                TodoItem.Priority priority;
                try {
                    priority = TodoItem.Priority.valueOf(priorityName);
                } catch (IllegalArgumentException e) {
                    priority = TodoItem.Priority.NORMAL;
                }
                
                TodoItem item = new TodoItem(text, priority);
                item.setCompleted(completed);
                todoItems.add(item);
            }
        }
        
        applyFiltersAndSort();
        Toast.makeText(this, "已加载本地缓存数据", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("退出应用");
        builder.setMessage("确定要退出应用吗？");
        
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}