package com.example.lyraapp.data

interface AuthRepository {

    /**
     * Verilen telefon numarası ve şifreyle giriş dener.
     *
     * @return Başarılıysa [Result.success], aksi halde hata mesajı taşıyan [Result.failure].
     */
    suspend fun login(phoneNumber: String, password: String): Result<Unit>

    /**
     * Verilen kullanıcı bilgileriyle yeni bir hesap oluşturmayı dener.
     *
     * @return Başarılıysa [Result.success], aksi halde hata mesajı taşıyan [Result.failure].
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String,
    ): Result<Unit>
}