-- 捷评智航考试系统 数据库初始化脚本
-- 数据库: MySQL 8.0
-- 字符集: utf8mb4
-- 创建时间: 2024

CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE exam_system;

-- =============================================
-- 1. 机构表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_org (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '机构ID',
    name        VARCHAR(100) NOT NULL COMMENT '机构名称',
    code        VARCHAR(50) UNIQUE COMMENT '机构编码',
    parent_id   BIGINT DEFAULT 0 COMMENT '父机构ID',
    type        TINYINT DEFAULT 1 COMMENT '机构类型：1-学校 2-企业 3-培训机构',
    contact     VARCHAR(50) COMMENT '联系人',
    phone       VARCHAR(20) COMMENT '联系电话',
    address     VARCHAR(200) COMMENT '地址',
    status      TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';

-- =============================================
-- 2. 部门表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_dept (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    name        VARCHAR(100) NOT NULL COMMENT '部门名称',
    org_id      BIGINT NOT NULL COMMENT '所属机构ID',
    parent_id   BIGINT DEFAULT 0 COMMENT '父部门ID',
    sort        INT DEFAULT 0 COMMENT '排序号',
    leader_id   BIGINT COMMENT '负责人ID',
    status      TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_org_id (org_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- =============================================
-- 3. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（登录账号）',
    password        VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name       VARCHAR(50) COMMENT '真实姓名',
    phone           VARCHAR(20) COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    avatar          VARCHAR(255) COMMENT '头像URL',
    gender          TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    org_id          BIGINT COMMENT '所属机构ID',
    dept_id         BIGINT COMMENT '所属部门ID',
    job_number      VARCHAR(50) COMMENT '学号/工号',
    role_code       VARCHAR(50) NOT NULL DEFAULT 'STUDENT' COMMENT '角色：SUPER_ADMIN/ORG_ADMIN/TEACHER/GRADER/STUDENT',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50) COMMENT '最后登录IP',
    remark          VARCHAR(500) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_org_id (org_id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 4. 科目表
-- =============================================
CREATE TABLE IF NOT EXISTS exam_subject (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '科目ID',
    name        VARCHAR(100) NOT NULL COMMENT '科目名称',
    code        VARCHAR(50) COMMENT '科目编码',
    description TEXT COMMENT '科目描述',
    org_id      BIGINT NOT NULL COMMENT '所属机构ID',
    parent_id   BIGINT DEFAULT 0 COMMENT '父科目ID',
    sort        INT DEFAULT 0 COMMENT '排序号',
    status      TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    creator_id  BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目表';

-- =============================================
-- 5. 题目表
-- =============================================
CREATE TABLE IF NOT EXISTS exam_question (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    content         TEXT NOT NULL COMMENT '题目内容（支持富文本/LaTeX）',
    type            TINYINT NOT NULL COMMENT '题型：1-单选 2-多选 3-判断 4-填空 5-简答 6-综合案例',
    options         TEXT COMMENT '选项（JSON格式，客观题使用）',
    answer          TEXT NOT NULL COMMENT '正确答案',
    analysis        TEXT COMMENT '答案解析',
    subject_id      BIGINT NOT NULL COMMENT '所属科目ID',
    knowledge_tags  VARCHAR(500) COMMENT '知识点标签（JSON数组）',
    difficulty      TINYINT DEFAULT 3 COMMENT '难度：1-容易 2-较易 3-中等 4-较难 5-困难',
    discrimination  DECIMAL(3,2) DEFAULT 0.50 COMMENT '区分度（0.00~1.00）',
    score           DECIMAL(6,2) DEFAULT 1.00 COMMENT '默认分值',
    audit_status    TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已审核 2-已拒绝',
    auditor_id      BIGINT COMMENT '审核人ID',
    audit_time      DATETIME COMMENT '审核时间',
    audit_remark    VARCHAR(500) COMMENT '审核备注',
    used_count      INT DEFAULT 0 COMMENT '使用次数',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-正常 2-作废',
    org_id          BIGINT COMMENT '所属机构ID',
    creator_id      BIGINT COMMENT '创建人ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    FULLTEXT INDEX ft_content (content),
    INDEX idx_subject_id (subject_id),
    INDEX idx_type (type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- =============================================
-- 6. 试卷表
-- =============================================
CREATE TABLE IF NOT EXISTS exam_paper (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '试卷ID',
    name            VARCHAR(200) NOT NULL COMMENT '试卷名称',
    subject_id      BIGINT COMMENT '所属科目ID',
    generate_mode   TINYINT DEFAULT 1 COMMENT '组卷模式：1-固定 2-随机 3-自适应',
    content         LONGTEXT COMMENT '试卷内容（JSON格式）',
    generate_rule   TEXT COMMENT '组卷规则（JSON格式，随机/自适应使用）',
    total_score     DECIMAL(8,2) DEFAULT 100.00 COMMENT '总分',
    pass_score      DECIMAL(8,2) DEFAULT 60.00 COMMENT '及格分',
    duration        INT DEFAULT 60 COMMENT '考试时长（分钟）',
    shuffle_question TINYINT DEFAULT 0 COMMENT '是否乱序题目：0-否 1-是',
    shuffle_option  TINYINT DEFAULT 0 COMMENT '是否乱序选项：0-否 1-是',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布',
    org_id          BIGINT COMMENT '所属机构ID',
    creator_id      BIGINT COMMENT '创建人ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_subject_id (subject_id),
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- =============================================
-- 7. 考试安排表
-- =============================================
CREATE TABLE IF NOT EXISTS exam_arrange (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考试ID',
    name                VARCHAR(200) NOT NULL COMMENT '考试名称',
    paper_id            BIGINT NOT NULL COMMENT '关联试卷ID',
    exam_type           TINYINT DEFAULT 1 COMMENT '考试类型：1-正式 2-练习 3-补考',
    start_time          DATETIME NOT NULL COMMENT '开始时间',
    end_time            DATETIME NOT NULL COMMENT '结束时间',
    duration            INT DEFAULT 0 COMMENT '考试时长（分钟，0=以试卷设置为准）',
    attendee_scope      TEXT COMMENT '参考人员范围（JSON）',
    show_score          TINYINT DEFAULT 1 COMMENT '是否允许查看成绩',
    show_analysis       TINYINT DEFAULT 0 COMMENT '是否允许查看解析',
    max_attempts        INT DEFAULT 1 COMMENT '最大答题次数（0=不限制）',
    switch_screen_limit INT DEFAULT 3 COMMENT '切屏次数限制（0=不限制）',
    face_verify         TINYINT DEFAULT 0 COMMENT '是否开启人脸核验',
    status              TINYINT DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-进行中 3-已结束 4-评卷中 5-已完成',
    org_id              BIGINT COMMENT '所属机构ID',
    creator_id          BIGINT COMMENT '创建人ID',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT DEFAULT 0,
    INDEX idx_paper_id (paper_id),
    INDEX idx_org_id (org_id),
    INDEX idx_status (status),
    INDEX idx_start_end_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';

-- =============================================
-- 8. 考试记录表（答卷）
-- =============================================
CREATE TABLE IF NOT EXISTS exam_record (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    exam_id          BIGINT NOT NULL COMMENT '关联考试ID',
    user_id          BIGINT NOT NULL COMMENT '考生ID',
    answers          LONGTEXT COMMENT '答卷内容（JSON格式）',
    total_score      DECIMAL(8,2) COMMENT '总分',
    objective_score  DECIMAL(8,2) COMMENT '客观题得分',
    subjective_score DECIMAL(8,2) COMMENT '主观题得分',
    passed           TINYINT COMMENT '是否通过：0-未通过 1-通过',
    status           TINYINT DEFAULT 0 COMMENT '状态：0-进行中 1-已交卷 2-评卷中 3-已完成',
    start_time       DATETIME COMMENT '开始时间',
    submit_time      DATETIME COMMENT '交卷时间',
    used_time        INT COMMENT '用时（秒）',
    switch_count     INT DEFAULT 0 COMMENT '切屏次数',
    ip_address       VARCHAR(50) COMMENT 'IP地址',
    is_similar       TINYINT DEFAULT 0 COMMENT '是否雷同卷',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exam_id (exam_id),
    INDEX idx_user_id (user_id),
    INDEX idx_exam_user (exam_id, user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- =============================================
-- 9. 评卷明细表
-- =============================================
CREATE TABLE IF NOT EXISTS exam_grade_detail (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评卷明细ID',
    record_id       BIGINT NOT NULL COMMENT '答卷记录ID',
    question_id     BIGINT NOT NULL COMMENT '题目ID',
    grader1_id      BIGINT COMMENT '第一评分员ID',
    score1          DECIMAL(6,2) COMMENT '第一评分员得分',
    comment1        TEXT COMMENT '第一评分员评语',
    grade_time1     DATETIME COMMENT '第一评分时间',
    grader2_id      BIGINT COMMENT '第二评分员ID',
    score2          DECIMAL(6,2) COMMENT '第二评分员得分',
    comment2        TEXT COMMENT '第二评分员评语',
    grade_time2     DATETIME COMMENT '第二评分时间',
    arbitrate_score DECIMAL(6,2) COMMENT '仲裁得分',
    arbitrator_id   BIGINT COMMENT '仲裁人ID',
    arbitrate_time  DATETIME COMMENT '仲裁时间',
    final_score     DECIMAL(6,2) COMMENT '最终得分',
    ai_pre_score    DECIMAL(6,2) COMMENT 'AI预评分',
    ai_score_basis  TEXT COMMENT 'AI预评依据（JSON）',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-待评 1-已评 2-待仲裁 3-已仲裁',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_id (record_id),
    INDEX idx_question_id (question_id),
    INDEX idx_grader1_id (grader1_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评卷明细表';

-- =============================================
-- 10. 操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id         BIGINT COMMENT '操作用户ID',
    username        VARCHAR(50) COMMENT '操作用户名',
    module          VARCHAR(50) COMMENT '操作模块',
    operation       VARCHAR(200) COMMENT '操作描述',
    method          VARCHAR(200) COMMENT '请求方法',
    request_url     VARCHAR(500) COMMENT '请求URL',
    request_method  VARCHAR(10) COMMENT '请求方式',
    request_ip      VARCHAR(50) COMMENT '请求IP',
    request_params  TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    status          TINYINT DEFAULT 1 COMMENT '操作状态：0-失败 1-成功',
    error_msg       TEXT COMMENT '错误信息',
    cost_time       BIGINT COMMENT '耗时（毫秒）',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =============================================
-- 11. 系统字典表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_dict (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典ID',
    dict_type   VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label  VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value  VARCHAR(100) NOT NULL COMMENT '字典值',
    sort        INT DEFAULT 0 COMMENT '排序号',
    remark      VARCHAR(500) COMMENT '备注',
    status      TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始机构
INSERT INTO sys_org (id, name, code, type, status) VALUES
(1, '捷评智航总部', 'JEPAI_ROOT', 1, 1);

-- 初始部门
INSERT INTO sys_dept (id, name, org_id, parent_id, sort, status) VALUES
(1, '技术部', 1, 0, 1, 1),
(2, '教学部', 1, 0, 2, 1);

-- 初始用户（密码均为 exam@123456，BCrypt加密）
-- BCrypt hash for "exam@123456"
INSERT INTO sys_user (id, username, password, real_name, org_id, dept_id, role_code, status) VALUES
(1, 'superadmin', '$2a$10$7JB720yubVSXvU1sHDUJsObBTanc00D6s7g2PW9Fob6wr3jnfLKki', '超级管理员', 1, 1, 'SUPER_ADMIN', 1),
(2, 'orgadmin', '$2a$10$7JB720yubVSXvU1sHDUJsObBTanc00D6s7g2PW9Fob6wr3jnfLKki', '机构管理员', 1, 2, 'ORG_ADMIN', 1),
(3, 'teacher01', '$2a$10$7JB720yubVSXvU1sHDUJsObBTanc00D6s7g2PW9Fob6wr3jnfLKki', '教师01', 1, 2, 'TEACHER', 1),
(4, 'grader01', '$2a$10$7JB720yubVSXvU1sHDUJsObBTanc00D6s7g2PW9Fob6wr3jnfLKki', '评卷员01', 1, 2, 'GRADER', 1),
(5, 'student01', '$2a$10$7JB720yubVSXvU1sHDUJsObBTanc00D6s7g2PW9Fob6wr3jnfLKki', '学员01', 1, 2, 'STUDENT', 1);

-- 初始科目
INSERT INTO exam_subject (id, name, code, org_id, parent_id, sort, status, creator_id) VALUES
(1, 'Java程序设计', 'JAVA', 1, 0, 1, 1, 1),
(2, '数据结构与算法', 'DSA', 1, 0, 2, 1, 1),
(3, '计算机网络', 'NETWORK', 1, 0, 3, 1, 1);

-- 初始字典数据
INSERT INTO sys_dict (dict_type, dict_label, dict_value, sort, status) VALUES
('question_type', '单选题', '1', 1, 1),
('question_type', '多选题', '2', 2, 1),
('question_type', '判断题', '3', 3, 1),
('question_type', '填空题', '4', 4, 1),
('question_type', '简答题', '5', 5, 1),
('question_type', '综合案例题', '6', 6, 1),
('difficulty', '容易', '1', 1, 1),
('difficulty', '较易', '2', 2, 1),
('difficulty', '中等', '3', 3, 1),
('difficulty', '较难', '4', 4, 1),
('difficulty', '困难', '5', 5, 1),
('exam_status', '草稿', '0', 1, 1),
('exam_status', '已发布', '1', 2, 1),
('exam_status', '进行中', '2', 3, 1),
('exam_status', '已结束', '3', 4, 1),
('exam_status', '评卷中', '4', 5, 1),
('exam_status', '已完成', '5', 6, 1),
('user_role', '超级管理员', 'SUPER_ADMIN', 1, 1),
('user_role', '机构管理员', 'ORG_ADMIN', 2, 1),
('user_role', '教师', 'TEACHER', 3, 1),
('user_role', '评卷员', 'GRADER', 4, 1),
('user_role', '考生', 'STUDENT', 5, 1);
