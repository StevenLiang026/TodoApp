# TodoApp Vercel云端部署指南

## 🚀 步骤1：准备GitHub仓库

1. **创建GitHub账户**（如果没有）
2. **创建新仓库**：
   - 仓库名：`todoapp`
   - 设为Public
3. **上传代码**：
   - 将整个TodoApp文件夹上传到GitHub

## 🌐 步骤2：Vercel部署

1. **访问 https://vercel.com**
2. **用GitHub账户登录**
3. **点击"New Project"**
4. **选择你的todoapp仓库**
5. **配置部署设置**：
   - Framework Preset: `Other`
   - Root Directory: `server`
   - Build Command: `npm install`
   - Output Directory: 留空
   - Install Command: `npm install`
6. **点击Deploy**

## 📱 步骤3：更新Android应用

部署完成后，你会获得类似这样的URL：
`https://todoapp-xxx.vercel.app`

更新ApiClient.java：
```java
private static final String BASE_URL = "https://todoapp-xxx.vercel.app/";
```

## ✅ 完成！

现在你的TodoApp可以在全球任何地方使用了！