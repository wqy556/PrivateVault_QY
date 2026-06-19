package com.privatevault.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType

@Entity(tableName = "libraries")
data class LibraryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

@Entity(
    tableName = "movies",
    foreignKeys = [
        ForeignKey(
            entity = LibraryEntity::class,
            parentColumns = ["id"],
            childColumns = ["library_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("library_id")]
)
data class MovieEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "library_id")
    val libraryId: String,
    val title: String,
    val notes: String,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
    @ColumnInfo(name = "last_opened_at")
    val lastOpenedAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

@Entity(
    tableName = "movie_images",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("movie_id")]
)
data class MovieImageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "movie_id")
    val movieId: String,
    @ColumnInfo(name = "private_path")
    val privatePath: String,
    @ColumnInfo(name = "original_uri")
    val originalUri: String?,
    @ColumnInfo(name = "import_mode")
    val importMode: ImportMode,
    @ColumnInfo(name = "original_removed")
    val originalRemoved: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

@Entity(
    tableName = "movie_links",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("movie_id")]
)
data class MovieLinkEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "movie_id")
    val movieId: String,
    val url: String,
    val type: LinkType,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

@Entity(
    tableName = "movie_tags",
    primaryKeys = ["movie_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["id"],
            childColumns = ["movie_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tag_id")]
)
data class MovieTagCrossRef(
    @ColumnInfo(name = "movie_id")
    val movieId: String,
    @ColumnInfo(name = "tag_id")
    val tagId: String
)

data class VaultSnapshot(
    val libraries: List<LibraryEntity>,
    val movies: List<MovieEntity>,
    val images: List<MovieImageEntity>,
    val links: List<MovieLinkEntity>,
    val tags: List<TagEntity>,
    val movieTags: List<MovieTagCrossRef>
) {
    companion object {
        fun empty(): VaultSnapshot {
            return VaultSnapshot(
                libraries = emptyList(),
                movies = emptyList(),
                images = emptyList(),
                links = emptyList(),
                tags = emptyList(),
                movieTags = emptyList()
            )
        }
    }
}
