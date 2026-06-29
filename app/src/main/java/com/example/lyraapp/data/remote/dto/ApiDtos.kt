package com.example.lyraapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val data: T,
)

@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBody? = null,
)

@Serializable
data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class OtpRequestBody(
    val phone: String,
)

@Serializable
data class OtpRequestDataDto(
    val sent: Boolean = false,
    @SerialName("firstTime") val firstTime: Boolean = false,
)

@Serializable
data class OtpVerifyBody(
    val phone: String,
    val code: String,
)

@Serializable
data class AuthTokensDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int = 0,
)

@Serializable
data class UserDto(
    val id: String,
    val phone: String,
    val displayName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val createdAt: String? = null,
    val profileCompleted: Boolean = false,
    val membership: MembershipDto? = null,
)

@Serializable
data class MembershipDto(
    val planId: String? = null,
    val type: String? = null,
    val status: String? = null,
    val autoRenew: Boolean = false,
    val startedAt: String? = null,
    val expiresAt: String? = null,
)

@Serializable
data class MembershipPlanDto(
    val id: String,
    val type: String,
    val name: String,
    val description: String? = null,
    val priceKurus: Int = 0,
    val price: Int = 0,
    val currency: String = "TRY",
    val durationDays: Int = 30,
    val autoRenew: Boolean = false,
)

@Serializable
data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int = 0,
    val user: UserDto? = null,
    @SerialName("firstTime") val firstTime: Boolean = false,
)

@Serializable
data class UpdateProfileBody(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
)

@Serializable
data class RefreshTokenBody(
    val refreshToken: String,
)

@Serializable
data class SongDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationMs: Long = 0L,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val createdAt: String? = null,
)

@Serializable
data class SongsResponseDto(
    val data: List<SongDto> = emptyList(),
    val nextCursor: String? = null,
)

@Serializable
data class PlaylistDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val ownerId: String? = null,
)

@Serializable
data class PlaylistWithSongsDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val ownerId: String? = null,
    val songs: List<SongDto> = emptyList(),
)

@Serializable
data class StreamUrlDto(
    val url: String,
    val expiresAt: String? = null,
    val mimeType: String? = null,
)

@Serializable
data class PlaybackNextBody(
    val songId: String,
)

@Serializable
data class AdDto(
    val id: String,
    val title: String,
    val advertiser: String? = null,
    val durationMs: Long = 0L,
    val mimeType: String? = null,
)

@Serializable
data class PlaybackNextDataDto(
    val type: String,
    val song: SongDto? = null,
    val stream: StreamUrlDto? = null,
    val ad: AdDto? = null,
    val adStream: StreamUrlDto? = null,
    val impressionId: String? = null,
)

@Serializable
data class AdCompleteBody(
    val impressionId: String,
)

@Serializable
data class AdCompleteDataDto(
    val completed: Boolean = false,
)

@Serializable
data class RecordPlayBody(
    val songId: String,
)

@Serializable
data class RecordPlayDataDto(
    val recorded: Boolean = false,
)

@Serializable
data class CheckoutCardBody(
    val number: String,
    val expMonth: Int,
    val expYear: Int,
    val cvc: String,
    val holderName: String? = null,
)

@Serializable
data class CheckoutRequestBody(
    val plan: String,
    val card: CheckoutCardBody,
)

@Serializable
data class CheckoutPaymentDto(
    val transactionId: String? = null,
    val amountKurus: Int = 0,
    val currency: String = "TRY",
)

@Serializable
data class CheckoutDataDto(
    val payment: CheckoutPaymentDto? = null,
    val membership: MembershipDto? = null,
)

@Serializable
data class DeletePlaylistDataDto(
    val deleted: Boolean = false,
)

@Serializable
data class LogoutDataDto(
    val revoked: Boolean = false,
)

@Serializable
data class AddPlaylistTrackBody(
    val songId: String,
)

@Serializable
data class PlaylistTrackAddedDto(
    val added: Boolean = false,
)

@Serializable
data class PlaylistTrackRemovedDto(
    val removed: Boolean = false,
)
