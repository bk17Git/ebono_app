package com.example.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val isFirebaseInitialized: Boolean by lazy {
        try {
            // Check if FirebaseAuth and FirebaseFirestore can be instantiated
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            auth != null && db != null
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase not initialized/available. Fallback mode active.")
            false
        }
    }

    suspend fun signUp(email: String, username: String): AuthResult = suspendCancellableCoroutine { continuation ->
        if (!isFirebaseInitialized) {
            continuation.resume(
                AuthResult.Success(
                    userId = "mock_uid_${System.currentTimeMillis()}",
                    username = username,
                    email = email,
                    isSimulated = true
                )
            )
            return@suspendCancellableCoroutine
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            auth.createUserWithEmailAndPassword(email, "password123")
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: "uid_temp"
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "username" to username,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            if (continuation.isActive) {
                                continuation.resume(
                                    AuthResult.Success(userId = uid, username = username, email = email, isSimulated = false)
                                )
                            }
                        }
                        .addOnFailureListener { e ->
                            if (continuation.isActive) {
                                continuation.resume(AuthResult.Success(userId = uid, username = username, email = email, isSimulated = false))
                            }
                        }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Error(e.localizedMessage ?: "Sign up failed"))
                    }
                }
        } catch (e: Throwable) {
            if (continuation.isActive) {
                continuation.resume(AuthResult.Error(e.localizedMessage ?: "Firebase error"))
            }
        }
    }

    suspend fun login(email: String): AuthResult = suspendCancellableCoroutine { continuation ->
        if (!isFirebaseInitialized) {
            val mockUsername = email.substringBefore("@").replaceFirstChar { it.uppercase() }
            continuation.resume(
                AuthResult.Success(
                    userId = "mock_uid_${System.currentTimeMillis()}",
                    username = mockUsername,
                    email = email,
                    isSimulated = true
                )
            )
            return@suspendCancellableCoroutine
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            auth.signInWithEmailAndPassword(email, "password123")
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: "uid_temp"
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val username = doc.getString("username") ?: email.substringBefore("@")
                            if (continuation.isActive) {
                                continuation.resume(
                                    ComposedSuccess(uid, username, email)
                                )
                            }
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) {
                                continuation.resume(
                                    ComposedSuccess(uid, email.substringBefore("@"), email)
                                )
                            }
                        }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Error(e.localizedMessage ?: "Login failed"))
                    }
                }
        } catch (e: Throwable) {
            if (continuation.isActive) {
                continuation.resume(AuthResult.Error(e.localizedMessage ?: "Firebase error"))
            }
        }
    }

    suspend fun signInWithGoogle(email: String, username: String): AuthResult = suspendCancellableCoroutine { continuation ->
        if (!isFirebaseInitialized) {
            continuation.resume(
                AuthResult.Success(
                    userId = "google_mock_uid_${System.currentTimeMillis()}",
                    username = username,
                    email = email,
                    isSimulated = true
                )
            )
            return@suspendCancellableCoroutine
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid ?: "google_uid_${email.hashCode()}"
            val userMap = hashMapOf(
                "uid" to uid,
                "username" to username,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val existingUsername = doc.getString("username") ?: username
                        if (continuation.isActive) {
                            continuation.resume(AuthResult.Success(userId = uid, username = existingUsername, email = email, isSimulated = false))
                        }
                    } else {
                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                if (continuation.isActive) {
                                    continuation.resume(AuthResult.Success(userId = uid, username = username, email = email, isSimulated = false))
                                }
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) {
                                    continuation.resume(AuthResult.Success(userId = uid, username = username, email = email, isSimulated = false))
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(AuthResult.Success(userId = uid, username = username, email = email, isSimulated = false))
                    }
                }
        } catch (e: Throwable) {
            if (continuation.isActive) {
                continuation.resume(AuthResult.Error(e.localizedMessage ?: "Google Firebase Sign-In error"))
            }
        }
    }

    private fun ComposedSuccess(uid: String, username: String, email: String) = AuthResult.Success(
        userId = uid,
        username = username,
        email = email,
        isSimulated = false
    )

    suspend fun syncWithCloud(
        userId: String,
        settings: UserSettings,
        books: List<Book>
    ): SyncResult = suspendCancellableCoroutine { continuation ->
        if (!isFirebaseInitialized) {
            continuation.resume(SyncResult.Success(isSimulated = true))
            return@suspendCancellableCoroutine
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val settingsMap = hashMapOf(
                "streakCount" to settings.streakCount,
                "longestStreak" to settings.longestStreak,
                "lastReadDate" to settings.lastReadDate,
                "dailyGoalMinutes" to settings.dailyGoalMinutes,
                "syncTimestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(userId).collection("settings").document("current").set(settingsMap)
                .addOnSuccessListener {
                    // Sync active books
                    val activeBooks = books.filter { it.currentPage > 0 || bookProgressIsActive(it) }
                    if (activeBooks.isEmpty()) {
                        if (continuation.isActive) {
                            continuation.resume(SyncResult.Success(isSimulated = false))
                        }
                        return@addOnSuccessListener
                    }

                    var remaining = activeBooks.size
                    var hasFailed = false

                    activeBooks.forEach { book ->
                        val progressMap = hashMapOf(
                            "id" to book.id,
                            "title" to book.title,
                            "currentPage" to book.currentPage,
                            "progressSeconds" to book.progressSeconds,
                            "lastUpdated" to System.currentTimeMillis()
                        )
                        db.collection("users").document(userId).collection("books").document(book.id).set(progressMap)
                            .addOnSuccessListener {
                                remaining--
                                if (remaining == 0 && !hasFailed && continuation.isActive) {
                                    continuation.resume(SyncResult.Success(isSimulated = false))
                                }
                            }
                            .addOnFailureListener { e ->
                                if (!hasFailed) {
                                    hasFailed = true
                                    if (continuation.isActive) {
                                        continuation.resume(SyncResult.Error(e.localizedMessage ?: "Failed syncing books"))
                                    }
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resume(SyncResult.Error(e.localizedMessage ?: "Failed syncing settings"))
                    }
                }
        } catch (e: Throwable) {
            if (continuation.isActive) {
                continuation.resume(SyncResult.Error(e.localizedMessage ?: "Firebase error"))
            }
        }
    }

    private fun bookProgressIsActive(book: Book): Boolean {
        return book.isAudiobook && book.progressSeconds > 0
    }
}

sealed class AuthResult {
    data class Success(val userId: String, val username: String, val email: String, val isSimulated: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class SyncResult {
    data class Success(val isSimulated: Boolean) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
