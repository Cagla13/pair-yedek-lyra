package com.example.lyraapp.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lyraapp.ui.profile.ProfileRoute
import com.example.lyraapp.ui.auth.login.LoginRoute
import com.example.lyraapp.ui.auth.otp.OtpRoute
import com.example.lyraapp.ui.auth.register.RegisterRoute
import com.example.lyraapp.ui.create_playlist.CreatePlaylistScreen
import com.example.lyraapp.ui.create_playlist.CreatePlaylistViewModel
import com.example.lyraapp.ui.favorites.FavoritesScreen
import com.example.lyraapp.ui.favorites.FavoritesViewModel
import com.example.lyraapp.ui.home.HomeRoute
import com.example.lyraapp.ui.library.LibraryScreen
import com.example.lyraapp.ui.player.LyraPlaybackBar
import com.example.lyraapp.ui.player.PlayerRoute
import com.example.lyraapp.ui.playlist_detail.PlaylistDetailScreen
import com.example.lyraapp.ui.search.SearchRoute

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
            startDestination = LyraDestination.Login.route,
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
                )
            }

            composable(LyraDestination.Search.route) {
                SearchRoute(
                    onNavigateToPlayer = {
                        navController.navigate(LyraDestination.Player.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }


            composable(LyraDestination.Library.route) {
                LibraryScreen(
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
                    }
                )
            }

            composable(LyraDestination.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = viewModel()
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(LyraDestination.Profile.route) {
                ProfileRoute()
            }

            composable(LyraDestination.Player.route) {
                PlayerRoute(
                    onNavigateBack = { navController.popBackStack() }
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