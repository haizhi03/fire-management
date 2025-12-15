<template>
  <div class="login-page">
    <div class="login-header">
      <van-icon name="fire-o" size="64" color="#ee0a24" />
      <h2>消防设施管理系统</h2>
      <p>移动端</p>
    </div>

    <van-form @submit="onSubmit" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login-btn">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
      </div>
    </van-form>

    <div class="login-footer">
      <p>默认账号: admin / admin123</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const onSubmit = async () => {
  try {
    loading.value = true
    const res = await request.post('/auth/login', form)
    
    // 保存 token
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    
    showToast('登录成功')
    router.push('/map')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
}

.login-header {
  text-align: center;
  color: white;
  margin-bottom: 40px;
}

.login-header h2 {
  margin: 16px 0 8px;
  font-size: 24px;
}

.login-header p {
  margin: 0;
  opacity: 0.8;
}

.login-form {
  background: white;
  border-radius: 16px;
  padding: 24px 16px;
}

.login-btn {
  margin-top: 24px;
  padding: 0 16px;
}

.login-footer {
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 24px;
  font-size: 12px;
}
</style>
