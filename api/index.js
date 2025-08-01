// Vercel Serverless Function - 最简版本
export default function handler(req, res) {
    try {
        // 设置CORS
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');
        
        // 处理OPTIONS请求
        if (req.method === 'OPTIONS') {
            return res.status(200).end();
        }

        // 健康检查接口
        if (req.url === '/api/health' || req.url === '/health') {
            return res.status(200).json({
                success: true,
                message: 'TodoApp 服务器运行正常',
                timestamp: new Date().toISOString(),
                vercel: true
            });
        }

        // 默认响应
        return res.status(200).json({
            success: true,
            message: 'TodoApp API 正在运行',
            url: req.url,
            method: req.method,
            timestamp: new Date().toISOString()
        });

    } catch (error) {
        return res.status(500).json({
            success: false,
            message: '服务器内部错误',
            error: error.message
        });
    }
}

// 创建数据表
db.serialize(() => {
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password TEXT NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )`);

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

// 健康检查接口
app.get('/api/health', (req, res) => {
    res.json({
        success: true,
        message: 'TodoApp 服务器运行正常',
        timestamp: new Date().toISOString()
    });
});

// 注册接口
app.post('/api/register', async (req, res) => {
    try {
        const { username, email, password } = req.body;

        if (!username || !email || !password) {
            return res.status(400).json({
                success: false,
                message: '用户名、邮箱和密码都是必填项'
            });
        }

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

            const hashedPassword = await bcrypt.hash(password, 10);

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

// 登录接口
app.post('/api/login', (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({
                success: false,
                message: '用户名和密码都是必填项'
            });
        }

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

            const isValidPassword = await bcrypt.compare(password, user.password);
            if (!isValidPassword) {
                return res.status(401).json({
                    success: false,
                    message: '用户名或密码错误'
                });
            }

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

// 其他API路由...
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

module.exports = app;
