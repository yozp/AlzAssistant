# 基于大模型的阿尔茨海默症智能问答系统

## 项目简介

本项目是一个基于大语言模型（LLM）的阿尔茨海默症智能问答系统，旨在通过人工智能技术为用户提供专业的阿尔茨海默症相关咨询和初步评估服务。系统利用专业的医学知识库，结合RAG（检索增强生成）技术，能够根据用户输入的症状描述，进行智能分析和识别，提供初步评估和专业建议。

### 核心功能

- **双模式对话**：**普通对话**（可选是否启用知识库检索）与**智能体模式**（结构化流式输出，医疗场景专用智能体管线）
- **智能问答**：基于大语言模型的自然语言对话，多轮上下文；对话记忆通过 **Redis** 与会话历史协同加载
- **症状与评估**：大模型辅助的症状分析；**量表评估**（可配置量表模板、计分规则、风险等级）；**评估记录**分页查询与留存
- **专业建议**：基于知识库提供就医、生活方式、照护与预防等建议（系统提示词约束回答风格与边界）
- **知识检索（RAG）**：从内置文档与后台上传的文档中向量化检索，支持管理端**重建向量索引**
- **知识库管理**：支持文档上传（含多文件）、元数据维护及 RAG 重建（管理员）
- **运维与扩展**：可切换 **DashScope / DeepSeek / OpenAI 兼容 / Ollama** 等模型（数据库 `ai_model` 与默认配置配合）；**Actuator + Prometheus** 健康与指标
- **对话管理**：多会话、历史分页、侧栏会话列表；支持**停止生成**、**联想建议**等接口
- **流式响应**：SSE 实时输出；智能体模式为结构化 JSON 流，前端按需解析展示

## 技术栈

### 后端技术

- **框架**：Spring Boot 3.5.7
- **语言**：Java 21
- **ORM**：MyBatis Flex 1.11.0
- **数据库**：MySQL 8.0+
- **缓存**：Redis（Session、LangChain4j 对话记忆）；向量检索使用应用内 **内存向量库**（非 Redis 向量存储）
- **AI框架**：LangChain4j 1.1.0（部分 Starter 为 `1.1.0-beta7`，与社区 DashScope / Ollama / OpenAI / Redis 等集成）
- **大模型与嵌入**（以 `application.yml`、`application-local.yml` 及 `ai_model` 表为准）：
  - **阿里云 DashScope**：如 **text-embedding-v4** 等嵌入模型
  - **对话模型**：可通过表内启用项切换 **DashScope / DeepSeek（OpenAI 兼容）/ Ollama 本地** 等
- **工具库**：
  - Hutool 5.8.38 - Java工具类库
  - Lombok - 简化代码
  - JSoup 1.19.1 - HTML解析
- **文档与报告**：Apache POI 5.2.5（Office 文档）、PDFBox 3.0.3、iText7 8.0.5（含中文字体）
- **对象存储**：腾讯云 COS SDK 5.6.54（头像、聊天附件、知识库文件等）
- **缓存**：Caffeine（AI 服务实例缓存等）
- **可观测性**：Spring Boot Actuator + Micrometer Prometheus
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
- **代码质量**：ESLint 9、Prettier 3.6、vue-tsc

## 仓库与目录说明

若工作区根目录为 `AlzAssistant` 的上级文件夹，实际 Maven 工程与本文档中的路径均在子目录 **`AlzAssistant/`** 下（即与 `pom.xml` 同级）。下文命令默认在该子目录中执行。

## 项目结构

```
AlzAssistant/
├── src/main/java/com/yzj/alzassistant/
│   ├── ai/                          # AI 相关
│   │   ├── AiChatService.java       # AI 聊天服务接口（LangChain4j AiServices）
│   │   ├── AiChatServiceFactory.java # 按 appId / 是否 RAG 构建服务，Caffeine 缓存
│   │   ├── agent/                   # 智能体（如医疗智能体流式管线）
│   │   └── tools/                   # 普通对话可调用的工具
│   │       ├── TimeInfoTool.java    # 时间信息
│   │       ├── WebSearchTool.java   # 联网搜索（SerpApi）
│   │       ├── WebScrapingTool.java # 网页抓取
│   │       ├── MapSearchTool.java   # 地图/附近 POI（高德）
│   │       ├── PDFReportTool.java   # PDF 报告相关
│   │       └── TerminateTool.java   # 智能体终止等控制
│   ├── rag/                         # RAG：ingestion、检索器、内存向量库配置
│   ├── core/                        # AiChatFacade、ChatFileSaver 等门面与编排
│   ├── controller/                  # 控制器层
│   │   ├── AppController.java       # 会话、流式聊天、建议、管理端应用接口
│   │   ├── ChatHistoryController.java
│   │   ├── UserController.java      # 用户、登录态、头像/附件上传等
│   │   ├── KnowledgeBaseController.java
│   │   ├── AssessmentScaleController.java
│   │   ├── AssessmentRecordController.java
│   │   └── AiModelController.java
│   ├── service/                     # 服务层
│   │   └── impl/                    # 服务实现
│   ├── mapper/                      # 数据访问层
│   ├── model/                       # 数据模型
│   │   ├── dto/                     # 数据传输对象
│   │   ├── entity/                  # 实体类
│   │   └── vo/                      # 视图对象
│   └── AlzAssistantApplication.java # 启动类
├── src/main/resources/
│   ├── application.yml              # 应用配置（公共结构，敏感项用占位）
│   ├── application-local.yml        # 本地环境（勿提交密钥）
│   ├── application-prod.yml         # 生产环境（勿提交密钥）
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
│   │   │   ├── HomePage.vue         # 主页（聊天）
│   │   │   ├── user/                # 登录注册、个人资料、评估记录等
│   │   │   └── admin/               # 用户管理、知识库、量表、AI 模型管理
│   │   ├── config/                  # 如 env.ts（API 基地址）
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

- **阿里云 DashScope**：对话与/或 Embedding 等（按你启用的模型填写 Key）
- **SerpApi**（可选）：联网搜索工具
- **高德地图 Web 服务 Key**（可选）：附近医院/POI 等地图类工具（`amap.api-key`）
- **腾讯云 COS**（可选）：头像、聊天附件、知识库文件存储（`cos.client.*`）

## 快速开始

### 1. 数据库初始化

```sql
-- 执行 sql/create_table.sql 创建数据库和表结构
mysql -u root -p < sql/create_table.sql
```

### 2. 后端配置

1. **激活配置**：`application.yml` 中默认 `spring.profiles.active: local`。数据库、Redis、各 API Key 等敏感项建议放在 **`application-local.yml`**（或生产环境 `application-prod.yml`），勿提交密钥到仓库。

2. **主配置结构**（`application.yml`）：已包含数据源占位、`alz.rag.*`（检索条数、相似度阈值、分块参数、启动是否加载知识库）、`langchain4j`（DashScope / OpenAI 兼容 / Ollama）、`serp-api`、`amap`、`cos`、`management.endpoints` 等。请在本机复制并填写 `application-local.yml` 中的真实连接串与密钥。

3. **示例（本地 `application-local.yml`，按实际裁剪）**：

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

langchain4j:
  community:
    dashscope:
      embedding-model:
        model-name: text-embedding-v4
        api-key: your_dashscope_api_key
  # 另可按需配置 open-ai（DeepSeek 等）、ollama 等

serp-api:
  api-key: your_serpapi_key  # 可选
```

对话所用**具体聊天模型**以系统内 **`ai_model` 表启用项**与 `ai-model.default` 默认配置为准；切换模型后工厂会清理 Caffeine 缓存以加载新实例。

### 3. 启动后端

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用IDE直接运行 AlzAssistantApplication
```

后端上下文路径为 **`/api`**（见 `server.servlet.context-path`），即服务根地址为 `http://localhost:8126/api`。  
API 文档（Knife4j）：`http://localhost:8126/api/doc.html`  
健康检查：`http://localhost:8126/api/actuator/health`  
Prometheus 指标：`http://localhost:8126/api/actuator/prometheus`

### 4. 启动前端

```bash
cd alz-assistant-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务默认 `http://localhost:5173`（以 Vite 控制台为准）。

**环境变量**：在 `alz-assistant-frontend` 目录配置 `VITE_API_BASE_URL`，指向后端 API 根路径（须包含 `/api`），例如：

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8126/api
```

生产构建可参考现有 `.env.production`。`src/config/env.ts` 通过 `import.meta.env.VITE_API_BASE_URL` 读取。

**常用脚本**：`npm run build`（含类型检查）、`npm run preview`、`npm run lint`、`npm run format`、`npm run openapi2ts`。

### 5. 访问系统

浏览器打开前端地址，注册/登录后使用。管理员功能需具备相应角色权限。

## 功能特性

### 1. 用户管理

- 用户注册和登录
- Session管理（基于Redis）
- 用户信息管理
- 管理员用户管理

### 2. 智能问答与对话类型

- **普通对话（`chat`）**：多轮上下文；可选 **useRag** 仅在本模式下注入 `ContentRetriever`
- **智能体模式（`agent`）**：走 `MedicalAgentService` 结构化流，与普聊管线分离
- **RAG**：向量库为 **`InMemoryEmbeddingStore`**（启动时/bootstrap 与知识库重建可写入；Redis 向量自动配置已排除，避免与内存库混用）
- **流式输出**：SSE；前端 Markdown 渲染组件展示普聊回复
- **附件与头像**：用户侧支持上传头像、聊天附件（依赖 COS 等存储配置）

### 3. 症状与量表评估

- **大模型辅助**：根据症状描述给出分析与建议时，须遵守免责声明（非诊断）
- **量表评估**：管理员维护 `assessment_scale`（题目 JSON、计分规则 JSON）；用户作答后产生 `assessment_record`（总分、风险等级、规则快照等）
- **评估记录**：用户中心可查看个人评估历史（分页接口）

### 4. 对话管理

- **多会话支持**：用户可以创建多个对话会话
- **对话历史**：自动保存对话历史，支持查看和继续对话
- **会话列表**：侧边栏抽屉式会话列表，支持分页加载
- **会话删除**：支持删除不需要的会话

### 5. AI 工具（普通对话 / 智能体按实现挂载）

- **时间信息**：当前时间、日期等
- **网络搜索**：SerpApi（需 Key）
- **网页抓取**：JSoup 抓取正文
- **地图搜索**：高德（附近医院等，需 Key）
- **PDF 报告 / 终止工具**：服务智能体与报告导出等能力

### 6. 知识库

系统内置了12个专业的阿尔茨海默症知识文档（`src/main/resources/document/`）：

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

内置文档与管理员上传的文档均可纳入 **Embedding** 与检索流程；管理端支持 **`/knowledgeBase/rebuild-rag`** 重建索引。

### 7. 后台管理

- **用户管理**：用户列表与维护
- **知识库管理**：上传、列表、删除、RAG 重建
- **评估量表管理**：量表 CRUD、启用/禁用
- **AI 模型管理**：多模型配置、启用/禁用与优先级（与运行时切换逻辑配合）

## 数据库设计

### 核心表结构

- **user**：用户表
- **app**：应用/会话表（含 `chatGenType`：`chat` / `agent`）
- **chat_history**：对话历史（含附件 JSON 字段）
- **knowledge_base**：知识库文档表
- **knowledge_chunk**：知识库分块表（元数据与向量关联预留）
- **assessment_scale**：评估量表模板
- **assessment_record**：评估记录表（AI / 量表、报告链接等）
- **symptom_record**：症状记录表
- **ai_model**：大模型配置表

详细表结构请参考 `sql/create_table.sql`。

## API文档

系统集成了Knife4j API文档工具，启动后端服务后访问：

```
http://localhost:8126/api/doc.html
```

以下路径均相对于 **`/api`**（完整示例：`http://localhost:8126/api/user/login`）。

**用户** `/user`：注册、登录、登出、当前用户、资料更新、头像/聊天附件上传、管理员用户分页等。

**应用/会话** `/app`：新增/更新/删除会话、分页列表、`GET|POST /app/chat`（SSE，支持 `chatType`、`useRag` 等）、`/app/chat/stop`、`/app/suggestions`、管理端会话维护等。

**对话历史** `/chatHistory`：按 `appId` 拉取会话消息、管理端列表等。

**知识库** `/knowledgeBase`：上传、批量上传、增删改查、分页、`/rebuild-rag`。

**评估量表** `/assessmentScale`：增删改、启用/禁用、分页与 VO 查询。

**评估记录** `/assessmentRecord`：新增、我的分页列表等。

**AI 模型** `/aiModel`：增删改、启用/禁用、分页、详情。

完整参数与请求体以 **Knife4j**（`/api/doc.html`）为准。

## 技术亮点

### 1. RAG（检索增强生成）

- LangChain4j：`EmbeddingStoreIngestor` + `EmbeddingStoreContentRetriever`，外层 `RagContentRetriever` 在检索为空时可返回固定提示
- 嵌入模型通过 **DashScope** 等配置注入（见 `langchain4j.community.dashscope.embedding-model`）
- 分块与检索阈值由 **`alz.rag`** 配置：**默认**最大块 1000 字符、重叠 200、最多返回 5 条、**最小相似度 0.5**（可在 yml 中调整）
- 向量存储：**`InMemoryEmbeddingStore`**（应用重启需重建或依赖 `bootstrap-enabled` 与知识库重建流程）

### 2. 流式响应与记忆

- 使用 **Spring WebFlux `Flux`** 输出 SSE
- 前端使用 **EventSource**（或同等能力）消费普聊流；智能体模式需解析 **JSON 分片**
- **Redis**：Spring Session + LangChain4j `RedisChatMemoryStore` 会话记忆；与 DB 历史加载协同

### 3. 系统提示词工程

- 精心设计的系统提示词（`system_prompt.txt`）
- 定义了AI角色为专业的神经科医生和阿尔茨海默症专家
- 规范了回答结构、语言风格和格式要求
- 包含安全与伦理规范，确保回答的准确性和安全性

### 4. 多模型支持

- **`ai_model` 表**维护多组 API 与模型名；服务运行时通过 `AiModelSwitchService` 选择当前流式/非流式模型
- **`application.yml`** 中同时预留 **DashScope、OpenAI 兼容（如 DeepSeek）、Ollama** 等块，按启用情况与表数据组合使用
- 切换后 **`AiChatServiceFactory.clearCache()`** 使新会话使用新模型实例

## 开发说明

### 后端开发

1. **添加新的AI工具**：
   - 在 `ai/tools/` 目录下创建工具类
   - 使用 `@Tool` 注解标记工具方法
   - 在 `AiChatServiceFactory` 中注册工具

2. **修改知识库**：
   - 在 `src/main/resources/document/` 下维护内置 Markdown，或通过管理端上传；需要时使用 **`/knowledgeBase/rebuild-rag`** 刷新内存向量
   - `alz.rag.bootstrap-enabled` 控制启动时是否自动加载

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
   - Redis：Session、聊天记忆
   - Caffeine：按 `appId` + `useRag` 缓存 `AiServices` 实例

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
- 开发周期：2024–2025（毕业设计 / 学习研究用途，以仓库实际为准）

## 致谢

- [LangChain4j](https://github.com/langchain4j/langchain4j) - Java AI应用开发框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java应用框架
- [Vue.js](https://vuejs.org/) - 渐进式JavaScript框架
- [Ant Design Vue](https://antdv.com/) - 企业级UI组件库
- [通义千问](https://tongyi.aliyun.com/) - 阿里云大语言模型

---

如有问题或建议，欢迎提出Issue或Pull Request。

