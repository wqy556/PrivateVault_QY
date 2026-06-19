# PrivateVault

一个**无账号、无云端、无服务器**的 Android 本地私密片库 App。

> 把不想出现在手机相册的内容放进来，按「片库 → 影片 → 详情图 / 链接 / 备注 / 标签」管理，从系统媒体库彻底隐匿。

## 功能概览

| 模块 | 说明 |
|---|---|
| 🔐 App 锁 | 启动 + 后台恢复双重锁定 |
| 📚 片库管理 | 新增 / 重命名 / 删除，一个片库可包含多部影片 |
| 🎬 影片管理 | 片名、封面图、详情图、网盘链接、备注、标签 |
| 📸 图片导入 | 从系统相册**复制**（保留原图）或**迁移**（复制后删除原图） |
| ⭐ 收藏 | 星标影片自动汇集在收藏 Tab |
| 🏷 标签 | 自定义标签管理，支持多对多关联 |
| 🔗 链接 | 夸克 / 百度网盘 / 迅雷 / 磁力 / 网页 多链接支持 |
| 💾 本地优先 | Room 数据库 + App 私有目录存储，数据不出手机 |

## 技术栈

| 层级 | 技术 |
|---|---|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + 不可变单向数据流 |
| 数据库 | Room + KSP + Coroutines Flow |
| 图片 | Coil 2.x |
| 媒体操作 | Android Photo Picker + MediaStore API |
| 测试 | JUnit 4 + kotlinx-coroutines-test |

## 项目结构

```
app/src/main/java/com/privatevault/
├── core/        纯 Kotlin 领域模型，零 Android 依赖
├── data/        Room 持久化层（DAO / Store / Repository）
├── media/       Android 媒体文件 I/O（导入 / 导出 / 删除原图）
├── ui/          Compose 界面 + ViewModel
└── MainActivity.kt
```

**## 截图** 

![片库首页·](screenshots/1.jpg)

 ![收藏详情](screenshots/2.jpg)



 ![片库详情](screenshots/3.jpg)



![设置](screenshots/4.jpg)

## 构建运行

### 前提

- Android Studio (Hedgehog 2023.1.1 或更新)
- JDK 17 或更高版本（Android Studio 内置 JDK 即可）
- Android SDK API 36

### 命令

```bash
方法一 # 推荐：直接用 Android Studio 打开项目，Sync Gradle 后点击 Run

方法二 # 命令行构建（需确保 JAVA_HOME 指向 JDK 17+）
./gradlew :app:testDebugUnitTest    # 运行单元测试
./gradlew assembleDebug             # 构建 Debug APK

# APK 输出路径
app/build/outputs/apk/debug/app-debug.apk
```

> 💡 如果命令行提示 `JVM 11`，请在系统环境变量中设置 `JAVA_HOME` 指向 JDK 17+ 的安装目录。Android Studio 用户无需额外配置。

## 当前实现状态

- [x] Compose 页面骨架（片库 / 收藏 / 设置 + 底部导航）
- [x] App 锁 UI（输入密码解锁 / 后台切回锁定）
- [x] 片库增删改 + 影片增删
- [x] 图片导入（复制 / 迁移两种模式 + 系统确认删除原图）
- [x] Room 持久化（片库 / 影片 / 详情图 / 链接 / 标签全部落库）
- [x] 备注编辑、链接增删、标签管理
- [x] 收藏切换 + 收藏页
- [x] 封面图及详情图实际图片加载（Coil）
- [x] 影片删除（含确认弹窗）
- [x] 详情图全屏查看（HorizontalPager 左右滑动）
- [x] 详情图删除（长按或按钮）
- [x] 片名编辑（点击标题编辑）
- [x] 图片导出到系统相册
- [x] 影片搜索 + 标签筛选
- [x] 种子数据（首次启动自动写入示例内容）
- [x] 系统返回键 / 右滑返回历史栈

### 待实现

- [ ] 真实 PIN 存储与生物识别解锁
- [ ] 文件级加密（当前使用 App 私有目录，未承诺文件级加密）
- [ ] Release 签名配置指南

## 安全边界

> ⚠️ 当前版本使用 App 私有目录 + App 锁流程，**不承诺文件级加密**。

文件存储在 `files/private_media/` 下（其他 App 不可访问，但未加密）。强加密方案在路线图中。

## 开源协议

MIT License — 详见 [LICENSE](LICENSE)。

## 贡献

欢迎提 Issue 和 PR。目前代码仍是个人早期项目，建议先沟通再动手，避免方向重复。
