# 捷评智航考试系统

> 高可用智能化在线考试与智能评卷一体化平台

## 系统简介

捷评智航考试系统面向职业院校、中大型企业内训部门、合规教培机构，以「智能捷速评卷」为核心竞争力，实现从题库搭建、组卷、考试、评卷到数据分析的全流程闭环。

## 技术架构

| 层次 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 2.7.x + Java 8 |
| 持久层 | MyBatis-Plus + MySQL 8.0 |
| 缓存 | Redis（答题进度实时缓存） |
| 消息队列 | RabbitMQ（异步评卷） |
| 安全 | Spring Security + JWT |
| API文档 | Knife4j (Swagger) |
| 反向代理 | Nginx（加权轮询负载均衡） |
| 前端框架 | Vue 3 + Element Plus |
| 状态管理 | Pinia |
| 可视化 | ECharts |
| 部署 | Docker Compose + Tomcat集群 |

## 核心特色

### 1. 多维度数据权限隔离
支持「机构-部门-科目」三级数据隔离，RBAC权限控制精确到单张试卷、单个题库。

### 2. 低代码批量题库导入
Excel模板一键批量导入，自动校验格式、去重，导入效率较手动录入提升90%。

### 3. 多模式智能组卷引擎
- **固定组卷**：人工选题，所有考生试卷一致
- **随机组卷**：按题型/知识点/难度随机抽题，天然防作弊
- **自适应组卷**：基于考生过往数据动态调整难度

### 4. 全链路防作弊管控
- 切屏检测与次数限制（超限自动交卷）
- 动态水印（考生姓名+工号）
- 禁用复制粘贴、右键菜单
- 题目/选项乱序
- 后台实时监控 + 监考员强制收卷

### 5. 断网续答高可用
Redis实时缓存每道题答案，断网重连后自动恢复完整答题进度。

### 6. 双轨智能捷评引擎
- **客观题**：秒级自动判分，准确率100%
- **主观题**：关键词权重匹配AI预评分 + 双评机制（分差超阈值自动仲裁）

### 7. 多维度学情数据分析
基于ECharts实现通过率、分数段分布、知识点掌握度等可视化分析。

## 目录结构

```
Online-Exam-System/
├── exam-backend/               # Spring Boot 后端
│   ├── src/main/java/com/jepai/exam/
│   │   ├── common/            # 公共组件（配置/工具/异常/响应）
│   │   └── modules/           # 业务模块
│   │       ├── auth/          # 认证模块
│   │       ├── user/          # 用户/机构/部门管理
│   │       ├── question/      # 题库管理
│   │       ├── paper/         # 试卷管理
│   │       ├── exam/          # 考试管理（含防作弊）
│   │       ├── grade/         # 评卷模块（智能+人工）
│   │       ├── stats/         # 统计分析
│   │       └── system/        # 系统管理（日志/字典）
│   ├── src/main/resources/
│   │   └── application.yml    # 配置文件
│   ├── Dockerfile
│   └── pom.xml
├── exam-frontend/              # Vue 3 前端
│   ├── src/
│   │   ├── api/               # API接口层
│   │   ├── components/        # 公共组件（布局等）
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia状态管理
│   │   ├── utils/             # 工具函数（request等）
│   │   └── views/             # 页面组件
│   │       ├── auth/          # 登录页
│   │       ├── admin/         # 管理端页面
│   │       └── student/       # 考生端页面
│   ├── vite.config.js
│   └── package.json
├── docs/
│   ├── init.sql               # 数据库初始化脚本
│   └── nginx.conf             # Nginx配置
└── docker-compose.yml         # Docker Compose一键部署
```

## 快速开始

### 环境要求

- JDK 8+
- Node.js 18+
- MySQL 8.0
- Redis 6+
- RabbitMQ 3.x

### 方式一：Docker Compose 一键部署（推荐）

```bash
# 克隆项目
git clone https://github.com/fzzzhizhi-w/Online-Exam-System.git
cd Online-Exam-System

# 构建前端
cd exam-frontend
npm install
npm run build
cd ..

# 启动所有服务
docker-compose up -d

# 访问地址
# 前端：http://localhost
# API文档：http://localhost/doc.html
# RabbitMQ管理：http://localhost:15672
```

### 方式二：本地开发部署

**1. 初始化数据库**

```sql
mysql -u root -p < docs/init.sql
```

**2. 启动后端**

```bash
cd exam-backend
# 修改 src/main/resources/application.yml 中的数据库连接信息
mvn spring-boot:run
# 后端访问地址：http://localhost:8080
# API文档地址：http://localhost:8080/doc.html
```

**3. 启动前端**

```bash
cd exam-frontend
npm install
npm run dev
# 前端访问地址：http://localhost:3000
```

## 测试账号

| 角色 | 用户名 | 密码 | 权限说明 |
|------|--------|------|---------|
| 超级管理员 | superadmin | exam@123456 | 全量权限 |
| 机构管理员 | orgadmin | exam@123456 | 本机构管理权限 |
| 教师 | teacher01 | exam@123456 | 题库/试卷/考试管理 |
| 评卷员 | grader01 | exam@123456 | 评卷权限 |
| 考生 | student01 | exam@123456 | 参加考试、查看成绩 |

## API 文档

启动后端后访问：`http://localhost:8080/doc.html`

主要接口：
- `POST /api/auth/login` - 用户登录
- `GET /api/questions/page` - 题目分页查询
- `POST /api/questions/import` - Excel批量导入
- `POST /api/exams/{examId}/enter` - 进入考试
- `POST /api/exams/answer/save` - 实时保存答题进度
- `POST /api/exams/answer/submit` - 交卷
- `GET /api/stats/exam/{examId}/overview` - 考试统计

## 数据库设计

核心表结构：

| 表名 | 说明 |
|------|------|
| sys_org | 机构表 |
| sys_dept | 部门表 |
| sys_user | 用户表 |
| exam_subject | 科目表 |
| exam_question | 题目表（支持全文检索） |
| exam_paper | 试卷表 |
| exam_arrange | 考试安排表 |
| exam_record | 答卷记录表 |
| exam_grade_detail | 评卷明细表（主观题双评） |
| sys_operation_log | 操作日志表 |
| sys_dict | 系统字典表 |

## 性能指标

- 支持1000+用户同时在线并发考试
- 峰值TPS ≥ 2000
- 核心答题、交卷接口平均响应 ≤ 200ms
- 系统可用性 ≥ 99.9%

## License

MIT
