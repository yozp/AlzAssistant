import request from '@/request'

/** 添加评估记录 POST /assessmentRecord/add */
export async function addAssessmentRecord(
  body: API.AssessmentRecordAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/assessmentRecord/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页查询我的评估记录 POST /assessmentRecord/my/list/page/vo */
export async function listMyAssessmentRecordVOByPage(
  body: API.AssessmentRecordQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAssessmentRecordVO>('/assessmentRecord/my/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
