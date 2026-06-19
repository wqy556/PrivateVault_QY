package com.privatevault.core

data class VaultLibrary(
    val id: String,
    val name: String,
    val movieIds: List<String>
)

data class VaultMovie(
    val id: String,
    val libraryId: String,
    val title: String,
    val detailImages: List<MovieImage>,
    val links: List<MovieLink>,
    val notes: String,
    val tagIds: List<String>,
    val isFavorite: Boolean,
    val lastOpenedAt: Long
) {
    val coverImage: MovieImage?
        get() = detailImages.firstOrNull()
}

data class MovieImage(
    val id: String,
    val localPath: String?,
    val importMode: ImportMode
)

data class MovieLink(
    val id: String,
    val url: String,
    val type: LinkType
)

enum class LinkType {
    Quark,
    Baidu,
    Xunlei,
    Magnet,
    Web,
    Other
}

data class MovieTag(
    val id: String,
    val name: String
)

enum class VaultTab {
    LibraryManage,
    Favorites,
    LibraryDetail,
    MovieDetail,
    Settings
}

data class VaultAppState(
    val isLocked: Boolean,
    val libraries: List<VaultLibrary>,
    val movies: List<VaultMovie>,
    val tags: List<MovieTag>,
    val selectedLibraryId: String?,
    val selectedMovieId: String?,
    val selectedTab: VaultTab,
    val pendingImportCount: Int,
    val pendingImportMode: ImportMode?
) {
    val selectedLibrary: VaultLibrary?
        get() = libraries.firstOrNull { it.id == selectedLibraryId }

    val selectedMovie: VaultMovie?
        get() = movies.firstOrNull { it.id == selectedMovieId }

    val moviesInSelectedLibrary: List<VaultMovie>
        get() = selectedLibrary?.movieIds
            ?.mapNotNull { movieId -> movies.firstOrNull { it.id == movieId } }
            .orEmpty()

    val recentlyOpenedMovies: List<VaultMovie>
        get() = movies.sortedByDescending { it.lastOpenedAt }.take(5)

    val favoriteMovies: List<VaultMovie>
        get() = movies.filter { it.isFavorite }

    fun unlock(passcode: String): VaultAppState {
        return if (passcode.isBlank()) this else copy(isLocked = false)
    }

    fun requireUnlock(): VaultAppState = copy(isLocked = true)

    fun selectTab(tab: VaultTab): VaultAppState {
        return when (tab) {
            VaultTab.LibraryManage -> copy(selectedTab = tab, selectedLibraryId = null, selectedMovieId = null)
            VaultTab.Favorites,
            VaultTab.Settings -> copy(selectedTab = tab, selectedMovieId = null)
            VaultTab.LibraryDetail,
            VaultTab.MovieDetail -> copy(selectedTab = tab)
        }
    }

    fun selectLibrary(libraryId: String): VaultAppState {
        return if (libraries.any { it.id == libraryId }) {
            copy(
                selectedLibraryId = libraryId,
                selectedMovieId = null,
                selectedTab = VaultTab.LibraryDetail
            )
        } else {
            this
        }
    }

    fun addLibrary(name: String): VaultAppState {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return this
        val nextNumber = libraries.size + 1
        val library = VaultLibrary(
            id = "library-$nextNumber",
            name = trimmedName,
            movieIds = emptyList()
        )
        return copy(libraries = libraries + library, selectedTab = VaultTab.LibraryManage)
    }

    fun renameLibrary(libraryId: String, name: String): VaultAppState {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return this
        return copy(
            libraries = libraries.map { library ->
                if (library.id == libraryId) library.copy(name = trimmedName) else library
            }
        )
    }

    fun deleteLibrary(libraryId: String): VaultAppState {
        val remainingLibraries = libraries.filterNot { it.id == libraryId }
        val removedMovieIds = libraries.firstOrNull { it.id == libraryId }?.movieIds.orEmpty()
        return copy(
            libraries = remainingLibraries,
            movies = movies.filterNot { it.id in removedMovieIds },
            selectedLibraryId = selectedLibraryId.takeUnless { it == libraryId },
            selectedMovieId = selectedMovieId.takeUnless { it in removedMovieIds },
            selectedTab = VaultTab.LibraryManage
        )
    }

    fun openMovie(movieId: String): VaultAppState {
        return if (movies.any { it.id == movieId }) {
            copy(selectedMovieId = movieId, selectedTab = VaultTab.MovieDetail)
        } else {
            this
        }
    }

    fun stageImageSelection(count: Int, mode: ImportMode): VaultAppState {
        return copy(
            pendingImportCount = count.coerceAtLeast(0),
            pendingImportMode = mode
        )
    }

    companion object {
        private const val DEFAULT_LIBRARY_ID = "library-main"
        private const val SECOND_LIBRARY_ID = "library-favorites"
        private const val SAMPLE_MOVIE_ID = "movie-sample"

        fun initial(): VaultAppState {
            val defaultLibrary = VaultLibrary(
                id = DEFAULT_LIBRARY_ID,
                name = "我的片库",
                movieIds = listOf(SAMPLE_MOVIE_ID)
            )
            val secondLibrary = VaultLibrary(
                id = SECOND_LIBRARY_ID,
                name = "收藏片库",
                movieIds = emptyList()
            )
            val tags = listOf(
                MovieTag(id = "tag-favorite", name = "收藏"),
                MovieTag(id = "tag-unwatched", name = "未看")
            )
            val sampleMovie = VaultMovie(
                id = SAMPLE_MOVIE_ID,
                libraryId = DEFAULT_LIBRARY_ID,
                title = "示例影片",
                detailImages = listOf(
                    MovieImage(
                        id = "image-cover",
                        localPath = null,
                        importMode = ImportMode.CopyOnly
                    )
                ),
                links = listOf(
                    MovieLink(
                        id = "link-quark",
                        url = "https://pan.example.com/sample",
                        type = LinkType.Quark
                    )
                ),
                notes = "这里可以记录影片说明、整理状态或来源信息。",
                tagIds = tags.map { it.id },
                isFavorite = true,
                lastOpenedAt = 1_000L
            )
            return VaultAppState(
                isLocked = true,
                libraries = listOf(defaultLibrary, secondLibrary),
                movies = listOf(sampleMovie),
                tags = tags,
                selectedLibraryId = null,
                selectedMovieId = null,
                selectedTab = VaultTab.LibraryManage,
                pendingImportCount = 0,
                pendingImportMode = null
            )
        }
    }
}
