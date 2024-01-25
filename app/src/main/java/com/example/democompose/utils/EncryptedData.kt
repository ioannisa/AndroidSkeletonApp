package com.example.democompose.utils

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class EncryptedData @Inject constructor(private val context: Context) {

    companion object {
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val KEY_ALIAS = "myKeyAlias"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val DATASTORE_NAME = "encrypted_datastore"

        // The context for the datastore extension function
        private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)
    }

    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "encrypted_prefs_filename", // Name of preference file
            masterKey, // Master key for encryption/decryption
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Key encryption scheme
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Value encryption scheme
        )
    }

    fun encryptSharedPreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun decryptSharedPreference(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE).apply {
        load(null)
        if (!containsAlias(KEY_ALIAS)) {
            generateSecretKey(KEY_ALIAS)
        }
    }

    // DataStore instance
    private val dataStore = context.dataStore

    suspend fun putDataStorePreference(key: String, value: String) {
        val dataKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[dataKey] = value
        }
    }

    suspend fun getDataStorePreference(key: String, defaultValue: String? = null): String? {
        val dataKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[dataKey] ?: defaultValue
    }

    suspend fun encryptDataStorePreference(key: String, value: String) {
        val dataKey = stringPreferencesKey(key)
        val encryptedValue = Base64.encodeToString(encryptData(value), Base64.DEFAULT)

        dataStore.edit { preferences ->
            preferences[dataKey] = encryptedValue
        }
    }

    suspend fun decryptDataStorePreference(key: String, defaultValue: String? = null): String? {
        val dataKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        val encryptedValue = preferences[dataKey] ?: defaultValue
        return decryptData(Base64.decode(encryptedValue, Base64.DEFAULT))
    }

    private fun generateSecretKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun encryptData(data: String): ByteArray {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    fun decryptData(encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(encryptedData), StandardCharsets.UTF_8)
    }
}