<template>
  <div class="facilities-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="设施类型">
          <el-select v-model="searchForm.facilityType" placeholder="全部" clearable>
            <el-option label="消防栓" :value="1" />
            <el-option label="消防车" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="设施状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="损坏" :value="2" />
            <el-option label="维修中" :value="3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="全部" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="设施名称或地址"
            clearable
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

    <!-- 数据表格 -->
    <el-card class="table-card">
      <div class="table-toolbar">
        <el-button 
          type="danger" 
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedRows.length }})
        </el-button>
        <el-button 
          type="success" 
          :disabled="selectedRows.length === 0"
          @click="handleBatchExport"
        >
          <el-icon><Download /></el-icon>
          导出选中
        </el-button>
        <el-button type="primary" @click="handleExportAll">
          <el-icon><Download /></el-icon>
          导出全部
        </el-button>
      </div>
      
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
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
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pressure" label="压力(MPa)" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
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
        <el-descriptions-item label="采集人员">
          {{ currentFacility.collectorName }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="currentFacility.auditStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentFacility.auditStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else type="danger">已驳回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
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
const currentFacility = ref(null)
const selectedRows = ref([])

const searchForm = reactive({
  facilityType: null,
  status: null,
  auditStatus: null,
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/facilities', {
      params: {
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
        ...searchForm
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
  searchForm.facilityType = null
  searchForm.status = null
  searchForm.auditStatus = null
  searchForm.keyword = ''
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

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '删除后将无法恢复，确定要删除该设施吗？',
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      distinguishCancelAndClose: true
    }
  ).then(async () => {
    try {
      await request.delete(`/facilities/${row.facilityId}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个设施吗？删除后将无法恢复！`,
    '批量删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      distinguishCancelAndClose: true
    }
  ).then(async () => {
    const loading = ElMessage({
      message: '正在删除...',
      type: 'info',
      duration: 0
    })
    
    try {
      // 批量删除
      const deletePromises = selectedRows.value.map(row => 
        request.delete(`/facilities/${row.facilityId}`)
      )
      await Promise.all(deletePromises)
      
      loading.close()
      ElMessage.success(`成功删除 ${selectedRows.value.length} 个设施`)
      selectedRows.value = []
      fetchData()
    } catch (error) {
      loading.close()
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 导出选中数据
const handleBatchExport = () => {
  exportToExcel(selectedRows.value, `设施数据_选中_${new Date().getTime()}.xlsx`)
}

// 导出全部数据
const handleExportAll = async () => {
  try {
    const loading = ElMessage({
      message: '正在导出...',
      type: 'info',
      duration: 0
    })
    
    // 获取所有数据（不分页）
    const res = await request.get('/facilities', {
      params: {
        pageNum: 1,
        pageSize: 10000,
        ...searchForm
      }
    })
    
    loading.close()
    exportToExcel(res.data.records, `设施数据_全部_${new Date().getTime()}.xlsx`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导出Excel
const exportToExcel = (data, filename) => {
  if (!data || data.length === 0) {
    ElMessage.warning('没有数据可导出')
    return
  }
  
  // 准备导出数据
  const exportData = data.map(item => ({
    'ID': item.facilityId,
    '设施名称': item.facilityName,
    '设施类型': item.facilityType === 1 ? '消防栓' : '消防车',
    '地址': item.address,
    '经度': item.longitude,
    '纬度': item.latitude,
    '状态': item.status === 1 ? '正常' : item.status === 2 ? '损坏' : '维修中',
    '审核状态': item.auditStatus === 0 ? '待审核' : item.auditStatus === 1 ? '已通过' : '已驳回',
    '工作压力(MPa)': item.pressure || '-',
    '创建时间': item.createTime
  }))
  
  // 创建工作表
  const ws = window.XLSX.utils.json_to_sheet(exportData)
  const wb = window.XLSX.utils.book_new()
  window.XLSX.utils.book_append_sheet(wb, ws, '设施数据')
  
  // 下载文件
  window.XLSX.writeFile(wb, filename)
  ElMessage.success('导出成功')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.facilities-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.facilities-container {
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

.table-toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 10px;
}
</style>
