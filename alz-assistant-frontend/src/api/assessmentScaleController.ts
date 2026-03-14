// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 添加量表 POST /assessmentScale/add */
export async function addAssessmentScale(
  body: API.AssessmentScaleAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/assessmentScale/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除量表 POST /assessmentScale/delete */
export async function deleteAssessmentScale(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/assessmentScale/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 停用量表 POST /assessmentScale/disable/${param0} */
export async function disableAssessmentScale(
  params: API.disableAssessmentScaleParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/assessmentScale/disable/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 启用量表 POST /assessmentScale/enable/${param0} */
export async function enableAssessmentScale(
  params: API.enableAssessmentScaleParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.BaseResponseBoolean>(`/assessmentScale/enable/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取量表详情 GET /assessmentScale/get */
export async function getAssessmentScaleById(
  params: API.getAssessmentScaleByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAssessmentScaleVO>('/assessmentScale/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页查询量表 POST /assessmentScale/list/page */
export async function listAssessmentScaleByPage(
  body: API.AssessmentScaleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAssessmentScaleVO>('/assessmentScale/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新量表 POST /assessmentScale/update */
export async function updateAssessmentScale(
  body: API.AssessmentScaleUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/assessmentScale/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

