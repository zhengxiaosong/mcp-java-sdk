# Java & Spring Framework 降级说明

## 1. 概述

本次降级的主要目标有两点：

1. 将项目的 Java 版本从 17 降级到 11，移除所有 Java 17+ 特性（如 record、sealed、switch 表达式等），并确保所有依赖和插件均兼容 Java 11。
2. 将项目与 Spring Boot 2.7.6 兼容，为此需要将 Spring Framework 从 6.x 版本降级到 5.3.24 版本。

> **补充说明：**
> 本次降级及重构过程中，大量细节和重复性劳动由 Cursor AI 辅助完成，极大提升了迁移效率和准确性。

## 2. 版本变更

### 2.1 项目版本

- 从: `0.10.1-SNAPSHOT`
- 到: `0.10.1-sp276-SNAPSHOT`
- 最后：`0.10.1-sp276-j11-SNAPSHOT`
