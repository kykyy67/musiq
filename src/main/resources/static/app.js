(function () {
    const { createApp } = Vue;

    const emptyPage = () => ({
        content: [],
        page: 0,
        size: 8,
        totalElements: 0,
        totalPages: 0
    });

    function toQuery(params) {
        const query = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value === null || value === undefined || value === "" || value === false) {
                return;
            }
            query.set(key, String(value));
        });
        return query.toString();
    }

    createApp({
        data() {
            return {
                activeView: "home",
                toast: "",
                dashboard: {
                    albums: 0,
                    tracks: 0,
                    artists: 0,
                    genres: 0,
                    users: 0
                },
                reference: {
                    albums: [],
                    tracks: [],
                    artists: [],
                    genres: [],
                    users: []
                },
                albums: emptyPage(),
                tracks: emptyPage(),
                artists: emptyPage(),
                genres: emptyPage(),
                users: emptyPage(),
                albumTracksModal: {
                    open: false,
                    album: null
                },
                artistAlbumsModal: {
                    open: false,
                    artist: null
                },
                genreAlbumsModal: {
                    open: false,
                    genre: null
                },
                albumFilters: {
                    title: "",
                    genreName: "",
                    trackTitle: "",
                    nativeQuery: false
                },
                trackFilters: { title: "" },
                artistFilters: { name: "" },
                genreFilters: { name: "" },
                userFilters: { name: "" },
                modal: {
                    open: false,
                    mode: "create",
                    type: "",
                    id: null,
                    form: {},
                    trackSelector: {
                        album: false,
                        artist: false,
                        genre: false
                    }
                },
                asyncForm: {
                    steps: 8,
                    delayMillis: 120,
                    incrementPerStep: 2
                },
                asyncTask: {
                    taskId: null,
                    status: null
                },
                raceForm: {
                    threads: 80,
                    incrementsPerThread: 10000
                },
                raceResult: null,
                navItems: [
                    { id: "home", label: "Главная" },
                    { id: "albums", label: "Альбомы" },
                    { id: "tracks", label: "Треки" },
                    { id: "artists", label: "Артисты" },
                    { id: "genres", label: "Жанры" },
                ]
            };
        },
        computed: {
            currentViewMeta() {
                const meta = {
                    home: {
                        kicker: "главная",
                        title: "Твоя музыкальная библиотека",
                    },
                    albums: {
                        kicker: "альбомы",
                        title: "Альбомы и их треки",
                    },
                    tracks: {
                        kicker: "треки",
                        title: "Треки каталога",
                    },
                    artists: {
                        kicker: "артисты",
                        title: "Артисты и их альбомы",
                    },
                    genres: {
                        kicker: "жанры",
                        title: "Жанры и музыкальные подборки",
                    },
                    users: {
                        kicker: "пользователи",
                        title: "Пользователи и любимые треки",
                    },
                    lab: {
                        kicker: "лаборатория",
                        title: "Асинхронные задачи и concurrency",
                    }
                };
                return meta[this.activeView];
            },
            selectedTrackArtistNames() {
                const albumId = Number(this.modal.form.albumId);
                if (!albumId) {
                    return [];
                }
                const album = this.reference.albums.find(item => item.id === albumId);
                if (!album || !album.artistIds) {
                    return [];
                }
                return album.artistIds.map(this.artistName);
            },
            featuredAlbum() {
                return this.reference.albums[0] || null;
            },
            albumsPreview() {
                return this.reference.albums.slice(0, 4);
            },
            artistsPreview() {
                return this.reference.artists.slice(0, 4);
            },
            genresPreview() {
                return this.reference.genres.slice(0, 4);
            },
            modalTitle() {
                const titles = {
                    album: "Альбом",
                    track: "Трек",
                    artist: "Артист",
                    genre: "Жанр",
                    user: "Пользователь"
                };
                return titles[this.modal.type] || "Запись";
            }
        },
        mounted() {
            const initial = window.location.hash.replace("#/", "");
            if (initial && this.navItems.some(item => item.id === initial)) {
                this.activeView = initial;
            }
            window.addEventListener("hashchange", this.handleHashChange);
            this.bootstrap();
        },
        beforeUnmount() {
            window.removeEventListener("hashchange", this.handleHashChange);
        },
        methods: {
            async bootstrap() {
                await this.refreshReferenceData();
                await this.refreshDashboard();
                await this.refreshActiveView();
            },
            handleHashChange() {
                const next = window.location.hash.replace("#/", "");
                if (next && this.navItems.some(item => item.id === next)) {
                    this.activeView = next;
                    this.refreshActiveView();
                }
            },
            navigate(view) {
                this.activeView = view;
                window.location.hash = `/${view}`;
                this.refreshActiveView();
            },
            openAlbumTracksModal(album) {
                this.albumTracksModal.open = true;
                this.albumTracksModal.album = album;
            },
            closeAlbumTracksModal() {
                this.albumTracksModal.open = false;
                this.albumTracksModal.album = null;
            },
            artistAlbumPreview(albumIds) {
                if (!albumIds || !albumIds.length) {
                    return "Нет альбомов";
                }
                const firstTwo = albumIds.slice(0, 2).map(this.albumTitle);
                return albumIds.length > 2 ? `${firstTwo.join(", ")}...` : firstTwo.join(", ");
            },
            openArtistAlbumsModal(artist) {
                this.artistAlbumsModal.open = true;
                this.artistAlbumsModal.artist = artist;
            },
            closeArtistAlbumsModal() {
                this.artistAlbumsModal.open = false;
                this.artistAlbumsModal.artist = null;
            },
            genreAlbumPreview(albumIds) {
                if (!albumIds || !albumIds.length) {
                    return "Нет альбомов";
                }
                const firstTwo = albumIds.slice(0, 2).map(this.albumTitle);
                return albumIds.length > 2 ? `${firstTwo.join(", ")}...` : firstTwo.join(", ");
            },
            openGenreAlbumsModal(genre) {
                this.genreAlbumsModal.open = true;
                this.genreAlbumsModal.genre = genre;
            },
            closeGenreAlbumsModal() {
                this.genreAlbumsModal.open = false;
                this.genreAlbumsModal.genre = null;
            },
            notify(message) {
                this.toast = message;
                clearTimeout(this.toastTimer);
                this.toastTimer = setTimeout(() => {
                    this.toast = "";
                }, 2600);
            },
            initials(value) {
                return (value || "MF")
                    .split(/\s+/)
                    .filter(Boolean)
                    .slice(0, 2)
                    .map(part => part[0].toUpperCase())
                    .join("");
            },
            trackSymbol(title) {
                return (title || "T").charAt(0).toUpperCase();
            },
            formatDuration(seconds) {
                if (seconds === null || seconds === undefined) {
                    return "—";
                }
                const mins = Math.floor(seconds / 60);
                const rest = String(seconds % 60).padStart(2, "0");
                return `${mins}:${rest}`;
            },
            pageCount(totalPages) {
                return totalPages > 0 ? totalPages : 1;
            },
            pretty(value) {
                return JSON.stringify(value, null, 2);
            },
            async api(path, options = {}) {
                const response = await fetch(path, {
                    headers: {
                        "Content-Type": "application/json",
                        ...(options.headers || {})
                    },
                    ...options
                });

                if (!response.ok) {
                    let message = `HTTP ${response.status}`;
                    try {
                        const body = await response.json();
                        message = body.message || body.error || JSON.stringify(body);
                    } catch (_error) {
                        const text = await response.text();
                        if (text) {
                            message = text;
                        }
                    }
                    throw new Error(message);
                }

                if (response.status === 204) {
                    return null;
                }

                const contentType = response.headers.get("content-type") || "";
                return contentType.includes("application/json")
                    ? response.json()
                    : response.text();
            },
            async fetchPaged(path, params) {
                const query = toQuery(params);
                return this.api(query ? `${path}?${query}` : path);
            },
            async refreshDashboard() {
                const [albums, tracks, artists, genres, users] = await Promise.all([
                    this.fetchPaged("/api/albums", { page: 0, size: 1 }),
                    this.fetchPaged("/api/tracks", { page: 0, size: 1 }),
                    this.fetchPaged("/api/artists", { page: 0, size: 1 }),
                    this.fetchPaged("/api/genres", { page: 0, size: 1 }),
                    this.fetchPaged("/api/users", { page: 0, size: 1 })
                ]);

                this.dashboard.albums = albums.totalElements;
                this.dashboard.tracks = tracks.totalElements;
                this.dashboard.artists = artists.totalElements;
                this.dashboard.genres = genres.totalElements;
                this.dashboard.users = users.totalElements;
            },
            async refreshReferenceData() {
                const [albums, tracks, artists, genres, users] = await Promise.all([
                    this.fetchPaged("/api/albums", { page: 0, size: 200 }),
                    this.fetchPaged("/api/tracks", { page: 0, size: 200 }),
                    this.fetchPaged("/api/artists", { page: 0, size: 200 }),
                    this.fetchPaged("/api/genres", { page: 0, size: 200 }),
                    this.fetchPaged("/api/users", { page: 0, size: 200 })
                ]);

                this.reference.albums = albums.content;
                this.reference.tracks = tracks.content;
                this.reference.artists = artists.content;
                this.reference.genres = genres.content;
                this.reference.users = users.content;
            },
            async refreshActiveView() {
                try {
                    if (this.activeView === "albums") {
                        await this.loadAlbums(this.albums.page || 0);
                    } else if (this.activeView === "tracks") {
                        await this.loadTracks(this.tracks.page || 0);
                    } else if (this.activeView === "artists") {
                        await this.loadArtists(this.artists.page || 0);
                    } else if (this.activeView === "genres") {
                        await this.loadGenres(this.genres.page || 0);
                    } else if (this.activeView === "users") {
                        await this.loadUsers(this.users.page || 0);
                    }
                } catch (error) {
                    this.notify(error.message);
                }
            },
            albumTitle(id) {
                return this.reference.albums.find(item => item.id === id)?.title || "Неизвестный альбом";
            },
            artistName(id) {
                return this.reference.artists.find(item => item.id === id)?.name || "Неизвестный артист";
            },
            genreName(id) {
                return this.reference.genres.find(item => item.id === id)?.name || "Неизвестный жанр";
            },
            trackTitle(id) {
                return this.reference.tracks.find(item => item.id === id)?.title || "Неизвестный трек";
            },
            artistList(ids) {
                if (!ids || !ids.length) {
                    return "Без указанных артистов";
                }
                return ids.map(this.artistName).join(", ");
            },
            albumList(ids) {
                if (!ids || !ids.length) {
                    return "Пока без альбомов";
                }
                return ids.map(this.albumTitle).join(", ");
            },
            trackList(ids) {
                if (!ids || !ids.length) {
                    return "Пока без любимых треков";
                }
                return ids.map(this.trackTitle).join(", ");
            },
            albumArtistList(albumId) {
                const album = this.reference.albums.find(item => item.id === albumId);
                if (!album || !album.artistIds || !album.artistIds.length) {
                    return "Исполнитель не указан";
                }
                return album.artistIds.map(this.artistName).join(", ");
            },
            openGenreFilter(genreName) {
                this.activeView = "albums";
                window.location.hash = "/albums";
                this.albumFilters.title = "";
                this.albumFilters.genreName = genreName;
                this.albumFilters.trackTitle = "";
                this.loadAlbums(0);
            },
            async loadAlbums(page = 0) {
                const baseParams = {
                    page,
                    size: this.albums.size || 8
                };
                let data;

                if (this.albumFilters.genreName || this.albumFilters.trackTitle || this.albumFilters.nativeQuery) {
                    data = await this.fetchPaged("/api/albums/search", {
                        ...baseParams,
                        genreName: this.albumFilters.genreName,
                        trackTitle: this.albumFilters.trackTitle,
                        nativeQuery: this.albumFilters.nativeQuery
                    });
                } else {
                    data = await this.fetchPaged("/api/albums", {
                        ...baseParams,
                        title: this.albumFilters.title
                    });
                }

                this.albums = data;
            },
            async loadTracks(page = 0) {
                this.tracks = await this.fetchPaged("/api/tracks", {
                    page,
                    size: this.tracks.size || 10,
                    title: this.trackFilters.title
                });
            },
            async loadArtists(page = 0) {
                this.artists = await this.fetchPaged("/api/artists", {
                    page,
                    size: this.artists.size || 9,
                    name: this.artistFilters.name
                });
            },
            async loadGenres(page = 0) {
                this.genres = await this.fetchPaged("/api/genres", {
                    page,
                    size: this.genres.size || 9,
                    name: this.genreFilters.name
                });
            },
            async loadUsers(page = 0) {
                this.users = await this.fetchPaged("/api/users", {
                    page,
                    size: this.users.size || 9,
                    name: this.userFilters.name
                });
            },
            resetAlbumFilters() {
                this.albumFilters = { title: "", genreName: "", trackTitle: "", nativeQuery: false };
                this.loadAlbums(0);
            },
            resetTracks() {
                this.trackFilters = { title: "" };
                this.loadTracks(0);
            },
            resetArtists() {
                this.artistFilters = { name: "" };
                this.loadArtists(0);
            },
            resetGenres() {
                this.genreFilters = { name: "" };
                this.loadGenres(0);
            },
            resetUsers() {
                this.userFilters = { name: "" };
                this.loadUsers(0);
            },
            emptyForm(type) {
                if (type === "album") {
                    return {
                        title: "",
                        releaseYear: new Date().getFullYear(),
                        artistIds: [],
                        genreIds: []
                    };
                }
                if (type === "track") {
                    return {
                        title: "",
                        durationSeconds: 180,
                        albumId: "",
                        artistId: "",
                        genreId: ""
                    };
                }
                if (type === "artist") {
                    return { name: "" };
                }
                if (type === "genre") {
                    return { name: "" };
                }
                if (type === "user") {
                    return { name: "", trackIds: [] };
                }
                return {};
            },
            formFromItem(type, item) {
                if (type === "album") {
                    return {
                        title: item.title,
                        releaseYear: item.releaseYear,
                        artistIds: [...item.artistIds],
                        genreIds: [...item.genreIds]
                    };
                }
                if (type === "track") {
                    return {
                        title: item.title,
                        durationSeconds: item.durationSeconds,
                        albumId: item.albumId ?? "",
                        artistId: item.artistId ?? "",
                        genreId: item.genreId ?? ""
                    };
                }
                if (type === "artist" || type === "genre") {
                    return { name: item.name };
                }
                if (type === "user") {
                    return {
                        name: item.name,
                        trackIds: [...item.trackIds]
                    };
                }
                return {};
            },
            openCreate(type) {
                this.modal.open = true;
                this.modal.mode = "create";
                this.modal.type = type;
                this.modal.id = null;
                this.modal.form = this.emptyForm(type);
                this.resetTrackSelectors();
            },
            openTrackForAlbum(album) {
                this.modal.open = true;
                this.modal.mode = "create";
                this.modal.type = "track";
                this.modal.id = null;
                this.modal.form = {
                    title: "",
                    durationSeconds: 180,
                    albumId: album.id,
                    artistId: "",
                    genreId: ""
                };
                this.resetTrackSelectors();
            },
            openEdit(type, item) {
                this.modal.open = true;
                this.modal.mode = "edit";
                this.modal.type = type;
                this.modal.id = item.id;
                this.modal.form = this.formFromItem(type, item);
            },
            closeModal() {
                this.modal.open = false;
                this.modal.type = "";
                this.modal.id = null;
                this.modal.form = {};
                this.resetTrackSelectors();
            },
            resetTrackSelectors() {
                this.modal.trackSelector = {
                    album: false,
                    artist: false,
                    genre: false
                };
            },
            toggleTrackSelector(field) {
                if (this.modal.type !== "track") {
                    return;
                }
                this.modal.trackSelector[field] = !this.modal.trackSelector[field];
            },
            normalizedPayload(type, form) {
                if (type === "album") {
                    return {
                        title: form.title,
                        releaseYear: Number(form.releaseYear),
                        artistIds: (form.artistIds || []).map(Number),
                        genreIds: (form.genreIds || []).map(Number)
                    };
                }
                if (type === "track") {
                    return {
                        title: form.title,
                        durationSeconds: Number(form.durationSeconds),
                        albumId: form.albumId ? Number(form.albumId) : null,
                        artistId: form.artistId ? Number(form.artistId) : null,
                        genreId: form.genreId ? Number(form.genreId) : null
                    };
                }
                if (type === "user") {
                    return {
                        name: form.name,
                        trackIds: (form.trackIds || []).map(Number)
                    };
                }
                return form;
            },
            async submitModal() {
                try {
                    const type = this.modal.type;
                    const mode = this.modal.mode;
                    const id = this.modal.id;
                    const payload = this.normalizedPayload(type, this.modal.form);

                    const paths = {
                        album: "/api/albums",
                        track: "/api/tracks",
                        artist: "/api/artists",
                        genre: "/api/genres",
                        user: "/api/users"
                    };

                    const path = mode === "create" ? paths[type] : `${paths[type]}/${id}`;
                    await this.api(path, {
                        method: mode === "create" ? "POST" : "PUT",
                        body: JSON.stringify(payload)
                    });

                    this.closeModal();
                    await this.refreshReferenceData();
                    await this.refreshDashboard();
                    await this.refreshActiveView();
                    this.notify("Изменения сохранены");
                } catch (error) {
                    this.notify(error.message);
                }
            },
            async removeEntity(type, id, label) {
                const titles = {
                    album: "альбом",
                    track: "трек",
                    artist: "артиста",
                    genre: "жанр",
                    user: "пользователя"
                };

                if (!window.confirm(`Удалить ${titles[type]} «${label}»?`)) {
                    return;
                }

                try {
                    const paths = {
                        album: `/api/albums/${id}`,
                        track: `/api/tracks/${id}`,
                        artist: `/api/artists/${id}`,
                        genre: `/api/genres/${id}`,
                        user: `/api/users/${id}`
                    };

                    await this.api(paths[type], { method: "DELETE" });
                    await this.refreshReferenceData();
                    await this.refreshDashboard();
                    await this.refreshActiveView();
                    this.notify("Запись удалена");
                } catch (error) {
                    this.notify(error.message);
                }
            },
            async startAsyncTask() {
                try {
                    const result = await this.api("/api/concurrency/tasks", {
                        method: "POST",
                        body: JSON.stringify(this.asyncForm)
                    });
                    this.asyncTask.taskId = result.taskId;
                    await this.refreshTaskStatus();
                    this.notify("Фоновая задача запущена");
                } catch (error) {
                    this.notify(error.message);
                }
            },
            async refreshTaskStatus() {
                if (!this.asyncTask.taskId) {
                    return;
                }
                try {
                    this.asyncTask.status = await this.api(`/api/concurrency/tasks/${this.asyncTask.taskId}`);
                } catch (error) {
                    this.notify(error.message);
                }
            },
            async runRaceDemo() {
                try {
                    const query = toQuery({
                        threads: this.raceForm.threads,
                        incrementsPerThread: this.raceForm.incrementsPerThread
                    });
                    this.raceResult = await this.api(`/api/concurrency/race-demo?${query}`, { method: "POST" });
                    this.notify("Demo выполнен");
                } catch (error) {
                    this.notify(error.message);
                }
            }
        }
    }).mount("#app");
})();
