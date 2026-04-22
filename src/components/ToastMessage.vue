<script setup>
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()
</script>

<template>
  <div class="toast" :class="{ show: store.toast.value.visible }" role="status" aria-live="polite">
    {{ store.toast.value.text }}
  </div>
</template>
