package com.privatevault.media

import com.privatevault.core.ImportMode
import com.privatevault.core.MovieImage
import com.privatevault.core.VaultLibrary
import com.privatevault.core.VaultMovie
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaExportPlanTest {
    @Test
    fun plansOnlyImagesFromSelectedLibraryWithLibraryNameAsAlbum() {
        val library = VaultLibrary(
            id = "library-main",
            name = "私人片库",
            movieIds = listOf("movie-1")
        )
        val movies = listOf(
            movie(
                id = "movie-1",
                libraryId = "library-main",
                images = listOf(
                    image(id = "image-1", path = "/private/a.jpg", mimeType = "image/jpeg"),
                    image(id = "image-2", path = "/private/b.mp4", mimeType = "video/mp4")
                )
            ),
            movie(
                id = "movie-2",
                libraryId = "library-other",
                images = listOf(image(id = "image-3", path = "/private/c.jpg", mimeType = "image/jpeg"))
            )
        )

        val plan = planLibraryImageExport(library = library, movies = movies)

        assertEquals("私人片库", plan.albumName)
        assertEquals(listOf("/private/a.jpg"), plan.items.map { it.privatePath })
    }

    private fun movie(id: String, libraryId: String, images: List<MovieImage>): VaultMovie {
        return VaultMovie(
            id = id,
            libraryId = libraryId,
            title = id,
            detailImages = images,
            links = emptyList(),
            notes = "",
            tagIds = emptyList(),
            isFavorite = false,
            lastOpenedAt = 0L
        )
    }

    private fun image(id: String, path: String, mimeType: String): MovieImage {
        return MovieImage(
            id = id,
            localPath = path,
            mimeType = mimeType,
            importMode = ImportMode.CopyOnly
        )
    }
}
