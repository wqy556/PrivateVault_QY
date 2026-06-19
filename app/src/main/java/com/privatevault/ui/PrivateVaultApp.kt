@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.privatevault.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.privatevault.core.ImportMode
import com.privatevault.core.LinkType
import com.privatevault.core.MovieTag
import com.privatevault.core.MovieImage
import com.privatevault.core.VaultAppState
import com.privatevault.core.VaultLibrary
import com.privatevault.core.VaultMovie
import com.privatevault.core.VaultTab
import com.privatevault.core.filterLibraries
import com.privatevault.core.filterMovies
import com.privatevault.data.VaultRepository
import com.privatevault.media.LibraryImageExporter
import com.privatevault.media.MediaStoreOriginalRemovalGateway
import com.privatevault.media.PrivateMediaImporter
import com.privatevault.media.planLibraryImageExport
import coil.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun PrivateVaultApp(repository: VaultRepository) {
    val viewModel: VaultViewModel = viewModel(factory = VaultViewModelFactory(repository))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var history by remember { mutableStateOf(emptyList<VaultAppState>()) }

    fun navigate(nextState: VaultAppState) {
        if (nextState != state) {
            history = history + state
            viewModel.applyNavigation(nextState)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BackHandler(enabled = history.isNotEmpty()) {
            viewModel.applyNavigation(history.last())
            history = history.dropLast(1)
        }
        VaultScaffold(
            state = state,
            onTabSelected = { navigate(state.selectTab(it)) },
            onAddLibrary = viewModel::addLibrary,
            onRenameLibrary = viewModel::renameLibrary,
            onDeleteLibrary = viewModel::deleteLibrary,
            onAddMovie = viewModel::addMovie,
            onSelectLibrary = { navigate(state.selectLibrary(it)) },
            onOpenMovie = { navigate(state.openMovie(it)) },
            onStageImages = viewModel::stageImageSelection,
            onImagesImported = viewModel::addMovieImages,
            onDeleteMovieImage = viewModel::deleteMovieImage,
            onUpdateMovieTitle = viewModel::updateMovieTitle,
            onUpdateMovieNotes = viewModel::updateMovieNotes,
            onToggleFavorite = viewModel::toggleFavorite,
            onAddLink = viewModel::addLink,
            onDeleteLink = viewModel::deleteLink,
            onAssignTag = viewModel::assignTag,
            onCreateAndAssignTag = viewModel::createAndAssignTag,
            onRemoveTag = viewModel::removeTag,
            onDeleteMovie = viewModel::deleteMovie
        )
    }
}

@Composable
private fun VaultScaffold(
    state: VaultAppState,
    onTabSelected: (VaultTab) -> Unit,
    onAddLibrary: (String) -> Unit,
    onRenameLibrary: (String, String) -> Unit,
    onDeleteLibrary: (String) -> Unit,
    onAddMovie: (String, String) -> Unit,
    onSelectLibrary: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    onStageImages: (Int, ImportMode) -> Unit,
    onImagesImported: (String, List<ImportedVaultMedia>, ImportMode, Boolean) -> Unit,
    onDeleteMovieImage: (String) -> Unit,
    onUpdateMovieTitle: (String, String) -> Unit,
    onUpdateMovieNotes: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddLink: (String, String, LinkType) -> Unit,
    onDeleteLink: (String) -> Unit,
    onAssignTag: (String, String) -> Unit,
    onCreateAndAssignTag: (String, String) -> Unit,
    onRemoveTag: (String, String) -> Unit,
    onDeleteMovie: (String) -> Unit
) {
    val context = LocalContext.current
    var showSystemLockDialog by remember { mutableStateOf(false) }

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
            TopHeader(onOpenSystemLockHelp = { showSystemLockDialog = true })
            when (state.selectedTab) {
                VaultTab.LibraryManage -> LibraryManageScreen(
                    libraries = state.libraries,
                    movies = state.movies,
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
                    allMovies = state.movies,
                    tags = state.tags,
                    onAddMovie = onAddMovie,
                    onOpenMovie = onOpenMovie
                )

                VaultTab.MovieDetail -> MovieDetailScreen(
                    movie = state.selectedMovie,
                    tags = state.tags,
                    pendingImportCount = state.pendingImportCount,
                    pendingImportMode = state.pendingImportMode,
                    onStageImages = onStageImages,
                    onImagesImported = onImagesImported,
                    onDeleteImage = onDeleteMovieImage,
                    onUpdateTitle = onUpdateMovieTitle,
                    onUpdateNotes = onUpdateMovieNotes,
                    onToggleFavorite = onToggleFavorite,
                    onAddLink = onAddLink,
                    onDeleteLink = onDeleteLink,
                    onAssignTag = onAssignTag,
                    onCreateAndAssignTag = onCreateAndAssignTag,
                    onRemoveTag = onRemoveTag,
                    onDeleteMovie = onDeleteMovie
                )

                VaultTab.Settings -> SettingsScreen()
            }
        }
    }

    if (showSystemLockDialog) {
        SystemLockHelpDialog(
            onDismiss = { showSystemLockDialog = false },
            onOpenSettings = {
                showSystemLockDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun TopHeader(onOpenSystemLockHelp: () -> Unit) {
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
        IconButton(onClick = onOpenSystemLockHelp) {
            Icon(imageVector = Icons.Default.LockOpen, contentDescription = "系统应用锁设置")
        }
    }
}

@Composable
private fun SystemLockHelpDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("使用系统应用锁") },
        text = {
            Text("PrivateVault 默认不内置 App 锁。建议在系统设置中为本应用开启应用锁、隐私锁或生物识别保护。不同手机厂商入口不同，这里会先打开本应用的系统设置页。")
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("打开设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun LibraryManageScreen(
    libraries: List<VaultLibrary>,
    movies: List<VaultMovie>,
    recentMovies: List<VaultMovie>,
    tags: List<MovieTag>,
    onAddLibrary: (String) -> Unit,
    onRenameLibrary: (String, String) -> Unit,
    onDeleteLibrary: (String) -> Unit,
    onSelectLibrary: (String) -> Unit,
    onOpenMovie: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<VaultLibrary?>(null) }
    var deleteTarget by remember { mutableStateOf<VaultLibrary?>(null) }
    var query by remember { mutableStateOf("") }
    var selectedTagIds by remember { mutableStateOf(emptySet<String>()) }
    val filteredLibraries = filterLibraries(libraries, query)
        .filter { library ->
            selectedTagIds.isEmpty() ||
                movies.any { movie -> movie.id in library.movieIds && movie.tagIds.any { it in selectedTagIds } }
        }
    val filteredRecentMovies = filterMovies(recentMovies, query, selectedTagIds)

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "片库管理", subtitle = "这里管理多个片库，不展示某个片库内部影片。")
        }
        item {
            SearchAndTagFilters(
                query = query,
                onQueryChange = { query = it },
                tags = tags,
                selectedTagIds = selectedTagIds,
                onToggleTag = { tagId ->
                    selectedTagIds = selectedTagIds.toggle(tagId)
                }
            )
        }
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.height(48.dp)
            ) {
                Text("新增片库")
            }
        }
        items(filteredLibraries) { library ->
            LibraryCard(
                library = library,
                onClick = { onSelectLibrary(library.id) },
                onRename = { renameTarget = library },
                onDelete = { deleteTarget = library }
            )
        }
        item {
            SectionHeader(title = "最近打开", subtitle = "最多显示最近 5 条。")
        }
        items(filteredRecentMovies.take(5)) { movie ->
            MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
        }
    }

    if (showAddDialog) {
        LibraryNameDialog(
            title = "新增片库",
            initialName = "",
            confirmText = "创建",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onAddLibrary(name)
                showAddDialog = false
            }
        )
    }

    renameTarget?.let { library ->
        LibraryNameDialog(
            title = "重命名片库",
            initialName = library.name,
            confirmText = "保存",
            onDismiss = { renameTarget = null },
            onConfirm = { name ->
                onRenameLibrary(library.id, name)
                renameTarget = null
            }
        )
    }

    deleteTarget?.let { library ->
        DeleteLibraryDialog(
            library = library,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDeleteLibrary(library.id)
                deleteTarget = null
            }
        )
    }
}

@Composable
private fun LibraryNameDialog(
    title: String,
    initialName: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    val trimmedName = name.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("片库名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = trimmedName.isNotEmpty(),
                onClick = { onConfirm(trimmedName) }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun DeleteLibraryDialog(
    library: VaultLibrary,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除片库") },
        text = {
            Text("删除“${library.name}”后，片库内的影片记录也会一起移除。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun FavoritesScreen(
    movies: List<VaultMovie>,
    recentMovies: List<VaultMovie>,
    tags: List<MovieTag>,
    onOpenMovie: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTagIds by remember { mutableStateOf(emptySet<String>()) }
    val filteredMovies = filterMovies(movies, query, selectedTagIds)
    val filteredRecentMovies = filterMovies(recentMovies, query, selectedTagIds)

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(title = "收藏", subtitle = "这里展示用户收藏的影片。")
        }
        item {
            SearchAndTagFilters(
                query = query,
                onQueryChange = { query = it },
                tags = tags,
                selectedTagIds = selectedTagIds,
                onToggleTag = { tagId -> selectedTagIds = selectedTagIds.toggle(tagId) }
            )
        }
        if (filteredMovies.isEmpty()) {
            item {
                MetadataCard(title = "暂无收藏", lines = listOf("进入影片详情后可将影片加入收藏。"))
            }
        } else {
            items(filteredMovies) { movie ->
                MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
            }
        }
        item {
            SectionHeader(title = "最近打开", subtitle = "最多显示最近 5 条。")
        }
        items(filteredRecentMovies.take(5)) { movie ->
            MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
        }
    }
}

@Composable
private fun LibraryDetailScreen(
    library: VaultLibrary?,
    movies: List<VaultMovie>,
    allMovies: List<VaultMovie>,
    tags: List<MovieTag>,
    onAddMovie: (String, String) -> Unit,
    onOpenMovie: (String) -> Unit
) {
    var showAddMovieDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedTagIds by remember { mutableStateOf(emptySet<String>()) }
    val filteredMovies = filterMovies(movies, query, selectedTagIds)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exporter = remember(context) { LibraryImageExporter(context) }
    var exportMessage by remember(library?.id) { mutableStateOf<String?>(null) }

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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { showAddMovieDialog = true },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("添加影片")
                }
                OutlinedButton(
                    enabled = library != null,
                    onClick = {
                        val selectedLibrary = library ?: return@OutlinedButton
                        val plan = planLibraryImageExport(selectedLibrary, allMovies)
                        if (plan.items.isEmpty()) {
                            exportMessage = "当前片库没有可导出的图片。"
                            return@OutlinedButton
                        }
                        scope.launch {
                            val result = exporter.export(plan)
                            exportMessage = "已导出 ${result.exportedCount} 张到相册“${plan.albumName}”" +
                                if (result.failedCount > 0) "，${result.failedCount} 张失败。" else "。"
                        }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("导出图片")
                }
            }
        }
        item {
            SearchAndTagFilters(
                query = query,
                onQueryChange = { query = it },
                tags = tags,
                selectedTagIds = selectedTagIds,
                onToggleTag = { tagId -> selectedTagIds = selectedTagIds.toggle(tagId) }
            )
        }
        exportMessage?.let { message ->
            item {
                MetadataCard(title = "导出结果", lines = listOf(message))
            }
        }
        if (filteredMovies.isEmpty()) {
            item {
                MetadataCard(title = "暂无影片", lines = listOf("可以添加影片，或调整搜索和标签筛选。"))
            }
        } else {
            items(filteredMovies) { movie ->
                MovieRow(movie = movie, tags = tags.filter { it.id in movie.tagIds }, onClick = { onOpenMovie(movie.id) })
            }
        }
    }

    if (showAddMovieDialog && library != null) {
        MovieTitleDialog(
            onDismiss = { showAddMovieDialog = false },
            onConfirm = { title ->
                onAddMovie(library.id, title)
                showAddMovieDialog = false
            }
        )
    }
}

@Composable
private fun MovieTitleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val trimmedTitle = title.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加影片") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("片名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = trimmedTitle.isNotEmpty(),
                onClick = { onConfirm(trimmedTitle) }
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SearchAndTagFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    tags: List<MovieTag>,
    selectedTagIds: Set<String>,
    onToggleTag: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("搜索") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tags) { tag ->
                    InputChip(
                        selected = tag.id in selectedTagIds,
                        onClick = { onToggleTag(tag.id) },
                        label = { Text(tag.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieDetailScreen(
    movie: VaultMovie?,
    tags: List<MovieTag>,
    pendingImportCount: Int,
    pendingImportMode: ImportMode?,
    onStageImages: (Int, ImportMode) -> Unit,
    onImagesImported: (String, List<ImportedVaultMedia>, ImportMode, Boolean) -> Unit,
    onDeleteImage: (String) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onUpdateNotes: (String, String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onAddLink: (String, String, LinkType) -> Unit,
    onDeleteLink: (String) -> Unit,
    onAssignTag: (String, String) -> Unit,
    onCreateAndAssignTag: (String, String) -> Unit,
    onRemoveTag: (String, String) -> Unit,
    onDeleteMovie: (String) -> Unit
) {
    if (movie == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("请选择一个影片")
        }
        return
    }

    val movieTags = tags.filter { it.id in movie.tagIds }
    val availableTags = tags.filter { it.id !in movie.tagIds }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val importer = remember(context) { PrivateMediaImporter(context) }
    val removalGateway = remember(context) { MediaStoreOriginalRemovalGateway(context) }
    var requestedImportMode by remember { mutableStateOf<ImportMode?>(null) }
    var pendingDeleteUris by remember { mutableStateOf(emptyList<Uri>()) }
    var pendingCopiedMedia by remember { mutableStateOf(emptyList<ImportedVaultMedia>()) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }
    var previewStartIndex by remember(movie.id) { mutableStateOf<Int?>(null) }

    // ── Editable state ──
    var editingTitle by remember(movie.id) { mutableStateOf(false) }
    var draftTitle by remember(movie.id, movie.title) { mutableStateOf(movie.title) }
    var editingNotes by remember(movie.id) { mutableStateOf(false) }
    var draftNotes by remember(movie.id, movie.notes) { mutableStateOf(movie.notes) }
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showDeleteMovieDialog by remember { mutableStateOf(false) }

    // ── Delete originals launcher ──
    val deleteOriginalsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val originalRemoved = result.resultCode == Activity.RESULT_OK
        val mode = requestedImportMode ?: ImportMode.MoveAndHideOriginal
        if (pendingCopiedMedia.isNotEmpty()) {
            onImagesImported(movie.id, pendingCopiedMedia, mode, originalRemoved)
        }
        if (!originalRemoved) {
            importErrorMessage = "已保存副本，但系统未移除相册原图；原图仍可能在相册和微信图片选择器中显示。"
        }
        pendingCopiedMedia = emptyList()
        pendingDeleteUris = emptyList()
        requestedImportMode = null
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        val mode = requestedImportMode ?: return@rememberLauncherForActivityResult
        if (uris.isEmpty()) {
            requestedImportMode = null
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            importErrorMessage = null
            runCatching {
                val copiedMedia = uris.map { uri -> importer.copyToPrivateStorage(uri) }
                if (mode == ImportMode.MoveAndHideOriginal) {
                    val deleteRequest = removalGateway.createDeleteRequest(uris)
                    if (deleteRequest == null) {
                        onImagesImported(movie.id, copiedMedia, mode, false)
                        importErrorMessage = "已保存副本，但系统未允许按当前来源移除原图；原图仍可能在相册和微信图片选择器中显示。"
                        requestedImportMode = null
                    } else {
                        pendingDeleteUris = uris
                        pendingCopiedMedia = copiedMedia
                        deleteOriginalsLauncher.launch(
                            IntentSenderRequest.Builder(deleteRequest.intentSender).build()
                        )
                    }
                } else {
                    onImagesImported(movie.id, copiedMedia, mode, false)
                    requestedImportMode = null
                }
            }.onFailure { error ->
                importErrorMessage = "导入失败：${error.localizedMessage ?: "无法读取所选媒体"}"
                pendingCopiedMedia = emptyList()
                pendingDeleteUris = emptyList()
                requestedImportMode = null
            }
        }
    }

    fun copyToClipboard(label: String, value: String) {
        val text = value.trim()
        if (text.isEmpty()) return
        clipboardManager.setText(AnnotatedString(text))
        Toast.makeText(context, "$label 已复制", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Title + Favorite ──
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingTitle) {
                        OutlinedTextField(
                            value = draftTitle,
                            onValueChange = { draftTitle = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val trimmedTitle = draftTitle.trim()
                            if (trimmedTitle.isNotEmpty()) {
                                onUpdateTitle(movie.id, trimmedTitle)
                                editingTitle = false
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "保存标题")
                        }
                        IconButton(onClick = {
                            draftTitle = movie.title
                            editingTitle = false
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "取消编辑标题")
                        }
                    } else {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { copyToClipboard("标题", movie.title) }) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "复制标题")
                        }
                        IconButton(onClick = {
                            draftTitle = movie.title
                            editingTitle = true
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "编辑标题")
                        }
                        IconButton(onClick = { onToggleFavorite(movie.id) }) {
                            Icon(
                                imageVector = if (movie.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (movie.isFavorite) "取消收藏" else "加入收藏",
                                tint = if (movie.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f)
                            )
                        }
                        IconButton(onClick = { showDeleteMovieDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除影片",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Text(
                    text = "封面图使用第一张详情图",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                )
            }
        }

        // ── Image summary + picker ──
        item {
            DetailImageSummary(
                movie = movie,
                onOpenImage = { index -> previewStartIndex = index },
                onDeleteImage = onDeleteImage
            )
        }
        item {
            ImageImportChoiceCard(
                pendingImportCount = pendingImportCount,
                pendingImportMode = pendingImportMode,
                pendingDeleteCount = pendingDeleteUris.size,
                importErrorMessage = importErrorMessage,
                onPickImages = { mode ->
                    requestedImportMode = mode
                    importErrorMessage = null
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)
                    )
                }
            )
        }

        // ── Editable Notes ──
        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "备注", style = MaterialTheme.typography.titleMedium)
                        Row {
                            if (!editingNotes && movie.notes.isNotBlank()) {
                                IconButton(onClick = { copyToClipboard("备注", movie.notes) }) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "复制备注")
                                }
                            }
                            IconButton(onClick = {
                                if (editingNotes) {
                                    onUpdateNotes(movie.id, draftNotes)
                                } else {
                                    draftNotes = movie.notes
                                }
                                editingNotes = !editingNotes
                            }) {
                                Icon(
                                    imageVector = if (editingNotes) Icons.Default.Check else Icons.Default.Edit,
                                    contentDescription = if (editingNotes) "保存备注" else "编辑备注"
                                )
                            }
                        }
                    }
                    if (editingNotes) {
                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { draftNotes = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6,
                            placeholder = { Text("输入备注内容…") }
                        )
                    } else {
                        Text(
                            text = movie.notes.ifEmpty { "暂无备注" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (movie.notes.isEmpty()) 0.54f else 1f
                            )
                        )
                    }
                }
            }
        }

        // ── Editable Links ──
        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "网盘地址", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showAddLinkDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "添加链接")
                        }
                    }
                    if (movie.links.isEmpty()) {
                        Text(
                            text = "暂无链接",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f)
                        )
                    } else {
                        movie.links.forEach { link ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = link.url,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = link.type.label(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { copyToClipboard("链接", link.url) }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "复制链接",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { onDeleteLink(link.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "删除链接",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Editable Tags ──
        item {
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "标签", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showAddTagDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "添加标签")
                        }
                    }
                    if (movieTags.isEmpty()) {
                        Text(
                            text = "未添加标签",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f)
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            movieTags.forEach { tag ->
                                InputChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(tag.name) },
                                    trailingIcon = {
                                        IconButton(onClick = { onRemoveTag(movie.id, tag.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "移除标签 ${tag.name}",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add Link Dialog ──
    if (showAddLinkDialog) {
        AddLinkDialog(
            onDismiss = { showAddLinkDialog = false },
            onConfirm = { url, type ->
                onAddLink(movie.id, url, type)
                showAddLinkDialog = false
            }
        )
    }

    // ── Add Tag Dialog ──
    if (showAddTagDialog) {
        AddTagDialog(
            availableTags = availableTags,
            onDismiss = { showAddTagDialog = false },
            onSelectExisting = { tagId ->
                onAssignTag(movie.id, tagId)
                showAddTagDialog = false
            },
            onCreateNew = { name ->
                onCreateAndAssignTag(movie.id, name)
                showAddTagDialog = false
            }
        )
    }

    // ── Delete Movie Confirmation ──
    if (showDeleteMovieDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteMovieDialog = false },
            title = { Text("删除影片") },
            text = { Text("确定要删除「${movie.title}」吗？\n影片的所有详情图和链接也会被删除，此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMovie(movie.id)
                    showDeleteMovieDialog = false
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteMovieDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    previewStartIndex?.let { startIndex ->
        if (movie.detailImages.isNotEmpty()) {
            MediaPreviewDialog(
                images = movie.detailImages,
                initialPage = startIndex.coerceIn(0, movie.detailImages.lastIndex),
                onDismiss = { previewStartIndex = null },
                onDeleteImage = { image, page ->
                    onDeleteImage(image.id)
                    previewStartIndex = if (movie.detailImages.size <= 1) {
                        null
                    } else {
                        page.coerceAtMost(movie.detailImages.lastIndex - 1)
                    }
                }
            )
        } else {
            previewStartIndex = null
        }
    }
}

@Composable
private fun AddLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (url: String, type: LinkType) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(LinkType.Web) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    val trimmedUrl = url.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加网盘链接") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("链接地址") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedButton(
                        onClick = { typeMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("类型：${selectedType.label()}")
                    }
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        LinkType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label()) },
                                onClick = {
                                    selectedType = type
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = trimmedUrl.isNotEmpty(),
                onClick = { onConfirm(trimmedUrl, selectedType) }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun AddTagDialog(
    availableTags: List<MovieTag>,
    onDismiss: () -> Unit,
    onSelectExisting: (String) -> Unit,
    onCreateNew: (String) -> Unit
) {
    var newTagName by remember { mutableStateOf("") }
    val trimmedName = newTagName.trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加标签") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (availableTags.isNotEmpty()) {
                    Text(
                        text = "已有标签",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableTags.forEach { tag ->
                            AssistChip(
                                onClick = { onSelectExisting(tag.id) },
                                label = { Text(tag.name) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("新标签名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = trimmedName.isNotEmpty(),
                onClick = { onCreateNew(trimmedName) }
            ) {
                Text("新建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
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
        MetadataCard(title = "系统应用锁", lines = listOf("本应用不内置 PIN。请在系统设置中启用应用锁、隐私锁或生物识别保护。"))
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
            IconButton(onClick = onRename) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "重命名片库")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "删除片库")
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
            MovieCoverImage(coverImage = movie.coverImage)
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
private fun MovieCoverImage(coverImage: MovieImage?) {
    Card(modifier = Modifier.size(width = 64.dp, height = 84.dp)) {
        MediaThumbnail(
            image = coverImage,
            contentDescription = "封面图",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DetailImageSummary(
    movie: VaultMovie,
    onOpenImage: (Int) -> Unit,
    onDeleteImage: (String) -> Unit
) {
    val movedAndHiddenCount = movie.detailImages.count {
        it.importMode == ImportMode.MoveAndHideOriginal && it.originalRemoved
    }
    val movedButStillVisibleCount = movie.detailImages.count {
        it.importMode == ImportMode.MoveAndHideOriginal && !it.originalRemoved && it.originalUri != null
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "详情图", style = MaterialTheme.typography.titleMedium)
            Text(text = "共 ${movie.detailImages.size} 张。第一张作为封面图。")
            when {
                movedButStillVisibleCount > 0 -> {
                    Text(
                        text = "有 $movedButStillVisibleCount 项已复制到私密目录，但原图仍可能在相册、微信等入口可见。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                movedAndHiddenCount > 0 -> {
                    Text(
                        text = "已迁移 $movedAndHiddenCount 项，系统已确认移除原图。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (movie.detailImages.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    movie.detailImages.take(4).forEachIndexed { index, image ->
                        Card(
                            modifier = Modifier
                                .size(width = 72.dp, height = 96.dp)
                                .clickable { onOpenImage(index) }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                MediaThumbnail(
                                    image = image,
                                    contentDescription = "打开详情媒体",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { onDeleteImage(image.id) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除详情媒体",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaThumbnail(
    image: MovieImage?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val path = image?.localPath
    when {
        path == null -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
            }
        }

        image.isVideoMedia() -> {
            Box(
                modifier = modifier.background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        else -> {
            AsyncImage(
                model = path,
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MediaPreviewDialog(
    images: List<MovieImage>,
    initialPage: Int,
    onDismiss: () -> Unit,
    onDeleteImage: (MovieImage, Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { images.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val image = images[page]
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val path = image.localPath
                    when {
                        path == null -> {
                            Text(
                                text = "文件不存在",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        image.isVideoMedia() -> {
                            VideoPreview(path = path, modifier = Modifier.fillMaxSize())
                        }

                        else -> {
                            AsyncImage(
                                model = path,
                                contentDescription = "详情图预览",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    IconButton(onClick = {
                        onDeleteImage(images[pagerState.currentPage], pagerState.currentPage)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除当前媒体",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭预览",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPreview(path: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setMediaController(MediaController(context).also { controller ->
                    controller.setAnchorView(this)
                })
                setVideoURI(Uri.fromFile(File(path)))
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    start()
                }
            }
        }
    )
}

private fun MovieImage?.isVideoMedia(): Boolean {
    val path = this?.localPath?.lowercase().orEmpty()
    val mimeType = this?.mimeType?.lowercase().orEmpty()
    return mimeType.startsWith("video/") ||
        path.endsWith(".mp4") ||
        path.endsWith(".mkv") ||
        path.endsWith(".webm") ||
        path.endsWith(".3gp") ||
        path.endsWith(".mov")
}

@Composable
private fun ImageImportChoiceCard(
    pendingImportCount: Int,
    pendingImportMode: ImportMode?,
    pendingDeleteCount: Int,
    importErrorMessage: String?,
    onPickImages: (ImportMode) -> Unit
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
            if (pendingDeleteCount > 0) {
                Text(
                    text = "等待系统确认移除原图：$pendingDeleteCount 项",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            importErrorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
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
                onClick = { onPickImages(ImportMode.MoveAndHideOriginal) },
                modifier = Modifier.height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("迁移/剪切图片")
            }
            OutlinedButton(
                onClick = { onPickImages(ImportMode.CopyOnly) },
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

private fun Set<String>.toggle(value: String): Set<String> {
    return if (value in this) this - value else this + value
}
