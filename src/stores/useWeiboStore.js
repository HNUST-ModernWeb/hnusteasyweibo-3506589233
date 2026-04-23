import { computed, reactive, toRefs } from 'vue'
import { apiRequest, assetUrl, uploadFile } from '../api/client'

const STORAGE = {
  token: 'hnustEasyWeibo.authToken',
  theme: 'hnustEasyWeibo.theme'
}

const defaultProfile = {
  name: '湖科同学',
  major: '湖南科技大学',
  bio: '登录后即可发布动态、点赞和评论，记录你的校园日常。',
  avatar: '/default-avatar.svg',
  avatarUrl: '/default-avatar.svg'
}

const state = reactive({
  posts: [],
  user: null,
  profile: { ...defaultProfile },
  token: readValue(STORAGE.token) || '',
  theme: readValue(STORAGE.theme) === 'cool' ? 'cool' : 'warm',
  initialized: false,
  loading: false,
  authLoading: false,
  error: '',
  toast: {
    text: '',
    visible: false
  }
})

let toastTimer = 0

const isAuthenticated = computed(() => Boolean(state.token && state.user))

const stats = computed(() => ({
  posts: state.posts.length,
  likes: state.posts.reduce((sum, post) => sum + Number(post.likes || 0), 0),
  comments: state.posts.reduce((sum, post) => sum + commentsOf(post).length, 0)
}))

const ownedPosts = computed(() => {
  if (!state.user) {
    return []
  }

  return state.posts.filter((post) => post.userId === state.user.id)
})

export function useWeiboStore() {
  return {
    ...toRefs(state),
    defaultProfile,
    isAuthenticated,
    stats,
    ownedPosts,
    initialize,
    fetchPosts,
    login,
    register,
    logout,
    addPost,
    deletePost,
    toggleLike,
    addComment,
    saveProfile,
    resetProfile,
    uploadImage,
    toggleTheme,
    showToast
  }
}

async function initialize() {
  if (state.initialized) {
    return
  }

  state.loading = true
  state.error = ''
  try {
    if (state.token) {
      await loadCurrentUser()
    }
    await fetchPosts()
  } catch (error) {
    state.error = friendlyError(error)
    if (error.status === 401) {
      clearAuth()
    }
  } finally {
    state.initialized = true
    state.loading = false
  }
}

async function fetchPosts(params = {}) {
  state.loading = true
  state.error = ''
  try {
    const query = new URLSearchParams()
    if (params.topic && params.topic !== 'all') {
      query.set('topic', params.topic)
    }
    if (params.keyword) {
      query.set('keyword', params.keyword)
    }
    query.set('page', String(params.page ?? 0))
    query.set('size', String(params.size ?? 50))

    const posts = await apiRequest(`/api/posts?${query.toString()}`, { token: state.token })
    state.posts = posts.map(mapPost)
  } catch (error) {
    state.error = friendlyError(error)
    throw error
  } finally {
    state.loading = false
  }
}

async function login(payload) {
  state.authLoading = true
  try {
    const response = await apiRequest('/api/auth/login', {
      method: 'POST',
      body: payload
    })
    applyAuth(response)
    await fetchPosts()
    showToast('登录成功，欢迎回来。')
  } finally {
    state.authLoading = false
  }
}

async function register(payload) {
  state.authLoading = true
  try {
    const response = await apiRequest('/api/auth/register', {
      method: 'POST',
      body: payload
    })
    applyAuth(response)
    await fetchPosts()
    showToast('注册成功，欢迎来到湖科微光。')
  } finally {
    state.authLoading = false
  }
}

async function logout() {
  if (state.token) {
    try {
      await apiRequest('/api/auth/logout', {
        method: 'POST',
        token: state.token
      })
    } catch (error) {
      // 退出请求失败不阻塞用户回到未登录状态。
    }
  }

  clearAuth()
  await fetchPosts()
  showToast('已退出登录。')
}

async function addPost(payload) {
  requireAuth()
  state.loading = true
  try {
    let imageUrl = payload.imageUrl || ''
    if (payload.imageFile) {
      const upload = await uploadImage(payload.imageFile)
      imageUrl = upload.url
    }

    const post = await apiRequest('/api/posts', {
      method: 'POST',
      token: state.token,
      body: {
        content: payload.content,
        topic: payload.topic,
        visibility: payload.visibility,
        imageUrl
      }
    })
    state.posts = [mapPost(post), ...state.posts]
    return post
  } finally {
    state.loading = false
  }
}

async function deletePost(id) {
  requireAuth()
  await apiRequest(`/api/posts/${id}`, {
    method: 'DELETE',
    token: state.token
  })
  state.posts = state.posts.filter((post) => post.id !== id)
}

async function toggleLike(id) {
  requireAuth()
  const post = await apiRequest(`/api/posts/${id}/like`, {
    method: 'POST',
    token: state.token
  })
  replacePost(mapPost(post))
}

async function addComment(id, text) {
  requireAuth()
  await apiRequest(`/api/posts/${id}/comments`, {
    method: 'POST',
    token: state.token,
    body: { content: text }
  })
  const post = await apiRequest(`/api/posts/${id}`, { token: state.token })
  replacePost(mapPost(post))
}

async function saveProfile(nextProfile) {
  requireAuth()
  state.authLoading = true
  try {
    let avatarUrl = nextProfile.avatarUrl || state.user.avatarUrl || defaultProfile.avatarUrl
    if (nextProfile.avatarFile) {
      const upload = await uploadImage(nextProfile.avatarFile)
      avatarUrl = upload.url
    }

    const user = await apiRequest('/api/users/me', {
      method: 'PUT',
      token: state.token,
      body: {
        displayName: nextProfile.name,
        major: nextProfile.major,
        bio: nextProfile.bio,
        avatarUrl
      }
    })
    applyUser(user)
    await fetchPosts()
    return user
  } finally {
    state.authLoading = false
  }
}

async function resetProfile() {
  return saveProfile({
    name: defaultProfile.name,
    major: defaultProfile.major,
    bio: defaultProfile.bio,
    avatarUrl: defaultProfile.avatarUrl
  })
}

async function uploadImage(file) {
  requireAuth()
  return uploadFile('/api/uploads', file, state.token)
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

async function loadCurrentUser() {
  const user = await apiRequest('/api/auth/me', { token: state.token })
  applyUser(user)
}

function applyAuth(response) {
  state.token = response.token
  writeValue(STORAGE.token, response.token)
  applyUser(response.user)
}

function applyUser(user) {
  state.user = user
  state.profile = mapProfile(user)
}

function clearAuth() {
  state.token = ''
  state.user = null
  state.profile = { ...defaultProfile }
  removeValue(STORAGE.token)
}

function replacePost(nextPost) {
  state.posts = state.posts.map((post) => (post.id === nextPost.id ? nextPost : post))
}

function mapProfile(user) {
  if (!user) {
    return { ...defaultProfile }
  }

  return {
    name: user.displayName,
    major: user.major,
    bio: user.bio,
    avatar: assetUrl(user.avatarUrl || defaultProfile.avatarUrl),
    avatarUrl: user.avatarUrl || defaultProfile.avatarUrl
  }
}

function mapPost(post) {
  return {
    id: post.id,
    userId: post.userId,
    author: post.authorName,
    major: post.authorMajor,
    avatar: assetUrl(post.authorAvatar || defaultProfile.avatarUrl),
    content: post.content,
    topic: post.topic,
    visibility: post.visibility,
    image: assetUrl(post.imageUrl || ''),
    imageUrl: post.imageUrl || '',
    likes: Number(post.likeCount || 0),
    liked: Boolean(post.likedByCurrentUser),
    comments: (post.comments || []).map(mapComment),
    createdAt: post.createdAt,
    updatedAt: post.updatedAt,
    owned: Boolean(state.user && post.userId === state.user.id)
  }
}

function mapComment(comment) {
  return {
    id: comment.id,
    userId: comment.userId,
    authorName: comment.authorName || '同学',
    authorAvatar: assetUrl(comment.authorAvatar || defaultProfile.avatarUrl),
    content: comment.content,
    createdAt: comment.createdAt
  }
}

function commentsOf(post) {
  return Array.isArray(post.comments) ? post.comments : []
}

function requireAuth() {
  if (!state.token || !state.user) {
    showToast('请先登录后再操作。')
    throw new Error('UNAUTHENTICATED')
  }
}

function friendlyError(error) {
  if (error?.message) {
    return error.message
  }
  return '暂时无法连接服务，请稍后再试。'
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
