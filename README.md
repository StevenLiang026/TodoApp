# TodoApp API - Vercel部署版

这是TodoApp的全新Vercel部署版本，采用更简洁的架构设计。

## 🚀 部署方式

### 方法1: 通过GitHub部署（推荐）
1. 将此文件夹推送到GitHub仓库
2. 在Vercel中连接GitHub仓库
3. 自动部署

### 方法2: 直接上传部署
1. 登录 https://vercel.com
2. 点击"New Project"
3. 上传此文件夹
4. 点击Deploy

## 📋 API端点

部署成功后，你的API地址格式为：
```
https://your-project-name.vercel.app/api/
```

### 可用端点：
- `GET /api/health` - 健康检查
- `POST /api/register` - 用户注册
- `POST /api/login` - 用户登录
- `GET /api/todos?userId=:id` - 获取待办事项
- `POST /api/todos` - 创建待办事项
- `PUT /api/todos/:id` - 更新待办事项
- `DELETE /api/todos/:id` - 删除待办事项

## 📱 Android客户端配置

部署成功后，在`ApiClient.java`中更新：
```java
private static final String BASE_URL = "https://your-project-name.vercel.app/api/";
```

## 🔧 技术特点

- ✅ Serverless架构
- ✅ 自动扩缩容
- ✅ 全球CDN加速
- ✅ HTTPS安全连接
- ✅ 零配置部署

## ⚠️ 注意事项

- 使用内存存储，数据在函数重启后会丢失
- 适合演示和测试使用
- 生产环境建议连接数据库

## 🎯 部署后测试

1. 访问 `https://your-domain.vercel.app/api/health`
2. 检查返回的JSON响应
3. 在Android应用中测试完整功能