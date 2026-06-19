package com.privatevault.data

import com.privatevault.core.ImportMode
import com.privatevault.data.LibraryEntity
import com.privatevault.data.MovieEntity
import com.privatevault.data.MovieImageEntity
import com.privatevault.data.MovieLinkEntity
import com.privatevault.data.MovieTagCrossRef
import com.privatevault.data.TagEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VaultRepositoryTest {
    @Test
    fun addLibraryPersistsThroughStoreAndUpdatesState() = runTest {
        val store = FakeVaultStore(VaultSnapshot.empty())
        val repository = VaultRepository(store, idGenerator = sequenceIds("library-uuid"))

        repository.addLibrary(" 新片库 ")
        val state = repository.state.first()

        assertEquals("新片库", state.libraries.single().name)
        assertEquals("library-uuid", state.libraries.single().id)
    }

    @Test
    fun addMovieImagePersistsImportMetadata() = runTest {
        val store = FakeVaultStore(
            VaultSnapshot(
                libraries = listOf(LibraryEntity("library-main", "片库", 0, 1L, 1L)),
                movies = listOf(
                    MovieEntity("movie-1", "library-main", "影片", "", false, 0L, 1L, 1L)
                ),
                images = emptyList(),
                links = emptyList(),
                tags = emptyList(),
                movieTags = emptyList()
            )
        )
        val repository = VaultRepository(store, idGenerator = sequenceIds("image-uuid"))

        repository.addMovieImage(
            movieId = "movie-1",
            privatePath = "/vault/private.jpg",
            originalUri = "content://media/image/1",
            mimeType = "image/jpeg",
            importMode = ImportMode.MoveAndHideOriginal,
            originalRemoved = true
        )
        val state = repository.state.first()

        assertEquals("/vault/private.jpg", state.movies.single().coverImage?.localPath)
        assertEquals(ImportMode.MoveAndHideOriginal, state.movies.single().coverImage?.importMode)
        assertEquals("content://media/image/1", state.movies.single().coverImage?.originalUri)
        assertEquals("image/jpeg", state.movies.single().coverImage?.mimeType)
        assertEquals(true, state.movies.single().coverImage?.originalRemoved)
    }

    @Test
    fun addMoviePersistsInSelectedLibrary() = runTest {
        val store = FakeVaultStore(
            VaultSnapshot(
                libraries = listOf(LibraryEntity("library-main", "片库", 0, 1L, 1L)),
                movies = emptyList(),
                images = emptyList(),
                links = emptyList(),
                tags = emptyList(),
                movieTags = emptyList()
            )
        )
        val repository = VaultRepository(store, idGenerator = sequenceIds("movie-uuid"))

        repository.addMovie(libraryId = "library-main", title = " 新影片 ")
        val state = repository.state.first()

        assertEquals("新影片", state.movies.single().title)
        assertEquals(listOf("movie-uuid"), state.libraries.single().movieIds)
    }

    @Test
    fun updateMovieTitleTrimsInputAndIgnoresBlankTitles() = runTest {
        val store = FakeVaultStore(
            VaultSnapshot(
                libraries = listOf(LibraryEntity("library-main", "片库", 0, 1L, 1L)),
                movies = listOf(
                    MovieEntity("movie-1", "library-main", "旧标题", "", false, 0L, 1L, 1L)
                ),
                images = emptyList(),
                links = emptyList(),
                tags = emptyList(),
                movieTags = emptyList()
            )
        )
        val repository = VaultRepository(store)

        repository.updateMovieTitle(movieId = "movie-1", title = " 新标题 ")
        repository.updateMovieTitle(movieId = "movie-1", title = "   ")
        val state = repository.state.first()

        assertEquals("新标题", state.movies.single().title)
    }

    @Test
    fun deleteMovieImageRemovesOnlyTheSelectedImage() = runTest {
        val store = FakeVaultStore(
            VaultSnapshot(
                libraries = listOf(LibraryEntity("library-main", "片库", 0, 1L, 1L)),
                movies = listOf(
                    MovieEntity("movie-1", "library-main", "影片", "", false, 0L, 1L, 1L)
                ),
                images = listOf(
                    MovieImageEntity(
                        id = "image-1",
                        movieId = "movie-1",
                        privatePath = "/vault/private-1.jpg",
                        originalUri = null,
                        mimeType = "image/jpeg",
                        importMode = ImportMode.CopyOnly,
                        originalRemoved = false,
                        sortOrder = 0,
                        createdAt = 1L
                    ),
                    MovieImageEntity(
                        id = "image-2",
                        movieId = "movie-1",
                        privatePath = "/vault/private-2.mp4",
                        originalUri = null,
                        mimeType = "video/mp4",
                        importMode = ImportMode.CopyOnly,
                        originalRemoved = false,
                        sortOrder = 1,
                        createdAt = 2L
                    )
                ),
                links = emptyList(),
                tags = emptyList(),
                movieTags = emptyList()
            )
        )
        val repository = VaultRepository(store)

        repository.deleteMovieImage("image-1")
        val state = repository.state.first()

        assertEquals(listOf("image-2"), state.movies.single().detailImages.map { it.id })
        assertEquals("video/mp4", state.movies.single().coverImage?.mimeType)
    }
}

private fun sequenceIds(vararg ids: String): () -> String {
    val queue = ArrayDeque(ids.toList())
    return { queue.removeFirst() }
}

private class FakeVaultStore(initialSnapshot: VaultSnapshot) : VaultStore {
    private val snapshots = MutableStateFlow(initialSnapshot)

    override fun observeSnapshot() = snapshots

    override suspend fun upsertLibrary(library: LibraryEntity) {
        snapshots.value = snapshots.value.copy(libraries = snapshots.value.libraries + library)
    }

    override suspend fun updateLibraryName(libraryId: String, name: String, updatedAt: Long) = Unit
    override suspend fun deleteLibrary(libraryId: String) = Unit

    override suspend fun insertMovie(movie: MovieEntity) {
        snapshots.value = snapshots.value.copy(movies = snapshots.value.movies + movie)
    }

    override suspend fun insertMovieImage(image: MovieImageEntity) {
        snapshots.value = snapshots.value.copy(images = snapshots.value.images + image)
    }

    override suspend fun updateMovieTitle(movieId: String, title: String, updatedAt: Long) {
        snapshots.value = snapshots.value.copy(
            movies = snapshots.value.movies.map { movie ->
                if (movie.id == movieId) movie.copy(title = title, updatedAt = updatedAt) else movie
            }
        )
    }

    override suspend fun updateMovieNotes(movieId: String, notes: String, updatedAt: Long) = Unit
    override suspend fun updateMovieFavorite(movieId: String, isFavorite: Boolean, updatedAt: Long) = Unit
    override suspend fun updateMovieLastOpenedAt(movieId: String, lastOpenedAt: Long) = Unit
    override suspend fun deleteMovieImage(imageId: String) {
        snapshots.value = snapshots.value.copy(
            images = snapshots.value.images.filterNot { it.id == imageId }
        )
    }
    override suspend fun insertLink(link: MovieLinkEntity) = Unit
    override suspend fun deleteLink(linkId: String) = Unit
    override suspend fun upsertTag(tag: TagEntity) = Unit
    override suspend fun insertMovieTagCrossRef(crossRef: MovieTagCrossRef) = Unit
    override suspend fun deleteMovieTagCrossRef(movieId: String, tagId: String) = Unit
    override suspend fun libraryCount() = snapshots.value.libraries.size
    override suspend fun movieCount() = snapshots.value.movies.size
}
