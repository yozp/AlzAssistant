// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /knowledgeBase/add */
export async function addKnowledgeBase(
  body: API.KnowledgeBaseAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/knowledgeBase/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /knowledgeBase/delete */
export async function deleteKnowledgeBase(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/knowledgeBase/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /knowledgeBase/get */
export async function getKnowledgeBaseById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getKnowledgeBaseByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseKnowledgeBaseVO>('/knowledgeBase/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /knowledgeBase/list/page */
export async function listKnowledgeBaseByPage(
  body: API.KnowledgeBaseQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageKnowledgeBaseVO>('/knowledgeBase/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /knowledgeBase/rebuild-rag */
export async function rebuildRag(options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/knowledgeBase/rebuild-rag', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /knowledgeBase/update */
export async function updateKnowledgeBase(
  body: API.KnowledgeBaseUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/knowledgeBase/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /knowledgeBase/upload */
export async function uploadDocument(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadDocumentParams,
  body: {},
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/knowledgeBase/upload', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /knowledgeBase/upload/multiple */
export async function uploadMultipleDocuments(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadMultipleDocumentsParams,
  body: {},
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseListLong>('/knowledgeBase/upload/multiple', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}
