package com.privatevault.data

import com.privatevault.core.MovieImage
import com.privatevault.core.MovieLink
import com.privatevault.core.MovieTag
import com.privatevault.core.VaultAppState
import com.privatevault.core.VaultLibrary
import com.privatevault.core.VaultMovie
import com.privatevault.core.VaultTab

fun VaultSnapshot.toVaultAppState(isLocked: Boolean): VaultAppState {
    val imagesByMovie = images
        .sortedBy { it.sortOrder }
        .groupBy { it.movieId }
    val linksByMovie = links
        .sortedBy { it.sortOrder }
        .groupBy { it.movieId }
    val tagIdsByMovie = movieTags.groupBy(
        keySelector = { it.movieId },
        valueTransform = { it.tagId }
    )
    val movieIdsByLibrary = movies.groupBy(
        keySelector = { it.libraryId },
        valueTransform = { it.id }
    )

    return VaultAppState(
        isLocked = isLocked,
        libraries = libraries.sortedBy { it.sortOrder }.map { library ->
            VaultLibrary(
                id = library.id,
                name = library.name,
                movieIds = movieIdsByLibrary[library.id].orEmpty()
            )
        },
        movies = movies.map { movie ->
            VaultMovie(
                id = movie.id,
                libraryId = movie.libraryId,
                title = movie.title,
                detailImages = imagesByMovie[movie.id].orEmpty().map { image ->
                    MovieImage(
                        id = image.id,
                        localPath = image.privatePath,
                        importMode = image.importMode,
                        originalUri = image.originalUri,
                        originalRemoved = image.originalRemoved
                    )
                },
                links = linksByMovie[movie.id].orEmpty().map { link ->
                    MovieLink(
                        id = link.id,
                        url = link.url,
                        type = link.type
                    )
                },
                notes = movie.notes,
                tagIds = tagIdsByMovie[movie.id].orEmpty(),
                isFavorite = movie.isFavorite,
                lastOpenedAt = movie.lastOpenedAt
            )
        },
        tags = tags.map { tag -> MovieTag(id = tag.id, name = tag.name) },
        selectedLibraryId = null,
        selectedMovieId = null,
        selectedTab = VaultTab.LibraryManage,
        pendingImportCount = 0,
        pendingImportMode = null
    )
}
