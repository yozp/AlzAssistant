// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /aiModel/add */
export async function addAiModel(body: API.AiModelAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/aiModel/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /aiModel/delete */
export async function deleteAiModel(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/aiModel/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /aiModel/disable/${param0} */
export async function disableAiModel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.disableAiModelParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/aiModel/disable/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /aiModel/enable/${param0} */
export async function enableAiModel(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.enableAiModelParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/aiModel/enable/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /aiModel/get */
export async function getAiModelById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAiModelByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAiModelVO>('/aiModel/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /aiModel/list/page */
export async function listAiModelByPage(
  body: API.AiModelQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAiModelVO>('/aiModel/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /aiModel/update */
export async function updateAiModel(
  body: API.AiModelUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/aiModel/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
