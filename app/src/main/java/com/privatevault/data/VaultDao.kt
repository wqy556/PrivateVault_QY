package com.privatevault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM libraries ORDER BY sort_order ASC")
    fun observeLibraries(): Flow<List<LibraryEntity>>

    @Query("SELECT * FROM movies ORDER BY updated_at DESC")
    fun observeMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movie_images ORDER BY sort_order ASC")
    fun observeImages(): Flow<List<MovieImageEntity>>

    @Query("SELECT * FROM movie_links ORDER BY sort_order ASC")
    fun observeLinks(): Flow<List<MovieLinkEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM movie_tags")
    fun observeMovieTags(): Flow<List<MovieTagCrossRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLibrary(library: LibraryEntity)

    @Query("UPDATE libraries SET name = :name, updated_at = :updatedAt WHERE id = :libraryId")
    suspend fun updateLibraryName(libraryId: String, name: String, updatedAt: Long)

    @Query("DELETE FROM libraries WHERE id = :libraryId")
    suspend fun deleteLibrary(libraryId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieImage(image: MovieImageEntity)

    @Query("DELETE FROM movie_images WHERE id = :imageId")
    suspend fun deleteMovieImage(imageId: String)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovie(movieId: String)

    // ── Movie update operations ──

    @Query("UPDATE movies SET title = :title, updated_at = :updatedAt WHERE id = :movieId")
    suspend fun updateMovieTitle(movieId: String, title: String, updatedAt: Long)

    @Query("UPDATE movies SET notes = :notes, updated_at = :updatedAt WHERE id = :movieId")
    suspend fun updateMovieNotes(movieId: String, notes: String, updatedAt: Long)

    @Query("UPDATE movies SET is_favorite = :isFavorite, updated_at = :updatedAt WHERE id = :movieId")
    suspend fun updateMovieFavorite(movieId: String, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE movies SET last_opened_at = :lastOpenedAt WHERE id = :movieId")
    suspend fun updateMovieLastOpenedAt(movieId: String, lastOpenedAt: Long)

    // ── Link CRUD ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: MovieLinkEntity)

    @Query("DELETE FROM movie_links WHERE id = :linkId")
    suspend fun deleteLink(linkId: String)

    // ── Tag CRUD ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovieTagCrossRef(crossRef: MovieTagCrossRef)

    @Query("DELETE FROM movie_tags WHERE movie_id = :movieId AND tag_id = :tagId")
    suspend fun deleteMovieTagCrossRef(movieId: String, tagId: String)

    // ── Count queries for ID generation ──

    @Query("SELECT COUNT(*) FROM libraries")
    suspend fun libraryCount(): Int

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun movieCount(): Int
}
