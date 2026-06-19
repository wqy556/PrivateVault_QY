package com.privatevault.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultAppStateTest {
    @Test
    fun freshStateStartsLockedWithLibrariesAndMovie() {
        val state = VaultAppState.initial()

        assertTrue(state.isLocked)
        assertEquals(2, state.libraries.size)
        assertEquals("我的片库", state.libraries.first().name)
        assertEquals("示例影片", state.movies.single().title)
        assertNotNull(state.movies.single().coverImage)
        assertEquals(VaultTab.LibraryManage, state.selectedTab)
    }

    @Test
    fun nonBlankPasscodeUnlocksVault() {
        val state = VaultAppState.initial().unlock("123456")

        assertFalse(state.isLocked)
    }

    @Test
    fun blankPasscodeKeepsVaultLocked() {
        val state = VaultAppState.initial().unlock("   ")

        assertTrue(state.isLocked)
    }

    @Test
    fun returningFromBackgroundRequiresUnlockAgain() {
        val state = VaultAppState.initial()
            .unlock("123456")
            .requireUnlock()

        assertTrue(state.isLocked)
    }

    @Test
    fun selectingLibraryShowsItsMovies() {
        val state = VaultAppState.initial()
            .selectLibrary("library-main")

        assertEquals(VaultTab.LibraryDetail, state.selectedTab)
        assertEquals("示例影片", state.moviesInSelectedLibrary.single().title)
    }

    @Test
    fun libraryManageTabDoesNotEnterLibraryDetail() {
        val state = VaultAppState.initial()
            .selectLibrary("library-main")
            .selectTab(VaultTab.LibraryManage)

        assertEquals(VaultTab.LibraryManage, state.selectedTab)
        assertEquals(null, state.selectedLibraryId)
    }

    @Test
    fun favoritesTabShowsFavoriteMovies() {
        val state = VaultAppState.initial()
            .selectTab(VaultTab.Favorites)

        assertEquals(VaultTab.Favorites, state.selectedTab)
        assertEquals("示例影片", state.favoriteMovies.single().title)
    }

    @Test
    fun canAddRenameAndDeleteLibraries() {
        val added = VaultAppState.initial().addLibrary("自定义片库")
        val newLibrary = added.libraries.last()

        assertEquals("自定义片库", newLibrary.name)

        val renamed = added.renameLibrary(newLibrary.id, "重命名片库")
        assertEquals("重命名片库", renamed.libraries.last().name)

        val deleted = renamed.deleteLibrary(newLibrary.id)
        assertEquals(2, deleted.libraries.size)
        assertEquals(VaultTab.LibraryManage, deleted.selectedTab)
    }

    @Test
    fun blankLibraryNameIsIgnored() {
        val state = VaultAppState.initial().addLibrary("   ")

        assertEquals(2, state.libraries.size)
    }

    @Test
    fun openingMovieShowsDetailTab() {
        val state = VaultAppState.initial()
            .openMovie("movie-sample")

        assertEquals(VaultTab.MovieDetail, state.selectedTab)
        assertEquals("示例影片", state.selectedMovie?.title)
    }

    @Test
    fun imageSelectionStoresUserChosenMode() {
        val state = VaultAppState.initial()
            .stageImageSelection(3, ImportMode.MoveAndHideOriginal)

        assertEquals(3, state.pendingImportCount)
        assertEquals(ImportMode.MoveAndHideOriginal, state.pendingImportMode)
    }
}
