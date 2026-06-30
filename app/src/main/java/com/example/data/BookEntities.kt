package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val description: String,
    val category: String, // "Fiction", "Sci-Fi", "Mystery", "Biography", "Self-Help"
    val coverUrl: String, // Resource identifier name or dummy URL
    val rating: Float,
    val isAudiobook: Boolean,
    val durationSeconds: Int = 0, // For audiobooks
    val progressSeconds: Int = 0, // Audio playback progress
    val currentPage: Int = 0,
    val totalPages: Int = 300,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val isPremium: Boolean = false,
    val priceHardCopy: Double = 14.99,
    val isAddedToCart: Boolean = false,
    val purchaseType: String = "Paperback", // "Paperback" or "Hardcover"
    val isEPUB: Boolean = false, // True if imported locally
    val localFilePath: String? = null
)

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val authorName: String,
    val authorRole: String = "Reader", // "Author", "Reader", "Librarian"
    val authorAvatarRes: String = "avatar_1",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val tag: String = "General", // "Discussion", "Recommendations", "Author Spotlight", "Milestone"
    val isLikedByMe: Boolean = false,
    val bookRefId: String? = null // Reference to a book if mentioned
)

@Entity(tableName = "forum_comments")
data class ForumComment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val authorAvatarRes: String = "avatar_2",
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val date: String, // "yyyy-MM-dd"
    val minutesRead: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: String = "current_user",
    val isSubscribed: Boolean = false,
    val isCloudBackupEnabled: Boolean = true,
    val streakCount: Int = 3, // Initial mock streak
    val longestStreak: Int = 12,
    val lastReadDate: String = "2026-06-24", // "yyyy-MM-dd"
    val dailyGoalMinutes: Int = 20,
    val syncTimestamp: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false,
    val username: String = "Guest Reader",
    val email: String = "",
    val userId: String = "",
    val isFirebaseSynced: Boolean = false
)
