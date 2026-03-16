# MTRWebCTC 技术方案

> 创建时间: 2026-03-15
> 仓库: https://github.com/LarkerO/MTRWebCTCMod

---

## 一、项目概述

| 项目 | 说明 |
|------|------|
| **名称** | MTRWebCTC |
| **目标** | 为 MTR 3.x 提供 Web 控制面板/API/调度看板 |
| **包名** | `cn.bg7qvu.mtrwebctc` |
| **仓库** | `LarkerO/MTRWebCTCMod` |
| **协议** | MIT |

### 功能特性

- ✅ Web 控制面板（车站/线路/车厂 CRUD）
- ✅ 网页端修改发车时间表（立即生效）
- ✅ 列车占用监控（区间/站台/车厂）
- ✅ 历史轨迹追踪（5分钟，可配置）
- ✅ WebSocket 实时推送
- ✅ Token + WebAuthn 双重鉴权
- ✅ 自动备份（保留3份，可配置）
- ✅ 操作日志记录

---

## 二、技术架构

### 整体架构图

```
┌─────────────────────────────────────────────────────┐
│  Minecraft Server (MTR 3.x installed)              │
│  ┌───────────────────────────────────────────────┐  │
│  │  MTRWebCTC Mod                                │  │
│  │  ├─ Ktor Server (Netty) — 端口 7044           │  │
│  │  │   ├─ REST API                              │  │
│  │  │   ├─ WebSocket 推送                        │  │
│  │  │   └─ 静态资源服务 (SPA)                    │  │
│  │  ├─ MTR 数据监听器                            │  │
│  │  ├─ 列车追踪器 (内存/SQLite)                  │  │
│  │  └─ 鉴权模块 (Token + WebAuthn)               │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
            ↓ HTTP/WS (port 7044)
┌─────────────────────────────────────────────────────┐
│  Browser (React SPA)                               │
│  ├─ SVG 地图                                       │
│  ├─ 列车监控面板                                   │
│  ├─ 车站/线路/车厂编辑器                           │
│  └─ 发车时间表编辑器                               │
└─────────────────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术 |
|------|------|
| **后端** | Java 8 + Ktor Server 2.x (Netty) |
| **前端** | React 18 + TypeScript（单独仓库 `LarkerO/MTRWebCTC-Web`） |
| **通信** | WebSocket + REST API |
| **序列化** | JSON (Gson) |
| **鉴权** | Token + WebAuthn |
| **存储** | SQLite (可选) + 内存 |
| **渲染** | SVG 矢量图 |

### 多版本支持

| Minecraft | Forge | Fabric | Java |
|-----------|-------|--------|------|
| 1.16.5 | 36.2.39 | 0.14.23 | 8 |
| 1.18.2 | 40.2.21 | 0.14.23 | 17 |
| 1.20.1 | 47.1.3 | 0.15.10 | 17 |

---

## 三、目录结构

```
MTRWebCTCMod/
├── common/
│   ├── build.gradle.kts
│   └── src/main/java/cn/bg7qvu/mtrwebctc/
│       ├── MTRWebCTCMod.java           # 主入口
│       ├── config/                     # 配置管理
│       ├── server/                     # Web 服务器
│       ├── api/                        # REST API 路由
│       ├── websocket/                  # WebSocket 处理
│       ├── auth/                       # 鉴权模块
│       ├── mtr/                        # MTR 数据管理
│       ├── model/                      # DTO 模型
│       ├── storage/                    # 存储后端
│       ├── backup/                     # 备份管理
│       └── util/                       # 工具类
├── fabric-1.16.5/
├── fabric-1.18.2/
├── fabric-1.20.1/
├── forge-1.16.5/
├── forge-1.18.2/
├── forge-1.20.1/
├── libs/mtr3/                          # MTR slim jars
├── web/                                # 前端编译产物
├── docs/
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

---

## 四、API 设计

### REST API

```plaintext
# 认证
POST   /api/auth/login              # 登录
POST   /api/auth/logout             # 登出
GET    /api/auth/webauthn/register  # WebAuthn 注册
POST   /api/auth/webauthn/verify    # WebAuthn 验证

# 车站
GET    /api/stations                # 所有车站
GET    /api/stations/{id}           # 单个车站
PUT    /api/stations/{id}           # 修改车站
GET    /api/stations/{id}/platforms # 站台列表

# 线路
GET    /api/routes                  # 所有线路
GET    /api/routes/{id}             # 单个线路
PUT    /api/routes/{id}             # 修改线路
GET    /api/routes/{id}/trains      # 线路列车

# 车厂
GET    /api/depots                  # 所有车厂
GET    /api/depots/{id}             # 单个车厂
PUT    /api/depots/{id}             # 修改车厂
PUT    /api/depots/{id}/schedule    # 修改发车时间
GET    /api/depots/{id}/trains      # 车厂列车

# 列车
GET    /api/trains                  # 所有列车
GET    /api/trains/{id}             # 单个列车
GET    /api/trains/{id}/history     # 历史轨迹

# 配置
GET    /api/config                  # 获取配置
PUT    /api/config                  # 修改配置
POST   /api/config/reload           # 重载配置

# 备份
GET    /api/backups                 # 备份列表
POST   /api/backups                 # 创建备份
POST   /api/backups/{id}/restore    # 恢复备份

# 日志
GET    /api/logs                    # 操作日志
```

### WebSocket 消息格式

**订阅**:
```json
{ "action": "subscribe", "channels": ["trains", "railway"] }
```

**推送**:
```json
{
  "channel": "trains",
  "timestamp": 1733616000000,
  "data": { "trains": [...] }
}
```

---

## 五、配置文件

`config/mtrwebctc.json`:

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

---

## 六、开发计划

### Phase 1: 核心框架
- 项目结构搭建
- Gradle 构建配置
- Ktor 服务器基础
- 配置加载/日志系统

### Phase 2: MTR 集成
- MTR 数据监听
- 列车追踪器
- 历史轨迹存储

### Phase 3: API 实现
- REST API 端点
- WebSocket 推送
- 鉴权模块
- 备份系统

### Phase 4: 前端开发（单独仓库）
- React 项目搭建
- SVG 地图组件
- 各编辑器页面

### Phase 5: 测试与优化
- 多版本测试
- 性能优化
- 文档完善

---

## 七、参考项目

- [beacon-provider](https://github.com/Hydroline/beacon-provider) - Architectury 架构模板
- [WebCTC](https://github.com/WebCTC/WebCTC) - 功能参考
- [MTR 3.x](https://github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway) - MTR API

---

*文档创建者: OpenClaw*
*最后更新: 2026-03-15*
