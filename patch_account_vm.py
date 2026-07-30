import re

with open('/app/app/src/main/kotlin/com/phantasia/music/ui/AccountViewModel.kt', 'r') as file:
    content = file.read()

content = re.sub(
    r'fun onSpotifyCodeReceived\(code: String, codeVerifier: String, clientId: String, onSuccess: \(\) -> Unit, onError: \(String\) -> Unit\) \{\n\s*// Stub implementation\n\s*onSuccess\(\)\n\s*\}',
    'fun onSpotifyTokenReceived(token: String) {\n        // Stub implementation\n    }',
    content
)

with open('/app/app/src/main/kotlin/com/phantasia/music/ui/AccountViewModel.kt', 'w') as file:
    file.write(content)
