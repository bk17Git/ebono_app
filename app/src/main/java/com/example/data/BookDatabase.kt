package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Book::class,
        ForumPost::class,
        ForumComment::class,
        ReadingSession::class,
        UserSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "lumina_books_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(BookDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BookDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.bookDao())
                }
            }
        }

        suspend fun populateDatabase(bookDao: BookDao) {
            // Delete all existing data
            // (Only runs onCreate, but good practice)

            // Prepopulate Books & Audiobooks
            val initialBooks = listOf(
                Book(
                    id = "book_1",
                    title = "The Whispers of Wind",
                    author = "Sophia Sterling",
                    description = "An immersive journey through remote valleys and old coastal towns, exploring human connection and the silent promises we keep with nature. Highly poetic and visually stunning prose.",
                    category = "Fiction",
                    coverUrl = "whispers_wind",
                    rating = 4.8f,
                    isAudiobook = false,
                    currentPage = 12,
                    totalPages = 284,
                    isDownloaded = true,
                    isFavorite = true,
                    isPremium = false,
                    priceHardCopy = 12.99
                ),
                Book(
                    id = "book_2",
                    title = "Echoes of Eternity",
                    author = "Marcus Aurelius",
                    description = "A collection of personal writings and philosophical reflections on life, duty, stoicism, and our place in the universe. A classic masterpiece translated into modern English for everyday wisdom.",
                    category = "Philosophy",
                    coverUrl = "echoes_eternity",
                    rating = 4.9f,
                    isAudiobook = false,
                    currentPage = 0,
                    totalPages = 190,
                    isDownloaded = false,
                    isFavorite = false,
                    isPremium = false,
                    priceHardCopy = 9.99
                ),
                Book(
                    id = "book_3",
                    title = "Cosmic Voyage",
                    author = "Dr. Elena Rostova",
                    description = "A gripping science fiction saga set on a generational colony ship traveling towards Tau Ceti. As systems begin to fail, a small group of scientists uncovers a deep conspiracy that could change human history forever.",
                    category = "Sci-Fi",
                    coverUrl = "cosmic_voyage",
                    rating = 4.7f,
                    isAudiobook = false,
                    currentPage = 145,
                    totalPages = 420,
                    isDownloaded = true,
                    isFavorite = true,
                    isPremium = true,
                    priceHardCopy = 18.50
                ),
                Book(
                    id = "book_4",
                    title = "Shadows of Florence",
                    author = "Giovanni Rossi",
                    description = "A dark historical mystery set in the renaissance alleys of Florence. A brilliant painter is murdered, leaving behind a series of coded symbols in his final canvas that point to the highest ranks of power.",
                    category = "Mystery",
                    coverUrl = "shadows_florence",
                    rating = 4.5f,
                    isAudiobook = false,
                    currentPage = 0,
                    totalPages = 310,
                    isDownloaded = false,
                    isFavorite = false,
                    isPremium = false,
                    priceHardCopy = 14.99
                ),
                // Audiobooks
                Book(
                    id = "audio_1",
                    title = "Beyond the Horizon",
                    author = "Christopher Vance",
                    description = "The thrilling biographical account of the first sub-orbital solo flight across Antarctica. Experience the sheer cold, isolation, and high-stakes split-second decisions read by the author himself.",
                    category = "Biography",
                    coverUrl = "beyond_horizon",
                    rating = 4.6f,
                    isAudiobook = true,
                    durationSeconds = 28800, // 8 hours
                    progressSeconds = 3450, // Around 57 minutes
                    isDownloaded = true,
                    isFavorite = true,
                    isPremium = true,
                    priceHardCopy = 24.99
                ),
                Book(
                    id = "audio_2",
                    title = "The Art of Mindful Living",
                    author = "Zen Master Thich Tue",
                    description = "A serene audiobook introducing daily mindfulness, peaceful breathing, and stress reduction strategies. Includes live meditation guides, bell ambient sounds, and relaxing soundscapes.",
                    category = "Self-Help",
                    coverUrl = "mindful_living",
                    rating = 4.9f,
                    isAudiobook = true,
                    durationSeconds = 14400, // 4 hours
                    progressSeconds = 0,
                    isDownloaded = false,
                    isFavorite = false,
                    isPremium = false,
                    priceHardCopy = 15.99
                ),
                Book(
                    id = "audio_3",
                    title = "Labyrinths of the Mind",
                    author = "Sarah Jenkins",
                    description = "A profound psychological thriller audiobook investigating the boundaries between dreams and reality. A psychologist tries to cure a patient trapped in an infinite dream state.",
                    category = "Mystery",
                    coverUrl = "labyrinths_mind",
                    rating = 4.4f,
                    isAudiobook = true,
                    durationSeconds = 25200, // 7 hours
                    progressSeconds = 1800, // 30 mins
                    isDownloaded = true,
                    isFavorite = false,
                    isPremium = false,
                    priceHardCopy = 19.99
                )
            )
            bookDao.insertBooks(initialBooks)

            // Prepopulate Forum Posts
            val initialPosts = listOf(
                ForumPost(
                    id = 1,
                    title = "My first milestone: 7 Days Reading Streak completed!",
                    content = "I finally reached a solid 7 days reading streak! Focusing for 20 minutes a day has really changed my routine. The distraction-free focus mode with forest ambient noise makes it so easy. Let's aim for 14 days next! Who else is tracking their streak?",
                    authorName = "Sarah Jenkins",
                    authorRole = "Verified Reader",
                    authorAvatarRes = "avatar_1",
                    likesCount = 18,
                    commentsCount = 2,
                    tag = "Milestone",
                    isLikedByMe = true,
                    bookRefId = "book_1"
                ),
                ForumPost(
                    id = 2,
                    title = "Sophia Sterling is hosting a Live Q&A next Friday!",
                    content = "Huge news for all fiction fans! Our favorite author Sophia Sterling is joining the Ebono forum for a live Q&A session next Friday at 6 PM EST. She'll be discussing her writing process for 'The Whispers of Wind' and dropping hints about her upcoming book! Drop your questions in the comments below.",
                    authorName = "Ebono Editorial",
                    authorRole = "Host",
                    authorAvatarRes = "avatar_lumina",
                    likesCount = 42,
                    commentsCount = 3,
                    tag = "Author Spotlight",
                    isLikedByMe = false,
                    bookRefId = "book_1"
                ),
                ForumPost(
                    id = 3,
                    title = "Looking for great Sci-Fi recommendations",
                    content = "I just finished 'Cosmic Voyage' by Dr. Elena Rostova and I'm absolutely hooked on generational ship space operas! Can anyone recommend similar hard sci-fi books that explore long-term space colonization?",
                    authorName = "Leo Vance",
                    authorRole = "Sci-Fi Fanatic",
                    authorAvatarRes = "avatar_3",
                    likesCount = 9,
                    commentsCount = 1,
                    tag = "Recommendations",
                    isLikedByMe = false,
                    bookRefId = "book_3"
                )
            )
            initialPosts.forEach { bookDao.insertPost(it) }

            // Prepopulate Comments
            val initialComments = listOf(
                ForumComment(
                    postId = 1,
                    authorName = "Markus Webb",
                    authorAvatarRes = "avatar_2",
                    content = "Congratulations! Keep going! I'm on day 15 myself and the streak reward milestones definitely help with consistency."
                ),
                ForumComment(
                    postId = 1,
                    authorName = "Sophia Sterling",
                    authorAvatarRes = "avatar_4",
                    content = "This is lovely to hear, Sarah! So glad to hear my book is part of your daily ritual. Keep shining!"
                ),
                ForumComment(
                    postId = 2,
                    authorName = "Aria Blake",
                    authorAvatarRes = "avatar_5",
                    content = "Oh my goodness, I can't wait! I want to ask Sophia about how she develops her nature allegories."
                ),
                ForumComment(
                    postId = 2,
                    authorName = "Leo Vance",
                    authorAvatarRes = "avatar_3",
                    content = "Added to my calendar! Need to know if she's planning a sequel."
                ),
                ForumComment(
                    postId = 2,
                    authorName = "Dave Groel",
                    authorAvatarRes = "avatar_1",
                    content = "Unbelievable! Ebono Forums are getting some high-profile guests. Kudos to the organizers."
                ),
                ForumComment(
                    postId = 3,
                    authorName = "Elena Rostova",
                    authorAvatarRes = "avatar_6",
                    content = "Thanks for reading, Leo! I'd recommend Arthur C. Clarke's 'Rendezvous with Rama' and Kim Stanley Robinson's 'Aurora' for that exact vibe."
                )
            )
            initialComments.forEach { bookDao.insertComment(it) }

            // Prepopulate Sessions
            val initialSessions = listOf(
                ReadingSession(bookId = "book_1", date = "2026-06-25", minutesRead = 22),
                ReadingSession(bookId = "book_1", date = "2026-06-24", minutesRead = 15),
                ReadingSession(bookId = "book_3", date = "2026-06-23", minutesRead = 30)
            )
            initialSessions.forEach { bookDao.insertReadingSession(it) }

            // User Settings
            val initialSettings = UserSettings(
                id = "current_user",
                isSubscribed = false,
                isCloudBackupEnabled = true,
                streakCount = 3,
                longestStreak = 12,
                lastReadDate = "2026-06-25",
                dailyGoalMinutes = 20
            )
            bookDao.insertUserSettings(initialSettings)
        }
    }
}
