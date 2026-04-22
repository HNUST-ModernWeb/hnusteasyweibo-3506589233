<script setup>
import { computed, ref } from 'vue'
import EmptyState from '../components/EmptyState.vue'
import PostCard from '../components/PostCard.vue'
import StatsGrid from '../components/StatsGrid.vue'
import { useWeiboStore } from '../stores/useWeiboStore'

const store = useWeiboStore()
const activeFilter = ref('all')
const filters = ['all', '学习', '生活', '活动']

const filteredPosts = computed(() => {
  if (activeFilter.value === 'all') {
    return store.posts.value
  }

  return store.posts.value.filter((post) => post.topic === activeFilter.value)
})

function filterLabel(filter) {
  return filter === 'all' ? '全部' : filter
}
</script>

<template>
  <main class="layout-shell">
    <aside class="hero-panel reveal">
      <p class="eyebrow">HNUST CAMPUS FEED</p>
      <h1>把校园里一闪而过的灵感，收进同一条时间线。</h1>
      <p class="hero-text">
        这是一个 Vue3 改造后的简易社交分享平台原型，支持文字分享、图片发布、点赞、评论和个人资料保存。
      </p>
      <div class="hero-actions">
        <RouterLink class="primary-button" to="/post">写一条动态</RouterLink>
        <RouterLink class="text-link" to="/profile">完善个人主页</RouterLink>
      </div>
      <StatsGrid :posts="store.stats.value.posts" :likes="store.stats.value.likes" :comments="store.stats.value.comments" />
    </aside>

    <section class="content-stack">
      <article class="composer-card reveal">
        <div>
          <p class="eyebrow">QUICK START</p>
          <h2>今天有什么想分享？</h2>
          <p>课程笔记、食堂新发现、社团招新、晚霞照片，都可以先从一条动态开始。</p>
        </div>
        <RouterLink class="primary-button" to="/post">去发布</RouterLink>
      </article>

      <section class="feed-toolbar reveal" aria-label="信息流筛选">
        <div>
          <p class="eyebrow">LIVE FEED</p>
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

      <div v-if="filteredPosts.length" class="feed-list" aria-live="polite">
        <PostCard v-for="post in filteredPosts" :key="post.id" :post="post" />
      </div>
      <EmptyState
        v-else
        title="还没有动态"
        description="去发布页写下第一条校园分享吧。"
        action-text="立即发布"
        action-to="/post"
      />
    </section>
  </main>
</template>
