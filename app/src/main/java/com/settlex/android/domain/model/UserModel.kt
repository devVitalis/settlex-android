package com.settlex.android.domain.model

/**
 * Represents a user data model specifically for creating user data in Firestore.
 * This model enforces that essential user information is provided upon creation.
 *
 * @property uid The unique identifier for the user. Cannot be null.
 * @property firstName The user's first name. Cannot be null.
 * @property lastName The user's last name. Cannot be null.
 * @property email The user's email address. Cannot be null.
 * @property phone The user's phone number. Cannot be null.
 * @property fcmToken The Firebase Cloud Messaging token for push notifications.
 * @property referralCode The user's referral code (optional).
 */
data class UserModel(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val fcmToken: String? = null,
    var referralCode: String? = null,
)
