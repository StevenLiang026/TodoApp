@echo off
echo 启动 TodoApp 服务器...
echo.

REM 检查是否安装了 Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 Node.js，请先安装 Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

REM 检查是否安装了依赖
if not exist "node_modules" (
    echo 正在安装依赖包...
    npm install
    if %errorlevel% neq 0 (
        echo 错误: 依赖安装失败
        pause
        exit /b 1
    )
)

echo 启动服务器...
echo 服务器地址: http://localhost:3000
echo 健康检查: http://localhost:3000/api/health
echo 按 Ctrl+C 停止服务器
echo.

npm start