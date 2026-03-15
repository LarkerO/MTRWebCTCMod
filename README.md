# MTRWebCTC

Web-based CTC (Centralized Traffic Control) system for MTR (Minecraft Transit Railway) 3.x

一个为 MTR 模组提供的 Web 控制面板、API 接口和调度看板。

## 特性

- 🖥️ **Web 控制面板** - 在浏览器中管理车站、线路、车厂
- 📊 **实时列车监控** - 查看列车位置、速度、延误信息
- 🔧 **发车时间表编辑** - 直接在网页上修改发车时间
- 📡 **REST API** - 完整的 API 接口供第三方集成
- 🔄 **WebSocket 推送** - 实时数据推送
- 🔐 **双重鉴权** - Token + WebAuthn 支持
- 💾 **自动备份** - 修改前自动备份 RailwayData
- 📜 **操作日志** - 记录所有修改操作

## 支持版本

| Minecraft | Forge | Fabric |
|-----------|-------|--------|
| 1.16.5 | ✅ | ✅ |
| 1.18.2 | ✅ | ✅ |
| 1.20.1 | ✅ | ✅ |

## 依赖

- MTR 3.2.2 或更高版本（3.x 系列）

## 安装

1. 下载对应版本的 mod jar 文件
2. 放入 `mods` 文件夹
3. 启动服务器，mod 会自动生成配置文件

## 配置

配置文件位于 `config/mtrwebctc/config.json`：

```json
{
  "server": {
    "port": 7044,
    "bind": "0.0.0.0",
    "staticResourceMode": "embedded"
  },
  "trainTracker": {
    "positionUpdateIntervalMs": 1000,
    "historyRetentionMinutes": 5
  },
  "websocket": {
    "pushIntervalMs": 15000
  },
  "backup": {
    "enabled": true,
    "maxBackups": 3
  },
  "storage": {
    "backend": "memory"
  },
  "auth": {
    "passwordHash": "",
    "webauthnEnabled": true
  }
}
```

## API 文档

### 认证

```
POST /api/auth/login     - 登录获取 token
POST /api/auth/logout    - 登出
```

### 车站

```
GET  /api/stations           - 获取所有车站
GET  /api/stations/{id}      - 获取单个车站
PUT  /api/stations/{id}      - 修改车站
GET  /api/stations/{id}/platforms - 获取站台列表
```

### 线路

```
GET  /api/routes             - 获取所有线路
GET  /api/routes/{id}        - 获取单个线路
PUT  /api/routes/{id}        - 修改线路
GET  /api/routes/{id}/trains - 获取线路上的列车
```

### 车厂

```
GET  /api/depots             - 获取所有车厂
GET  /api/depots/{id}        - 获取单个车厂
PUT  /api/depots/{id}        - 修改车厂
PUT  /api/depots/{id}/schedule - 修改发车时间表
GET  /api/depots/{id}/trains - 获取车厂内的列车
```

### 列车

```
GET  /api/trains             - 获取所有列车
GET  /api/trains/{id}        - 获取单个列车
GET  /api/trains/{id}/history - 获取历史轨迹
```

### WebSocket

```
ws://host:port/ws

订阅频道：
{
  "action": "subscribe",
  "channels": ["trains", "railway"]
}

推送消息：
{
  "channel": "trains",
  "timestamp": 1733616000000,
  "data": { "trains": [...] }
}
```

## 构建

### 准备工作

1. **下载 MTR JAR 文件**（编译必需）：
   ```bash
   # 从 CurseForge 或 Modrinth 下载 slim jars
   # 放置到 libs/mtr3/ 目录
   ```
   详见 `libs/mtr3/README.md`。

2. **设置 GitHub Actions**（可选）：
   ```bash
   mkdir -p .github/workflows
   cp docs/build.yml.example .github/workflows/build.yml
   ```

### 构建命令

```bash
# 下载依赖
./gradlew downloadAllDependencies

# 构建所有版本
./gradlew buildAllTargets

# 构建特定版本
./gradlew :forge-1.20.1:shadowJar
./gradlew :fabric-1.20.1:shadowJar
```

## 前端

前端代码位于单独的仓库：[LarkerO/MTRWebCTC-Web](https://github.com/LarkerO/MTRWebCTC-Web)

## 许可证

MIT License

## 致谢

- [MTR](https://github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway) - Minecraft Transit Railway mod
- [WebCTC](https://github.com/WebCTC/WebCTC) - 功能参考
- [beacon-provider](https://github.com/Hydroline/beacon-provider) - 架构参考
