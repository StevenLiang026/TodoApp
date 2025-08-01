package com.example.todoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todoapp.service.TodoService;
import com.example.todoapp.model.RegisterResponse;

public class RegisterActivity extends Activity {
    
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    
    private TodoService todoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        todoService = new TodoService(this);
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
    }

    private void setupListeners() {
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });
        
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回登录界面
            }
        });
    }

    private void attemptRegister() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        // 验证输入
        if (!validateInput(username, email, password, confirmPassword)) {
            return;
        }
        
        // 禁用注册按钮防止重复点击
        buttonRegister.setEnabled(false);
        buttonRegister.setText("注册中...");
        
        // 尝试注册
        todoService.register(username, email, password, new TodoService.RegisterCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
                        finish(); // 返回登录界面
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + error, Toast.LENGTH_SHORT).show();
                        if (error.contains("用户名") || error.contains("username")) {
                            editTextUsername.setError("用户名已存在");
                            editTextUsername.requestFocus();
                        }
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("注册");
                    }
                });
            }
        });
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        // 验证用户名
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("请输入用户名");
            editTextUsername.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            editTextUsername.setError("用户名至少需要3个字符");
            editTextUsername.requestFocus();
            return false;
        }
        
        // 验证邮箱
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("请输入邮箱地址");
            editTextEmail.requestFocus();
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("请输入有效的邮箱地址");
            editTextEmail.requestFocus();
            return false;
        }
        
        // 验证密码
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            editTextPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            editTextPassword.setError("密码至少需要6个字符");
            editTextPassword.requestFocus();
            return false;
        }
        
        // 验证确认密码
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("请确认密码");
            editTextConfirmPassword.requestFocus();
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("两次输入的密码不一致");
            editTextConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
}