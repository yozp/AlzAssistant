# 数据库初始化

-- 创建库
create database if not exists alz_assistant;

-- 切换库
use alz_assistant;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 应用表
create table  app
(
    id         bigint auto_increment comment 'id' primary key,
    appName    varchar(256)                       null comment '应用名称/会话标题',
    cover      varchar(512)                       null comment '应用封面',
    initPrompt text                               null comment '应用初始化的 prompt',
    priority   int      default 0                 not null comment '优先级',
    userId     bigint                             not null comment '创建用户id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appName (appName),
    INDEX idx_userId (userId),
    INDEX idx_userId_createTime (userId, createTime)
) comment '应用' collate = utf8mb4_unicode_ci;

ALTER TABLE app
    ADD COLUMN chatGenType varchar(64) null comment '应用类型';

-- 对话历史表
create table if not exists chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息内容',
    messageType varchar(32)                        not null comment '消息类型：user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),
    INDEX idx_createTime (createTime),
    INDEX idx_appId_createTime (appId, createTime)
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 知识库文档表
create table if not exists knowledge_base
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                          not null comment '文档标题',
    content    longtext                              not null comment '文档内容',
    source     varchar(256)                          null comment '文档来源',
    category   varchar(128)                          null comment '文档分类',
    fileType   varchar(32)                           null comment '文件类型：pdf/txt/docx等',
    filePath   varchar(1024)                         null comment '文件路径',
    fileUrl    varchar(1024)                         null comment '文件URL（对象存储）',
    status     varchar(32) default 'active'          not null comment '状态：active/inactive',
    userId     bigint                                null comment '上传用户id（管理员）',
    createTime datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime    default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint     default 0                 not null comment '是否删除',
    INDEX idx_title (title),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime)
) comment '知识库文档' collate = utf8mb4_unicode_ci;

-- 为现有knowledge_base表添加fileUrl字段（如果表已存在）
ALTER TABLE knowledge_base ADD COLUMN fileUrl varchar(1024) null comment '文件URL（对象存储）' AFTER filePath;

-- 知识库分块表（RAG向量检索）
-- 注意：向量embedding建议存储在专门的向量数据库（如Milvus、Pinecone）中
-- 本表存储chunk元数据和向量ID，用于关联向量数据库
create table if not exists knowledge_chunk
(
    id           bigint auto_increment comment 'id' primary key,
    knowledgeId  bigint                             not null comment '知识库文档id',
    chunkContent text                               not null comment '分块内容',
    chunkIndex   int                                not null comment '分块序号',
    vectorId     varchar(128)                       null comment '向量数据库中的向量ID（如Milvus的ID）',
    metadata     json                               null comment '元数据（JSON格式，可存储额外信息）',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_knowledgeId (knowledgeId),
    INDEX idx_vectorId (vectorId),
    INDEX idx_knowledgeId_chunkIndex (knowledgeId, chunkIndex)
) comment '知识库分块（RAG向量检索）' collate = utf8mb4_unicode_ci;

-- 评估记录表
create table if not exists assessment_record
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             not null comment '用户id',
    appId       bigint                             null comment '关联的应用/会话id',
    symptomDesc text                               not null comment '症状描述',
    assessment  text                               null comment '评估结果',
    suggestion  text                               null comment '建议',
    riskLevel   varchar(32)                        null comment '风险等级：low/medium/high',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_userId (userId),
    INDEX idx_appId (appId),
    INDEX idx_riskLevel (riskLevel),
    INDEX idx_createTime (createTime),
    INDEX idx_userId_createTime (userId, createTime)
) comment '评估记录' collate = utf8mb4_unicode_ci;

-- 症状记录表
create table if not exists symptom_record
(
    id           bigint auto_increment comment 'id' primary key,
    assessmentId bigint                             not null comment '评估记录id',
    symptomName  varchar(256)                       not null comment '症状名称',
    symptomDesc  text                               null comment '症状描述',
    severity     varchar(32)                        null comment '严重程度：mild/moderate/severe',
    frequency    varchar(32)                        null comment '频率：rare/occasional/frequent',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_assessmentId (assessmentId),
    INDEX idx_symptomName (symptomName)
) comment '症状记录' collate = utf8mb4_unicode_ci;

-- 大模型管理表
create table if not exists ai_model
(
    id          bigint auto_increment comment 'id' primary key,
    modelName   varchar(256)                       not null comment '模型名称',
    modelKey    varchar(128)                       not null comment '模型唯一标识（如gpt-4、claude-3等）',
    apiKey      varchar(512)                       null comment 'API密钥',
    baseUrl     varchar(512)                       null comment 'API基础URL',
    modelType   varchar(64)                        null comment '模型类型（openai/claude/custom等）',
    status      varchar(32) default 'inactive'     not null comment '状态：active/inactive',
    priority    int         default 0              not null comment '优先级',
    maxTokens   int         default 2000           null comment '最大token数',
    temperature decimal(3, 2) default 0.70         null comment '温度参数（0-2）',
    topP        decimal(3, 2) default 1.00         null comment 'top_p参数（0-1）',
    description varchar(1024)                      null comment '模型描述',
    userId      bigint                             null comment '创建用户id（管理员）',
    createTime  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime    datetime    default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint     default 0              not null comment '是否删除',
    UNIQUE KEY uk_modelKey (modelKey),
    INDEX idx_modelName (modelName),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_modelType (modelType)
) comment '大模型管理' collate = utf8mb4_unicode_ci;
