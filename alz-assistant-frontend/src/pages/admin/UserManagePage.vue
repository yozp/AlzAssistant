<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item label="用户角色">
        <a-select
          v-model:value="searchParams.userRole"
          placeholder="选择角色"
          style="width: 120px"
          allow-clear
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="admin">管理员</a-select-option>
          <a-select-option value="user">普通用户</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
        <a-button style="margin-left: 8px" @click="resetSearch">重置</a-button>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="showAddModal">新增用户</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image v-if="record.userAvatar" :src="record.userAvatar" :width="60" :height="60" />
          <a-avatar v-else :size="60">{{ record.userName?.[0] || 'U' }}</a-avatar>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="showEditModal(record)">编辑</a-button>
            <a-popconfirm title="确定要删除这个用户吗？" @confirm="doDelete(record.id)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑用户模态框 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleSubmit"
      @cancel="handleCancel"
      :confirm-loading="submitLoading"
    >
      <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="账号" name="userAccount">
          <a-input
            v-model:value="formState.userAccount"
            placeholder="请输入账号"
            :disabled="isEdit"
          />
        </a-form-item>
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像URL" name="userAvatar">
          <a-input v-model:value="formState.userAvatar" placeholder="请输入头像URL" />
        </a-form-item>
        <a-form-item label="简介" name="userProfile">
          <a-textarea
            v-model:value="formState.userProfile"
            placeholder="请输入简介"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="用户角色" name="userRole">
          <a-select v-model:value="formState.userRole" placeholder="请选择用户角色">
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="user">普通用户</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage, addUser, updateUser, getUserById } from '@/api/userController.ts'
import { message } from 'ant-design-vue'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    width: 150,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    width: 150,
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    width: 100,
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    width: 200,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 模态框相关
const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const isEdit = ref(false)
const submitLoading = ref(false)
const formState = reactive<API.UserAddRequest & { id?: number }>({
  userAccount: '',
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})

// 格式化时间
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取数据
const fetchData = async () => {
  try {
    const res = await listUserVoByPage({
      ...searchParams,
    })
    if (res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  searchParams.userAccount = ''
  searchParams.userName = ''
  searchParams.userRole = ''
  searchParams.pageNum = 1
  fetchData()
}

// 显示新增模态框
const showAddModal = () => {
  isEdit.value = false
  modalTitle.value = '新增用户'
  formState.id = undefined
  formState.userAccount = ''
  formState.userName = ''
  formState.userAvatar = ''
  formState.userProfile = ''
  formState.userRole = 'user'
  modalVisible.value = true
}

// 显示编辑模态框
const showEditModal = async (record: API.UserVO) => {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  try {
    // 获取用户详细信息
    const res = await getUserById({ id: record.id! })
    if (res.data.code === 0 && res.data.data) {
      const user = res.data.data
      formState.id = user.id
      formState.userAccount = user.userAccount || ''
      formState.userName = user.userName || ''
      formState.userAvatar = user.userAvatar || ''
      formState.userProfile = user.userProfile || ''
      formState.userRole = user.userRole || 'user'
      modalVisible.value = true
    } else {
      message.error('获取用户信息失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
    message.error('获取用户信息失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formState.userAccount || !formState.userName || !formState.userRole) {
    message.error('请填写完整信息')
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      // 编辑用户
      const res = await updateUser({
        id: formState.id,
        userName: formState.userName,
        userAvatar: formState.userAvatar,
        userProfile: formState.userProfile,
        userRole: formState.userRole,
      })
      if (res.data.code === 0) {
        message.success('更新成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error('更新失败，' + res.data.message)
      }
    } else {
      // 新增用户
      const res = await addUser({
        userAccount: formState.userAccount,
        userName: formState.userName,
        userAvatar: formState.userAvatar,
        userProfile: formState.userProfile,
        userRole: formState.userRole,
      })
      if (res.data.code === 0) {
        message.success('新增成功')
        modalVisible.value = false
        fetchData()
      } else {
        message.error('新增失败，' + res.data.message)
      }
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  modalVisible.value = false
}

// 删除数据
const doDelete = async (id?: number) => {
  if (!id) {
    return
  }
  try {
    const res = await deleteUser({ id })
    if (res.data.code === 0) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败，' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>

