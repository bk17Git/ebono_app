package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    // === Books ===
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book?

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookByIdFlow(id: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE isDownloaded = 1")
    fun getDownloadedBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    fun getFavoriteBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isAddedToCart = 1")
    fun getCartBooks(): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Query("UPDATE books SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE books SET isAddedToCart = :isAdded, purchaseType = :purchaseType WHERE id = :id")
    suspend fun updateCartStatus(id: String, isAdded: Boolean, purchaseType: String)

    @Query("UPDATE books SET currentPage = :page WHERE id = :id")
    suspend fun updatePageProgress(id: String, page: Int)

    @Query("UPDATE books SET progressSeconds = :seconds WHERE id = :id")
    suspend fun updateAudioProgress(id: String, seconds: Int)

    // === Forums ===
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllForumPosts(): Flow<List<ForumPost>>

    @Query("SELECT * FROM forum_posts WHERE tag = :tag ORDER BY timestamp DESC")
    fun getForumPostsByTag(tag: String): Flow<List<ForumPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ForumPost): Long

    @Update
    suspend fun updatePost(post: ForumPost)

    @Query("DELETE FROM forum_posts WHERE id = :id")
    suspend fun deletePost(id: Long)

    @Query("SELECT * FROM forum_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<ForumComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ForumComment): Long

    // === Reading Sessions ===
    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC")
    fun getAllReadingSessions(): Flow<List<ReadingSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingSession(session: ReadingSession)

    // === User Settings ===
    @Query("SELECT * FROM user_settings WHERE id = 'current_user'")
    fun getUserSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 'current_user'")
    suspend fun getUserSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettings)
}
