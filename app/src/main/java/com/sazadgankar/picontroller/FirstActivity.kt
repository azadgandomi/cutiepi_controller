package com.sazadgankar.picontroller

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import java.io.InputStreamReader
import java.security.KeyPairGenerator
import java.security.KeyStore


class FirstActivity : AppCompatActivity() {
    companion object {
        const val TAG = "FirstActivity"
    }

    private val jsch = JSch()

    init {
        jsch.addIdentity(
            KeyStoreBasedIdentity(),
            ByteArray(0)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getSshKey(view: View) {
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (!keyStore.containsAlias(SSH_KEY_PAIR_ALIAS)) {
            generateKeys()
        }
        val entry = keyStore.getEntry(SSH_KEY_PAIR_ALIAS, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            Log.e(TAG, "Not an instance of a PrivateKeyEntry")
            return
        }
        val publicKey = entry.certificate.publicKey.encoded.toString(Charsets.US_ASCII)

        Log.i(TAG, "Public Key: $publicKey")
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CutiePi Ssh Public key", publicKey)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Public Key copied to clipboard!", Toast.LENGTH_SHORT).show()
    }


    @Suppress("UNUSED_PARAMETER")
    fun startCutiePi(view: View) {
        val session = jsch.getSession(USER_NAME, HOST_NAME, 22)
        session.connect()

        val channel = session.openChannel("exec") as ChannelExec
        channel.setCommand("echo CutieTest")
        channel.inputStream = null
        val inputStream = channel.inputStream
        channel.connect()
        Log.i(TAG, "Server reply: ${InputStreamReader(inputStream).readText()}")
    }


    @Suppress("UNUSED_PARAMETER")
    fun startLocalNetworkActivity(view: View) {
        startActivity(Intent(this, LocalNetworkConnectionActivity::class.java))
    }


    @Suppress("UNUSED_PARAMETER")
    fun startInternetActivity(view: View) {
        startActivity(Intent(this, InternetConnectionActivity::class.java))
    }


    private fun generateKeys() {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            "AndroidKeyStore"
        )
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            SSH_KEY_PAIR_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            build()
        }
        kpg.initialize(parameterSpec)
        val kp = kpg.generateKeyPair()
        Log.i(TAG, "Generated public key: ${kp.public.encoded.toString(Charsets.US_ASCII)}")
    }

}
