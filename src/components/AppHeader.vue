<script setup>
import { RouterLink } from 'vue-router'
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()

function handleThemeToggle() {
  store.toggleTheme()
  store.showToast('页面氛围已切换。')
}
</script>

<template>
  <header class="site-header">
    <RouterLink class="brand" to="/" aria-label="湖科微光首页">
      <span class="brand-mark">H</span>
      <span>
        <strong>湖科微光</strong>
        <small>Easy Weibo</small>
      </span>
    </RouterLink>

    <nav class="top-nav" aria-label="主导航">
      <RouterLink to="/" exact-active-class="active">首页</RouterLink>
      <RouterLink to="/post" exact-active-class="active">发布</RouterLink>
      <RouterLink to="/profile" exact-active-class="active">个人主页</RouterLink>
    </nav>

    <div class="header-actions">
      <RouterLink class="user-pill" to="/profile">
        {{ store.isAuthenticated.value ? store.profile.value.name : '未登录' }}
      </RouterLink>
      <button class="ghost-button compact" type="button" @click="handleThemeToggle">
        切换氛围
      </button>
    </div>
  </header>
</template>
