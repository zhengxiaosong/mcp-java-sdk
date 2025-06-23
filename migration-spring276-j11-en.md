# Java & Spring Framework Downgrade Guide

## 1. Overview

The main goals of this downgrade are:

1. To downgrade the project's Java version from 17 to 11, removing all Java 17+ features (such as `record`, `sealed`, switch expressions, etc.) and ensuring all dependencies and plugins are compatible with Java 11.
2. To make the project compatible with Spring Boot 2.7.6, which requires downgrading Spring Framework from version 6.x to 5.3.24.

> **Additional Note:**  
> A significant portion of this downgrade and refactoring was efficiently and accurately completed with the assistance of Cursor AI, which greatly improved the migration's efficiency and accuracy.

## 2. Version Changes

### 2.1 Project Version

- From: `0.10.1-SNAPSHOT`
- To: `0.10.1-sp276-SNAPSHOT`
- Lastest: `0.10.1-sp276-j11-SNAPSHOT`
