1. **Fix Package Name & Application ID**: Update `app/build.gradle.kts` to strictly use `com.phantasia.music`.
2. **Theme update**: Update `Theme.kt` for Midnight Purple color scheme with appropriate gradients and semi-transparent elements.
3. **App Navigation & System Bars**: Update `AppNavigation.kt` to fix system bar overlapping by using `WindowInsets.systemBars`. Add `Stats` to the middle of the bottom navigation bar. Fix the navigation backstack (e.g., from Settings/Stats) by properly popping the stack.
4. **Settings Screen Re-design**: Completely rewrite `SettingsScreen.kt` to have Velune-like categories (Appearance, Account, Listen Together, Player & Audio, Content, Discord, Integration, Privacy). Use modern block design.
5. **Home Recommendations**: Update `HomeScreen.kt` to include mood/genre chips and actual home recommendations (by adding a simple call to InnerTube for home data if possible, or mapping to mock recommendations).
6. **Search Screen & History**: Update `SearchScreen.kt` and `SearchViewModel.kt` to persist search history properly, add a clear all button, a cross button for individual queries, and a "Play Random" button for history. Fix the random crash in Search.
7. **Player UI Updates**: Update `PlayerScreen.kt` to have a toggle for Video and Thumbnail at the top, and smooth animations.
8. **Library Importer**: Update `LibraryScreen.kt` to include the Importer option in the top right, with options for YTM Playlist, YTM Liked, Spotify Playlist, and Spotify Liked.
9. **Account Login Screens**: Simplify `SpotifyLoginScreen.kt` and `YtmLoginScreen.kt` to use basic WebViews for cookie/token extraction, avoiding Client ID errors.
10. **Pre-commit**: Run pre_commit_instructions.
11. **Git Commit**: Commit all changes.
