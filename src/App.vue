<script setup>
import { watchEffect } from 'vue'
import { RouterView } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import ToastMessage from './components/ToastMessage.vue'
import { useWeiboStore } from './stores/useWeiboStore'

const store = useWeiboStore()

watchEffect(() => {
  if (typeof document === 'undefined') {
    return
  }

  document.body.classList.toggle('cool-theme', store.theme.value === 'cool')
})
</script>

<template>
  <AppHeader />
  <RouterView />
  <ToastMessage />
</template>
