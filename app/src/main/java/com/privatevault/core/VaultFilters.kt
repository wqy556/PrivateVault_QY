package com.privatevault.core

fun filterLibraries(
    libraries: List<VaultLibrary>,
    query: String
): List<VaultLibrary> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return libraries
    return libraries.filter { library ->
        library.name.contains(normalizedQuery, ignoreCase = true)
    }
}

fun filterMovies(
    movies: List<VaultMovie>,
    query: String,
    selectedTagIds: Set<String>
): List<VaultMovie> {
    val normalizedQuery = query.trim()
    return movies.filter { movie ->
        val matchesQuery = normalizedQuery.isEmpty() ||
            movie.title.contains(normalizedQuery, ignoreCase = true)
        val matchesTags = selectedTagIds.isEmpty() ||
            movie.tagIds.any { tagId -> tagId in selectedTagIds }
        matchesQuery && matchesTags
    }
}
