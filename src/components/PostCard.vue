<script setup>
import { ref } from 'vue'
import { useWeiboStore } from '../stores/useWeiboStore'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const store = useWeiboStore()
const commentText = ref('')

function handleLike() {
  store.toggleLike(props.post.id)
}

function handleDelete() {
  if (!window.confirm('确定删除这条动态吗？')) {
    return
  }

  store.deletePost(props.post.id)
  store.showToast('动态已删除。')
}

function handleComment() {
  const text = commentText.value.trim()
  if (!text) {
    return
  }

  store.addComment(props.post.id, text)
  commentText.value = ''
}

function formatTime(timestamp) {
  const diff = Date.now() - Number(timestamp)
  const minutes = Math.max(1, Math.floor(diff / 60000))

  if (minutes < 60) {
    return `${minutes} 分钟前`
  }

  const hours = Math.floor(minutes / 60)
  if (hours < 24) {
    return `${hours} 小时前`
  }

  return `${Math.floor(hours / 24)} 天前`
}
</script>

<template>
  <article class="post-card reveal">
    <header class="post-header">
      <img class="avatar" :src="post.avatar || '/default-avatar.svg'" :alt="`${post.author}的头像`">
      <div>
        <strong>{{ post.author }}</strong>
        <span>{{ [post.major, formatTime(post.createdAt), post.visibility].filter(Boolean).join(' · ') }}</span>
      </div>
    </header>

    <p class="post-content">{{ post.content }}</p>
    <img v-if="post.image" class="post-image" :src="post.image" alt="动态配图">

    <div class="tag-row">
      <span class="tag">#{{ post.topic }}</span>
      <span class="tag">{{ post.visibility }}</span>
    </div>

    <footer class="post-actions">
      <button class="action-button" :class="{ liked: post.liked }" type="button" @click="handleLike">
        {{ post.liked ? '已点赞' : '点赞' }} {{ post.likes }}
      </button>
      <button class="action-button" type="button" @click="$refs.commentInput?.focus()">评论</button>
      <button v-if="post.owned" class="action-button danger" type="button" @click="handleDelete">删除</button>
    </footer>

    <form class="comment-form" @submit.prevent="handleComment">
      <input
        ref="commentInput"
        v-model="commentText"
        type="text"
        maxlength="80"
        placeholder="写一句友善的评论..."
        aria-label="评论内容"
      >
      <button type="submit">发送</button>
    </form>

    <div class="comment-list">
      <div v-for="(comment, index) in post.comments" :key="`${post.id}-comment-${index}`" class="comment">
        <strong>同学：</strong>{{ comment }}
      </div>
    </div>
  </article>
</template>
