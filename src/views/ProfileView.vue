<script setup>
import { computed, ref, watch } from 'vue'
import AuthPanel from '../components/AuthPanel.vue'
import EmptyState from '../components/EmptyState.vue'
import PostCard from '../components/PostCard.vue'
import StatsGrid from '../components/StatsGrid.vue'
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()

const name = ref('')
const major = ref('')
const bio = ref('')
const avatar = ref('')
const avatarUrl = ref('')
const avatarFile = ref(null)
const message = ref('')
const messageType = ref('')

const ownedStats = computed(() => ({
  posts: store.ownedPosts.value.length,
  likes: store.ownedPosts.value.reduce((sum, post) => sum + Number(post.likes || 0), 0),
  comments: store.ownedPosts.value.reduce((sum, post) => sum + post.comments.length, 0)
}))

watch(
  () => store.profile.value,
  () => syncFormFromStore(),
  { immediate: true, deep: true }
)

function handleAvatarChange(event) {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }

  if (!file.type.startsWith('image/')) {
    setMessage('请选择图片文件作为头像。')
    event.target.value = ''
    return
  }

  avatarFile.value = file
  const reader = new FileReader()
  reader.addEventListener('load', () => {
    avatar.value = reader.result
    setMessage('头像已更新预览，保存后会用于你的个人主页。', 'ok')
  })
  reader.readAsDataURL(file)
}

async function saveProfile() {
  if (!name.value.trim()) {
    setMessage('昵称不能为空。')
    return
  }

  try {
    await store.saveProfile({
      name: name.value,
      major: major.value,
      bio: bio.value,
      avatarUrl: avatarUrl.value,
      avatarFile: avatarFile.value
    })
    avatarFile.value = null
    syncFormFromStore()
    setMessage('资料保存成功。', 'ok')
    store.showToast('个人主页已更新。')
  } catch (error) {
    setMessage(error.message || '资料保存失败。')
  }
}

async function resetProfile() {
  try {
    await store.resetProfile()
    avatarFile.value = null
    syncFormFromStore()
    setMessage('已恢复默认资料。', 'ok')
  } catch (error) {
    setMessage(error.message || '恢复默认资料失败。')
  }
}

function syncFormFromStore() {
  name.value = store.profile.value.name
  major.value = store.profile.value.major
  bio.value = store.profile.value.bio
  avatar.value = store.profile.value.avatar
  avatarUrl.value = store.profile.value.avatarUrl
}

function setMessage(text, type = 'error') {
  message.value = text
  messageType.value = text ? type : ''
}
</script>

<template>
  <main v-if="!store.isAuthenticated.value" class="profile-layout">
    <section class="profile-card reveal">
      <div class="profile-cover"></div>
      <div class="profile-body">
        <img class="profile-avatar" src="/default-avatar.svg" alt="默认头像">
        <p class="eyebrow">我的主页</p>
        <h1>登录后查看主页</h1>
        <p class="profile-major">记录校园生活</p>
        <p class="profile-bio">
          注册或登录后，你可以维护个人资料，发布动态，参与点赞和评论。
        </p>
      </div>
    </section>

    <AuthPanel />
  </main>

  <main v-else class="profile-layout">
    <section class="profile-card reveal">
      <div class="profile-cover"></div>
      <div class="profile-body">
        <img class="profile-avatar" :src="avatar || '/default-avatar.svg'" alt="个人头像">
        <p class="eyebrow">我的主页</p>
        <h1>{{ name || '湖科同学' }}</h1>
        <p class="profile-major">{{ major || store.defaultProfile.major }}</p>
        <p class="profile-bio">{{ bio || store.defaultProfile.bio }}</p>

        <StatsGrid
          compact
          :posts="ownedStats.posts"
          :likes="ownedStats.likes"
          :comments="ownedStats.comments"
        />

        <button class="ghost-button logout-button" type="button" @click="store.logout()">退出登录</button>
      </div>
    </section>

    <section class="settings-panel reveal">
      <p class="eyebrow">资料设置</p>
      <h2>编辑个人资料</h2>
      <p class="section-lead">
        完善昵称、专业和简介，让同学更容易认识你。
      </p>

      <form class="profile-form" @submit.prevent="saveProfile">
        <label class="upload-zone avatar-upload-zone" for="profile-avatar-input">
          <input id="profile-avatar-input" type="file" accept="image/*" @change="handleAvatarChange">
          <span>更换头像</span>
          <small>选择图片后会立即预览，保存后用于个人头像</small>
        </label>

        <label class="field-block" for="profile-name">
          <span>昵称</span>
          <input id="profile-name" v-model="name" type="text" maxlength="40" placeholder="请输入昵称" required>
        </label>

        <label class="field-block" for="profile-major">
          <span>身份/专业</span>
          <input id="profile-major" v-model="major" type="text" maxlength="80" placeholder="例如：计算机科学与技术">
        </label>

        <label class="field-block" for="profile-bio">
          <span>个人简介</span>
          <textarea id="profile-bio" v-model="bio" rows="5" maxlength="240" placeholder="介绍一下你自己" />
        </label>

        <p class="form-message" :class="messageType" role="alert">{{ message }}</p>

        <div class="form-actions">
          <button class="primary-button" type="submit" :disabled="store.authLoading.value">
            {{ store.authLoading.value ? '保存中...' : '保存资料' }}
          </button>
          <button class="ghost-button" type="button" @click="resetProfile">恢复默认</button>
        </div>
      </form>
    </section>

    <section class="my-posts-panel reveal">
      <div class="section-heading">
        <div>
          <p class="eyebrow">我的发布</p>
          <h2>我的动态</h2>
        </div>
        <RouterLink class="text-link" to="/post">继续发布</RouterLink>
      </div>
      <div v-if="store.ownedPosts.value.length" class="feed-list compact-feed">
        <PostCard v-for="post in store.ownedPosts.value" :key="post.id" :post="post" />
      </div>
      <EmptyState
        v-else
        title="你还没有发布动态"
        description="写下第一条内容后，它会展示在这里。"
        action-text="去发布"
        action-to="/post"
      />
    </section>
  </main>
</template>
