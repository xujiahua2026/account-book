# 个人记账管理系统

基于 Spring Boot + MyBatis + MySQL 的个人收支记账系统，支持注册登录、收支记录管理、月度收支统计。

## 技术栈

| 层次 | 技术 |
|---|---|
| 语言 | Java 17 |
| 框架 | Spring Boot 4.0.9、Spring MVC、MyBatis |
| 数据库 | MySQL 8.0（utf8mb4） |
| 构建 | Maven |
| 前端 | 原生 HTML/CSS/JavaScript（fetch API） |

## 功能特性

- **用户模块**：注册（用户名查重、MD5 密码加密）、登录（凭证校验、统一错误提示防账号枚举）
- **记账模块**：新增收支记录、按用户查询列表、删除记录、按月统计收入/支出
- **数据安全**：删除接口通过 `user_id` 条件实现数据权限隔离，防止越权操作；金额使用 `BigDecimal` 避免精度丢失
- **前端页面**：纯静态页面随 jar 一起部署，同源调用接口，`localStorage` 保存登录态

## 项目结构

```
src/main/java/com/jzbook/accountbook/
├── AccountBookApplication.java   启动类
├── HelloController.java          用户接口（/register、/login）
├── RecordController.java         记账接口（/record/*）
├── entity/
│   ├── User.java
│   └── Record.java
└── mapper/
    ├── UserMapper.java
    └── RecordMapper.java

src/main/resources/
├── application.properties        数据库与 MyBatis 配置
└── static/index.html             前端页面
```

## 接口文档

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /register | 注册（username、password、nickname） |
| POST | /login | 登录，返回 userId、nickname |
| POST | /record/add | 新增记录（userId、type、category、amount、recordDate、remark） |
| GET | /record/list?userId= | 查询用户全部记录 |
| POST | /record/delete | 删除记录（id、userId） |
| GET | /record/summary?userId=&month= | 按月统计（month 格式 yyyy-MM） |

统一响应格式：

```json
{ "code": 0, "msg": "成功", "data": {} }
```

`code` 约定：0 成功、400 参数错误、401 未认证、404 资源不存在。

## 数据库设计

**t_user**：id、username（唯一）、password（MD5）、nickname、create_time

**t_record**：id、user_id、type（1支出/2收入）、category、amount（DECIMAL 10,2）、record_date、remark、create_time

索引：`idx_user_date (user_id, record_date)` —— 支撑"按用户 + 按日期"的列表查询与月度统计。

建表脚本：`src/main/resources/db/schema.sql`

## 本地运行

1. 启动 MySQL，创建数据库并执行 `schema.sql`
2. 修改 `application.properties` 中的数据库账号密码
3. 运行 `AccountBookApplication`
4. 浏览器访问 `http://localhost:8080`

## 测试

使用 JUnit 5 + Spring Boot Test 编写数据访问层单元测试（`src/test/java/.../MapperTest.java`），4 个用例全部通过：

| 测试用例 | 验证内容 |
|---|---|
| insertThenFindByUsername | 插入用户后能按用户名正确查出 |
| findByUsernameReturnsNullWhenNotExist | 查询不存在的用户返回 null |
| insertRecordThenListAndSummary | 新增记录后列表可查、月度统计正确返回 |
| deleteOthersRecordShouldFail | 越权删除影响 0 行，本人删除成功 |

测试类使用 `@Transactional`，每个用例执行完自动回滚，不污染数据库。

## 后续计划

- [x] JUnit 单元测试
- [ ] 部署至云服务器
- [ ] Token 登录态与接口鉴权
