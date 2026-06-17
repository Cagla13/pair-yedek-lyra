package com.example.lyraapp.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lyraapp.ui.auth.login.LoginRoute
import com.example.lyraapp.ui.auth.register.RegisterRoute
import com.example.lyraapp.ui.favorites.FavoritesScreen
import com.example.lyraapp.ui.favorites.FavoritesViewModel
import com.example.lyraapp.ui.home.HomeRoute
import com.example.lyraapp.ui.search.SearchScreen
import com.example.lyraapp.ui.search.SearchViewModel

import com.example.lyraapp.ui.playlist_detail.PlaylistDetailScreen
import com.example.lyraapp.ui.playlist_detail.PlaylistDetailViewModel


import com.example.lyraapp.ui.create_playlist.CreatePlaylistScreen
import com.example.lyraapp.ui.create_playlist.CreatePlaylistViewModel

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
        LyraDestination.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in showBottomBarScreens) {
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
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LyraDestination.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(LyraDestination.Login.route) {
                LoginRoute(
                    onNavigateToHome = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(LyraDestination.Register.route) {
                            launchSingleTop = true
                        }
                    },
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
                    onNavigateToLogin = {
                        navController.navigate(LyraDestination.Login.route) {
                            popUpTo(LyraDestination.Login.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(LyraDestination.Home.route) {
                HomeRoute(
                    onNavigateToDetails = { itemId ->
                        navController.navigate(LyraDestination.PlaylistDetail.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToProfile = {
                        navController.navigate(LyraDestination.Profile.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(LyraDestination.Search.route) {
                val searchViewModel: SearchViewModel = viewModel()
                SearchScreen(viewModel = searchViewModel)
            }


            composable(LyraDestination.Library.route) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Müzik Kütüphaneniz",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }


                    FloatingActionButton(
                        onClick = {
                            navController.navigate(LyraDestination.CreatePlaylist.route) {
                                launchSingleTop = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yeni Çalma Listesi Oluştur",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            composable(LyraDestination.Favorites.route) {
                val favoritesViewModel: FavoritesViewModel = viewModel()
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigateBack = {
                        navController.navigate(LyraDestination.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(LyraDestination.Profile.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Profil Ekranı İçeriği")
                }
            }

            composable(LyraDestination.PlaylistDetail.route) {
                val playlistDetailViewModel: PlaylistDetailViewModel = hiltViewModel()
                PlaylistDetailScreen(
                    viewModel = playlistDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
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