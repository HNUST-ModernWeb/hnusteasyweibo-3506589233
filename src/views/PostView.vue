<script setup>
import { computed, ref } from 'vue'
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()

const content = ref('')
const topic = ref('学习')
const visibility = ref('全校可见')
const selectedImage = ref('')
const message = ref('')
const messageType = ref('')
const fileInput = ref(null)

const topics = ['学习', '生活', '活动', '失物招领']
const visibilities = ['全校可见', '仅同学可见', '仅自己可见']

const recentPosts = computed(() => store.posts.value.slice(0, 3))
const previewText = computed(() => content.value.trim() || '你输入的动态会显示在这里。')

function handleImageChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }

  if (!file.type.startsWith('image/')) {
    setMessage('请选择图片文件。')
    event.target.value = ''
    return
  }

  const reader = new FileReader()
  reader.addEventListener('load', () => {
    selectedImage.value = reader.result
    setMessage('')
  })
  reader.readAsDataURL(file)
}

function clearImage() {
  selectedImage.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function resetForm() {
  content.value = ''
  topic.value = '学习'
  visibility.value = '全校可见'
  clearImage()
  setMessage('')
}

function submitPost() {
  const text = content.value.trim()

  if (text.length < 5) {
    setMessage('动态内容至少需要 5 个字。')
    return
  }

  store.addPost({
    content: text,
    topic: topic.value,
    visibility: visibility.value,
    image: selectedImage.value
  })
  store.showToast('发布成功，已经保存到首页信息流。')
  resetForm()
}

function setMessage(text, type = 'error') {
  message.value = text
  messageType.value = text ? type : ''
}

function trimText(text, maxLength) {
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

function formatTime(timestamp) {
  const minutes = Math.max(1, Math.floor((Date.now() - Number(timestamp)) / 60000))
  if (minutes < 60) {
    return `${minutes} 分钟前`
  }

  return `${Math.floor(minutes / 60)} 小时前`
}
</script>

<template>
  <main class="page-grid">
    <section class="form-panel reveal">
      <p class="eyebrow">CREATE POST</p>
      <h1>发布一条校园动态</h1>
      <p class="section-lead">
        支持文字、话题分类、可见范围和本地图片预览。发布后会保存到浏览器本地，并出现在首页和个人主页。
      </p>

      <form class="post-form" @submit.prevent="submitPost">
        <label class="field-block" for="post-content">
          <span>动态内容</span>
          <textarea
            id="post-content"
            v-model="content"
            rows="7"
            maxlength="280"
            placeholder="例如：图书馆四楼靠窗的位置很适合复习，今晚还有很好看的晚霞。"
            required
          />
          <small><span>{{ content.length }}</span>/280 字</small>
        </label>

        <div class="field-row">
          <label class="field-block" for="post-topic">
            <span>话题分类</span>
            <select id="post-topic" v-model="topic">
              <option v-for="item in topics" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>

          <label class="field-block" for="post-visibility">
            <span>可见范围</span>
            <select id="post-visibility" v-model="visibility">
              <option v-for="item in visibilities" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>
        </div>

        <label class="upload-zone" for="post-image">
          <input id="post-image" ref="fileInput" type="file" accept="image/*" @change="handleImageChange">
          <span>上传图片</span>
          <small>选择一张本地图片，页面会实时预览</small>
        </label>

        <div v-if="selectedImage" class="image-preview">
          <img :src="selectedImage" alt="待发布图片预览">
          <button class="ghost-button compact" type="button" @click="clearImage">移除图片</button>
        </div>

        <p class="form-message" :class="messageType" role="alert">{{ message }}</p>

        <div class="form-actions">
          <button class="primary-button" type="submit">发布动态</button>
          <button class="ghost-button" type="button" @click="resetForm">清空</button>
        </div>
      </form>
    </section>

    <aside class="preview-panel reveal">
      <p class="eyebrow">PREVIEW</p>
      <h2>实时预览</h2>
      <article class="post-card preview-card">
        <header class="post-header">
          <img class="avatar" :src="store.profile.value.avatar || '/default-avatar.svg'" alt="">
          <div>
            <strong>{{ store.profile.value.name }}</strong>
            <span>刚刚 · {{ visibility }}</span>
          </div>
        </header>
        <p class="post-content">{{ previewText }}</p>
        <img v-if="selectedImage" class="post-image" :src="selectedImage" alt="预览配图">
        <div class="tag-row">
          <span class="tag">#{{ topic }}</span>
        </div>
      </article>

      <div class="mini-list">
        <h3>最近发布</h3>
        <article v-for="post in recentPosts" :key="post.id" class="mini-post">
          <p>{{ trimText(post.content, 48) }}</p>
          <span>#{{ post.topic }} · {{ formatTime(post.createdAt) }}</span>
        </article>
      </div>
    </aside>
  </main>
</template>
