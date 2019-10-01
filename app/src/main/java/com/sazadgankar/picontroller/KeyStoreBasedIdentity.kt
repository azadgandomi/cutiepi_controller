package com.sazadgankar.picontroller

import android.util.Log
import com.jcraft.jsch.Identity
import java.security.KeyStore
import java.security.Signature

class KeyNotFoundException : Exception()

class KeyStoreBasedIdentity : Identity {
    companion object {
        const val TAG = "KeyStoreBasedIdentity"
    }

    private val keys: KeyStore.PrivateKeyEntry

    init {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (!keyStore.containsAlias(SSH_KEY_PAIR_ALIAS)) {
            throw KeyNotFoundException()
        }
        val entry = keyStore.getEntry(SSH_KEY_PAIR_ALIAS, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw KeyNotFoundException()
        }
        keys = entry
    }

    override fun getPublicKeyBlob(): ByteArray? {
        return keys.certificate.publicKey.encoded
    }

    override fun clear() {
        Log.v(TAG, "Clear() called")
    }

    override fun getAlgName(): String {
        return "ssh-rsa"
    }

    override fun getName(): String {
        return TAG
    }

    override fun isEncrypted(): Boolean {
        return false
    }

    override fun getSignature(data: ByteArray?): ByteArray {
        return Signature.getInstance("SHA256withRSA").run {
            initSign(keys.privateKey)
            update(data)
            sign()
        }
    }

    override fun decrypt(): Boolean {
        return true
    }

    override fun setPassphrase(passphrase: ByteArray?): Boolean {
        return true
    }

}