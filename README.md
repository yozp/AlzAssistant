# 基于大模型的阿尔茨海默症智能问答系统

## 项目简介

本项目是一个基于大语言模型（LLM）的阿尔茨海默症智能问答系统，旨在通过人工智能技术为用户提供专业的阿尔茨海默症相关咨询和初步评估服务。系统利用专业的医学知识库，结合RAG（检索增强生成）技术，能够根据用户输入的症状描述，进行智能分析和识别，提供初步评估和专业建议。

### 核心功能

- **智能问答**：基于大语言模型的自然语言对话，支持多轮对话和上下文理解
- **症状评估**：通过用户输入的症状描述，利用大模型分析和识别阿尔茨海默症相关症状，提供初步评估
- **专业建议**：基于知识库提供就医建议、生活方式建议、照护建议和预防建议
- **知识检索**：基于RAG技术，从专业医学知识库中检索相关信息，确保回答的准确性和专业性
- **对话管理**：支持多会话管理，保存对话历史，方便用户回顾和继续对话
- **流式响应**：采用SSE（Server-Sent Events）技术，实现AI回复的实时流式输出

## 技术栈

### 后端技术

- **框架**：Spring Boot 3.5.7
- **语言**：Java 21
- **ORM**：MyBatis Flex 1.11.0
- **数据库**：MySQL 8.0+
- **缓存**：Redis（用于Session管理和向量存储）
- **AI框架**：LangChain4j 1.1.0
- **大模型**：
  - 通义千问（Qwen-Max）- 主要对话模型
  - 通义千问 Embedding（text-embedding-v4）- 向量嵌入模型
- **工具库**：
  - Hutool 5.8.38 - Java工具类库
  - Lombok - 简化代码
  - JSoup 1.19.1 - HTML解析
- **API文档**：Knife4j（基于OpenAPI3）
- **连接池**：HikariCP

### 前端技术

- **框架**：Vue 3.5.22
- **语言**：TypeScript
- **UI组件库**：Ant Design Vue 4.2.6
- **状态管理**：Pinia 3.0.3
- **路由**：Vue Router 4.6.3
- **构建工具**：Vite 7.1.11
- **Markdown渲染**：marked 17.0.0 + highlight.js 11.11.1
- **HTTP客户端**：Axios 1.13.2

## 项目结构

```
AlzAssistant/
├── src/main/java/com/yzj/alzassistant/
│   ├── ai/                          # AI相关服务
│   │   ├── AiChatService.java       # AI聊天服务接口
│   │   ├── AiChatServiceFactory.java # AI服务工厂
│   │   ├── rag/                     # RAG相关实现
│   │   └── tools/                   # AI工具类
│   │       ├── TimeInfoTool.java    # 时间信息工具
│   │       ├── WebSearchTool.java   # 网络搜索工具
│   │       └── WebScrapingTool.java # 网页抓取工具
│   ├── config/                      # 配置类
│   │   └── RagConfig.java           # RAG配置
│   ├── controller/                  # 控制器层
│   │   ├── AppController.java       # 应用/会话管理
│   │   ├── ChatHistoryController.java # 对话历史管理
│   │   └── UserController.java     # 用户管理
│   ├── service/                     # 服务层
│   │   └── impl/                    # 服务实现
│   ├── mapper/                      # 数据访问层
│   ├── model/                       # 数据模型
│   │   ├── dto/                     # 数据传输对象
│   │   ├── entity/                  # 实体类
│   │   └── vo/                      # 视图对象
│   ├── core/                        # 核心功能
│   │   └── AiChatFacade.java        # AI聊天外观类
│   └── AlzAssistantApplication.java # 启动类
├── src/main/resources/
│   ├── application.yml              # 应用配置
│   ├── application-local.yml        # 本地环境配置
│   ├── document/                    # 知识库文档（12个Markdown文件）
│   │   ├── 01_疾病概述.md
│   │   ├── 02_病因与风险因素.md
│   │   ├── 03_症状与临床表现.md
│   │   ├── 04_疾病分期.md
│   │   ├── 05_诊断与评估.md
│   │   ├── 06_治疗方法.md
│   │   ├── 07_护理与支持.md
│   │   ├── 08_预防与早期干预.md
│   │   ├── 09_常见问题解答.md
│   │   ├── 10_最新研究进展.md
│   │   ├── 11_评估量表与工具.md
│   │   └── 12_药物详细说明.md
│   ├── prompt/
│   │   └── system_prompt.txt        # 系统提示词
│   └── mapper/                      # MyBatis映射文件
├── alz-assistant-frontend/          # 前端项目
│   ├── src/
│   │   ├── api/                     # API接口定义
│   │   ├── components/              # 组件
│   │   │   └── MarkdownRenderer.vue  # Markdown渲染组件
│   │   ├── pages/                   # 页面
│   │   │   ├── HomePage.vue         # 主页（聊天界面）
│   │   │   ├── user/                # 用户相关页面
│   │   │   └── admin/               # 管理页面
│   │   ├── router/                  # 路由配置
│   │   ├── stores/                  # 状态管理
│   │   └── main.ts                  # 入口文件
│   └── package.json
├── sql/
│   └── create_table.sql            # 数据库初始化脚本
└── pom.xml                          # Maven配置
```

## 环境要求

### 开发环境

- **JDK**：21+
- **Node.js**：^20.19.0 或 >=22.12.0
- **Maven**：3.6+
- **MySQL**：8.0+
- **Redis**：6.0+

### 第三方服务

- **通义千问API**：需要阿里云DashScope API Key
- **SerpApi**（可选）：用于网络搜索功能，需要API Key

## 快速开始

### 1. 数据库初始化

```sql
-- 执行 sql/create_table.sql 创建数据库和表结构
mysql -u root -p < sql/create_table.sql
```

### 2. 后端配置

1. 修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/alz_assistant
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

2. 修改 `src/main/resources/application-local.yml` 中的AI模型配置：

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        model-name: qwen-max
        api-key: your_dashscope_api_key
      streaming-chat-model:
        model-name: qwen-max
        api-key: your_dashscope_api_key
      embedding-model:
        model-name: text-embedding-v4
        api-key: your_dashscope_api_key

serp-api:
  api-key: your_serpapi_key  # 可选
```

### 3. 启动后端

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用IDE直接运行 AlzAssistantApplication
```

后端服务将在 `http://localhost:8126` 启动，API文档访问地址：`http://localhost:8126/api/doc.html`

### 4. 启动前端

```bash
cd alz-assistant-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5173` 启动（默认端口）

### 5. 访问系统

打开浏览器访问 `http://localhost:5173`，注册/登录后即可开始使用。

## 功能特性

### 1. 用户管理

- 用户注册和登录
- Session管理（基于Redis）
- 用户信息管理
- 管理员用户管理

### 2. 智能问答

- **多轮对话**：支持上下文理解，保持对话连贯性
- **RAG检索**：基于向量检索从知识库中获取相关信息
- **流式输出**：使用SSE实现实时流式响应，提升用户体验
- **Markdown渲染**：支持Markdown格式的AI回复，包括标题、列表、代码块等

### 3. 症状评估

系统能够根据用户输入的症状描述，提供：

- **症状分析**：识别并分析关键症状，进行分类和严重程度评估
- **初步评估**：基于知识库提供初步评估意见（明确告知不能替代专业医疗诊断）
- **专业建议**：
  - 就医建议（时机、科室、准备事项、检查建议）
  - 生活方式建议（认知活动、身体活动、饮食、睡眠、压力管理）
  - 照护建议（根据疾病阶段提供相应建议）
  - 预防建议（风险因素控制和早期干预）

### 4. 对话管理

- **多会话支持**：用户可以创建多个对话会话
- **对话历史**：自动保存对话历史，支持查看和继续对话
- **会话列表**：侧边栏抽屉式会话列表，支持分页加载
- **会话删除**：支持删除不需要的会话

### 5. AI工具集成

系统集成了多个AI工具，增强AI能力：

- **时间信息工具**：获取当前时间、日期、星期等信息
- **网络搜索工具**：通过SerpApi进行Google搜索，获取最新信息
- **网页抓取工具**：抓取网页内容进行分析

### 6. 知识库

系统内置了12个专业的阿尔茨海默症知识文档：

1. 疾病概述
2. 病因与风险因素
3. 症状与临床表现
4. 疾病分期
5. 诊断与评估
6. 治疗方法
7. 护理与支持
8. 预防与早期干预
9. 常见问题解答
10. 最新研究进展
11. 评估量表与工具
12. 药物详细说明

这些文档通过RAG技术进行向量化存储和检索，确保AI回答的准确性和专业性。

## 数据库设计

### 核心表结构

- **user**：用户表
- **app**：应用/会话表
- **chat_history**：对话历史表
- **knowledge_base**：知识库文档表
- **knowledge_chunk**：知识库分块表（用于RAG）
- **assessment_record**：评估记录表
- **symptom_record**：症状记录表

详细表结构请参考 `sql/create_table.sql`。

## API文档

系统集成了Knife4j API文档工具，启动后端服务后访问：

```
http://localhost:8126/api/doc.html
```

主要API接口：

- **用户相关**：`/api/user/*`
  - POST `/api/user/register` - 用户注册
  - POST `/api/user/login` - 用户登录
  - GET `/api/user/get/login` - 获取当前登录用户
  - POST `/api/user/logout` - 用户注销

- **应用/会话相关**：`/api/app/*`
  - POST `/api/app/add` - 创建新会话
  - POST `/api/app/update` - 更新会话
  - GET `/api/app/chat` - 流式聊天接口（SSE）
  - DELETE `/api/app/delete` - 删除会话

- **对话历史相关**：`/api/chatHistory/*`
  - GET `/api/chatHistory/list` - 获取对话历史列表

## 技术亮点

### 1. RAG（检索增强生成）

- 使用LangChain4j的Easy RAG功能
- 基于通义千问Embedding模型进行向量化
- 文档按段落分割，最大1000字符，重叠200字符
- 向量检索最多返回5个结果，最小相似度0.75
- 使用内存向量存储（InMemoryEmbeddingStore）

### 2. 流式响应

- 使用Spring WebFlux的Flux实现流式输出
- 前端使用EventSource接收SSE流
- 实时更新AI回复内容，提升用户体验

### 3. 系统提示词工程

- 精心设计的系统提示词（`system_prompt.txt`）
- 定义了AI角色为专业的神经科医生和阿尔茨海默症专家
- 规范了回答结构、语言风格和格式要求
- 包含安全与伦理规范，确保回答的准确性和安全性

### 4. 多模型支持

- 支持切换不同的大语言模型（通过配置）
- 当前主要使用通义千问（Qwen-Max）
- 预留了DeepSeek等模型的配置接口

## 开发说明

### 后端开发

1. **添加新的AI工具**：
   - 在 `ai/tools/` 目录下创建工具类
   - 使用 `@Tool` 注解标记工具方法
   - 在 `AiChatServiceFactory` 中注册工具

2. **修改知识库**：
   - 在 `src/main/resources/document/` 目录下添加或修改Markdown文档
   - 重启应用后，RAG配置会自动重新加载文档

3. **自定义系统提示词**：
   - 修改 `src/main/resources/prompt/system_prompt.txt`
   - 重启应用生效

### 前端开发

1. **添加新页面**：
   - 在 `src/pages/` 目录下创建Vue组件
   - 在 `src/router/index.ts` 中添加路由配置

2. **API接口**：
   - 使用OpenAPI自动生成TypeScript类型定义
   - 运行 `npm run openapi2ts` 更新API类型

3. **状态管理**：
   - 使用Pinia进行状态管理
   - Store文件位于 `src/stores/`

## 注意事项

### 安全提示

1. **API密钥安全**：
   - 不要将API密钥提交到代码仓库
   - 使用环境变量或配置文件管理敏感信息
   - 生产环境建议使用配置中心

2. **数据隐私**：
   - 系统不存储用户的敏感医疗信息
   - 对话历史仅用于用户体验优化
   - 建议定期清理历史数据

### 性能优化

1. **向量存储**：
   - 当前使用内存向量存储，适合小规模知识库
   - 生产环境建议使用专门的向量数据库（如Milvus、Pinecone）

2. **缓存策略**：
   - Redis用于Session管理
   - 可考虑添加对话缓存，减少重复计算

3. **数据库优化**：
   - 已为常用查询字段添加索引
   - 对话历史表建议定期归档

## 免责声明

⚠️ **重要提示**：本系统提供的信息仅供参考，不能替代专业医疗诊断和治疗。系统提供的评估和建议均为初步的、参考性的，不能作为医疗诊断依据。如有健康问题，请及时咨询专业医生或前往医疗机构就诊。

本系统严格遵循以下原则：

1. **不提供医疗诊断**：所有评估都是初步的、参考性的
2. **不替代医疗**：不能替代医生的诊断和治疗
3. **紧急情况处理**：识别到紧急情况时，会建议立即就医
4. **隐私保护**：不要求用户提供个人身份信息，保护用户隐私

## 许可证

本项目为毕业设计项目，仅供学习和研究使用。

## 作者

- 项目名称：基于大模型的阿尔茨海默症智能问答系统的设计与实现
- 开发时间：2024年

## 致谢

- [LangChain4j](https://github.com/langchain4j/langchain4j) - Java AI应用开发框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java应用框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Ant Design Vue](https://antdv.com/) - 企业级UI组件库
- [通义千问](https://tongyi.aliyun.com/) - 阿里云大语言模型

---

如有问题或建议，欢迎提出Issue或Pull Request。

