package com.example.lyraapp.data.remote

import com.example.lyraapp.data.remote.dto.AddPlaylistTrackBody
import com.example.lyraapp.data.remote.dto.ApiEnvelope
import com.example.lyraapp.data.remote.dto.AuthSessionDto
import com.example.lyraapp.data.remote.dto.CreatePlaylistRequest
import com.example.lyraapp.data.remote.dto.LogoutDataDto
import com.example.lyraapp.data.remote.dto.OtpRequestBody
import com.example.lyraapp.data.remote.dto.OtpRequestDataDto
import com.example.lyraapp.data.remote.dto.OtpVerifyBody
import com.example.lyraapp.data.remote.dto.PlaylistDto
import com.example.lyraapp.data.remote.dto.PlaylistTrackAddedDto
import com.example.lyraapp.data.remote.dto.PlaylistTrackRemovedDto
import com.example.lyraapp.data.remote.dto.PlaylistWithSongsDto
import com.example.lyraapp.data.remote.dto.RecordPlayBody
import com.example.lyraapp.data.remote.dto.RecordPlayDataDto
import com.example.lyraapp.data.remote.dto.RefreshTokenBody
import com.example.lyraapp.data.remote.dto.SongDto
import com.example.lyraapp.data.remote.dto.SongsResponseDto
import com.example.lyraapp.data.remote.dto.StreamUrlDto
import com.example.lyraapp.data.remote.dto.UpdateProfileBody
import com.example.lyraapp.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LyraApiService {

    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(
        @Body body: OtpRequestBody,
    ): ApiEnvelope<OtpRequestDataDto>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(
        @Body body: OtpVerifyBody,
    ): ApiEnvelope<AuthSessionDto>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body body: RefreshTokenBody,
    ): ApiEnvelope<AuthSessionDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Body body: RefreshTokenBody,
    ): ApiEnvelope<LogoutDataDto>

    @GET("api/v1/me")
    suspend fun getCurrentUser(): ApiEnvelope<UserDto>

    @POST("api/v1/me/update-informations")
    suspend fun updateProfile(
        @Body body: UpdateProfileBody,
    ): ApiEnvelope<UserDto>

    @GET("api/v1/me/for-you")
    suspend fun getForYou(
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<List<SongDto>>

    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<List<SongDto>>

    @GET("api/v1/me/recommendations")
    suspend fun getRecommendations(
        @Query("limit") limit: Int = 20,
    ): ApiEnvelope<List<SongDto>>

    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(): ApiEnvelope<List<PlaylistDto>>

    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(
        @Body request: CreatePlaylistRequest,
    ): ApiEnvelope<PlaylistDto>

    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(
        @Path("id") playlistId: String,
        @Body body: AddPlaylistTrackBody,
    ): ApiEnvelope<PlaylistTrackAddedDto>

    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeTrackFromPlaylist(
        @Path("id") playlistId: String,
        @Path("songId") songId: String,
    ): ApiEnvelope<PlaylistTrackRemovedDto>

    @GET("api/v1/playlists")
    suspend fun getPublicPlaylists(): ApiEnvelope<List<PlaylistDto>>

    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylist(
        @Path("id") playlistId: String,
    ): ApiEnvelope<PlaylistWithSongsDto>

    @GET("api/v1/songs")
    suspend fun searchSongs(
        @Query("q") query: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
    ): SongsResponseDto

    @GET("api/v1/songs/{id}")
    suspend fun getSong(
        @Path("id") songId: String,
    ): ApiEnvelope<SongDto>

    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(
        @Path("id") songId: String,
    ): ApiEnvelope<StreamUrlDto>

    @POST("api/v1/me/plays")
    suspend fun recordPlay(
        @Body body: RecordPlayBody,
    ): ApiEnvelope<RecordPlayDataDto>
}
