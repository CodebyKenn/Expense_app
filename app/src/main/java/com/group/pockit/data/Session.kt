package com.group.pockit.data

/**
 * Who is using the app right now. Every account, record and budget is stored with a userId,
 * so each person only ever sees their own data.
 *
 * AUTH TEAMMATE: set these right after a successful login AND when the app starts and
 * Firebase already has a logged-in user:
 *
 *     Session.userId = firebaseUser.uid
 *     Session.displayName = firebaseUser.displayName ?: ""
 *
 * On logout: Session.clear(). Set them BEFORE the Dashboard opens, because the ViewModels
 * read the userId when they are created.
 */
object Session {
    const val OFFLINE_USER = "local-user"

    @Volatile var userId: String = OFFLINE_USER
    @Volatile var displayName: String = ""

    fun clear() {
        userId = OFFLINE_USER
        displayName = ""
    }
}
