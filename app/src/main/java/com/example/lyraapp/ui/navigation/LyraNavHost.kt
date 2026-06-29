package com.example.lyraapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lyraapp.ui.auth.login.LoginRoute
import com.example.lyraapp.ui.auth.otp.OtpRoute
import com.example.lyraapp.ui.auth.register.RegisterRoute
import com.example.lyraapp.ui.create_playlist.CreatePlaylistScreen
import com.example.lyraapp.ui.create_playlist.CreatePlaylistViewModel
import com.example.lyraapp.ui.favorites.FavoritesScreen
import com.example.lyraapp.ui.favorites.FavoritesViewModel
import com.example.lyraapp.ui.home.HomeRoute
import com.example.lyraapp.ui.home_section.HomeSection
import com.example.lyraapp.ui.home_section.HomeSectionRoute
import com.example.lyraapp.ui.library.LibraryRoute
import com.example.lyraapp.ui.payment.PaymentRoute
import com.example.lyraapp.ui.player.LyraPlaybackBar
import com.example.lyraapp.ui.player.PlayerRoute
import com.example.lyraapp.ui.playlist_detail.PlaylistDetailScreen
import com.example.lyraapp.ui.premium.PremiumRoute
import com.example.lyraapp.ui.profile.EditProfileRoute
import com.example.lyraapp.ui.profile.ProfileRoute
import com.example.lyraapp.ui.recently_played.RecentlyPlayedRoute
import com.example.lyraapp.ui.search.SearchRoute
import com.example.lyraapp.ui.session.SessionViewModel
import com.example.lyraapp.ui.song_detail.SongDetailRoute

@Composable
fun LyraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBarScreens = listOf(
        LyraDestination.Home.route,
        LyraDestination.Search.route,
        LyraDestination.Library.route,
        LyraDestination.Favorites.route,
        LyraDestination.Profile.route,
    )

    val showGlobalMiniPlayer = currentRoute in listOf(
        LyraDestination.Search.route,
        LyraDestination.Library.route,
        LyraDestination.Favorites.route,
        LyraDestination.Profile.route,
    )

    val sessionViewModel: SessionViewModel = hiltViewModel()
    val isSessionReady by sessionViewModel.isReady.collectAsStateWithLifecycle()
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isSessionReady) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (isLoggedIn) LyraDestination.Home.route else LyraDestination.Login.route

    val navigateToSongDetail: (String) -> Unit = { songId ->
        navController.navigate(LyraDestination.SongDetail.createRoute(songId)) {
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in showBottomBarScreens) {
                Column {
                    if (showGlobalMiniPlayer) {
                        LyraPlaybackBar(
                            onNavigateToPlayer = {
                                navController.navigate(LyraDestination.Player.route) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    LyraBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { targetRoute ->
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LyraDestination.Login.route) {
                LoginRoute(
                    onNavigateToOtp = { phone ->
                        navController.navigate(LyraDestination.OtpVerify.createRoute(phone)) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = LyraDestination.OtpVerify.route,
                arguments = listOf(
                    navArgument(LyraDestination.OtpVerify.PHONE_ARG) {
                        type = NavType.StringType
                    },
                ),
            ) {
                OtpRoute(
                    onNavigateToHome = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCompleteProfile = {
                        navController.navigate(LyraDestination.Register.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(LyraDestination.Register.route) {
                RegisterRoute(
                    onNavigateToHome = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(LyraDestination.Home.route) {
                HomeRoute(
                    onNavigateToProfile = {
                        navController.navigate(LyraDestination.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRecentlyPlayed = {
                        navController.navigate(LyraDestination.RecentlyPlayed.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToForYou = {
                        navController.navigate(
                            LyraDestination.HomeSection.createRoute(HomeSection.FOR_YOU.routeKey),
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRecommendations = {
                        navController.navigate(
                            LyraDestination.HomeSection.createRoute(HomeSection.RECOMMENDATIONS.routeKey),
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFeaturedPlaylists = {
                        navController.navigate(
                            LyraDestination.HomeSection.createRoute(HomeSection.FEATURED_PLAYLISTS.routeKey),
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(LyraDestination.PlaylistDetail.createRoute(playlistId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremium = { planType ->
                        navController.navigate(LyraDestination.Premium.createRoute(planType)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSongDetail = navigateToSongDetail,
                )
            }

            composable(LyraDestination.Search.route) {
                SearchRoute(
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSongDetail = navigateToSongDetail,
                )
            }


            composable(LyraDestination.Library.route) {
                LibraryRoute(
                    onNavigateToSearch = {
                        navController.navigate(LyraDestination.Search.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCreatePlaylist = {
                        navController.navigate(LyraDestination.CreatePlaylist.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(LyraDestination.PlaylistDetail.createRoute(playlistId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToFavorites = {
                        navController.navigate(LyraDestination.Favorites.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSongDetail = navigateToSongDetail,
                )
            }

            composable(LyraDestination.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSongDetail = navigateToSongDetail,
                )
            }

            composable(LyraDestination.Profile.route) {
                ProfileRoute(
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(LyraDestination.EditProfile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPremium = {
                        navController.navigate(LyraDestination.Premium.createRoute()) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(LyraDestination.EditProfile.route) {
                EditProfileRoute(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = LyraDestination.SongDetail.route,
                arguments = listOf(
                    navArgument(LyraDestination.SongDetail.SONG_ID_ARG) {
                        type = NavType.StringType
                    },
                ),
            ) {
                SongDetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(LyraDestination.RecentlyPlayed.route) {
                RecentlyPlayedRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSongDetail = navigateToSongDetail,
                )
            }

            composable(
                route = LyraDestination.HomeSection.route,
                arguments = listOf(
                    navArgument(LyraDestination.HomeSection.SECTION_ARG) {
                        type = NavType.StringType
                    },
                ),
            ) {
                HomeSectionRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlaylistDetail = { playlistId ->
                        navController.navigate(LyraDestination.PlaylistDetail.createRoute(playlistId)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSongDetail = navigateToSongDetail,
                )
            }

            composable(LyraDestination.Player.route) {
                PlayerRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSongDetail = { songId ->
                        navController.navigate(LyraDestination.SongDetail.createRoute(songId)) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = LyraDestination.Premium.route,
                arguments = listOf(
                    navArgument(LyraDestination.Premium.PLAN_ARG) {
                        type = NavType.StringType
                        defaultValue = LyraDestination.Premium.DEFAULT_PLAN
                    },
                ),
            ) {
                PremiumRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPayment = { plan, price, title, desc ->
                        navController.navigate(LyraDestination.Payment.createRoute(plan, price, title, desc)) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(
                route = LyraDestination.Payment.route,
                arguments = listOf(
                    navArgument(LyraDestination.Payment.PLAN_ARG) {
                        type = NavType.StringType
                        defaultValue = LyraDestination.Premium.DEFAULT_PLAN
                    },
                    navArgument(LyraDestination.Payment.PRICE_ARG) {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument(LyraDestination.Payment.TITLE_ARG) {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument(LyraDestination.Payment.DESC_ARG) {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
            ) {
                PaymentRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onPaymentSuccess = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(LyraDestination.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = LyraDestination.PlaylistDetail.route,
                arguments = listOf(
                    navArgument(LyraDestination.PlaylistDetail.PLAYLIST_ID_ARG) {
                        type = NavType.StringType
                    },
                ),
            ) {
                PlaylistDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }


            composable(LyraDestination.CreatePlaylist.route) {
                val createPlaylistViewModel: CreatePlaylistViewModel = hiltViewModel()
                CreatePlaylistScreen(
                    viewModel = createPlaylistViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
