package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookViewModel(
    application: Application,
    private val repository: BookRepository
) : AndroidViewModel(application) {

    // === Core Database State ===
    val allBooks = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloadedBooks = repository.downloadedBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteBooks = repository.favoriteBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val cartBooks = repository.cartBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val forumPosts = repository.allForumPosts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userSettings = repository.userSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    val readingSessions = repository.readingSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // === E-Reader State ===
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    private val _readerFontSize = MutableStateFlow(18f) // in sp
    val readerFontSize: StateFlow<Float> = _readerFontSize.asStateFlow()

    private val _readerPageColor = MutableStateFlow("Paper") // "Paper", "Sepia", "Dark", "Midnight"
    val readerPageColor: StateFlow<String> = _readerPageColor.asStateFlow()

    fun selectBook(book: Book?) {
        _selectedBook.value = book
    }

    fun adjustReaderFontSize(delta: Float) {
        _readerFontSize.update { (it + delta).coerceIn(12f, 32f) }
    }

    fun setReaderPageColor(color: String) {
        _readerPageColor.value = color
    }

    fun updateReadingProgress(bookId: String, page: Int) {
        viewModelScope.launch {
            repository.updatePageProgress(bookId, page)
            // Update selected book reference
            _selectedBook.update { book ->
                if (book?.id == bookId) book.copy(currentPage = page) else book
            }
        }
    }

    // === Audiobook Player State ===
    private val _activeAudiobook = MutableStateFlow<Book?>(null)
    val activeAudiobook: StateFlow<Book?> = _activeAudiobook.asStateFlow()

    private val _audioIsPlaying = MutableStateFlow(false)
    val audioIsPlaying: StateFlow<Boolean> = _audioIsPlaying.asStateFlow()

    private val _audioPlaySpeed = MutableStateFlow(1.0f) // 1.0x, 1.25x, 1.5x, 2.0x
    val audioPlaySpeed: StateFlow<Float> = _audioPlaySpeed.asStateFlow()

    private val _audioSleepMinutesRemaining = MutableStateFlow<Int?>(null)
    val audioSleepMinutesRemaining: StateFlow<Int?> = _audioSleepMinutesRemaining.asStateFlow()

    private var audioPlaybackJob: Job? = null
    private var sleepTimerJob: Job? = null

    fun selectAudiobook(book: Book?) {
        _activeAudiobook.value = book
        if (book != null) {
            _audioIsPlaying.value = false
            stopAudioSimulation()
        }
    }

    fun toggleAudioPlay() {
        val currentBook = _activeAudiobook.value ?: return
        val currentlyPlaying = _audioIsPlaying.value
        _audioIsPlaying.value = !currentlyPlaying

        if (!currentlyPlaying) {
            startAudioSimulation(currentBook)
        } else {
            stopAudioSimulation()
        }
    }

    private fun startAudioSimulation(book: Book) {
        audioPlaybackJob?.cancel()
        audioPlaybackJob = viewModelScope.launch {
            var currentSeconds = book.progressSeconds
            while (_audioIsPlaying.value && currentSeconds < book.durationSeconds) {
                delay((1000 / _audioPlaySpeed.value).toLong())
                currentSeconds += 1
                repository.updateAudioProgress(book.id, currentSeconds)
                _activeAudiobook.update { b ->
                    if (b?.id == book.id) b.copy(progressSeconds = currentSeconds) else b
                }
            }
            if (currentSeconds >= book.durationSeconds) {
                _audioIsPlaying.value = false
                repository.updateAudioProgress(book.id, 0)
                _activeAudiobook.update { b ->
                    if (b?.id == book.id) b.copy(progressSeconds = 0) else b
                }
            }
        }
    }

    private fun stopAudioSimulation() {
        audioPlaybackJob?.cancel()
    }

    fun seekAudio(seconds: Int) {
        val book = _activeAudiobook.value ?: return
        viewModelScope.launch {
            val newProgress = (book.progressSeconds + seconds).coerceIn(0, book.durationSeconds)
            repository.updateAudioProgress(book.id, newProgress)
            _activeAudiobook.update { b ->
                if (b?.id == book.id) b.copy(progressSeconds = newProgress) else b
            }
            // If playing, restart the ticker with updated progress
            if (_audioIsPlaying.value) {
                startAudioSimulation(book.copy(progressSeconds = newProgress))
            }
        }
    }

    fun setAudioPlaySpeed(speed: Float) {
        _audioPlaySpeed.value = speed
        val playingBook = _activeAudiobook.value
        if (_audioIsPlaying.value && playingBook != null) {
            startAudioSimulation(playingBook)
        }
    }

    fun startSleepTimer(minutes: Int) {
        _audioSleepMinutesRemaining.value = minutes
        sleepTimerJob?.cancel()
        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60000) // 1 minute
                remaining -= 1
                _audioSleepMinutesRemaining.value = remaining
            }
            // Timer finished, pause playback
            _audioIsPlaying.value = false
            stopAudioSimulation()
            _audioSleepMinutesRemaining.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _audioSleepMinutesRemaining.value = null
    }

    // === Focus Mode Timer State ===
    private val _focusSecondsRemaining = MutableStateFlow(0)
    val focusSecondsRemaining: StateFlow<Int> = _focusSecondsRemaining.asStateFlow()

    private val _focusTimerRunning = MutableStateFlow(false)
    val focusTimerRunning: StateFlow<Boolean> = _focusTimerRunning.asStateFlow()

    private val _focusSound = MutableStateFlow("None") // "None", "Rain", "Forest", "White Noise"
    val focusSound: StateFlow<String> = _focusSound.asStateFlow()

    private var focusTimerJob: Job? = null
    private var focusInitialMinutes = 0
    private var focusTargetBookId: String? = null

    fun startFocusSession(minutes: Int, bookId: String?) {
        focusInitialMinutes = minutes
        focusTargetBookId = bookId
        _focusSecondsRemaining.value = minutes * 60
        _focusTimerRunning.value = true

        focusTimerJob?.cancel()
        focusTimerJob = viewModelScope.launch {
            while (_focusSecondsRemaining.value > 0) {
                delay(1000)
                _focusSecondsRemaining.update { it - 1 }
            }
            // Focus timer complete! Record reading session
            _focusTimerRunning.value = false
            recordFocusSessionComplete()
        }
    }

    fun stopFocusSession() {
        focusTimerJob?.cancel()
        _focusTimerRunning.value = false
        _focusSecondsRemaining.value = 0
    }

    fun toggleFocusPause() {
        val currentlyRunning = _focusTimerRunning.value
        _focusTimerRunning.value = !currentlyRunning
        if (!currentlyRunning) {
            focusTimerJob = viewModelScope.launch {
                while (_focusSecondsRemaining.value > 0 && _focusTimerRunning.value) {
                    delay(1000)
                    _focusSecondsRemaining.update { it - 1 }
                }
                if (_focusSecondsRemaining.value == 0) {
                    _focusTimerRunning.value = false
                    recordFocusSessionComplete()
                }
            }
        } else {
            focusTimerJob?.cancel()
        }
    }

    fun setFocusSound(sound: String) {
        _focusSound.value = sound
    }

    private fun recordFocusSessionComplete() {
        viewModelScope.launch {
            val bookId = focusTargetBookId ?: "focused_session"
            repository.addReadingSession(bookId, focusInitialMinutes)
            // Post a milestone forum post!
            repository.insertForumPost(
                title = "Accomplished Focus Session: $focusInitialMinutes mins!",
                content = "I just completed a quiet, distraction-free reading focus session of $focusInitialMinutes minutes. Feeling incredibly aligned and close to my reading goals. Ebono's offline focus space works wonders!",
                tag = "Milestone",
                bookId = focusTargetBookId
            )
        }
    }

    // === Forum Post details and comments ===
    private val _selectedPost = MutableStateFlow<ForumPost?>(null)
    val selectedPost: StateFlow<ForumPost?> = _selectedPost.asStateFlow()

    val selectedPostComments = _selectedPost.flatMapLatest { post ->
        if (post != null) {
            repository.getCommentsForPost(post.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectPost(post: ForumPost?) {
        _selectedPost.value = post
    }

    fun addForumPost(title: String, content: String, tag: String, bookId: String? = null) {
        viewModelScope.launch {
            repository.insertForumPost(title, content, tag, bookId)
        }
    }

    fun likeForumPost(postId: Long) {
        viewModelScope.launch {
            repository.likePost(postId)
            // Update selected post state to reflect visual likes
            _selectedPost.update { post ->
                if (post?.id == postId) {
                    post.copy(
                        isLikedByMe = !post.isLikedByMe,
                        likesCount = if (post.isLikedByMe) post.likesCount - 1 else post.likesCount + 1
                    )
                } else post
            }
        }
    }

    fun addForumComment(postId: Long, content: String) {
        viewModelScope.launch {
            repository.insertForumComment(postId, content)
            _selectedPost.update { post ->
                if (post?.id == postId) post.copy(commentsCount = post.commentsCount + 1) else post
            }
        }
    }

    // === Core Actions (Favorites, Downloads, Cart, Subscription, Import) ===
    fun toggleBookFavorite(bookId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(bookId)
            _selectedBook.update { book ->
                if (book?.id == bookId) book.copy(isFavorite = !book.isFavorite) else book
            }
        }
    }

    fun toggleBookDownload(bookId: String) {
        viewModelScope.launch {
            repository.toggleDownload(bookId)
            _selectedBook.update { book ->
                if (book?.id == bookId) book.copy(isDownloaded = !book.isDownloaded) else book
            }
        }
    }

    fun toggleBookCart(bookId: String, purchaseType: String = "Paperback") {
        viewModelScope.launch {
            repository.toggleCart(bookId, purchaseType)
            _selectedBook.update { book ->
                if (book?.id == bookId) book.copy(isAddedToCart = !book.isAddedToCart, purchaseType = purchaseType) else book
            }
        }
    }

    fun checkoutCart() {
        viewModelScope.launch {
            repository.checkoutCart()
        }
    }

    fun subscribeUser() {
        viewModelScope.launch {
            repository.subscribeUser()
        }
    }

    fun importLocalEPUB(title: String, author: String, description: String) {
        viewModelScope.launch {
            repository.importEPUBFile(title, author, description)
        }
    }

    fun toggleCloudBackup() {
        viewModelScope.launch {
            repository.toggleCloudBackup()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.triggerSyncProgress()
        }
    }

    fun signUp(email: String, username: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.signUpUser(email, username)
            if (res is AuthResult.Success) {
                onResult(null) // Success
            } else if (res is AuthResult.Error) {
                onResult(res.message) // Error message
            }
        }
    }

    fun login(email: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.loginUser(email)
            if (res is AuthResult.Success) {
                onResult(null) // Success
            } else if (res is AuthResult.Error) {
                onResult(res.message) // Error message
            }
        }
    }

    fun signInWithGoogle(email: String, username: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.signInWithGoogleUser(email, username)
            if (res is AuthResult.Success) {
                onResult(null) // Success
            } else if (res is AuthResult.Error) {
                onResult(res.message) // Error message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
        }
    }

    // === Gemini Recommendation State ===
    private val _aiRecommendation = MutableStateFlow<String>("")
    val aiRecommendation: StateFlow<String> = _aiRecommendation.asStateFlow()

    private val _isGeneratingRecommendation = MutableStateFlow(false)
    val isGeneratingRecommendation: StateFlow<Boolean> = _isGeneratingRecommendation.asStateFlow()

    fun generateAiRecommendation(customPrompt: String? = null) {
        viewModelScope.launch {
            _isGeneratingRecommendation.value = true
            _aiRecommendation.value = ""

            // Gather profile info for prompt customization
            val history = allBooks.value.filter { it.currentPage > 0 }.map { "${it.title} by ${it.author}" }
            val favs = favoriteBooks.value.map { it.category }.distinct()

            val recommendation = GeminiService.getBookRecommendation(
                readingHistory = history,
                favoriteCategories = if (favs.isEmpty()) listOf("Fiction", "Philosophy") else favs,
                userPrompt = customPrompt
            )

            _aiRecommendation.value = recommendation
            _isGeneratingRecommendation.value = false
        }
    }
}

class BookViewModelFactory(
    private val application: Application,
    private val repository: BookRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
