package com.example.lyraapp.ui.search

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lyraapp.ui.icons.LyraIcons
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchContract.SideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Başlık
        Text(
            text = "Ara",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Arama Giriş Alanı
        SearchInputField(
            query = state.searchQuery,
            onQueryChange = { viewModel.onIntent(SearchContract.Intent.OnSearchQueryChanged(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Kategoriler (Hepsi, Pop, Elektronik...)
        CategoryFilterRow(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onCategorySelect = { viewModel.onIntent(SearchContract.Intent.OnCategorySelected(it)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Alt Başlık
        Text(
            text = "Türlere göz at",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Grid Listesi (Kalan alanı kaplaması için weight verdik)
        Box(modifier = Modifier.weight(1f)) {
            GenresVerticalGrid(
                genres = state.genres,
                onGenreClick = { viewModel.onIntent(SearchContract.Intent.OnGenreClick(it)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        placeholder = { Text("Şarkı, sanatçı veya albüm") },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = "Search Icon"
            )
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color.LightGray else Color.Gray.copy(alpha = 0.2f))
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun GenresVerticalGrid(
    genres: List<GenreUiModel>,
    onGenreClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(genres) { genre ->

            val parsedColor = try {
                if (genre.colorHex.startsWith("#")) {
                    Color(android.graphics.Color.parseColor(genre.colorHex))
                } else {
                    Color(genre.colorHex.removePrefix("0x").toLong(16))
                }
            } catch (e: Exception) {
                Color.Gray
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(parsedColor, parsedColor.copy(alpha = 0.7f))
                        )
                    )
                    .clickable { onGenreClick(genre.name) }
                    .padding(16.dp)
            ) {
                Text(
                    text = genre.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
        }
    }
}