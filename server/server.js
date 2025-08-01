const express = require('express');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = 'todoapp_secret_key_2025'; // 生产环境中应使用环境变量

// 中间件
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// 数据库初始化
const dbPath = path.join(__dirname, 'todoapp.db');
const db = new sqlite3.Database(dbPath);

// 创建数据表
db.serialize(() => {
    // 用户表
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )`);

    // 笔记表
    db.run(`CREATE TABLE IF NOT EXISTS todos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        text TEXT NOT NULL,
        priority TEXT DEFAULT 'MEDIUM',
        priority_color TEXT DEFAULT '#FFA500',
        is_completed BOOLEAN DEFAULT 0,
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        complete_time DATETIME NULL,
        FOREIGN KEY (user_id) REFERENCES users (id)
    )`);
});

// JWT 验证中间件
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ 
            success: false, 
            message: '访问令牌缺失' 
        });
    }

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ 
                success: false, 
                message: '访问令牌无效' 
            });
        }
        req.user = user;
        next();
    });
};

// 1. 注册接口
app.post('/api/register', async (req, res) => {
    try {
        const { username, email, password } = req.body;

        // 验证输入
        if (!username || !email || !password) {
            return res.status(400).json({
                success: false,
                message: '用户名、邮箱和密码都是必填项'
            });
        }

        // 检查用户是否已存在
        db.get('SELECT * FROM users WHERE username = ? OR email = ?', 
            [username, email], async (err, row) => {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: '数据库错误'
                });
            }

            if (row) {
                return res.status(400).json({
                    success: false,
                    message: '用户名或邮箱已存在'
                });
            }

            // 加密密码
            const hashedPassword = await bcrypt.hash(password, 10);

            // 创建用户
            db.run('INSERT INTO users (username, email, password) VALUES (?, ?, ?)',
                [username, email, hashedPassword], function(err) {
                if (err) {
                    return res.status(500).json({
                        success: false,
                        message: '用户创建失败'
                    });
                }

                res.status(201).json({
                    success: true,
                    message: '用户注册成功',
                    data: {
                        userId: this.lastID,
                        username: username,
                        email: email
                    }
                });
            });
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 2. 登录接口
app.post('/api/login', (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({
                success: false,
                message: '用户名和密码都是必填项'
            });
        }

        // 查找用户
        db.get('SELECT * FROM users WHERE username = ? OR email = ?', 
            [username, username], async (err, user) => {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: '数据库错误'
                });
            }

            if (!user) {
                return res.status(401).json({
                    success: false,
                    message: '用户名或密码错误'
                });
            }

            // 验证密码
            const isValidPassword = await bcrypt.compare(password, user.password);
            if (!isValidPassword) {
                return res.status(401).json({
                    success: false,
                    message: '用户名或密码错误'
                });
            }

            // 生成 JWT
            const token = jwt.sign(
                { userId: user.id, username: user.username },
                JWT_SECRET,
                { expiresIn: '24h' }
            );

            res.json({
                success: true,
                message: '登录成功',
                data: {
                    token: token,
                    user: {
                        id: user.id,
                        username: user.username,
                        email: user.email
                    }
                }
            });
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 3. 新增笔记接口
app.post('/api/todos', authenticateToken, (req, res) => {
    try {
        const { text, priority = 'MEDIUM' } = req.body;
        const userId = req.user.userId;

        if (!text) {
            return res.status(400).json({
                success: false,
                message: '笔记内容不能为空'
            });
        }

        // 设置优先级颜色
        const priorityColors = {
            'HIGH': '#FF0000',
            'MEDIUM': '#FFA500', 
            'LOW': '#008000'
        };
        const priorityColor = priorityColors[priority] || '#FFA500';

        db.run(`INSERT INTO todos (user_id, text, priority, priority_color) 
                VALUES (?, ?, ?, ?)`,
            [userId, text, priority, priorityColor], function(err) {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: '笔记创建失败'
                });
            }

            // 返回新创建的笔记
            db.get('SELECT * FROM todos WHERE id = ?', [this.lastID], (err, todo) => {
                if (err) {
                    return res.status(500).json({
                        success: false,
                        message: '获取笔记失败'
                    });
                }

                res.status(201).json({
                    success: true,
                    message: '笔记创建成功',
                    data: {
                        id: todo.id,
                        text: todo.text,
                        priority: todo.priority,
                        priorityColor: todo.priority_color,
                        isCompleted: Boolean(todo.is_completed),
                        createTime: todo.create_time,
                        completeTime: todo.complete_time
                    }
                });
            });
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 4. 查询笔记接口
app.get('/api/todos', authenticateToken, (req, res) => {
    try {
        const userId = req.user.userId;
        const { completed, priority, page = 1, limit = 50 } = req.query;

        let query = 'SELECT * FROM todos WHERE user_id = ?';
        let params = [userId];

        // 添加过滤条件
        if (completed !== undefined) {
            query += ' AND is_completed = ?';
            params.push(completed === 'true' ? 1 : 0);
        }

        if (priority) {
            query += ' AND priority = ?';
            params.push(priority);
        }

        // 添加排序和分页
        query += ' ORDER BY create_time DESC LIMIT ? OFFSET ?';
        params.push(parseInt(limit), (parseInt(page) - 1) * parseInt(limit));

        db.all(query, params, (err, todos) => {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: '查询笔记失败'
                });
            }

            // 格式化返回数据
            const formattedTodos = todos.map(todo => ({
                id: todo.id,
                text: todo.text,
                priority: todo.priority,
                priorityColor: todo.priority_color,
                isCompleted: Boolean(todo.is_completed),
                createTime: todo.create_time,
                completeTime: todo.complete_time
            }));

            res.json({
                success: true,
                message: '查询成功',
                data: {
                    todos: formattedTodos,
                    total: formattedTodos.length,
                    page: parseInt(page),
                    limit: parseInt(limit)
                }
            });
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 5. 更新笔记状态接口
app.put('/api/todos/:id', authenticateToken, (req, res) => {
    try {
        const todoId = req.params.id;
        const userId = req.user.userId;
        const { isCompleted, text, priority } = req.body;

        // 验证笔记是否属于当前用户
        db.get('SELECT * FROM todos WHERE id = ? AND user_id = ?', 
            [todoId, userId], (err, todo) => {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: '数据库错误'
                });
            }

            if (!todo) {
                return res.status(404).json({
                    success: false,
                    message: '笔记不存在'
                });
            }

            // 构建更新语句
            let updateFields = [];
            let params = [];

            if (text !== undefined) {
                updateFields.push('text = ?');
                params.push(text);
            }

            if (priority !== undefined) {
                updateFields.push('priority = ?');
                params.push(priority);
                
                const priorityColors = {
                    'HIGH': '#FF0000',
                    'MEDIUM': '#FFA500', 
                    'LOW': '#008000'
                };
                updateFields.push('priority_color = ?');
                params.push(priorityColors[priority] || '#FFA500');
            }

            if (isCompleted !== undefined) {
                updateFields.push('is_completed = ?');
                params.push(isCompleted ? 1 : 0);
                
                if (isCompleted) {
                    updateFields.push('complete_time = CURRENT_TIMESTAMP');
                } else {
                    updateFields.push('complete_time = NULL');
                }
            }

            if (updateFields.length === 0) {
                return res.status(400).json({
                    success: false,
                    message: '没有提供更新字段'
                });
            }

            params.push(todoId, userId);
            const updateQuery = `UPDATE todos SET ${updateFields.join(', ')} WHERE id = ? AND user_id = ?`;

            db.run(updateQuery, params, function(err) {
                if (err) {
                    return res.status(500).json({
                        success: false,
                        message: '更新笔记失败'
                    });
                }

                // 返回更新后的笔记
                db.get('SELECT * FROM todos WHERE id = ?', [todoId], (err, updatedTodo) => {
                    if (err) {
                        return res.status(500).json({
                            success: false,
                            message: '获取更新后的笔记失败'
                        });
                    }

                    res.json({
                        success: true,
                        message: '笔记更新成功',
                        data: {
                            id: updatedTodo.id,
                            text: updatedTodo.text,
                            priority: updatedTodo.priority,
                            priorityColor: updatedTodo.priority_color,
                            isCompleted: Boolean(updatedTodo.is_completed),
                            createTime: updatedTodo.create_time,
                            completeTime: updatedTodo.complete_time
                        }
                    });
                });
            });
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 6. 删除笔记接口
app.delete('/api/todos/:id', authenticateToken, (req, res) => {
    try {
        const todoId = req.params.id;
        const userId = req.user.userId;

        db.run('DELETE FROM todos WHERE id = ? AND user_id = ?', 
            [todoId, userId], function(err) {
            if (err) {
                return res.status(500).json({
                    success: false,
                    message: '删除笔记失败'
                });
            }

            if (this.changes === 0) {
                return res.status(404).json({
                    success: false,
                    message: '笔记不存在'
                });
            }

            res.json({
                success: true,
                message: '笔记删除成功'
            });
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: '服务器内部错误'
        });
    }
});

// 健康检查接口
app.get('/api/health', (req, res) => {
    res.json({
        success: true,
        message: 'TodoApp 服务器运行正常',
        timestamp: new Date().toISOString()
    });
});

// 启动服务器
app.listen(PORT, () => {
    console.log(`TodoApp 服务器已启动，端口: ${PORT}`);
    console.log(`健康检查: http://localhost:${PORT}/api/health`);
});

// 优雅关闭
process.on('SIGINT', () => {
    console.log('\n正在关闭服务器...');
    db.close((err) => {
        if (err) {
            console.error('关闭数据库连接时出错:', err.message);
        } else {
            console.log('数据库连接已关闭');
        }
        process.exit(0);
    });
});