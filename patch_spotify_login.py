import re

with open('/app/app/src/main/kotlin/com/phantasia/music/ui/SpotifyLoginScreen.kt', 'r') as file:
    content = file.read()

# Replace the complicated developer instructions and client ID check with a simple webview that grabs the SpDcCookie
setup_block = r'''    // Show setup instructions if no client ID configured
    if \(SPOTIFY_CLIENT_ID\.isBlank\(\)\) \{
        SpotifySetupInstructions\(nav\)
        return
    \}'''

content = re.sub(setup_block, '', content)

auth_url_block = r'''    val codeVerifier by remember \{ mutableStateOf\(generateCodeVerifier\(\)\) \}
    val authUrl      by remember \{ mutableStateOf\(buildAuthUrl\(codeVerifier, SPOTIFY_CLIENT_ID\)\) \}'''

content = re.sub(auth_url_block, '    val authUrl = "https://accounts.spotify.com/en/login"', content)

webview_block = r'''                            webViewClient = object : WebViewClient\(\) \{
                                    override fun shouldOverrideUrlLoading\(
                                        view: WebView, request: WebResourceRequest
                                    \): Boolean \{
                                        val url = request\.url\.toString\(\)
                                        if \(url\.startsWith\("phantasia://spotify-callback"\)\) \{
                                            val code  = request\.url\.getQueryParameter\("code"\)
                                            val error = request\.url\.getQueryParameter\("error"\)
                                            when \{
                                                error != null -> \{
                                                    errorMessage = "Spotify declined: \$error"
                                                \}
                                                code != null -> \{
                                                    isExchanging = true
                                                    vm\.onSpotifyCodeReceived\(
                                                        code         = code,
                                                        codeVerifier = codeVerifier,
                                                        clientId     = SPOTIFY_CLIENT_ID,
                                                        onSuccess    = \{ nav\.navigateUp\(\) \},
                                                        onError      = \{ e ->
                                                            isExchanging = false
                                                            errorMessage = e
                                                        \}
                                                    \)
                                                \}
                                            \}
                                            return true
                                        \}
                                        return false
                                    \}
                                \}'''

webview_replace = '''                            webViewClient = object : android.webkit.WebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String?) {
                                        url ?: return
                                        if (url.startsWith("https://accounts.spotify.com") || url.startsWith("https://open.spotify.com")) {
                                            val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                                            if (!cookie.isNullOrBlank() && "sp_dc" in cookie && !isExchanging) {
                                                isExchanging = true
                                                // Extract just the sp_dc token
                                                val spDc = cookie.split("; ").find { it.startsWith("sp_dc=") }?.substringAfter("sp_dc=")
                                                if (spDc != null) {
                                                    vm.onSpotifyTokenReceived(spDc)
                                                    android.os.Handler(android.os.Looper.getMainLooper())
                                                        .postDelayed({ nav.navigateUp() }, 600)
                                                } else {
                                                    isExchanging = false
                                                }
                                            }
                                        }
                                    }
                                }'''

content = re.sub(webview_block, webview_replace, content)

with open('/app/app/src/main/kotlin/com/phantasia/music/ui/SpotifyLoginScreen.kt', 'w') as file:
    file.write(content)
