package com.privatevault.core

import org.junit.Assert.assertEquals
import org.junit.Test

class VaultFiltersTest {
    @Test
    fun filtersLibrariesByFuzzyName() {
        val libraries = listOf(
            VaultLibrary(id = "library-1", name = "动作片库", movieIds = emptyList()),
            VaultLibrary(id = "library-2", name = "剧情收藏", movieIds = emptyList())
        )

        val filtered = filterLibraries(libraries = libraries, query = "动作")

        assertEquals(listOf("library-1"), filtered.map { it.id })
    }

    @Test
    fun filtersMoviesByTitleAndAnySelectedTag() {
        val movies = listOf(
            movie(id = "movie-1", title = "海边假日", tagIds = listOf("tag-a")),
            movie(id = "movie-2", title = "城市夜景", tagIds = listOf("tag-b")),
            movie(id = "movie-3", title = "海边访谈", tagIds = listOf("tag-c"))
        )

        val filtered = filterMovies(
            movies = movies,
            query = "海边",
            selectedTagIds = setOf("tag-a", "tag-b")
        )

        assertEquals(listOf("movie-1"), filtered.map { it.id })
    }

    private fun movie(id: String, title: String, tagIds: List<String>): VaultMovie {
        return VaultMovie(
            id = id,
            libraryId = "library-main",
            title = title,
            detailImages = emptyList(),
            links = emptyList(),
            notes = "",
            tagIds = tagIds,
            isFavorite = false,
            lastOpenedAt = 0L
        )
    }
}
