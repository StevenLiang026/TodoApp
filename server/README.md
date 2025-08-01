# TodoApp 服务器

这是 TodoApp 的后端服务器，提供用户认证和笔记管理的 RESTful API。

## 功能特性

- 用户注册和登录
- JWT 身份验证
- 笔记的增删改查
- 优先级管理
- 完成状态跟踪
- SQLite 数据库存储

## 安装和运行

### 1. 安装依赖
```bash
cd TodoApp/server
npm install
```

### 2. 启动服务器
```bash
# 开发模式（自动重启）
npm run dev

# 生产模式
npm start
```

服务器将在 http://localhost:3000 启动

## API 接口文档

### 基础信息
- 基础URL: `http://localhost:3000/api`
- 认证方式: Bearer Token (JWT)
- 数据格式: JSON

### 1. 用户注册
**POST** `/api/register`

**请求体:**
```json
{
    "username": "用户名",
    "email": "邮箱地址",
    "password": "密码"
}
```

**响应:**
```json
{
    "success": true,
    "message": "用户注册成功",
    "data": {
        "userId": 1,
        "username": "用户名",
        "email": "邮箱地址"
    }
}
```

### 2. 用户登录
**POST** `/api/login`

**请求体:**
```json
{
    "username": "用户名或邮箱",
    "password": "密码"
}
```

**响应:**
```json
{
    "success": true,
    "message": "登录成功",
    "data": {
        "token": "JWT令牌",
        "user": {
            "id": 1,
            "username": "用户名",
            "email": "邮箱地址"
        }
    }
}
```

### 3. 新增笔记
**POST** `/api/todos`

**请求头:**
```
Authorization: Bearer <JWT令牌>
```

**请求体:**
```json
{
    "text": "笔记内容",
    "priority": "HIGH|MEDIUM|LOW"
}
```

**响应:**
```json
{
    "success": true,
    "message": "笔记创建成功",
    "data": {
        "id": 1,
        "text": "笔记内容",
        "priority": "MEDIUM",
        "priorityColor": "#FFA500",
        "isCompleted": false,
        "createTime": "2025-01-01 12:00:00",
        "completeTime": null
    }
}
```

### 4. 查询笔记
**GET** `/api/todos`

**请求头:**
```
Authorization: Bearer <JWT令牌>
```

**查询参数:**
- `completed`: true/false (可选，过滤完成状态)
- `priority`: HIGH/MEDIUM/LOW (可选，过滤优先级)
- `page`: 页码 (可选，默认1)
- `limit`: 每页数量 (可选，默认50)

**响应:**
```json
{
    "success": true,
    "message": "查询成功",
    "data": {
        "todos": [
            {
                "id": 1,
                "text": "笔记内容",
                "priority": "MEDIUM",
                "priorityColor": "#FFA500",
                "isCompleted": false,
                "createTime": "2025-01-01 12:00:00",
                "completeTime": null
            }
        ],
        "total": 1,
        "page": 1,
        "limit": 50
    }
}
```

### 5. 更新笔记
**PUT** `/api/todos/:id`

**请求头:**
```
Authorization: Bearer <JWT令牌>
```

**请求体:**
```json
{
    "text": "更新的内容",
    "priority": "HIGH",
    "isCompleted": true
}
```

### 6. 删除笔记
**DELETE** `/api/todos/:id`

**请求头:**
```
Authorization: Bearer <JWT令牌>
```

### 7. 健康检查
**GET** `/api/health`

**响应:**
```json
{
    "success": true,
    "message": "TodoApp 服务器运行正常",
    "timestamp": "2025-01-01T12:00:00.000Z"
}
```

## 错误响应格式

```json
{
    "success": false,
    "message": "错误描述"
}
```

## 数据库结构

### users 表
- id: 用户ID (主键)
- username: 用户名 (唯一)
- email: 邮箱 (唯一)
- password: 加密密码
- created_at: 创建时间

### todos 表
- id: 笔记ID (主键)
- user_id: 用户ID (外键)
- text: 笔记内容
- priority: 优先级 (HIGH/MEDIUM/LOW)
- priority_color: 优先级颜色
- is_completed: 是否完成
- create_time: 创建时间
- complete_time: 完成时间

## 安全特性

- 密码使用 bcrypt 加密存储
- JWT 令牌认证
- CORS 跨域支持
- 用户数据隔离
- SQL 注入防护

## 开发说明

- 使用 SQLite 数据库，数据文件: `todoapp.db`
- JWT 密钥在生产环境中应使用环境变量
- 支持热重载开发模式