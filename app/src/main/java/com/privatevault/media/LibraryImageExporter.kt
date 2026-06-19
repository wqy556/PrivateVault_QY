package com.privatevault.media

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LibraryImageExportResult(
    val exportedCount: Int,
    val failedCount: Int
)

class LibraryImageExporter(private val context: Context) {
    suspend fun export(plan: LibraryImageExportPlan): LibraryImageExportResult = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var exportedCount = 0
        var failedCount = 0

        plan.items.forEach { item ->
            val sourceFile = File(item.privatePath)
            if (!sourceFile.isFile) {
                failedCount += 1
                return@forEach
            }

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, item.fileName)
                put(MediaStore.Images.Media.MIME_TYPE, item.mimeType ?: guessMimeType(item.fileName))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/${plan.albumName.toSafeAlbumPath()}")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                failedCount += 1
                return@forEach
            }

            runCatching {
                resolver.openOutputStream(uri).use { output ->
                    requireNotNull(output) { "Cannot open export target" }
                    sourceFile.inputStream().use { input -> input.copyTo(output) }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                exportedCount += 1
            }.onFailure {
                failedCount += 1
                runCatching { resolver.delete(uri, null, null) }
            }
        }

        LibraryImageExportResult(exportedCount = exportedCount, failedCount = failedCount)
    }

    private fun guessMimeType(fileName: String): String {
        val extension = File(fileName).extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/jpeg"
    }

    private fun String.toSafeAlbumPath(): String {
        return trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .ifEmpty { "PrivateVault" }
    }
}
