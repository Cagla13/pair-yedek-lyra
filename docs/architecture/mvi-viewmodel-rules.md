# LyraApp - ViewModel ve UI Bağlama Kuralları

> ViewModel, UI bağlama (Route/Screen) ve DI için **tek doğruluk kaynağıdır**.
> Sözleşme için bkz. [mvi-contracts.md](mvi-contracts.md), genel akış için [mvi-overview.md](mvi-overview.md).
>
> Referans: `ui/auth/login/LoginViewModel.kt`, `LoginScreen.kt`, `di/AuthModule.kt`.

---

## 1. Temel Kural

> ViewModel'in UI ile tek temas noktası `fun onIntent(intent: <Screen>Intent)`'tir.
> State `StateFlow`, Effect `Channel` ile dışarı açılır; her ikisinin de mutable hali
> **private** tutulur. ViewModel içinde **hiçbir Android/Compose/Context bağımlılığı bulunamaz.**

---

## 2. ViewModel İskeleti

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged -> updateForm { it.copy(phoneNumber = intent.value) }
            is LoginIntent.PasswordChanged    -> updateForm { it.copy(password = intent.value) }
            is LoginIntent.TogglePasswordVisibility ->
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is LoginIntent.Submit -> submit()
        }
    }
}
```

---