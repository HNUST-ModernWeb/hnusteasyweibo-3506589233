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
const busy = ref(false)

async function handleLike() {
  await runAction(() => store.toggleLike(props.post.id))
}

async function handleDelete() {
  if (!window.confirm('确定删除这条动态吗？')) {
    return
  }

  await runAction(async () => {
    await store.deletePost(props.post.id)
    store.showToast('动态已删除。')
  })
}

async function handleComment() {
  const text = commentText.value.trim()
  if (!text) {
    return
  }

  await runAction(async () => {
    await store.addComment(props.post.id, text)
    commentText.value = ''
  })
}

async function runAction(action) {
  busy.value = true
  try {
    await action()
  } catch (error) {
    if (error.message !== 'UNAUTHENTICATED') {
      store.showToast(error.message || '操作失败，请稍后重试。')
    }
  } finally {
    busy.value = false
  }
}

function formatTime(timestamp) {
  const time = typeof timestamp === 'number' ? timestamp : Date.parse(timestamp)
  if (Number.isNaN(time)) {
    return '刚刚'
  }

  const diff = Date.now() - time
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
      <button class="action-button" :class="{ liked: post.liked }" type="button" :disabled="busy" @click="handleLike">
        {{ post.liked ? '已点赞' : '点赞' }} {{ post.likes }}
      </button>
      <button class="action-button" type="button" @click="$refs.commentInput?.focus()">评论</button>
      <button v-if="post.owned" class="action-button danger" type="button" :disabled="busy" @click="handleDelete">删除</button>
    </footer>

    <form class="comment-form" @submit.prevent="handleComment">
      <input
        ref="commentInput"
        v-model="commentText"
        type="text"
        maxlength="80"
        :placeholder="store.isAuthenticated.value ? '写一句友善的评论...' : '登录后可以评论'"
        aria-label="评论内容"
        :disabled="busy || !store.isAuthenticated.value"
      >
      <button type="submit" :disabled="busy || !store.isAuthenticated.value">发送</button>
    </form>

    <div class="comment-list">
      <div v-for="(comment, index) in post.comments" :key="comment.id || `${post.id}-comment-${index}`" class="comment">
        <strong>{{ comment.authorName || '同学' }}：</strong>{{ comment.content }}
      </div>
    </div>
  </article>
</template>
