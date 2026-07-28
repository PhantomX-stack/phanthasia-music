import re
with open('app/src/main/kotlin/com/phantasia/music/ui/LibraryScreen.kt', 'r') as f:
    content = f.read()

# Add variables at the top
top_vars = """
    val vm: LibraryViewModel = hiltViewModel()
    val favs      by vm.favourites.collectAsState(initial = emptyList())
    val playlists by vm.playlists.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName       by remember { mutableStateOf("") }

    var showImportSheet  by remember { mutableStateOf(false) }
    var importUrl        by remember { mutableStateOf("") }
    var importMode       by remember { mutableStateOf<ImportMode?>(null) }
    val accountVm: AccountViewModel = hiltViewModel()
    val accountState by accountVm.state.collectAsState()
"""

content = re.sub(r'val vm: LibraryViewModel = hiltViewModel\(\)[\s\S]*?var newPlaylistName\s*by remember \{ mutableStateOf\(""\) \}', top_vars.strip(), content)

# Add enum at the file level
enum_def = """
enum class ImportMode {
    YTM_PLAYLIST, YTM_LIKED, SPOTIFY_PLAYLIST, SPOTIFY_LIKED
}

@Composable
"""

content = re.sub(r'@Composable', enum_def.strip() + '\n@Composable', content, count=1)

# replace bottom sheet logic
new_sheet = """
    if (showImportSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showImportSheet = false; importUrl = ""; importMode = null },
            containerColor   = PhantasiaColors.SurfaceHigh
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 32.dp)) {
                Text("Import music", style = MaterialTheme.typography.titleMedium,
                    color = PhantasiaColors.OnSurface, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp))

                // YouTube Music options
                Text("YouTube Music", style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFF4444), modifier = Modifier.padding(bottom = 8.dp))

                ImportOptionRow(
                    icon    = Icons.Default.Link,
                    title   = "Import YTM playlist",
                    subtitle= "Paste a YouTube Music playlist link",
                    colour  = Color(0xFFFF4444),
                    onClick = { importMode = ImportMode.YTM_PLAYLIST }
                )
                ImportOptionRow(
                    icon    = Icons.Default.Favorite,
                    title   = "Import liked songs",
                    subtitle= "Import all your YTM liked songs",
                    colour  = Color(0xFFFF4444),
                    onClick = {
                        if (!accountState.isYtmConnected) {
                            showImportSheet = false
                            nav.navigate(Route.YtmLogin.path)
                        } else {
                            accountVm.syncYtm()
                            showImportSheet = false
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Spotify options
                Text("Spotify", style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF1DB954), modifier = Modifier.padding(bottom = 8.dp))

                ImportOptionRow(
                    icon    = Icons.Default.Link,
                    title   = "Import Spotify playlist",
                    subtitle= "Paste a Spotify playlist link",
                    colour  = Color(0xFF1DB954),
                    onClick = { importMode = ImportMode.SPOTIFY_PLAYLIST }
                )
                ImportOptionRow(
                    icon    = Icons.Default.Favorite,
                    title   = "Import liked songs",
                    subtitle= "Import all your Spotify saved songs",
                    colour  = Color(0xFF1DB954),
                    onClick = {
                        if (!accountState.isSpotifyConnected) {
                            showImportSheet = false
                            nav.navigate(Route.SpotifyLogin.path)
                        } else {
                            accountVm.syncSpotify()
                            showImportSheet = false
                        }
                    }
                )

                // URL input dialog for playlist link options
                if (importMode == ImportMode.YTM_PLAYLIST || importMode == ImportMode.SPOTIFY_PLAYLIST) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (importMode == ImportMode.YTM_PLAYLIST)
                            "Paste YouTube Music playlist URL"
                        else "Paste Spotify playlist URL",
                        style = MaterialTheme.typography.labelMedium,
                        color = PhantasiaColors.OnDim,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value         = importUrl,
                        onValueChange = { importUrl = it },
                        placeholder   = {
                            Text(
                                if (importMode == ImportMode.YTM_PLAYLIST)
                                    "https://music.youtube.com/playlist?list=..."
                                else "https://open.spotify.com/playlist/...",
                                color = PhantasiaColors.OnHint,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = TextFieldDefaults.colors(
                            focusedIndicatorColor   = PhantasiaColors.Primary,
                            unfocusedIndicatorColor = PhantasiaColors.Outline,
                            focusedTextColor     = PhantasiaColors.OnSurface,
                            unfocusedTextColor   = PhantasiaColors.OnSurface
                        ),
                        singleLine = false,
                        maxLines   = 3
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick  = {
                            // TODO: Parse URL and trigger import
                            // For now show message that feature is coming
                            showImportSheet = false
                            importUrl = ""
                            importMode = null
                        },
                        enabled  = importUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)
                    ) { Text("Import") }
                }
            }
        }
    }
}

@Composable
private fun ImportOptionRow(
    icon:     androidx.compose.ui.graphics.vector.ImageVector,
    title:    String,
    subtitle: String,
    colour:   Color,
    onClick:  () -> Unit
) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(colour.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = colour, modifier = Modifier.size(20.dp))
            }
        },
        colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
"""

content = re.sub(r'if \(showImportSheet\) \{[\s\S]*?\}\s*\}\s*\}', new_sheet.strip(), content)

with open('app/src/main/kotlin/com/phantasia/music/ui/LibraryScreen.kt', 'w') as f:
    f.write(content)
