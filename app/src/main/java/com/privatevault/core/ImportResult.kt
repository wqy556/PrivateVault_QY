package com.privatevault.core

data class ImportResult(
    val mode: ImportMode,
    val isInVault: Boolean,
    val originalVisibleOutsideVault: Boolean
) {
    val isMoveComplete: Boolean
        get() = mode == ImportMode.MoveAndHideOriginal &&
            isInVault &&
            !originalVisibleOutsideVault

    companion object {
        fun afterCopy(
            mode: ImportMode,
            originalRemovalSucceeded: Boolean
        ): ImportResult {
            return ImportResult(
                mode = mode,
                isInVault = true,
                originalVisibleOutsideVault = when (mode) {
                    ImportMode.CopyOnly -> true
                    ImportMode.MoveAndHideOriginal -> !originalRemovalSucceeded
                }
            )
        }
    }
}
