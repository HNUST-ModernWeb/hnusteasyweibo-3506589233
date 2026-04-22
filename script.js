(function () {
    const STORAGE = {
        posts: 'hnustEasyWeibo.posts',
        profile: 'hnustEasyWeibo.profile',
        theme: 'hnustEasyWeibo.theme'
    };

    const defaultProfile = {
        name: '湖科同学',
        major: '现代 Web 学习者',
        bio: '喜欢记录课堂灵感、校园日常和一点点没来由的好奇心。',
        avatar: 'default-avatar.svg'
    };

    const seedPosts = [
        {
            id: 'seed-1',
            author: '湖科同学',
            major: '现代 Web 学习者',
            avatar: 'default-avatar.svg',
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
            avatar: 'default-avatar.svg',
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
            avatar: 'default-avatar.svg',
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
    ];

    document.addEventListener('DOMContentLoaded', init);

    function init() {
        applySavedTheme();
        bindThemeToggle();

        const page = document.body.dataset.page;
        if (page === 'home') {
            initHomePage();
        }
        if (page === 'post') {
            initPostPage();
        }
        if (page === 'profile') {
            initProfilePage();
        }
    }

    function initHomePage() {
        renderStats();
        renderFeed(document.getElementById('feed-list'), {
            emptyElement: document.getElementById('feed-empty')
        });
        bindFilters();
    }

    function initPostPage() {
        const form = document.getElementById('post-form');
        const content = document.getElementById('post-content');
        const topic = document.getElementById('post-topic');
        const visibility = document.getElementById('post-visibility');
        const imageInput = document.getElementById('post-image');
        const imagePreview = document.getElementById('image-preview');
        const imagePreviewImg = imagePreview.querySelector('img');
        const clearImage = document.getElementById('clear-image');
        const resetButton = document.getElementById('reset-post');
        const message = document.getElementById('post-message');
        const count = document.getElementById('content-count');
        const previewContent = document.getElementById('preview-content');
        const previewTopic = document.getElementById('preview-topic');
        const previewMeta = document.getElementById('preview-meta');
        const previewImage = document.getElementById('preview-image');
        const previewAuthor = document.getElementById('preview-author');
        const previewAvatar = document.getElementById('preview-avatar');
        const profile = getProfile();
        let selectedImage = '';

        previewAuthor.textContent = profile.name;
        previewAvatar.src = profile.avatar;
        updatePreview();
        renderRecentPosts();

        content.addEventListener('input', updatePreview);
        topic.addEventListener('change', updatePreview);
        visibility.addEventListener('change', updatePreview);

        imageInput.addEventListener('change', function (event) {
            const file = event.target.files[0];
            if (!file) {
                return;
            }

            if (!file.type.startsWith('image/')) {
                showFormMessage(message, '请选择图片文件。');
                imageInput.value = '';
                return;
            }

            const reader = new FileReader();
            reader.addEventListener('load', function () {
                selectedImage = reader.result;
                imagePreviewImg.src = selectedImage;
                imagePreview.hidden = false;
                previewImage.src = selectedImage;
                previewImage.hidden = false;
                showFormMessage(message, '');
            });
            reader.readAsDataURL(file);
        });

        clearImage.addEventListener('click', function () {
            selectedImage = '';
            imageInput.value = '';
            imagePreview.hidden = true;
            imagePreviewImg.src = '';
            previewImage.hidden = true;
            previewImage.src = '';
        });

        resetButton.addEventListener('click', function () {
            form.reset();
            selectedImage = '';
            imagePreview.hidden = true;
            imagePreviewImg.src = '';
            previewImage.hidden = true;
            previewImage.src = '';
            showFormMessage(message, '');
            updatePreview();
        });

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            const text = content.value.trim();

            if (text.length < 5) {
                showFormMessage(message, '动态内容至少需要 5 个字。');
                content.focus();
                return;
            }

            const posts = getPosts();
            posts.unshift({
                id: 'post-' + Date.now(),
                author: profile.name,
                major: profile.major,
                avatar: profile.avatar,
                content: text,
                topic: topic.value,
                visibility: visibility.value,
                image: selectedImage,
                likes: 0,
                liked: false,
                comments: [],
                createdAt: Date.now(),
                owned: true
            });

            savePosts(posts);
            showToast('发布成功，已经保存到首页信息流。');
            form.reset();
            selectedImage = '';
            imagePreview.hidden = true;
            imagePreviewImg.src = '';
            previewImage.hidden = true;
            previewImage.src = '';
            updatePreview();
            renderRecentPosts();
        });

        function updatePreview() {
            const text = content.value.trim();
            count.textContent = String(content.value.length);
            previewContent.textContent = text || '你输入的动态会显示在这里。';
            previewTopic.textContent = '#' + topic.value;
            previewMeta.textContent = '刚刚 · ' + visibility.value;
        }
    }

    function initProfilePage() {
        const form = document.getElementById('profile-form');
        const avatarInput = document.getElementById('profile-avatar-input');
        const nameInput = document.getElementById('profile-name');
        const majorInput = document.getElementById('profile-major');
        const bioInput = document.getElementById('profile-bio');
        const resetButton = document.getElementById('reset-profile');
        const message = document.getElementById('profile-message');
        let profile = getProfile();

        fillProfileForm(profile);
        renderProfile(profile);
        renderProfilePosts();

        avatarInput.addEventListener('change', function (event) {
            const file = event.target.files[0];
            if (!file) {
                return;
            }

            if (!file.type.startsWith('image/')) {
                showFormMessage(message, '请选择图片文件作为头像。');
                avatarInput.value = '';
                return;
            }

            const reader = new FileReader();
            reader.addEventListener('load', function () {
                profile = {
                    ...profile,
                    avatar: reader.result
                };
                renderProfile(profile);
                showFormMessage(message, '头像已更新，点击保存资料后会同步到动态。', 'ok');
            });
            reader.readAsDataURL(file);
        });

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            const name = nameInput.value.trim();

            if (!name) {
                showFormMessage(message, '昵称不能为空。');
                nameInput.focus();
                return;
            }

            profile = {
                name: name,
                major: majorInput.value.trim() || defaultProfile.major,
                bio: bioInput.value.trim() || defaultProfile.bio,
                avatar: profile.avatar || defaultProfile.avatar
            };

            saveProfile(profile);
            syncOwnedPostsProfile(profile);
            renderProfile(profile);
            renderProfilePosts();
            showFormMessage(message, '资料保存成功。', 'ok');
            showToast('个人主页已更新。');
        });

        resetButton.addEventListener('click', function () {
            profile = { ...defaultProfile };
            saveProfile(profile);
            syncOwnedPostsProfile(profile);
            fillProfileForm(profile);
            renderProfile(profile);
            renderProfilePosts();
            showFormMessage(message, '已恢复默认资料。', 'ok');
        });
    }

    function renderFeed(container, options) {
        if (!container) {
            return;
        }

        const filter = options && options.filter ? options.filter : 'all';
        const emptyElement = options ? options.emptyElement : null;
        const posts = getPosts().filter(function (post) {
            return filter === 'all' || post.topic === filter;
        });

        container.innerHTML = '';
        if (emptyElement) {
            emptyElement.hidden = posts.length > 0;
        }

        posts.forEach(function (post) {
            container.appendChild(createPostCard(post));
        });
    }

    function createPostCard(post) {
        const template = document.getElementById('post-template');
        const card = template.content.firstElementChild.cloneNode(true);
        const avatar = card.querySelector('[data-role="avatar"]');
        const author = card.querySelector('[data-role="author"]');
        const meta = card.querySelector('[data-role="meta"]');
        const content = card.querySelector('[data-role="content"]');
        const image = card.querySelector('[data-role="image"]');
        const tags = card.querySelector('[data-role="tags"]');
        const likeButton = card.querySelector('[data-action="like"]');
        const deleteButton = card.querySelector('[data-action="delete"]');
        const commentForm = card.querySelector('[data-role="comment-form"]');
        const comments = card.querySelector('[data-role="comments"]');

        avatar.src = post.avatar || defaultProfile.avatar;
        avatar.alt = post.author + '的头像';
        author.textContent = post.author;
        meta.textContent = [post.major, formatTime(post.createdAt), post.visibility].filter(Boolean).join(' · ');
        content.textContent = post.content;

        if (post.image) {
            image.src = post.image;
            image.hidden = false;
        } else {
            image.removeAttribute('src');
            image.hidden = true;
        }

        tags.innerHTML = '';
        ['#' + post.topic, post.visibility].forEach(function (text) {
            const tag = document.createElement('span');
            tag.className = 'tag';
            tag.textContent = text;
            tags.appendChild(tag);
        });

        updateLikeButton(likeButton, post);
        deleteButton.hidden = !post.owned;

        likeButton.addEventListener('click', function () {
            const posts = getPosts();
            const target = posts.find(function (item) {
                return item.id === post.id;
            });

            if (!target) {
                return;
            }

            target.liked = !target.liked;
            target.likes += target.liked ? 1 : -1;
            savePosts(posts);
            updateLikeButton(likeButton, target);
            renderStats();
            renderProfileStats();
        });

        deleteButton.addEventListener('click', function () {
            const confirmed = window.confirm('确定删除这条动态吗？');
            if (!confirmed) {
                return;
            }

            const posts = getPosts().filter(function (item) {
                return item.id !== post.id;
            });
            savePosts(posts);
            showToast('动态已删除。');
            refreshCurrentFeed();
        });

        commentForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const input = commentForm.elements.comment;
            const text = input.value.trim();
            if (!text) {
                return;
            }

            const posts = getPosts();
            const target = posts.find(function (item) {
                return item.id === post.id;
            });

            if (!target) {
                return;
            }

            target.comments.push(text);
            savePosts(posts);
            input.value = '';
            renderComments(comments, target.comments);
            renderStats();
            renderProfileStats();
        });

        renderComments(comments, post.comments);
        return card;
    }

    function renderComments(container, comments) {
        container.innerHTML = '';
        if (!comments.length) {
            return;
        }

        comments.forEach(function (comment) {
            const item = document.createElement('div');
            item.className = 'comment';
            item.innerHTML = '<strong>同学：</strong>' + escapeHtml(comment);
            container.appendChild(item);
        });
    }

    function updateLikeButton(button, post) {
        button.textContent = (post.liked ? '已点赞 ' : '点赞 ') + post.likes;
        button.classList.toggle('liked', Boolean(post.liked));
    }

    function bindFilters() {
        const chips = Array.from(document.querySelectorAll('.filter-chip'));
        const feed = document.getElementById('feed-list');
        const empty = document.getElementById('feed-empty');

        chips.forEach(function (chip) {
            chip.addEventListener('click', function () {
                chips.forEach(function (item) {
                    item.classList.remove('active');
                });
                chip.classList.add('active');
                renderFeed(feed, {
                    emptyElement: empty,
                    filter: chip.dataset.filter
                });
            });
        });
    }

    function renderRecentPosts() {
        const container = document.getElementById('recent-posts');
        if (!container) {
            return;
        }

        const posts = getPosts().slice(0, 3);
        container.innerHTML = '';
        posts.forEach(function (post) {
            const item = document.createElement('article');
            item.className = 'mini-post';
            item.innerHTML = '<p>' + escapeHtml(trimText(post.content, 48)) + '</p><span>#' + escapeHtml(post.topic) + ' · ' + formatTime(post.createdAt) + '</span>';
            container.appendChild(item);
        });
    }

    function renderProfilePosts() {
        const container = document.getElementById('profile-posts');
        const empty = document.getElementById('profile-empty');
        if (!container) {
            return;
        }

        const posts = getPosts().filter(function (post) {
            return post.owned;
        });
        container.innerHTML = '';
        empty.hidden = posts.length > 0;
        posts.forEach(function (post) {
            container.appendChild(createPostCard(post));
        });
        renderProfileStats();
    }

    function fillProfileForm(profile) {
        document.getElementById('profile-name').value = profile.name;
        document.getElementById('profile-major').value = profile.major;
        document.getElementById('profile-bio').value = profile.bio;
    }

    function renderProfile(profile) {
        const avatar = document.getElementById('profile-avatar');
        if (!avatar) {
            return;
        }

        avatar.src = profile.avatar || defaultProfile.avatar;
        document.getElementById('profile-name-display').textContent = profile.name;
        document.getElementById('profile-major-display').textContent = profile.major;
        document.getElementById('profile-bio-display').textContent = profile.bio;
        renderProfileStats();
    }

    function renderStats() {
        const posts = getPosts();
        setText('stat-posts', posts.length);
        setText('stat-likes', posts.reduce(function (sum, post) {
            return sum + post.likes;
        }, 0));
        setText('stat-comments', posts.reduce(function (sum, post) {
            return sum + post.comments.length;
        }, 0));
    }

    function renderProfileStats() {
        const ownedPosts = getPosts().filter(function (post) {
            return post.owned;
        });
        setText('profile-post-count', ownedPosts.length);
        setText('profile-like-count', ownedPosts.reduce(function (sum, post) {
            return sum + post.likes;
        }, 0));
        setText('profile-comment-count', ownedPosts.reduce(function (sum, post) {
            return sum + post.comments.length;
        }, 0));
    }

    function refreshCurrentFeed() {
        const page = document.body.dataset.page;
        if (page === 'home') {
            renderStats();
            const activeFilter = document.querySelector('.filter-chip.active');
            renderFeed(document.getElementById('feed-list'), {
                emptyElement: document.getElementById('feed-empty'),
                filter: activeFilter ? activeFilter.dataset.filter : 'all'
            });
        }
        if (page === 'profile') {
            renderProfilePosts();
        }
    }

    function syncOwnedPostsProfile(profile) {
        const posts = getPosts().map(function (post) {
            if (!post.owned) {
                return post;
            }

            return {
                ...post,
                author: profile.name,
                major: profile.major,
                avatar: profile.avatar
            };
        });
        savePosts(posts);
    }

    function getPosts() {
        const saved = readJson(STORAGE.posts);
        if (saved && Array.isArray(saved)) {
            return saved;
        }
        savePosts(seedPosts);
        return seedPosts;
    }

    function savePosts(posts) {
        localStorage.setItem(STORAGE.posts, JSON.stringify(posts));
    }

    function getProfile() {
        return {
            ...defaultProfile,
            ...(readJson(STORAGE.profile) || {})
        };
    }

    function saveProfile(profile) {
        localStorage.setItem(STORAGE.profile, JSON.stringify(profile));
    }

    function readJson(key) {
        const value = localStorage.getItem(key);
        if (!value) {
            return null;
        }

        try {
            return JSON.parse(value);
        } catch (error) {
            localStorage.removeItem(key);
            return null;
        }
    }

    function bindThemeToggle() {
        const button = document.getElementById('theme-toggle');
        if (!button) {
            return;
        }

        button.addEventListener('click', function () {
            document.body.classList.toggle('cool-theme');
            localStorage.setItem(STORAGE.theme, document.body.classList.contains('cool-theme') ? 'cool' : 'warm');
            showToast('页面氛围已切换。');
        });
    }

    function applySavedTheme() {
        if (localStorage.getItem(STORAGE.theme) === 'cool') {
            document.body.classList.add('cool-theme');
        }
    }

    function showFormMessage(element, text, type) {
        if (!element) {
            return;
        }

        element.textContent = text;
        element.style.color = type === 'ok' ? '#247a5b' : '';
    }

    function showToast(text) {
        const toast = document.getElementById('toast');
        if (!toast) {
            return;
        }

        toast.textContent = text;
        toast.classList.add('show');
        window.clearTimeout(showToast.timer);
        showToast.timer = window.setTimeout(function () {
            toast.classList.remove('show');
        }, 2400);
    }

    function formatTime(timestamp) {
        const diff = Date.now() - Number(timestamp);
        const minutes = Math.max(1, Math.floor(diff / 60000));

        if (minutes < 60) {
            return minutes + ' 分钟前';
        }

        const hours = Math.floor(minutes / 60);
        if (hours < 24) {
            return hours + ' 小时前';
        }

        return Math.floor(hours / 24) + ' 天前';
    }

    function trimText(text, maxLength) {
        return text.length > maxLength ? text.slice(0, maxLength) + '...' : text;
    }

    function setText(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = String(value);
        }
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
})();
