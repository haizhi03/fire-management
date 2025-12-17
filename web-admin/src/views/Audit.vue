<template>
  <div class="audit-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input
            v-model="keyword"
            placeholder="设施名称或地址"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 待审核列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>待审核列表</span>
          <el-tag type="warning">共 {{ pagination.total }} 条待审核</el-tag>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="facilityId" label="ID" width="80" />
        <el-table-column prop="facilityName" label="设施名称" width="180" />
        <el-table-column label="设施类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.facilityType === 1" type="primary">消防栓</el-tag>
            <el-tag v-else type="success">消防车</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">损坏</el-tag>
            <el-tag v-else type="warning">维修中</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pressure" label="压力(MPa)" width="100" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看详情
            </el-button>
            <el-button link type="success" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button link type="danger" @click="handleReject(row)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="设施详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentFacility">
        <el-descriptions-item label="设施名称">
          {{ currentFacility.facilityName }}
        </el-descriptions-item>
        <el-descriptions-item label="设施类型">
          {{ currentFacility.facilityType === 1 ? '消防栓' : '消防车' }}
        </el-descriptions-item>
        <el-descriptions-item label="经度">
          {{ currentFacility.longitude }}
        </el-descriptions-item>
        <el-descriptions-item label="纬度">
          {{ currentFacility.latitude }}
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">
          {{ currentFacility.address }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentFacility.status === 1" type="success">正常</el-tag>
          <el-tag v-else-if="currentFacility.status === 2" type="danger">损坏</el-tag>
          <el-tag v-else type="warning">维修中</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工作压力">
          {{ currentFacility.pressure }} MPa
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">
          {{ currentFacility.createTime }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 照片展示 -->
      <div v-if="currentFacility && currentFacility.photos && currentFacility.photos.length > 0" class="photos-section">
        <h4>设施照片</h4>
        <div class="photos-grid">
          <el-image
            v-for="photo in currentFacility.photos"
            :key="photo.photoId"
            :src="photo.photoUrl"
            :preview-src-list="currentFacility.photos.map(p => p.photoUrl)"
            fit="cover"
            class="photo-item"
          >
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleApprove(currentFacility)">
          审核通过
        </el-button>
        <el-button type="danger" @click="handleReject(currentFacility)">
          审核驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 驳回对话框 -->
    <el-dialog
      v-model="rejectVisible"
      title="审核驳回"
      width="500px"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentFacility = ref(null)
const keyword = ref('')

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const rejectForm = reactive({
  reason: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/audit/pending', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        keyword: keyword.value
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  keyword.value = ''
  handleSearch()
}

const handleView = async (row) => {
  try {
    const res = await request.get(`/facilities/${row.facilityId}`)
    currentFacility.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确定审核通过该设施吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await request.post(`/audit/${row.facilityId}/approve`)
      ElMessage.success('审核通过')
      detailVisible.value = false
      fetchData()
    } catch (error) {
      console.error('审核失败:', error)
    }
  })
}

const handleReject = (row) => {
  currentFacility.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }

  try {
    await request.post(`/audit/${currentFacility.value.facilityId}/reject`, null, {
      params: {
        reason: rejectForm.reason
      }
    })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    detailVisible.value = false
    fetchData()
  } catch (error) {
    console.error('驳回失败:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.audit-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.audit-container {
  position: relative;
  z-index: 1;
}

.search-card,
.table-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.photos-section {
  margin-top: 20px;
}

.photos-section h4 {
  margin-bottom: 10px;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.photo-item {
  width: 100%;
  height: 150px;
  border-radius: 4px;
  cursor: pointer;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}
</style>
