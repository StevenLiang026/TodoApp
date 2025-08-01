# TodoApp 部署指南

## 🚀 方案1：CloudStudio 云端部署（推荐）

### 1. 准备部署
- 确保服务端代码在 `server/` 目录下
- 确保有 `package.json` 和所有依赖

### 2. 部署步骤
1. 点击顶部工具栏的"部署"按钮
2. 选择CloudStudio部署
3. 等待部署完成，获得云端URL

### 3. 更新Android应用配置
部署完成后，将获得的云端URL替换到 `ApiClient.java` 中：
```java
private static final String BASE_URL = "https://your-deployed-url.com/";
```

## 🌐 方案2：手动云服务器部署

### 支持的平台：
- Heroku
- Vercel  
- Railway
- DigitalOcean
- AWS EC2

### 部署后的优势：
✅ 应用可在任何地方使用
✅ 不依赖本地服务器
✅ 支持多用户同时使用
✅ 数据持久化存储

## 📱 方案3：本地网络使用

### 如果只在局域网使用：
1. 获取电脑IP地址：`ipconfig`
2. 修改 `ApiClient.java`：
```java
private static final String BASE_URL = "http://你的电脑IP:3000/";
```
3. 确保防火墙允许3000端口访问

## 🔧 当前状态
- 本地开发：✅ 可用
- 云端部署：⏳ 需要部署
- 随处访问：⏳ 需要云端URL