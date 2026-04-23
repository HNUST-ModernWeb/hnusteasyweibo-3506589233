<script setup>
import { onMounted, ref, watch } from 'vue'
import EmptyState from '../components/EmptyState.vue'
import PostCard from '../components/PostCard.vue'
import StatsGrid from '../components/StatsGrid.vue'
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()
const activeFilter = ref('all')
const filters = ['all', '学习', '生活', '活动']

onMounted(() => {
  store.fetchPosts({ topic: activeFilter.value }).catch(() => {})
})

watch(activeFilter, (topic) => {
  store.fetchPosts({ topic }).catch(() => {})
})

function filterLabel(filter) {
  return filter === 'all' ? '全部' : filter
}

function refreshFeed() {
  store.fetchPosts({ topic: activeFilter.value })
    .then(() => store.showToast('信息流已刷新。'))
    .catch((error) => store.showToast(error.message || '刷新失败'))
}
</script>

<template>
  <main class="layout-shell">
    <aside class="hero-panel reveal">
      <p class="eyebrow">湖科微光</p>
      <h1>把校园里一闪而过的灵感，收进同一条时间线。</h1>
      <p class="hero-text">
        分享学习瞬间、生活见闻和活动消息，也把同学们的点赞与评论好好留住。
      </p>
      <div class="hero-actions">
        <RouterLink class="primary-button" to="/post">写一条动态</RouterLink>
        <RouterLink class="text-link" to="/profile">
          {{ store.isAuthenticated.value ? '查看个人主页' : '登录后开始' }}
        </RouterLink>
      </div>
      <StatsGrid :posts="store.stats.value.posts" :likes="store.stats.value.likes" :comments="store.stats.value.comments" />
    </aside>

    <section class="content-stack">
      <article class="composer-card reveal">
        <div>
          <p class="eyebrow">今日校园</p>
          <h2>校园动态已准备好</h2>
          <p>刷新一下，就能看到同学们最新的分享；页面重新打开后，内容也会继续保留。</p>
        </div>
        <button class="primary-button" type="button" :disabled="store.loading.value" @click="refreshFeed">
          {{ store.loading.value ? '刷新中...' : '刷新动态' }}
        </button>
      </article>

      <section class="feed-toolbar reveal" aria-label="信息流筛选">
        <div>
          <p class="eyebrow">实时动态</p>
          <h2>校园信息流</h2>
        </div>
        <div class="filter-group" role="group" aria-label="动态类型筛选">
          <button
            v-for="filter in filters"
            :key="filter"
            class="filter-chip"
            :class="{ active: activeFilter === filter }"
            type="button"
            @click="activeFilter = filter"
          >
            {{ filterLabel(filter) }}
          </button>
        </div>
      </section>

      <div v-if="store.error.value" class="status-card error">
        {{ store.error.value }}
      </div>
      <div v-else-if="store.loading.value && !store.posts.value.length" class="status-card">
        正在加载校园信息流...
      </div>

      <div v-if="store.posts.value.length" class="feed-list" aria-live="polite">
        <PostCard v-for="post in store.posts.value" :key="post.id" :post="post" />
      </div>
      <EmptyState
        v-else-if="!store.loading.value"
        title="还没有动态"
        description="写下第一条校园分享，让这里热闹起来。"
        action-text="去发布"
        action-to="/post"
      />
    </section>
  </main>
</template>
