package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookRepository(private val bookDao: BookDao) {

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val downloadedBooks: Flow<List<Book>> = bookDao.getDownloadedBooks()
    val favoriteBooks: Flow<List<Book>> = bookDao.getFavoriteBooks()
    val cartBooks: Flow<List<Book>> = bookDao.getCartBooks()
    val allForumPosts: Flow<List<ForumPost>> = bookDao.getAllForumPosts()
    val userSettings: Flow<UserSettings?> = bookDao.getUserSettingsFlow()
    val readingSessions: Flow<List<ReadingSession>> = bookDao.getAllReadingSessions()

    fun getCommentsForPost(postId: Long): Flow<List<ForumComment>> = bookDao.getCommentsForPost(postId)

    fun getBookByIdFlow(id: String): Flow<Book?> = bookDao.getBookByIdFlow(id)

    suspend fun toggleFavorite(bookId: String) {
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            bookDao.updateFavoriteStatus(bookId, !book.isFavorite)
        }
    }

    suspend fun toggleDownload(bookId: String) {
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            bookDao.updateDownloadStatus(bookId, !book.isDownloaded)
        }
    }

    suspend fun toggleCart(bookId: String, purchaseType: String = "Paperback") {
        val book = bookDao.getBookById(bookId)
        if (book != null) {
            bookDao.updateCartStatus(bookId, !book.isAddedToCart, purchaseType)
        }
    }

    suspend fun checkoutCart() {
        val itemsInCart = bookDao.getCartBooks().firstOrNull() ?: emptyList()
        itemsInCart.forEach { book ->
            // Mark as bought and remove from cart
            bookDao.updateBook(book.copy(isAddedToCart = false, isDownloaded = true))
        }
    }

    suspend fun subscribeUser() {
        val current = bookDao.getUserSettings() ?: UserSettings()
        bookDao.insertUserSettings(current.copy(isSubscribed = true))
    }

    suspend fun updatePageProgress(bookId: String, page: Int) {
        bookDao.updatePageProgress(bookId, page)
    }

    suspend fun updateAudioProgress(bookId: String, seconds: Int) {
        bookDao.updateAudioProgress(bookId, seconds)
    }

    suspend fun addReadingSession(bookId: String, minutes: Int) {
        if (minutes <= 0) return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        bookDao.insertReadingSession(
            ReadingSession(bookId = bookId, date = today, minutesRead = minutes)
        )

        // Calculate and update Reading Streak
        val currentSettings = bookDao.getUserSettings() ?: UserSettings()
        val lastRead = currentSettings.lastReadDate
        var streak = currentSettings.streakCount
        var longest = currentSettings.longestStreak

        if (lastRead != today) {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val lastDate = format.parse(lastRead)
                val todayDate = format.parse(today)
                if (lastDate != null && todayDate != null) {
                    val diff = todayDate.time - lastDate.time
                    val diffDays = diff / (24 * 60 * 60 * 1000)
                    if (diffDays == 1L) {
                        streak += 1
                    } else if (diffDays > 1L) {
                        streak = 1
                    }
                } else {
                    streak = 1
                }
            } catch (e: Exception) {
                streak = 1
            }

            if (streak > longest) {
                longest = streak
            }

            bookDao.insertUserSettings(
                currentSettings.copy(
                    streakCount = streak,
                    longestStreak = longest,
                    lastReadDate = today,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun insertForumPost(title: String, content: String, tag: String, bookId: String? = null) {
        val post = ForumPost(
            title = title,
            content = content,
            tag = tag,
            authorName = "Me (You)",
            authorRole = "Verified Reader",
            authorAvatarRes = "avatar_me",
            bookRefId = bookId
        )
        bookDao.insertPost(post)
    }

    suspend fun likePost(postId: Long) {
        val posts = bookDao.getAllForumPosts().firstOrNull() ?: emptyList()
        val post = posts.find { it.id == postId }
        if (post != null) {
            val updated = post.copy(
                isLikedByMe = !post.isLikedByMe,
                likesCount = if (post.isLikedByMe) post.likesCount - 1 else post.likesCount + 1
            )
            bookDao.updatePost(updated)
        }
    }

    suspend fun insertForumComment(postId: Long, content: String) {
        val comment = ForumComment(
            postId = postId,
            authorName = "Me (You)",
            authorAvatarRes = "avatar_me",
            content = content
        )
        bookDao.insertComment(comment)

        // Increment commentsCount in post
        val posts = bookDao.getAllForumPosts().firstOrNull() ?: emptyList()
        val post = posts.find { it.id == postId }
        if (post != null) {
            bookDao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
        }
    }

    suspend fun importEPUBFile(title: String, author: String, description: String) {
        val randomId = "epub_${System.currentTimeMillis()}"
        val book = Book(
            id = randomId,
            title = title,
            author = author,
            description = description,
            category = "Imported EPUB",
            coverUrl = "epub_imported",
            rating = 5.0f,
            isAudiobook = false,
            currentPage = 0,
            totalPages = 350,
            isDownloaded = true,
            isEPUB = true,
            localFilePath = "/storage/emulated/0/Download/$title.epub"
        )
        bookDao.insertBook(book)

        // Create a forum post about this imported book!
        val post = ForumPost(
            title = "Imported a new EPUB: $title",
            content = "Just added '$title' by $author to my local digital library via EPUB upload. Starting to read it now! Anyone else read this?",
            authorName = "Me (You)",
            authorRole = "Verified Reader",
            authorAvatarRes = "avatar_me",
            tag = "Discussion",
            bookRefId = randomId
        )
        bookDao.insertPost(post)
    }

    suspend fun toggleCloudBackup() {
        val current = bookDao.getUserSettings() ?: UserSettings()
        bookDao.insertUserSettings(current.copy(isCloudBackupEnabled = !current.isCloudBackupEnabled))
    }

    suspend fun triggerSyncProgress() {
        val current = bookDao.getUserSettings() ?: UserSettings()
        val books = bookDao.getAllBooks().firstOrNull() ?: emptyList()
        
        var isSynced = current.isFirebaseSynced
        if (current.isLoggedIn && current.isCloudBackupEnabled) {
            val syncResult = FirebaseManager.syncWithCloud(current.userId, current, books)
            if (syncResult is SyncResult.Success) {
                isSynced = !syncResult.isSimulated
            }
        }
        
        bookDao.insertUserSettings(
            current.copy(
                syncTimestamp = System.currentTimeMillis(),
                isFirebaseSynced = isSynced
            )
        )
    }

    suspend fun signUpUser(email: String, username: String): AuthResult {
        val result = FirebaseManager.signUp(email, username)
        if (result is AuthResult.Success) {
            val current = bookDao.getUserSettings() ?: UserSettings()
            bookDao.insertUserSettings(
                current.copy(
                    isLoggedIn = true,
                    username = result.username,
                    email = result.email,
                    userId = result.userId,
                    isFirebaseSynced = !result.isSimulated,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    suspend fun loginUser(email: String): AuthResult {
        val result = FirebaseManager.login(email)
        if (result is AuthResult.Success) {
            val current = bookDao.getUserSettings() ?: UserSettings()
            bookDao.insertUserSettings(
                current.copy(
                    isLoggedIn = true,
                    username = result.username,
                    email = result.email,
                    userId = result.userId,
                    isFirebaseSynced = !result.isSimulated,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    suspend fun signInWithGoogleUser(email: String, username: String): AuthResult {
        val result = FirebaseManager.signInWithGoogle(email, username)
        if (result is AuthResult.Success) {
            val current = bookDao.getUserSettings() ?: UserSettings()
            bookDao.insertUserSettings(
                current.copy(
                    isLoggedIn = true,
                    username = result.username,
                    email = result.email,
                    userId = result.userId,
                    isFirebaseSynced = !result.isSimulated,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }
        return result
    }

    suspend fun logoutUser() {
        val current = bookDao.getUserSettings() ?: UserSettings()
        bookDao.insertUserSettings(
            current.copy(
                isLoggedIn = false,
                username = "Guest Reader",
                email = "",
                userId = "",
                isFirebaseSynced = false,
                syncTimestamp = System.currentTimeMillis()
            )
        )
    }
}
