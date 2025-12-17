<template>
  <div class="login-container">
    <!-- 左侧打字机效果标语 -->
    <div class="slogan-box">
      <div class="slogan-content">
        <span class="slogan-icon">🔥</span>
        <span class="slogan-text">{{ displayText }}<span class="cursor">|</span></span>
      </div>
    </div>

    <!-- 右侧登录框 -->
    <div class="login-box">
      <div class="login-header">
        <h2>消防设施管理系统</h2>
        <p>Fire Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips">
        <p>测试账号：admin / 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

// 消防安全标语列表
const slogans = [
  '油锅起火不用慌，快关火源盖锅盖',
  '消防安全同心同行，平安中山共建共享',
  '电脑且有防火墙，人脑更须防火心',
  '消防隐患要消除，中山人民才享福',
  '中山拥抱消防，消防助力中山',
  '火星虽小莫轻视，燃尽基业悔断肠',
  '与火灾作斗争，是我们共同的责任',
  '多一点消防意识，谋中山民生福祉',
  '星星之火可燎原，中山防火记心间',
  '消防安全连你我，平安福城美万家'
]

const displayText = ref('')
let sloganIndex = 0
let charIndex = 0
let isDeleting = false
let typewriterTimer = null

// 打字机效果
const typeWriter = () => {
  const currentSlogan = slogans[sloganIndex]
  
  if (!isDeleting) {
    displayText.value = currentSlogan.substring(0, charIndex + 1)
    charIndex++
    
    if (charIndex === currentSlogan.length) {
      isDeleting = true
      typewriterTimer = setTimeout(typeWriter, 2000)
      return
    }
  } else {
    displayText.value = currentSlogan.substring(0, charIndex - 1)
    charIndex--
    
    if (charIndex === 0) {
      isDeleting = false
      sloganIndex = (sloganIndex + 1) % slogans.length
    }
  }
  
  typewriterTimer = setTimeout(typeWriter, isDeleting ? 50 : 100)
}

onMounted(() => {
  typeWriter()
})

onBeforeUnmount(() => {
  if (typewriterTimer) {
    clearTimeout(typewriterTimer)
  }
})

const loginForm = reactive({
  username: '',
  password: '',
  clientType: 'web'
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', loginForm)
        
        // 保存token和用户信息
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 100vh;
  padding: 0 10%;
  background-image: url('http://117.72.182.67:9000/blog/article/articleCover/c8928e7f-492b-4883-9127-81378b0238a3.jpg');
  background-size: cover;
  background-position: center;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background-image: inherit;
  background-size: cover;
  background-position: center;
  animation: breathe 8s ease-in-out infinite;
  z-index: 0;
}

@keyframes breathe {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.login-box {
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(15px);
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 24px;
}

.login-header p {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.login-form {
  margin-top: 30px;
}

.login-button {
  width: 100%;
}

.login-tips {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 12px;
}

/* 左侧标语样式 */
.slogan-box {
  flex: 1;
  max-width: 500px;
  padding: 30px;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
  margin-right: 50px;
}

.slogan-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.slogan-icon {
  font-size: 40px;
  animation: flame 0.5s ease-in-out infinite alternate;
}

@keyframes flame {
  from {
    transform: scale(1) rotate(-5deg);
  }
  to {
    transform: scale(1.1) rotate(5deg);
  }
}

.slogan-text {
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B6B, #FFE66D, #4ECDC4, #45B7D1, #96E6A1);
  background-size: 300% 300%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradient 5s ease infinite;
  min-height: 36px;
  text-shadow: none;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.slogan-box .cursor {
  animation: blink 0.8s infinite;
  font-weight: 300;
  color: #FFE66D;
  -webkit-text-fill-color: #FFE66D;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}
</style>
