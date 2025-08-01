// Vercel Serverless Function - TodoApp API
const express = require('express');
const cors = require('cors');

// 创建Express应用
const app = express();

// 中间件
app.use(cors({
  origin: '*',
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());

// 内存数据存储
let users = [];
let todos = [];

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    message: "TodoApp API 运行正常",
    timestamp: new Date().toISOString(),
    platform: "Vercel",
    users: users.length,
    todos: todos.length
  });
});

// 用户注册
app.post('/api/register', (req, res) => {
  try {
    const { username, password } = req.body;
    
    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: "用户名和密码不能为空"
      });
    }
    
    const existingUser = users.find(user => user.username === username);
    if (existingUser) {
      return res.status(400).json({
        success: false,
        message: "用户名已存在"
      });
    }
    
    const newUser = {
      id: users.length + 1,
      username,
      password
    };
    
    users.push(newUser);
    
    res.status(201).json({
      success: true,
      message: "注册成功",
      userId: newUser.id
    });
  } catch (error) {
    console.error("注册错误:", error);
    res.status(500).json({
      success: false,
      message: "服务器错误"
    });
  }
});

// 用户登录
app.post('/api/login', (req, res) => {
  try {
    const { username, password } = req.body;
    
    if (!username || !password) {
      return res.status(400).json({
        success: false,
        message: "用户名和密码不能为空"
      });
    }
    
    const user = users.find(user => user.username === username && user.password === password);
    if (!user) {
      return res.status(401).json({
        success: false,
        message: "用户名或密码错误"
      });
    }
    
    res.json({
      success: true,
      message: "登录成功",
      userId: user.id,
      token: `token_${user.id}_${Date.now()}`
    });
  } catch (error) {
    console.error("登录错误:", error);
    res.status(500).json({
      success: false,
      message: "服务器错误"
    });
  }
});

// 获取待办事项
app.get('/api/todos', (req, res) => {
  try {
    const userId = req.query.userId;
    
    if (!userId) {
      return res.status(400).json({
        success: false,
        message: "缺少用户ID"
      });
    }
    
    const userTodos = todos.filter(todo => todo.userId === parseInt(userId));
    
    res.json({
      success: true,
      todos: userTodos
    });
  } catch (error) {
    console.error("获取待办事项错误:", error);
    res.status(500).json({
      success: false,
      message: "服务器错误"
    });
  }
});

// 创建待办事项
app.post('/api/todos', (req, res) => {
  try {
    const { userId, title, description, dueDate, priority } = req.body;
    
    if (!userId || !title) {
      return res.status(400).json({
        success: false,
        message: "用户ID和标题不能为空"
      });
    }
    
    const newTodo = {
      id: todos.length + 1,
      userId: parseInt(userId),
      title,
      description: description || "",
      dueDate: dueDate || null,
      priority: priority || "中",
      completed: false,
      createdAt: new Date().toISOString()
    };
    
    todos.push(newTodo);
    
    res.status(201).json({
      success: true,
      message: "待办事项创建成功",
      todo: newTodo
    });
  } catch (error) {
    console.error("创建待办事项错误:", error);
    res.status(500).json({
      success: false,
      message: "服务器错误"
    });
  }
});

// 更新待办事项
app.put('/api/todos/:id', (req, res) => {
  try {
    const todoId = parseInt(req.params.id);
    const { title, description, dueDate, priority, completed } = req.body;
    
    const todoIndex = todos.findIndex(todo => todo.id === todoId);
    
    if (todoIndex === -1) {
      return res.status(404).json({
        success: false,
        message: "待办事项不存在"
      });
    }
    
    if (title) todos[todoIndex].title = title;
    if (description !== undefined) todos[todoIndex].description = description;
    if (dueDate !== undefined) todos[todoIndex].dueDate = dueDate;
    if (priority !== undefined) todos[todoIndex].priority = priority;
    if (completed !== undefined) todos[todoIndex].completed = completed;
    
    res.json({
      success: true,
      message: "待办事项更新成功",
      todo: todos[todoIndex]
    });
  } catch (error) {
    console.error("更新待办事项错误:", error);
    res.status(500).json({
      success: false,
      message: "服务器错误"
    });
  }
});

// 删除待办事项
app.delete('/api/todos/:id', (req, res) => {
  try {
    const todoId = parseInt(req.params.id);
    
    const todoIndex = todos.findIndex(todo => todo.id === todoId);
    
    if (todoIndex === -1) {
      return res.status(404).json({
        success: false,
        message: "待办事项不存在"
      });
    }
    
    todos.splice(todoIndex, 1);
    
    res.json({
      success: true,
      message: "待办事项删除成功"
    });
  } catch (error) {
    console.error("删除待办事项错误:", error);
    res.status(500).json({
      success: false,
      message: "服务器错误"
    });
  }
});

// 根路径 - API文档
app.get('/api', (req, res) => {
  res.json({
    success: true,
    message: "TodoApp API - Vercel部署版",
    timestamp: new Date().toISOString(),
    endpoints: {
      health: "GET /api/health",
      register: "POST /api/register",
      login: "POST /api/login",
      todos: {
        list: "GET /api/todos?userId=:id",
        create: "POST /api/todos",
        update: "PUT /api/todos/:id",
        delete: "DELETE /api/todos/:id"
      }
    },
    stats: {
      users: users.length,
      todos: todos.length
    }
  });
});

// 处理所有其他路径
app.all('*', (req, res) => {
  res.status(404).json({
    success: false,
    message: "API端点不存在",
    path: req.path,
    method: req.method
  });
});

// 导出为Vercel函数
module.exports = app;