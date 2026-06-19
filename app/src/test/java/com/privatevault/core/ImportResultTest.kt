package com.privatevault.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportResultTest {
    @Test
    fun copyOnlyKeepsOriginalVisibleOutsideVault() {
        val result = ImportResult.afterCopy(
            mode = ImportMode.CopyOnly,
            originalRemovalSucceeded = false
        )

        assertTrue(result.isInVault)
        assertTrue(result.originalVisibleOutsideVault)
        assertFalse(result.isMoveComplete)
    }

    @Test
    fun moveIsCompleteOnlyWhenOriginalRemovalSucceeds() {
        val result = ImportResult.afterCopy(
            mode = ImportMode.MoveAndHideOriginal,
            originalRemovalSucceeded = true
        )

        assertTrue(result.isInVault)
        assertFalse(result.originalVisibleOutsideVault)
        assertTrue(result.isMoveComplete)
    }

    @Test
    fun failedMoveRemovalMeansImportedButStillVisible() {
        val result = ImportResult.afterCopy(
            mode = ImportMode.MoveAndHideOriginal,
            originalRemovalSucceeded = false
        )

        assertTrue(result.isInVault)
        assertTrue(result.originalVisibleOutsideVault)
        assertFalse(result.isMoveComplete)
    }
}
