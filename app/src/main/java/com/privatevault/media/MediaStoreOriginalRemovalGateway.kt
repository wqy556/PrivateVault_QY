package com.privatevault.media

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log

class MediaStoreOriginalRemovalGateway(private val context: Context) {
    fun createDeleteRequest(uris: List<Uri>): PendingIntent? {
        if (uris.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return runCatching {
            MediaStore.createDeleteRequest(context.contentResolver, uris)
        }.onFailure { error ->
            Log.w(TAG, "Cannot create MediaStore delete request for selected items.", error)
        }.getOrNull()
    }

    private companion object {
        const val TAG = "OriginalRemoval"
    }
}
