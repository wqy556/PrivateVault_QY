@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.privatevault.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType
import com.privatevault.core.MovieTag
import com.privatevault.core.VaultAppState
import com.privatevault.core.VaultLibrary
import com.privatevault.core.VaultMovie
import com.privatevault.core.VaultTab

@Composable
fun PrivateVaultApp() {
    var state by remember { mutableStateOf(VaultAppState.initial()) }
    var history by remember { mutableStateOf(emptyList<VaultAppState>()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    fun navigate(nextState: VaultAppState) {
        if (nextState != state) {
            history = history + state
            state = nextState
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                state = state.requireUnlock()
                history = emptyList()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (state.isLocked) {
            UnlockScreen(onUnlock = { passcode -> state = state.unlock(passcode) })
        } else {
            BackHandler(enabled = history.isNotEmpty()) {
                state = history.last()
                history = history.dropLast(1)
            }
            VaultScaffold(
                state = state,
                onTabSelected = { navigate(state.selectTab(it)) },
                onLock = {
                    state = state.requireUnlock()
                    history = emptyList()
                },
                onAddLibrary = { navigate(state.addLibrary()) },
                onRenameLibrary = { libraryId, name -> navigate(state.renameLibrary(libraryId, name)) },
                onDeleteLibrary = { libraryId -> navigate(state.deleteLibrary(libraryId)) },
                onSelectLibrary = { navigate(state.selectLibrary(it)) },
                onOpenMovie = { navigate(state.openMovie(it)) },
                onStageImages = { mode -> navigate(state.stageImageSelection(3, mode)) }
            )
        }
    }
}

@Composable
private fun UnlockScreen(onUnlock: (String) -> Unit) {
    var passcode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "PrivateVault", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "输入密码以进入本地私密片库",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = passcode,
            onValueChange = { passcode = it },
            label = { Text("密码或 PIN") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onUnlock(passcode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("解锁")
        }
    }
}

@Composable
private fun VaultScaffold(
    state: VaultAppState,
    onTabSelected: (VaultTab) -> Unit,
    onLock: () -> Unit,
    onAddLibrary: () -> Unit,
    onRenameLibrary: (String, String) -> Unit,
    onDeleteLibrary: (String) -> Unit,
    onSelectLibrary: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    onStageImages: (ImportMode) -> Unit
) {
    Scaffold(
        bottomBar = {
            VaultBottomBar(selectedTab = state.selectedTab, onTabSelected = onTabSelected)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TopHeader(onLock = onLock)
            when (state.selectedTab) {
                VaultTab.LibraryManage -> LibraryManageScreen(
                    libraries = state.libraries,
                    recentMovies = state.recentlyOpenedMovies,
                    tags = state.tags,
                    onAddLibrary = onAddLibrary,
                    onRenameLibrary = onRenameLibrary,
                    onDeleteLibrary = onDeleteLibrary,
                    onSelectLibrary = onSelectLibrary,
                    onOpenMovie = onOpenMovie
                )

                VaultTab.Favorites -> FavoritesScreen(
                    movies = state.favoriteMovies,
                    recentMovies = state.recentlyOpenedMovies,
                    tags = state.tags,
                    onOpenMovie = onOpenMovie
                )

                VaultTab.LibraryDetail -> LibraryDetailScreen(
                    library = state.selectedLibrary,
                    movies = state.moviesInSelectedLibrary,
                    tags = state.tags,
                    onOpenMovie = onOpenMovie
                )

                VaultTab.MovieDetail -> MovieDetailScreen(
                    movie = state.selectedMovie,
                    tags = state.tags,
                    pendingImportCount = state.pendingImportCount,
                    pendingImportMode = state.pendingImportMode,
                    onStageImages = onStageImages
                )

                VaultTab.Settings -> SettingsScreen()
            }
        }
    }
}

@Composable
private fun TopHeader(onLock: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "PrivateVault", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "本地优先的私密片库",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
            )
        }
        IconButton(onClick = onLock) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = "锁定 App")
        }
    }
}

@Composable
private fun LibraryManageScreen(
    libraries: List<VaultLibrary>,
    recentMovies: List<VaultMovie>,
    tags: List<MovieTag>,
    onAddLibrary: () -> Unit,
    onRenameLibrary: (String, String) -> Unit,
    onDeleteLibrary: (String) -> Unit,
    onSelectLibrary: (String) -> Unit,
    onOpenMovie: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "片库管理", subtitle = "这里管理多个片库，不展示某个片库内部影片。")
        }
        item {
            Button(
                onClick = onAddLibrary,
                modifier = Modifier.height(48.dp)
            ) {
                Text("新增片库")
            }
        }
        items(libraries) { library ->
            LibraryCard(
                library = library,
                showManageActions = true,
                onClick = { onSelectLibrary(library.id) },
                onRename = { onRenameLibrary(library.id, "${library.name}（已改名）") },
                onDelete = { onDeleteLibrary(library.id) }
            )
        }
        item {
            SectionHeader(title = "最近打开", subtitle = "最多显示最近 5 条。")
        }
        items(recentMovies.take(5)) { movie ->
            MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
        }
    }
}

@Composable
private fun FavoritesScreen(
    movies: List<VaultMovie>,
    recentMovies: List<VaultMovie>,
    tags: List<MovieTag>,
    onOpenMovie: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "收藏", subtitle = "这里展示用户收藏的影片。")
        }
        if (movies.isEmpty()) {
            item {
                MetadataCard(title = "暂无收藏", lines = listOf("进入影片详情后可将影片加入收藏。"))
            }
        } else {
            items(movies) { movie ->
                MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
            }
        }
        item {
            SectionHeader(title = "最近打开", subtitle = "最多显示最近 5 条。")
        }
        items(recentMovies.take(5)) { movie ->
            MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
        }
    }
}

@Composable
private fun LibraryDetailScreen(
    library: VaultLibrary?,
    movies: List<VaultMovie>,
    tags: List<MovieTag>,
    onOpenMovie: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = library?.name ?: "片库",
                subtitle = "当前片库内的影片。右滑或系统返回可回到上一页。"
            )
        }
        item {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.height(48.dp)
            ) {
                Text("添加影片")
            }
        }
        items(movies) { movie ->
            MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
        }
    }
}

@Composable
private fun MovieDetailScreen(
    movie: VaultMovie?,
    tags: List<MovieTag>,
    pendingImportCount: Int,
    pendingImportMode: ImportMode?,
    onStageImages: (ImportMode) -> Unit
) {
    if (movie == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("请选择一个影片")
        }
        return
    }

    val movieTags = tags.filter { it.id in movie.tagIds }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "封面图使用第一张详情图",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
            )
        }
        item {
            DetailImageSummary(movie = movie)
        }
        item {
            ImageImportChoiceCard(
                pendingImportCount = pendingImportCount,
                pendingImportMode = pendingImportMode,
                onStageImages = onStageImages
            )
        }
        item {
            MetadataCard(title = "网盘地址", lines = movie.links.map { "${it.type.label()}  ${it.url}" })
        }
        item {
            MetadataCard(title = "备注", lines = listOf(movie.notes))
        }
        item {
            MetadataCard(title = "标签", lines = movieTags.map { it.name }.ifEmpty { listOf("未添加标签") })
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "设置", style = MaterialTheme.typography.headlineSmall)
        MetadataCard(title = "App 锁", lines = listOf("后续接入 PIN 存储和生物识别。"))
        MetadataCard(title = "安全边界", lines = listOf("当前版本使用 App 私有目录，不承诺文件级加密。"))
        MetadataCard(title = "截图", lines = listOf("后续可通过窗口安全标记禁止系统截图。"))
    }
}

@Composable
private fun VaultBottomBar(selectedTab: VaultTab, onTabSelected: (VaultTab) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == VaultTab.LibraryManage || selectedTab == VaultTab.LibraryDetail,
            onClick = { onTabSelected(VaultTab.LibraryManage) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text("片库") }
        )
        NavigationBarItem(
            selected = selectedTab == VaultTab.Favorites,
            onClick = { onTabSelected(VaultTab.Favorites) },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("收藏") }
        )
        NavigationBarItem(
            selected = selectedTab == VaultTab.Settings,
            onClick = { onTabSelected(VaultTab.Settings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("设置") }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
    }
}

@Composable
private fun LibraryCard(
    library: VaultLibrary,
    showManageActions: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Folder, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = library.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "${library.movieIds.size} 个影片", style = MaterialTheme.typography.bodyMedium)
            }
            if (showManageActions) {
                IconButton(onClick = onRename) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "重命名片库")
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "删除片库")
                }
            }
        }
    }
}

@Composable
private fun MovieRow(movie: VaultMovie, tags: List<MovieTag>, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverPlaceholder()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = movie.title, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tags.take(2).forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag.name) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverPlaceholder() {
    Card(modifier = Modifier.size(width = 64.dp, height = 84.dp)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
        }
    }
}

@Composable
private fun DetailImageSummary(movie: VaultMovie) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "详情图", style = MaterialTheme.typography.titleMedium)
            Text(text = "共 ${movie.detailImages.size} 张。第一张作为封面图。")
        }
    }
}

@Composable
private fun ImageImportChoiceCard(
    pendingImportCount: Int,
    pendingImportMode: ImportMode?,
    onStageImages: (ImportMode) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "添加详情图", style = MaterialTheme.typography.titleMedium)
            Text(text = "可从系统相册复制或迁移。迁移会在复制成功后请求移除相册原图。")
            AssistChip(onClick = {}, label = { Text("已选择：$pendingImportCount") })
            pendingImportMode?.let { mode ->
                Text(
                    text = when (mode) {
                        ImportMode.CopyOnly -> "当前模式：复制，完成后保留两份。"
                        ImportMode.MoveAndHideOriginal -> "当前模式：迁移/剪切，完成后请求隐藏原图。"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = { onStageImages(ImportMode.MoveAndHideOriginal) },
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("迁移/剪切图片")
            }
            OutlinedButton(
                onClick = { onStageImages(ImportMode.CopyOnly) },
                modifier = Modifier.height(48.dp)
            ) {
                Text("复制图片")
            }
        }
    }
}

@Composable
private fun MetadataCard(title: String, lines: List<String>) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            lines.forEach { line ->
                Text(text = line, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun LinkType.label(): String {
    return when (this) {
        LinkType.Quark -> "夸克"
        LinkType.Baidu -> "百度网盘"
        LinkType.Xunlei -> "迅雷"
        LinkType.Magnet -> "磁力"
        LinkType.Web -> "网页"
        LinkType.Other -> "其他"
    }
}
