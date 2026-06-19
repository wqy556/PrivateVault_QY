package com.privatevault.media

import com.privatevault.core.MovieImage
import com.privatevault.core.VaultLibrary
import com.privatevault.core.VaultMovie
import java.io.File

data class LibraryImageExportPlan(
    val albumName: String,
    val items: List<LibraryImageExportItem>
)

data class LibraryImageExportItem(
    val privatePath: String,
    val fileName: String,
    val mimeType: String?
)

fun planLibraryImageExport(
    library: VaultLibrary,
    movies: List<VaultMovie>
): LibraryImageExportPlan {
    val libraryMovieIds = library.movieIds.toSet()
    val items = movies
        .filter { movie -> movie.id in libraryMovieIds || movie.libraryId == library.id }
        .flatMap { movie ->
            movie.detailImages
                .filter { image -> image.localPath != null && image.isImageMedia() }
                .mapIndexed { index, image ->
                    val privatePath = requireNotNull(image.localPath)
                    LibraryImageExportItem(
                        privatePath = privatePath,
                        fileName = buildExportFileName(movie.title, index, privatePath),
                        mimeType = image.mimeType
                    )
                }
        }
    return LibraryImageExportPlan(albumName = library.name.trim().ifEmpty { "PrivateVault" }, items = items)
}

private fun MovieImage.isImageMedia(): Boolean {
    val mimeType = mimeType?.lowercase().orEmpty()
    val path = localPath?.lowercase().orEmpty()
    return mimeType.startsWith("image/") ||
        path.endsWith(".jpg") ||
        path.endsWith(".jpeg") ||
        path.endsWith(".png") ||
        path.endsWith(".webp") ||
        path.endsWith(".gif") ||
        path.endsWith(".heic")
}

private fun buildExportFileName(movieTitle: String, index: Int, privatePath: String): String {
    val extension = File(privatePath).extension.takeIf { it.isNotBlank() } ?: "jpg"
    val safeTitle = movieTitle
        .trim()
        .replace(Regex("""[\\/:*?"<>|]"""), "_")
        .ifEmpty { "image" }
    return "$safeTitle-${index + 1}.$extension"
}
