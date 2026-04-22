<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  actionText: {
    type: String,
    default: ''
  },
  actionTo: {
    type: String,
    default: '/'
  }
})
</script>

<template>
  <div class="empty-state">
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
    <RouterLink v-if="actionText" class="primary-button" :to="actionTo">
      {{ actionText }}
    </RouterLink>
  </div>
</template>
