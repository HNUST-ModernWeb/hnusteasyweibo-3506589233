import { computed, reactive, toRefs } from 'vue'

const STORAGE = {
  posts: 'hnustEasyWeibo.posts',
  profile: 'hnustEasyWeibo.profile',
  theme: 'hnustEasyWeibo.theme'
}

const defaultProfile = {
  name: '湖科同学',
  major: '现代 Web 学习者',
  bio: '喜欢记录课堂灵感、校园日常和一点点没来由的好奇心。',
  avatar: '/default-avatar.svg'
}

const seedPosts = [
  {
    id: 'seed-1',
    author: '湖科同学',
    major: '现代 Web 学习者',
    avatar: '/default-avatar.svg',
    content: '今天把 HTML、CSS 和 JavaScript 串起来做了一个简易信息流。原来页面真的可以一点点长出自己的性格。',
    topic: '学习',
    visibility: '全校可见',
    image: '',
    likes: 12,
    liked: false,
    comments: ['页面配色很舒服！', '这个描述有点像课程项目日志。'],
    createdAt: Date.now() - 1000 * 60 * 36,
    owned: true
  },
  {
    id: 'seed-2',
    author: '星湖观察员',
    major: '校园摄影爱好者',
    avatar: '/default-avatar.svg',
    content: '晚饭后操场的风很轻，适合散步，也适合把脑子里打结的 bug 慢慢解开。',
    topic: '生活',
    visibility: '全校可见',
    image: '',
    likes: 24,
    liked: false,
    comments: ['这条动态很有画面感。'],
    createdAt: Date.now() - 1000 * 60 * 90,
    owned: false
  },
  {
    id: 'seed-3',
    author: '前端小组',
    major: 'Web 课程项目',
    avatar: '/default-avatar.svg',
    content: '本周社团分享会主题：如何从静态页面过渡到 Vue 组件。欢迎带着你的页面原型来交流。',
    topic: '活动',
    visibility: '仅同学可见',
    image: '',
    likes: 31,
    liked: true,
    comments: [],
    createdAt: Date.now() - 1000 * 60 * 150,
    owned: false
  }
]

const state = reactive({
  posts: loadPosts(),
  profile: loadProfile(),
  theme: loadTheme(),
  toast: {
    text: '',
    visible: false
  }
})

let toastTimer = 0

const stats = computed(() => ({
  posts: state.posts.length,
  likes: state.posts.reduce((sum, post) => sum + Number(post.likes || 0), 0),
  comments: state.posts.reduce((sum, post) => sum + normalizedComments(post).length, 0)
}))

const ownedPosts = computed(() => state.posts.filter((post) => post.owned))

export function useWeiboStore() {
  return {
    ...toRefs(state),
    defaultProfile,
    stats,
    ownedPosts,
    addPost,
    deletePost,
    toggleLike,
    addComment,
    saveProfile,
    resetProfile,
    toggleTheme,
    showToast
  }
}

function addPost(payload) {
  const post = {
    id: 'post-' + Date.now(),
    author: state.profile.name,
    major: state.profile.major,
    avatar: state.profile.avatar || defaultProfile.avatar,
    content: payload.content,
    topic: payload.topic,
    visibility: payload.visibility,
    image: payload.image || '',
    likes: 0,
    liked: false,
    comments: [],
    createdAt: Date.now(),
    owned: true
  }

  state.posts.unshift(post)
  persistPosts()
  return post
}

function deletePost(id) {
  const index = state.posts.findIndex((post) => post.id === id)
  if (index === -1) {
    return
  }

  state.posts.splice(index, 1)
  persistPosts()
}

function toggleLike(id) {
  const post = state.posts.find((item) => item.id === id)
  if (!post) {
    return
  }

  post.liked = !post.liked
  post.likes = Math.max(0, Number(post.likes || 0) + (post.liked ? 1 : -1))
  persistPosts()
}

function addComment(id, text) {
  const post = state.posts.find((item) => item.id === id)
  const comment = text.trim()
  if (!post || !comment) {
    return false
  }

  post.comments = normalizedComments(post)
  post.comments.push(comment)
  persistPosts()
  return true
}

function saveProfile(nextProfile) {
  state.profile = {
    name: nextProfile.name.trim(),
    major: nextProfile.major.trim() || defaultProfile.major,
    bio: nextProfile.bio.trim() || defaultProfile.bio,
    avatar: nextProfile.avatar || defaultProfile.avatar
  }

  syncOwnedPostsProfile()
  writeJson(STORAGE.profile, state.profile)
  persistPosts()
}

function resetProfile() {
  saveProfile({ ...defaultProfile })
}

function toggleTheme() {
  state.theme = state.theme === 'cool' ? 'warm' : 'cool'
  writeValue(STORAGE.theme, state.theme)
}

function showToast(text) {
  state.toast.text = text
  state.toast.visible = true
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    state.toast.visible = false
  }, 2400)
}

function syncOwnedPostsProfile() {
  state.posts.forEach((post) => {
    if (!post.owned) {
      return
    }

    post.author = state.profile.name
    post.major = state.profile.major
    post.avatar = state.profile.avatar
  })
}

function persistPosts() {
  writeJson(STORAGE.posts, state.posts)
}

function loadPosts() {
  const saved = readJson(STORAGE.posts)
  if (Array.isArray(saved)) {
    return saved.map(normalizePost)
  }

  const seeded = clone(seedPosts)
  writeJson(STORAGE.posts, seeded)
  return seeded
}

function loadProfile() {
  const profile = {
    ...defaultProfile,
    ...(readJson(STORAGE.profile) || {})
  }

  return {
    ...profile,
    avatar: normalizeAssetPath(profile.avatar)
  }
}

function loadTheme() {
  return readValue(STORAGE.theme) === 'cool' ? 'cool' : 'warm'
}

function normalizePost(post) {
  return {
    id: post.id || 'post-' + Date.now(),
    author: post.author || defaultProfile.name,
    major: post.major || defaultProfile.major,
    avatar: normalizeAssetPath(post.avatar || defaultProfile.avatar),
    content: post.content || '',
    topic: post.topic || '学习',
    visibility: post.visibility || '全校可见',
    image: post.image || '',
    likes: Number(post.likes || 0),
    liked: Boolean(post.liked),
    comments: normalizedComments(post),
    createdAt: Number(post.createdAt || Date.now()),
    owned: Boolean(post.owned)
  }
}

function normalizedComments(post) {
  return Array.isArray(post.comments) ? post.comments.filter(Boolean) : []
}

function normalizeAssetPath(path) {
  if (!path || path === 'default-avatar.svg') {
    return '/default-avatar.svg'
  }

  return path
}

function readJson(key) {
  const value = readValue(key)
  if (!value) {
    return null
  }

  try {
    return JSON.parse(value)
  } catch (error) {
    removeValue(key)
    return null
  }
}

function writeJson(key, value) {
  writeValue(key, JSON.stringify(value))
}

function readValue(key) {
  if (typeof localStorage === 'undefined') {
    return null
  }

  return localStorage.getItem(key)
}

function writeValue(key, value) {
  if (typeof localStorage === 'undefined') {
    return
  }

  localStorage.setItem(key, value)
}

function removeValue(key) {
  if (typeof localStorage === 'undefined') {
    return
  }

  localStorage.removeItem(key)
}

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}
