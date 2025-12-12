declare namespace API {
  type App = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    chatGenType?: string
    priority?: number
    userId?: number
    createTime?: string
    editTime?: string
    updateTime?: string
    isDelete?: number
  }

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    appName?: string
    cover?: string
    priority?: number
  }

  type AppQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    chatGenType?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: number
    appName?: string
  }

  type AppVO = {
    id?: number
    appName?: string
    cover?: string
    initPrompt?: string
    chatGenType?: string
    priority?: number
    userId?: number
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type BaseResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseKnowledgeBaseVO = {
    code?: number
    data?: KnowledgeBaseVO
    message?: string
  }

  type BaseResponseListLong = {
    code?: number
    data?: number[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponsePageKnowledgeBaseVO = {
    code?: number
    data?: PageKnowledgeBaseVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ChatHistory = {
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type ChatHistoryQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    message?: string
    messageType?: string
    appId?: number
    userId?: number
    lastCreateTime?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type doChatWithSSEParams = {
    appId: number
    message: string
  }

  type getAppByIdParams = {
    id: number
  }

  type getAppVOByIdByAdminParams = {
    id: number
  }

  type getInfo1Params = {
    id: number
  }

  type getInfo2Params = {
    id: number
  }

  type getInfoParams = {
    id: number
  }

  type getKnowledgeBaseByIdParams = {
    id: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVOByIdParams = {
    id: number
  }

  type KnowledgeBaseAddRequest = {
    title?: string
    content?: string
    source?: string
    category?: string
    fileType?: string
    filePath?: string
    status?: string
  }

  type KnowledgeBaseQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    title?: string
    category?: string
    status?: string
    fileType?: string
  }

  type KnowledgeBaseUpdateRequest = {
    id?: number
    title?: string
    content?: string
    source?: string
    category?: string
    fileType?: string
    filePath?: string
    status?: string
  }

  type KnowledgeBaseVO = {
    id?: number
    title?: string
    content?: string
    source?: string
    category?: string
    fileType?: string
    filePath?: string
    status?: string
    userId?: number
    createTime?: string
    editTime?: string
    updateTime?: string
  }

  type listAppChatHistoryParams = {
    appId: number
    pageSize?: number
    lastCreateTime?: string
  }

  type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type page1Params = {
    page: PageChatHistory
  }

  type page2Params = {
    page: PageApp
  }

  type PageApp = {
    records?: App[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageKnowledgeBaseVO = {
    records?: KnowledgeBaseVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type pageParams = {
    page: PageUser
  }

  type PageUser = {
    records?: User[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type remove1Params = {
    id: number
  }

  type remove2Params = {
    id: number
  }

  type removeParams = {
    id: number
  }

  type ServerSentEventString = true

  type uploadDocumentParams = {
    category?: string
  }

  type uploadMultipleDocumentsParams = {
    category?: string
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    editTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddRequest = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserUpdateMyRequest = {
    userName?: string
    userAvatar?: string
    userProfile?: string
  }

  type UserUpdateRequest = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
