# MTRWebCTCMod 项目交接文档

> 写给下一位接手的 AI Agent | 2026-03-16

---

## 1. 当前任务目标

**主线目标**: 为 MTRWebCTCMod 项目配置 GitHub Actions CI/CD，实现 6 个构建目标的自动化构建。

**构建目标**:
- fabric-1.16.5
- fabric-1.18.2
- fabric-1.20.1
- forge-1.16.5
- forge-1.18.2
- forge-1.20.1

**完成标准**: GitHub Actions 构建全部通过，生成 6 个平台的 mod jar 文件。

---

## 2. 当前进展

### ✅ 已完成
| 阶段 | 状态 | 提交 |
|------|------|------|
| CI/CD 工作流配置 | ✅ 完成 | `.github/workflows/build.yml` 从 beacon-provider 复制 |
| Gradle 8.6 升级 | ✅ 完成 | architectury.loom 1.6.422 要求最低 8.6 |
| Mojang mappings 切换 | ✅ 完成 | 解决 Yarn mappings 与 MTR 类冲突 |
| Fabric 项目配置 | ✅ 完成 | 3 个版本全部通过 remap |
| Forge 项目配置 | ✅ 完成 | 添加 `loom.platform=forge` 到 gradle.properties |
| Kotlin DSL 语法修复 | ✅ 完成 | `forge("...")` → `add("forge", ...)` |
| Java 8 兼容性修复 | ✅ 完成 | text blocks → 字符串拼接 |
| Ktor API 兼容性修复 | ✅ 完成 | HttpStatusCode 构造函数替代 companion object |

### 📊 当前构建状态
**最后提交**: `2a9032d` - `fix: Ktor API compatibility`

**预期**: common 模块现在应该能编译通过，下一步需要验证完整构建。

---

## 3. 关键上下文

### 项目信息
- **仓库**: `LarkerO/MTRWebCTCMod`
- **本地路径**: `/root/.openclaw/workspace/MTRWebCTCMod`
- **项目类型**: Minecraft mod (Architectury 多平台)
- **MTR 版本**: 3.2.2-hotfix-1 (Fabric), 3.2.2-hotfix-2 (Forge 1.20.1)
- **Ktor 版本**: 2.3.9
- **Architectury Loom**: 1.6.422

### 用户要求
- ⚠️ "减少 push 上去和使用 actions 的频率，尽量 push 有价值的，能减少 build failed"
- 建议: 本地先测试构建，确认通过后再 push

### 已解决的关键问题

#### 问题 1: Mapping 冲突
- **症状**: `WidgetShorterSlider.renderButton` 方法名冲突
- **根因**: Yarn mappings 与 MTR 3.2.2 的类映射冲突
- **解决**: 切换到 Mojang official mappings
- **配置**: `mappings(loom.officialMojangMappings())`

#### 问题 2: Forge Kotlin DSL 语法
- **症状**: `forge("...")` 无法识别
- **解决**: 改用 `add("forge", "...")`

#### 问题 3: Forge platform 未声明
- **症状**: "Loom is not running on Forge"
- **解决**: 在每个 Forge 项目的 `gradle.properties` 添加 `loom.platform=forge`

#### 问题 4: Java 8 兼容性
- **症状**: text blocks 不支持 Java 8
- **解决**: `StorageManager.java` 中的 SQL 语句改用字符串拼接

#### 问题 5: Ktor API 互操作
- **症状**: `HttpStatusCode.Unauthorized has private access`
- **根因**: Java 无法直接访问 Kotlin companion object 属性
- **解决**: 使用 `new HttpStatusCode(401, "Unauthorized")` 构造函数

---

## 4. 关键发现

### 架构发现
1. **common 模块是核心**: 包含 54 个 Java 文件，所有平台共享
2. **Ktor 作为 Web 服务器**: 提供 REST API 和 WebSocket
3. **MTR 依赖方式**: 从 Modrinth API 自动下载到 `libs/mtr3/` 目录

### 依赖版本匹配
| 平台 | Fabric API 版本 |
|------|----------------|
| 1.16.5 | 0.42.0+1.16 |
| 1.18.2 | 0.76.0+1.18.2 |
| 1.20.1 | 0.86.1+1.20.1 |

⚠️ **关键**: Fabric API 版本必须与 MTR 3.2.2 兼容，否则构建失败

### 代码中的 TODO 列表
这些功能尚未实现，但不影响编译:
1. `TrainTracker`: 从 MTR TrainServer.simulationHolder 获取列车数据
2. `MTRDataManager`: 实现 getTrainsByRoute/getTrainsByDepot/getAllTrains
3. `WebSocketHandler`: 推送 RailwayData 变化
4. `BackupRouter`: 列出备份、恢复备份
5. `WebAuthnManager`: 实际验证 WebAuthn 响应
6. `LogRouter`: 实现日志读取

---

## 5. 未完成事项

### 优先级 1: 验证完整构建
- [ ] 等待 GitHub Actions 运行结果
- [ ] 如果失败，查看错误日志并修复
- [ ] 目标: 所有 6 个平台构建通过

### 优先级 2: 可能需要的修复
- [ ] 检查是否有其他 Java/Kotlin 互操作问题
- [ ] 检查 LogRouter.java 中的 `routing.route()` 方法调用
- [ ] 验证 WebSocket 相关代码编译

### 优先级 3: 后续功能开发
- [ ] 实现代码中的 TODO 功能
- [ ] 前端集成 (独立仓库 LarkerO/MTRWebCTC-Web)

---

## 6. 建议接手路径

### 第一步: 检查构建状态
```bash
cd /root/.openclaw/workspace/MTRWebCTCMod
gh run list --limit 5
```

### 第二步: 如果构建失败
```bash
# 查看失败日志
gh run view <run_id> --log-failed

# 或查看完整日志
gh run view <run_id> --log
```

### 第三步: 本地验证 (可选)
```bash
# 先下载依赖
./gradlew downloadAllDependencies

# 构建所有目标
./gradlew buildAllTargets
```

### 关键文件路径
```
MTRWebCTCMod/
├── .github/workflows/build.yml     # CI 配置
├── build.gradle.kts                # 根项目配置
├── common/
│   ├── build.gradle.kts            # common 模块配置
│   └── src/main/java/.../api/      # API 路由 (已修复 Ktor 兼容性)
├── fabric-1.16.5/
│   └── build.gradle.kts            # Fabric 1.16.5 配置
├── fabric-1.18.2/
│   └── build.gradle.kts
├── fabric-1.20.1/
│   └── build.gradle.kts
├── forge-1.16.5/
│   ├── build.gradle.kts
│   └── gradle.properties           # 包含 loom.platform=forge
├── forge-1.18.2/
│   ├── build.gradle.kts
│   └── gradle.properties
└── forge-1.20.1/
    ├── build.gradle.kts
    └── gradle.properties
```

---

## 7. 风险与注意事项

### ⚠️ 已验证的坑 (不要重复踩)
1. **不要用 Yarn mappings** - 会与 MTR 3.2.2 冲突，必须用 Mojang official mappings
2. **不要在 Forge 项目缺少 gradle.properties** - 必须有 `loom.platform=forge`
3. **不要在 Kotlin DSL 中用 `forge("...")`** - 必须用 `add("forge", ...)`
4. **不要用 text blocks** - common 模块需要兼容 Java 8
5. **不要直接访问 HttpStatusCode companion object 属性** - Java 互操作需用构造函数

### ⚠️ 用户偏好
- 用户希望减少不必要的 push 和 build failure
- **建议**: 本地验证通过后再 push

### 🔍 可能的后续问题
- LogRouter.java 的 `routing.route()` 调用可能还有问题
- WebSocket 相关代码可能有其他 Ktor 互操作问题
- 部分路由文件可能遗漏了 HttpStatusCode 修复

---

## 下一位 Agent 的第一步建议

```
1. 执行: gh run list --limit 3
   - 查看最新构建状态

2. 如果构建失败:
   - 执行: gh run view <latest_run_id> --log-failed | tail -100
   - 分析错误类型
   - 根据错误类型修复代码

3. 如果构建成功:
   - 恭喜，CI/CD 配置完成！
   - 可以继续实现 TODO 功能

4. 如果遇到新问题:
   - 参考 memory/2026-03-16.md 中的详细记录
   - 参考 beacon-provider 项目作为工作示例
```

---

**祝你好运！** 🚀
