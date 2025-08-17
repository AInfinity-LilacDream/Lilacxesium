# MusicDodge 客户端Mod

这是一个Fabric客户端Mod，用于优化MCEFramework服务端插件中MusicDodge游戏的粒子显示效果。

## 功能特性

- **Plugin Message接收**：监听来自服务端的攻击数据
- **智能粒子渲染**：根据攻击类型渲染对应的粒子效果
- **实时同步**：与服务端攻击时序完全同步
- **性能优化**：减少网络包数量，提升游戏体验
- **面向对象设计**：模块化、可扩展的代码结构

## 系统架构

### 核心组件

```
MusicDodgeClientManager (核心控制器)
├── NetworkHandler (网络通信)
├── ParticleRenderer (粒子渲染)
├── AttackDataDecoder (数据解码)
└── ClientConfig (配置管理)
```

### 类职责

1. **AttackDataDecoder**：解码服务端发送的攻击数据字符串
2. **NetworkHandler**：处理Plugin Message接收和网络通信
3. **ParticleRenderer**：将攻击数据转换为客户端粒子效果
4. **MusicDodgeClientManager**：协调各组件，管理生命周期
5. **ClientConfig**：管理客户端配置和调试选项

## 支持的攻击类型

- **LASER**：直线激光攻击
- **SQUARE_RING**：正方形环攻击
- **SPIN**：旋转射线攻击
- **CIRCLE**：圆形攻击
- **WALL**：墙攻击

## 安装和使用

### 环境要求

- Minecraft 1.20.1+
- Fabric Loader
- Fabric API

### 安装步骤

1. 将编译好的Mod文件放入客户端的`mods`文件夹
2. 启动游戏，Mod会自动初始化
3. 连接到安装了MCEFramework的服务器
4. 进入MusicDodge游戏世界即可体验优化效果

### 配置选项

通过JVM参数配置：

```bash
# 启用调试模式
-Dmusicdodge.debug=true

# 显示粒子计数
-Dmusicdodge.debug.particles=true

# 记录网络数据
-Dmusicdodge.debug.network=true

# 设置粒子缩放
-Dmusicdodge.particle.scale=1.5

# 设置最大粒子数
-Dmusicdodge.particle.max=2000

# 禁用粒子优化
-Dmusicdodge.particle.noopt=true
```

## 网络协议

### Plugin Message频道
- **频道名称**：`mce:musicdodge`
- **数据格式**：UTF-8编码的字符串

### 数据格式

```
攻击类型|参数|颜色|剩余时间#攻击类型|参数|颜色|剩余时间
```

#### 示例
```
SPIN|14.0,-60.0,-25.0,5,45.0,100|RED|120#LASER|10.0,60.0,20.0,30.0,60.0,20.0|GRAY|240
```

### 攻击参数格式

- **LASER**: `x1,y1,z1,x2,y2,z2`
- **SQUARE_RING**: `centerX,centerY,centerZ,innerRadius,outerRadius`
- **SPIN**: `centerX,centerY,centerZ,rayCount,angleOffset,maxDistance`
- **CIRCLE**: `centerX,centerY,centerZ,radius`
- **WALL**: `direction,position`

## 开发指南

### 扩展新攻击类型

1. **在AttackDataDecoder中添加新类型**：
```java
public enum AttackType {
    // 现有类型...
    NEW_ATTACK // 新攻击类型
}

public static class NewAttackParameters extends AttackParameters {
    // 参数定义
}
```

2. **在ParticleRenderer中添加渲染逻辑**：
```java
private void renderNewAttack(NewAttackParameters params, ClientWorld world, DustParticleEffect particleEffect) {
    // 渲染逻辑
}
```

3. **更新解析器**：
```java
case NEW_ATTACK:
    // 解析参数逻辑
    break;
```

### 调试工具

**启用详细日志**：
```bash
-Dmusicdodge.debug=true -Dmusicdodge.debug.network=true
```

**查看配置**：
```java
ClientConfig.getInstance().printConfig();
```

**获取当前攻击数据**：
```java
List<AttackData> attacks = MusicDodgeClientManager.getInstance().getCurrentAttacks();
```

## 性能优化

### 粒子渲染优化

- 自动检测固体方块，避免在其中渲染粒子
- 可配置的粒子密度和缩放
- 基于距离的LOD（细节层次）系统

### 网络优化

- 单一Plugin Message频道
- 压缩的攻击数据格式
- 智能数据缓存和更新

### 内存管理

- 攻击数据的及时清理
- 粒子对象池（未来版本）
- 智能垃圾回收

## 故障排除

### 常见问题

**Q: 看不到攻击粒子**
A: 确保：
- 服务端启用了粒子拦截器
- 客户端成功连接到服务器
- 在MusicDodge世界中

**Q: 粒子显示不正确**
A: 检查：
- 服务端和客户端版本匹配
- 网络连接稳定
- 开启调试模式查看日志

**Q: 性能问题**
A: 尝试：
- 降低粒子缩放：`-Dmusicdodge.particle.scale=0.5`
- 减少最大粒子数：`-Dmusicdodge.particle.max=500`
- 启用粒子优化（默认启用）

### 日志分析

**正常启动日志**：
```
Initializing Lilacxesium Client Mod...
MusicDodge Client Manager initialized
MusicDodge client features initialized
Lilacxesium Client Mod initialized successfully!
```

**接收数据日志**（调试模式）：
```
Received 3 attacks
Attack: SPIN, Phase: ALERT, Remaining: 240 ticks
Attack: LASER, Phase: ATTACK, Remaining: 120 ticks
Attack: SQUARE_RING, Phase: ALERT, Remaining: 180 ticks
```

## 兼容性

- **Minecraft版本**：1.20.1+
- **Fabric Loader**：0.14.0+
- **Fabric API**：0.83.0+
- **服务端**：需要MCEFramework插件的粒子优化功能

## 更新日志

### v1.0.0
- 初始版本
- 支持基本攻击类型
- Plugin Message通信
- 面向对象设计
- 配置管理系统

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 编写测试
4. 提交PR

## 许可证

根据项目根目录的LICENSE.txt文件授权。