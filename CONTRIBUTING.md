# 贡献指南

感谢你考虑为 PrivateVault 贡献代码！

## 沟通先行

这是一个个人早期项目，建议 **先开 Issue 讨论** 再动手，避免方向重复或设计冲突。

## 开发环境

- Android Studio Hedgehog (2023.1.1) 或更新
- JDK 17+
- Android SDK API 36

## 提交规范

- 一个 PR 只做一件事
- 保持提交信息清晰，推荐格式：`类型: 简短描述`（如 `feat: 添加影片搜索` / `fix: 修复导入失败提示`）
- 新功能需要同步更新 `设计.md` 中的实现状态

## 代码风格

- 遵循 Kotlin 官方编码规范
- 不可变数据优先，State 变更返回新对象
- Compose 组件文件不超过 500 行；超过时考虑拆分子文件
- 纯逻辑放在 `core/`，不引入 Android 依赖
- 新单测放在对应 `src/test/` 目录下

## 测试

```bash
./gradlew :app:testDebugUnitTest
```

确保所有已有测试通过后再提 PR。
