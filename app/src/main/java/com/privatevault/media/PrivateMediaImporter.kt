package com.privatevault.media

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.privatevault.ui.ImportedVaultMedia
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrivateMediaImporter(private val context: Context) {
    suspend fun copyToPrivateStorage(uri: Uri): ImportedVaultMedia = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val extension = mimeType
            ?.let { mimeType -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) }
            ?.takeIf { it.isNotBlank() }
            ?: "bin"
        val mediaDir = File(context.filesDir, "private_media").apply { mkdirs() }
        val targetFile = File(mediaDir, "${UUID.randomUUID()}.$extension")

        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Cannot open selected media: $uri" }
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        ImportedVaultMedia(
            privatePath = targetFile.absolutePath,
            originalUri = uri.toString(),
            mimeType = mimeType
        )
    }
}
