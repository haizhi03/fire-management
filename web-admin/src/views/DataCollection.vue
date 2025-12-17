<template>
  <div class="collection-container">
    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="新增设施" name="add">
        <el-card class="form-card">
          <template #header>
            <span>设施信息录入</span>
          </template>
          
          <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            label-width="100px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="设施名称" prop="facilityName">
                  <el-input v-model="form.facilityName" placeholder="请输入设施名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="设施类型" prop="facilityType">
                  <el-select v-model="form.facilityType" placeholder="请选择设施类型" style="width: 100%">
                    <el-option label="消防栓" :value="1" />
                    <el-option label="消防车" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="设施状态" prop="status">
                  <el-select v-model="form.status" placeholder="请选择设施状态" style="width: 100%">
                    <el-option label="正常" :value="1" />
                    <el-option label="损坏" :value="2" />
                    <el-option label="维修中" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="工作压力" prop="pressure">
                  <el-input-number 
                    v-model="form.pressure" 
                    :min="0" 
                    :max="10" 
                    :precision="2"
                    :step="0.1"
                    placeholder="MPa"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="16">
                <el-form-item label="地址" prop="address">
                  <el-input v-model="form.address" placeholder="请输入地址或通过地图选点获取" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item>
                  <el-button type="primary" @click="openMapDialog">
                    <el-icon><Location /></el-icon>
                    地图选点
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="经度" prop="longitude">
                  <el-input-number 
                    v-model="form.longitude" 
                    :min="-180" 
                    :max="180" 
                    :precision="6"
                    :controls="false"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="纬度" prop="latitude">
                  <el-input-number 
                    v-model="form.latitude" 
                    :min="-90" 
                    :max="90" 
                    :precision="6"
                    :controls="false"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="现场照片">
              <el-upload
                v-model:file-list="photoList"
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :on-change="handlePhotoChange"
                :on-remove="handlePhotoRemove"
                :before-upload="beforePhotoUpload"
                :limit="5"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="el-upload__tip">
                    最多上传5张照片，单张不超过5MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                提交采集数据
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="采集记录" name="records">
        <el-card class="records-card">
          <el-table :data="records" v-loading="loadingRecords" style="width: 100%">
            <el-table-column prop="facilityId" label="ID" width="80" />
            <el-table-column prop="facilityName" label="设施名称" width="150" />
            <el-table-column label="设施类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.facilityType === 1" type="primary">消防栓</el-tag>
                <el-tag v-else type="success">消防车</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="address" label="地址" min-width="200" />
            <el-table-column label="审核状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.auditStatus === 0" type="warning">待审核</el-tag>
                <el-tag v-else-if="row.auditStatus === 1" type="success">已通过</el-tag>
                <el-tag v-else type="danger">已驳回</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="驳回原因" width="150">
              <template #default="{ row }">
                <span v-if="row.auditStatus === 2">{{ row.rejectReason || '-' }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 地图选点对话框 -->
    <el-dialog v-model="mapDialogVisible" title="地图选点" width="800px">
      <div class="map-tip">点击地图选择设施位置</div>
      <div id="map-picker" class="map-picker"></div>
      <template #footer>
        <el-button @click="mapDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMapSelection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { validateFileSize, validatePhotoCount, validateRequiredFields, MAX_FILE_SIZE } from '@/utils/collectionValidation'

const activeTab = ref('add')
const formRef = ref(null)
const submitting = ref(false)
const loadingRecords = ref(false)
const mapDialogVisible = ref(false)
const photoList = ref([])
const records = ref([])

let mapInstance = null
let mapMarker = null
let tempLocation = { lng: null, lat: null, address: '' }

const form = reactive({
  facilityName: '',
  facilityType: null,
  address: '',
  longitude: null,
  latitude: null,
  status: null,
  pressure: null
})

const formRules = {
  facilityName: [{ required: true, message: '请输入设施名称', trigger: 'blur' }],
  facilityType: [{ required: true, message: '请选择设施类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }],
  longitude: [{ required: true, message: '请选择经度', trigger: 'change' }],
  latitude: [{ required: true, message: '请选择纬度', trigger: 'change' }],
  status: [{ required: true, message: '请选择设施状态', trigger: 'change' }]
}

// 打开地图选点对话框
const openMapDialog = () => {
  mapDialogVisible.value = true
  setTimeout(() => {
    initMapPicker()
  }, 100)
}

// 初始化地图选点
const initMapPicker = () => {
  const checkAMap = setInterval(() => {
    if (window.AMap) {
      clearInterval(checkAMap)
      
      if (mapInstance) {
        mapInstance.destroy()
      }
      
      mapInstance = new window.AMap.Map('map-picker', {
        zoom: 13,
        center: form.longitude && form.latitude 
          ? [form.longitude, form.latitude] 
          : [116.397428, 39.90923]
      })
      
      // 点击地图获取位置
      mapInstance.on('click', (e) => {
        const lng = e.lnglat.getLng()
        const lat = e.lnglat.getLat()
        
        tempLocation.lng = lng
        tempLocation.lat = lat
        
        // 更新标记
        if (mapMarker) {
          mapMarker.setPosition([lng, lat])
        } else {
          mapMarker = new window.AMap.Marker({
            position: [lng, lat],
            map: mapInstance
          })
        }
        
        // 逆地理编码获取地址
        window.AMap.plugin('AMap.Geocoder', () => {
          const geocoder = new window.AMap.Geocoder()
          geocoder.getAddress([lng, lat], (status, result) => {
            if (status === 'complete' && result.regeocode) {
              tempLocation.address = result.regeocode.formattedAddress
            }
          })
        })
      })
    }
  }, 100)
}

// 确认地图选点
const confirmMapSelection = () => {
  if (tempLocation.lng && tempLocation.lat) {
    form.longitude = tempLocation.lng
    form.latitude = tempLocation.lat
    if (tempLocation.address) {
      form.address = tempLocation.address
    }
    mapDialogVisible.value = false
    ElMessage.success('位置选择成功')
  } else {
    ElMessage.warning('请先在地图上选择位置')
  }
}
