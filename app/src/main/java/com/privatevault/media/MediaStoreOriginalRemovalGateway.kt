package com.privatevault.media

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log

class MediaStoreOriginalRemovalGateway(private val context: Context) {
    fun createDeleteRequest(uris: List<Uri>): PendingIntent? {
        if (uris.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return runCatching {
            val deleteUris = uris.map { uri ->
                resolveSpecificMediaStoreUri(uri) ?: uri
            }
            MediaStore.createDeleteRequest(context.contentResolver, deleteUris)
        }.onFailure { error ->
            Log.w(TAG, "Cannot create MediaStore delete request for selected items.", error)
        }.getOrNull()
    }

    private fun resolveSpecificMediaStoreUri(uri: Uri): Uri? {
        val resolver = context.contentResolver
        val mediaId = uri.lastPathSegment?.toLongOrNull() ?: resolver.queryLong(uri, BaseColumns._ID)
        if (mediaId == null || mediaId < 0L) return null

        val mimeType = resolver.getType(uri) ?: resolver.queryString(uri, MediaStore.MediaColumns.MIME_TYPE)
        val collectionUri = when {
            mimeType?.startsWith("image/") == true -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            mimeType?.startsWith("video/") == true -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }
        return ContentUris.withAppendedId(collectionUri, mediaId)
    }

    private companion object {
        const val TAG = "OriginalRemoval"
    }
}

private fun ContentResolver.queryLong(uri: Uri, column: String): Long? {
    return query(uri, arrayOf(column), null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val columnIndex = cursor.getColumnIndex(column)
        if (columnIndex < 0 || cursor.isNull(columnIndex)) null else cursor.getLong(columnIndex)
    }
}

private fun ContentResolver.queryString(uri: Uri, column: String): String? {
    return query(uri, arrayOf(column), null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val columnIndex = cursor.getColumnIndex(column)
        if (columnIndex < 0 || cursor.isNull(columnIndex)) null else cursor.getString(columnIndex)
    }
}
