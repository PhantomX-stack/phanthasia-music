package com.phantasia.music.accounts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.phantasia.music.R

class GoogleLoginActivity : ComponentActivity() {

    companion object {
        const val RC_SIGN_IN = 9001
        const val EXTRA_DISPLAY_NAME = "display_name"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_AVATAR_URL = "avatar_url"
        const val EXTRA_AUTH_CODE = "auth_code"
        const val YTM_SCOPE = "https://www.googleapis.com/auth/youtube.readonly"
        private const val TAG = "GoogleLoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(getString(R.string.google_oauth_client_id), true)
            .requestScopes(Scope(YTM_SCOPE))
            .build()

        val client = GoogleSignIn.getClient(this, gso)
        client.signOut().addOnCompleteListener {
            startActivityForResult(client.signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Using deprecated API for compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != RC_SIGN_IN) return

        runCatching {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val result = Intent().apply {
                putExtra(EXTRA_DISPLAY_NAME, account.displayName.orEmpty())
                putExtra(EXTRA_EMAIL, account.email.orEmpty())
                putExtra(EXTRA_AVATAR_URL, account.photoUrl?.toString().orEmpty())
                putExtra(EXTRA_AUTH_CODE, account.serverAuthCode.orEmpty())
            }
            setResult(Activity.RESULT_OK, result)
        }.onFailure { e ->
            Log.e(TAG, "Google sign-in failed", e)
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }
}
