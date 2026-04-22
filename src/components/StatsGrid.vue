<script setup>
defineProps({
  posts: {
    type: Number,
    required: true
  },
  likes: {
    type: Number,
    required: true
  },
  comments: {
    type: Number,
    required: true
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<template>
  <div class="metric-grid" :class="{ 'compact-metrics': compact }" aria-label="平台数据">
    <div>
      <strong>{{ posts }}</strong>
      <span>动态</span>
    </div>
    <div>
      <strong>{{ likes }}</strong>
      <span>点赞</span>
    </div>
    <div>
      <strong>{{ comments }}</strong>
      <span>评论</span>
    </div>
  </div>
</template>
