package com.privatevault.data

import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType
import com.privatevault.core.VaultTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class VaultMappersTest {
    @Test
    fun snapshotMapsToUnlockedLibraryStateWithMovieDetails() {
        val snapshot = VaultSnapshot(
            libraries = listOf(
                LibraryEntity(id = "library-main", name = "片库A", sortOrder = 0, createdAt = 1L, updatedAt = 2L)
            ),
            movies = listOf(
                MovieEntity(
                    id = "movie-1",
                    libraryId = "library-main",
                    title = "影片A",
                    notes = "备注A",
                    isFavorite = true,
                    lastOpenedAt = 9L,
                    createdAt = 3L,
                    updatedAt = 4L
                )
            ),
            images = listOf(
                MovieImageEntity(
                    id = "image-1",
                    movieId = "movie-1",
                    privatePath = "/vault/image-1.jpg",
                    originalUri = "content://media/image/1",
                    mimeType = "image/jpeg",
                    importMode = ImportMode.MoveAndHideOriginal,
                    originalRemoved = false,
                    sortOrder = 0,
                    createdAt = 5L
                )
            ),
            links = listOf(
                MovieLinkEntity(
                    id = "link-1",
                    movieId = "movie-1",
                    url = "https://pan.example.com/a",
                    type = LinkType.Quark,
                    sortOrder = 0,
                    createdAt = 6L
                )
            ),
            tags = listOf(
                TagEntity(id = "tag-1", name = "精选", color = null, createdAt = 7L, updatedAt = 8L)
            ),
            movieTags = listOf(MovieTagCrossRef(movieId = "movie-1", tagId = "tag-1"))
        )

        val state = snapshot.toVaultAppState(isLocked = false)

        assertFalse(state.isLocked)
        assertEquals(VaultTab.LibraryManage, state.selectedTab)
        assertEquals("片库A", state.libraries.single().name)
        assertEquals(listOf("movie-1"), state.libraries.single().movieIds)
        assertEquals("影片A", state.movies.single().title)
        assertEquals("/vault/image-1.jpg", state.movies.single().coverImage?.localPath)
        assertEquals("content://media/image/1", state.movies.single().coverImage?.originalUri)
        assertEquals(false, state.movies.single().coverImage?.originalRemoved)
        assertEquals(LinkType.Quark, state.movies.single().links.single().type)
        assertEquals(listOf("tag-1"), state.movies.single().tagIds)
        assertEquals("精选", state.tags.single().name)
        assertNotNull(state.favoriteMovies.single().coverImage)
    }
}
