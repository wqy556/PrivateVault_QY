package com.privatevault.data

import kotlinx.coroutines.flow.Flow

interface VaultStore {
    fun observeSnapshot(): Flow<VaultSnapshot>

    suspend fun upsertLibrary(library: LibraryEntity)

    suspend fun updateLibraryName(libraryId: String, name: String, updatedAt: Long)

    suspend fun deleteLibrary(libraryId: String)

    suspend fun insertMovie(movie: MovieEntity)

    suspend fun insertMovieImage(image: MovieImageEntity)

    suspend fun deleteMovieImage(imageId: String)

    // ── Movie update operations ──

    suspend fun updateMovieTitle(movieId: String, title: String, updatedAt: Long)

    suspend fun updateMovieNotes(movieId: String, notes: String, updatedAt: Long)

    suspend fun updateMovieFavorite(movieId: String, isFavorite: Boolean, updatedAt: Long)

    suspend fun updateMovieLastOpenedAt(movieId: String, lastOpenedAt: Long)

    // ── Link CRUD ──

    suspend fun insertLink(link: MovieLinkEntity)

    suspend fun deleteLink(linkId: String)

    // ── Tag CRUD ──

    suspend fun upsertTag(tag: TagEntity)

    suspend fun insertMovieTagCrossRef(crossRef: MovieTagCrossRef)

    suspend fun deleteMovieTagCrossRef(movieId: String, tagId: String)

    // ── Count queries ──

    suspend fun libraryCount(): Int

    suspend fun movieCount(): Int
}
