<script setup>
import { ref } from 'vue'
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()
const mode = ref('login')
const username = ref('')
const password = ref('')
const displayName = ref('')
const major = ref('湖南科技大学')
const bio = ref('正在记录湖科校园生活。')
const message = ref('')

async function submitAuth() {
  message.value = ''
  try {
    if (mode.value === 'login') {
      await store.login({
        username: username.value.trim(),
        password: password.value
      })
      return
    }

    await store.register({
      username: username.value.trim(),
      password: password.value,
      displayName: displayName.value.trim() || username.value.trim(),
      major: major.value,
      bio: bio.value
    })
  } catch (error) {
    message.value = error.message || '登录或注册失败，请稍后重试。'
  }
}

function switchMode(nextMode) {
  mode.value = nextMode
  message.value = ''
}
</script>

<template>
  <section class="auth-panel reveal">
    <p class="eyebrow">账号入口</p>
    <h2>{{ mode === 'login' ? '登录账号' : '注册账号' }}</h2>
    <p class="section-lead">
      登录后就可以发布动态、点赞、评论，也能保存自己的个人资料。
    </p>

    <div class="auth-tabs" role="group" aria-label="账号操作">
      <button type="button" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
      <button type="button" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
    </div>

    <form class="profile-form" @submit.prevent="submitAuth">
      <label class="field-block" for="auth-username">
        <span>用户名</span>
        <input id="auth-username" v-model="username" type="text" minlength="3" maxlength="40" required placeholder="例如 student001">
      </label>

      <label class="field-block" for="auth-password">
        <span>密码</span>
        <input id="auth-password" v-model="password" type="password" minlength="6" maxlength="72" required placeholder="至少 6 位">
      </label>

      <template v-if="mode === 'register'">
        <label class="field-block" for="auth-display-name">
          <span>昵称</span>
          <input id="auth-display-name" v-model="displayName" type="text" maxlength="40" placeholder="湖科同学">
        </label>

        <label class="field-block" for="auth-major">
          <span>身份/专业</span>
          <input id="auth-major" v-model="major" type="text" maxlength="80">
        </label>

        <label class="field-block" for="auth-bio">
          <span>个人简介</span>
          <textarea id="auth-bio" v-model="bio" rows="3" maxlength="240" />
        </label>
      </template>

      <p class="form-message" role="alert">{{ message }}</p>
      <button class="primary-button" type="submit" :disabled="store.authLoading.value">
        {{ store.authLoading.value ? '处理中...' : mode === 'login' ? '登录' : '注册并登录' }}
      </button>
    </form>
  </section>
</template>
