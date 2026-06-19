package com.privatevault.media

import android.net.Uri

interface OriginalMediaRemovalGateway {
    suspend fun requestRemoveOriginals(uris: List<Uri>): OriginalRemovalResult
}

sealed class OriginalRemovalResult {
    object Removed : OriginalRemovalResult()
    object Denied : OriginalRemovalResult()
    data class Failed(val reason: String) : OriginalRemovalResult()
}
