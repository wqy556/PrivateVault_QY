package com.privatevault.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType
import com.privatevault.core.VaultAppState
import com.privatevault.core.VaultTab
import com.privatevault.data.VaultRepository
import com.privatevault.data.VaultSnapshot
import com.privatevault.data.toVaultAppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {
    private val transientState = MutableStateFlow(VaultTransientState())

    val uiState: StateFlow<VaultAppState> = combine(
        repository.state,
        transientState
    ) { persistedState, transient ->
        persistedState.copy(
            isLocked = transient.isLocked,
            selectedLibraryId = transient.selectedLibraryId,
            selectedMovieId = transient.selectedMovieId,
            selectedTab = transient.selectedTab,
            pendingImportCount = transient.pendingImportCount,
            pendingImportMode = transient.pendingImportMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VaultSnapshot.empty().toVaultAppState(isLocked = false)
    )

    // ── Navigation ──

    fun applyNavigation(state: VaultAppState) {
        transientState.update {
            it.copy(
                selectedLibraryId = state.selectedLibraryId,
                selectedMovieId = state.selectedMovieId,
                selectedTab = state.selectedTab,
                pendingImportCount = state.pendingImportCount,
                pendingImportMode = state.pendingImportMode
            )
        }
    }

    // ── Library ──

    fun addLibrary(name: String) {
        viewModelScope.launch { repository.addLibrary(name) }
    }

    fun renameLibrary(libraryId: String, name: String) {
        viewModelScope.launch { repository.renameLibrary(libraryId = libraryId, name = name) }
    }

    fun deleteLibrary(libraryId: String) {
        transientState.update {
            if (it.selectedLibraryId == libraryId) {
                it.copy(selectedLibraryId = null, selectedMovieId = null, selectedTab = VaultTab.LibraryManage)
            } else {
                it
            }
        }
        viewModelScope.launch { repository.deleteLibrary(libraryId) }
    }

    // ── Movie ──

    fun addMovie(libraryId: String, title: String) {
        viewModelScope.launch { repository.addMovie(libraryId = libraryId, title = title) }
    }

    fun updateMovieTitle(movieId: String, title: String) {
        viewModelScope.launch { repository.updateMovieTitle(movieId = movieId, title = title) }
    }

    fun updateMovieNotes(movieId: String, notes: String) {
        viewModelScope.launch { repository.updateMovieNotes(movieId = movieId, notes = notes) }
    }

    fun toggleFavorite(movieId: String) {
        viewModelScope.launch { repository.toggleFavorite(movieId) }
    }

    fun updateLastOpenedAt(movieId: String) {
        viewModelScope.launch { repository.updateLastOpenedAt(movieId) }
    }

    // ── Images ──

    fun stageImageSelection(count: Int, mode: ImportMode) {
        transientState.update {
            it.copy(pendingImportCount = count.coerceAtLeast(0), pendingImportMode = mode)
        }
    }

    fun addMovieImages(
        movieId: String,
        importedMedia: List<ImportedVaultMedia>,
        importMode: ImportMode,
        originalRemoved: Boolean
    ) {
        stageImageSelection(importedMedia.size, importMode)
        viewModelScope.launch {
            importedMedia.forEach { media ->
                repository.addMovieImage(
                    movieId = movieId,
                    privatePath = media.privatePath,
                    originalUri = media.originalUri,
                    mimeType = media.mimeType,
                    importMode = importMode,
                    originalRemoved = originalRemoved
                )
            }
        }
    }

    fun deleteMovieImage(imageId: String) {
        viewModelScope.launch { repository.deleteMovieImage(imageId) }
    }

    fun deleteMovie(movieId: String) {
        transientState.update {
            it.copy(selectedMovieId = null, selectedTab = VaultTab.LibraryManage)
        }
        viewModelScope.launch { repository.deleteMovie(movieId) }
    }

    // ── Links ──

    fun addLink(movieId: String, url: String, type: LinkType) {
        viewModelScope.launch { repository.addLink(movieId = movieId, url = url, type = type) }
    }

    fun deleteLink(linkId: String) {
        viewModelScope.launch { repository.deleteLink(linkId) }
    }

    // ── Tags ──

    fun createAndAssignTag(movieId: String, name: String) {
        viewModelScope.launch {
            val tagId = repository.createTag(name)
            repository.assignTag(movieId = movieId, tagId = tagId)
        }
    }

    fun assignTag(movieId: String, tagId: String) {
        viewModelScope.launch { repository.assignTag(movieId = movieId, tagId = tagId) }
    }

    fun removeTag(movieId: String, tagId: String) {
        viewModelScope.launch { repository.removeTag(movieId = movieId, tagId = tagId) }
    }

    // ── Seed ──

    fun seedIfEmpty() {
        viewModelScope.launch { repository.seedSampleDataIfEmpty() }
    }
}

data class ImportedVaultMedia(
    val privatePath: String,
    val originalUri: String?,
    val mimeType: String?
)

private data class VaultTransientState(
    val isLocked: Boolean = false,
    val selectedLibraryId: String? = null,
    val selectedMovieId: String? = null,
    val selectedTab: VaultTab = VaultTab.LibraryManage,
    val pendingImportCount: Int = 0,
    val pendingImportMode: ImportMode? = null
)

class VaultViewModelFactory(
    private val repository: VaultRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            return VaultViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
