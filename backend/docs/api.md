# 阶段 3 后端 API 文档

## 运行环境

- Java: 本机 Java 24 可运行，Gradle toolchain 使用 Java 24。
- 构建工具: `backend/gradlew.bat`
- 数据库: MySQL 8.0
- 默认服务地址: `http://localhost:8080`

## MySQL 配置

先启动 `MySQL80` 服务，并创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS hnust_easy_weibo
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

后端通过环境变量读取连接信息：

```powershell
$env:WEIBO_DB_URL="jdbc:mysql://localhost:3306/hnust_easy_weibo?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:WEIBO_DB_USERNAME="root"
$env:WEIBO_DB_PASSWORD="你的密码"
.\gradlew.bat bootRun
```

## 认证方式

登录或注册成功后返回：

```json
{
  "tokenType": "Bearer",
  "token": "...",
  "user": {}
}
```

需要登录的接口在请求头中携带：

```http
Authorization: Bearer <token>
```

## 统一错误响应

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数不符合要求",
  "details": {
    "content": "size must be between 5 and 280"
  },
  "timestamp": "2026-04-22T18:00:00"
}
```

## Auth

### 注册

`POST /api/auth/register`

```json
{
  "username": "student001",
  "password": "secret123",
  "displayName": "湖科同学",
  "major": "现代 Web 课程",
  "bio": "正在学习 JavaWeb"
}
```

### 登录

`POST /api/auth/login`

```json
{
  "username": "student001",
  "password": "secret123"
}
```

### 当前用户

`GET /api/auth/me`

需要登录。

### 退出登录

`POST /api/auth/logout`

需要登录，成功返回 `204 No Content`。

## Posts

### 动态列表

`GET /api/posts?topic=学习&keyword=Vue&page=0&size=20`

`topic`、`keyword` 可选。

### 动态详情

`GET /api/posts/{id}`

### 发布动态

`POST /api/posts`

需要登录。

```json
{
  "content": "这是一条校园动态，至少 5 个字，最多 280 个字。",
  "topic": "学习",
  "visibility": "全校可见",
  "imageUrl": "/uploads/demo.png"
}
```

### 编辑动态

`PUT /api/posts/{id}`

需要登录，且只能编辑自己的动态。

### 删除动态

`DELETE /api/posts/{id}`

需要登录，且只能删除自己的动态。成功返回 `204 No Content`。

### 点赞/取消点赞

`POST /api/posts/{id}/like`

需要登录。重复调用会在点赞和取消点赞之间切换。

### 评论

`POST /api/posts/{id}/comments`

需要登录。

```json
{
  "content": "评论最多 80 字。"
}
```

## Users

### 用户公开资料

`GET /api/users/{id}`

### 更新当前用户资料

`PUT /api/users/me`

需要登录。

```json
{
  "displayName": "湖科同学",
  "major": "JavaWeb 阶段",
  "bio": "正在把项目接入后端。",
  "avatarUrl": "/uploads/avatar.png"
}
```

## Uploads

### 上传图片

`POST /api/uploads`

需要登录，`multipart/form-data`，字段名为 `file`。只允许图片 MIME 类型，大小不超过 5MB。

成功响应：

```json
{
  "id": 1,
  "url": "/uploads/uuid.png",
  "originalFilename": "avatar.png",
  "contentType": "image/png",
  "sizeBytes": 1024
}
```

### 访问图片

`GET /uploads/{filename}`
