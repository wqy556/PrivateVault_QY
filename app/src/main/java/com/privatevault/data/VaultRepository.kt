package com.privatevault.data

import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType
import com.privatevault.core.VaultAppState
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class VaultRepository(
    private val store: VaultStore,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val idGenerator: () -> String = { UUID.randomUUID().toString() }
) {
    val state: Flow<VaultAppState> = store.observeSnapshot()
        .map { snapshot -> snapshot.toVaultAppState(isLocked = false) }

    suspend fun addLibrary(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return

        val snapshot = store.observeSnapshot().first()
        val now = clock()
        store.upsertLibrary(
            LibraryEntity(
                id = idGenerator(),
                name = trimmedName,
                sortOrder = snapshot.libraries.size,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun renameLibrary(libraryId: String, name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        store.updateLibraryName(libraryId = libraryId, name = trimmedName, updatedAt = clock())
    }

    suspend fun deleteLibrary(libraryId: String) {
        store.deleteLibrary(libraryId)
    }

    suspend fun addMovie(libraryId: String, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        val snapshot = store.observeSnapshot().first()
        if (snapshot.libraries.none { it.id == libraryId }) return

        val now = clock()
        store.insertMovie(
            MovieEntity(
                id = idGenerator(),
                libraryId = libraryId,
                title = trimmedTitle,
                notes = "",
                isFavorite = false,
                lastOpenedAt = now,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun addMovieImage(
        movieId: String,
        privatePath: String,
        originalUri: String?,
        mimeType: String?,
        importMode: ImportMode,
        originalRemoved: Boolean
    ) {
        val snapshot = store.observeSnapshot().first()
        val nextNumber = snapshot.images.count { it.movieId == movieId } + 1
        store.insertMovieImage(
            MovieImageEntity(
                id = idGenerator(),
                movieId = movieId,
                privatePath = privatePath,
                originalUri = originalUri,
                mimeType = mimeType,
                importMode = importMode,
                originalRemoved = originalRemoved,
                sortOrder = nextNumber - 1,
                createdAt = clock()
            )
        )
    }

    suspend fun deleteMovieImage(imageId: String) {
        val snapshot = store.observeSnapshot().first()
        val image = snapshot.images.firstOrNull { it.id == imageId } ?: return
        store.deleteMovieImage(imageId)
        runCatching { File(image.privatePath).delete() }
    }

    // ── Movie update operations ──

    suspend fun updateMovieTitle(movieId: String, title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return
        store.updateMovieTitle(movieId = movieId, title = trimmedTitle, updatedAt = clock())
    }

    suspend fun updateMovieNotes(movieId: String, notes: String) {
        store.updateMovieNotes(movieId = movieId, notes = notes.trim(), updatedAt = clock())
    }

    suspend fun toggleFavorite(movieId: String) {
        val snapshot = store.observeSnapshot().first()
        val movie = snapshot.movies.firstOrNull { it.id == movieId } ?: return
        store.updateMovieFavorite(
            movieId = movieId,
            isFavorite = !movie.isFavorite,
            updatedAt = clock()
        )
    }

    suspend fun updateLastOpenedAt(movieId: String) {
        store.updateMovieLastOpenedAt(movieId = movieId, lastOpenedAt = clock())
    }

    // ── Link CRUD ──

    suspend fun addLink(movieId: String, url: String, type: LinkType) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) return

        val snapshot = store.observeSnapshot().first()
        val nextNumber = snapshot.links.count { it.movieId == movieId } + 1
        store.insertLink(
            MovieLinkEntity(
                id = idGenerator(),
                movieId = movieId,
                url = trimmedUrl,
                type = type,
                sortOrder = nextNumber - 1,
                createdAt = clock()
            )
        )
    }

    suspend fun deleteLink(linkId: String) {
        store.deleteLink(linkId)
    }

    // ── Tag CRUD ──

    suspend fun createTag(name: String): String {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) throw IllegalArgumentException("Tag name must not be blank")

        val snapshot = store.observeSnapshot().first()
        val existingTag = snapshot.tags.firstOrNull { it.name.equals(trimmedName, ignoreCase = true) }
        if (existingTag != null) return existingTag.id

        val now = clock()
        val tag = TagEntity(
            id = idGenerator(),
            name = trimmedName,
            color = null,
            createdAt = now,
            updatedAt = now
        )
        store.upsertTag(tag)
        return tag.id
    }

    suspend fun assignTag(movieId: String, tagId: String) {
        store.insertMovieTagCrossRef(MovieTagCrossRef(movieId = movieId, tagId = tagId))
    }

    suspend fun removeTag(movieId: String, tagId: String) {
        store.deleteMovieTagCrossRef(movieId = movieId, tagId = tagId)
    }

    // ── Initial data seeding ──

    suspend fun seedSampleDataIfEmpty() {
        val snapshot = store.observeSnapshot().first()
        if (snapshot.libraries.isNotEmpty()) return

        val now = clock()
        val defaultLibrary = LibraryEntity(
            id = "library-main",
            name = "我的片库",
            sortOrder = 0,
            createdAt = now,
            updatedAt = now
        )
        val secondLibrary = LibraryEntity(
            id = "library-favorites",
            name = "收藏片库",
            sortOrder = 1,
            createdAt = now,
            updatedAt = now
        )
        store.upsertLibrary(defaultLibrary)
        store.upsertLibrary(secondLibrary)

        val sampleMovie = MovieEntity(
            id = "movie-sample",
            libraryId = "library-main",
            title = "示例影片",
            notes = "这里可以记录影片说明、整理状态或来源信息。",
            isFavorite = true,
            lastOpenedAt = now,
            createdAt = now,
            updatedAt = now
        )
        store.insertMovie(sampleMovie)

        val tag1 = TagEntity(id = "tag-favorite", name = "收藏", color = null, createdAt = now, updatedAt = now)
        val tag2 = TagEntity(id = "tag-unwatched", name = "未看", color = null, createdAt = now, updatedAt = now)
        store.upsertTag(tag1)
        store.upsertTag(tag2)

        store.insertMovieTagCrossRef(MovieTagCrossRef(movieId = "movie-sample", tagId = "tag-favorite"))
        store.insertMovieTagCrossRef(MovieTagCrossRef(movieId = "movie-sample", tagId = "tag-unwatched"))

        store.insertLink(
            MovieLinkEntity(
                id = "movie-sample-link-1",
                movieId = "movie-sample",
                url = "https://pan.example.com/sample",
                type = LinkType.Quark,
                sortOrder = 0,
                createdAt = now
            )
        )
    }
}
