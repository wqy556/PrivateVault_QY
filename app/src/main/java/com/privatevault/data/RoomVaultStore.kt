package com.privatevault.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomVaultStore(private val dao: VaultDao) : VaultStore {
    override fun observeSnapshot(): Flow<VaultSnapshot> {
        val partialFlow = combine(
            dao.observeLibraries(),
            dao.observeMovies(),
            dao.observeImages(),
            dao.observeLinks(),
            dao.observeTags()
        ) { libraries, movies, images, links, tags ->
            PartialVaultSnapshot(
                libraries = libraries,
                movies = movies,
                images = images,
                links = links,
                tags = tags
            )
        }

        return combine(partialFlow, dao.observeMovieTags()) { partial, movieTags ->
            VaultSnapshot(
                libraries = partial.libraries,
                movies = partial.movies,
                images = partial.images,
                links = partial.links,
                tags = partial.tags,
                movieTags = movieTags
            )
        }
    }

    override suspend fun upsertLibrary(library: LibraryEntity) {
        dao.upsertLibrary(library)
    }

    override suspend fun updateLibraryName(libraryId: String, name: String, updatedAt: Long) {
        dao.updateLibraryName(libraryId = libraryId, name = name, updatedAt = updatedAt)
    }

    override suspend fun deleteLibrary(libraryId: String) {
        dao.deleteLibrary(libraryId)
    }

    override suspend fun insertMovie(movie: MovieEntity) {
        dao.insertMovie(movie)
    }

    override suspend fun insertMovieImage(image: MovieImageEntity) {
        dao.insertMovieImage(image)
    }

    // ── Movie update operations ──

    override suspend fun updateMovieNotes(movieId: String, notes: String, updatedAt: Long) {
        dao.updateMovieNotes(movieId = movieId, notes = notes, updatedAt = updatedAt)
    }

    override suspend fun updateMovieFavorite(movieId: String, isFavorite: Boolean, updatedAt: Long) {
        dao.updateMovieFavorite(movieId = movieId, isFavorite = isFavorite, updatedAt = updatedAt)
    }

    override suspend fun updateMovieLastOpenedAt(movieId: String, lastOpenedAt: Long) {
        dao.updateMovieLastOpenedAt(movieId = movieId, lastOpenedAt = lastOpenedAt)
    }

    // ── Link CRUD ──

    override suspend fun insertLink(link: MovieLinkEntity) {
        dao.insertLink(link)
    }

    override suspend fun deleteLink(linkId: String) {
        dao.deleteLink(linkId)
    }

    // ── Tag CRUD ──

    override suspend fun upsertTag(tag: TagEntity) {
        dao.upsertTag(tag)
    }

    override suspend fun insertMovieTagCrossRef(crossRef: MovieTagCrossRef) {
        dao.insertMovieTagCrossRef(crossRef)
    }

    override suspend fun deleteMovieTagCrossRef(movieId: String, tagId: String) {
        dao.deleteMovieTagCrossRef(movieId = movieId, tagId = tagId)
    }

    // ── Count queries ──

    override suspend fun libraryCount(): Int {
        return dao.libraryCount()
    }

    override suspend fun movieCount(): Int {
        return dao.movieCount()
    }
}

private data class PartialVaultSnapshot(
    val libraries: List<LibraryEntity>,
    val movies: List<MovieEntity>,
    val images: List<MovieImageEntity>,
    val links: List<MovieLinkEntity>,
    val tags: List<TagEntity>
)
