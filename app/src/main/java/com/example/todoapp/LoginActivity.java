package com.example.todoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todoapp.service.TodoService;
import com.example.todoapp.utils.SessionManager;
import com.example.todoapp.model.LoginResponse;

public class LoginActivity extends Activity {
    
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    
    private SessionManager sessionManager;
    private TodoService todoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        sessionManager = new SessionManager(this);
        todoService = new TodoService(this);
        
        // 检查是否已经登录
        if (sessionManager.isLoggedIn()) {
            startMainActivity();
            return;
        }
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterActivity();
            }
        });
    }

    private void attemptLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("请输入用户名");
            editTextUsername.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            editTextPassword.requestFocus();
            return;
        }
        
        // 禁用登录按钮防止重复点击
        buttonLogin.setEnabled(false);
        buttonLogin.setText("登录中...");
        
        // 尝试登录
        todoService.login(username, password, new TodoService.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse.LoginData loginData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "登录成功！欢迎 " + username, Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "登录失败: " + error, Toast.LENGTH_SHORT).show();
                        editTextPassword.setText("");
                        editTextPassword.requestFocus();
                        buttonLogin.setEnabled(true);
                        buttonLogin.setText("登录");
                    }
                });
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void startRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    @Override
    public void onBackPressed() {
        // 禁用返回键，防止用户绕过登录
        moveTaskToBack(true);
    }
}