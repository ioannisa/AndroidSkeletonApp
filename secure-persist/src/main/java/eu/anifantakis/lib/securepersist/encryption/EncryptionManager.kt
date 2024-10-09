package eu.anifantakis.lib.securepersist.encryption

import android.content.Context
import android.content.res.AssetManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.Certificate
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * EncryptionManager class handles encryption and decryption using the Android KeyStore system or an external key.
 */
class EncryptionManager private constructor(
    private val context: Context,
    private var keyAlias: String?,
    private var externalKey: SecretKey?
) : IEncryptionManager {

    init {
        if ((keyAlias == null && externalKey == null) || (keyAlias != null && externalKey != null)) {
            throw IllegalArgumentException("Either keyAlias or externalKey must be provided, but not both.")
        }
    }

    companion object {
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12 // IV size for GCM is 12 bytes
        private const val TAG_SIZE = 128 // Tag size for GCM is 128 bits

        /**
         * Starts the building process for EncryptionManager.
         *
         * @param context The application context.
         * @return A new Builder instance.
         */
        fun builder(context: Context): Builder {
            return Builder(context)
        }

        /**
         * Generates a new external secret key.
         *
         * @return The generated secret key.
         */
        fun generateExternalKey(): SecretKey {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256)
            return keyGenerator.generateKey()
        }

        /**
         * Encrypts a value and encodes it to a Base64 string using an external key.
         *
         * @param value The value to encrypt.
         * @param secretKey The secret key to use for encryption.
         * @return The encrypted value as a Base64 string.
         */
        fun <T> encryptValue(value: T, secretKey: SecretKey): String {
            val stringValue = value.toString()
            val encryptedData = encryptData(stringValue, secretKey)
            return Base64.encodeToString(encryptedData, Base64.NO_WRAP)
        }

        /**
         * Decrypts a Base64 encoded string and returns the original value using an external key.
         *
         * @param encryptedValue The encrypted value as a Base64 string.
         * @param defaultValue The default value to return if decryption fails.
         * @param secretKey The secret key to use for decryption.
         * @return The decrypted value.
         */
        fun <T> decryptValue(encryptedValue: String, defaultValue: T, secretKey: SecretKey): T {
            return try {
                val encryptedData = Base64.decode(encryptedValue, Base64.NO_WRAP)
                val decryptedString = decryptData(encryptedData, secretKey)
                when (defaultValue) {
                    is Boolean -> decryptedString.toBoolean() as T
                    is Int -> decryptedString.toInt() as T
                    is Float -> decryptedString.toFloat() as T
                    is Long -> decryptedString.toLong() as T
                    is String -> decryptedString as T
                    else -> throw IllegalArgumentException("Unsupported type")
                }
            } catch (e: Exception) {
                Log.e("EncryptionManager", "Decryption failed", e)
                defaultValue
            }
        }

        /**
         * Encrypts the given data using the provided secret key.
         *
         * @param data The plaintext data to encrypt.
         * @param secretKey The secret key to use for encryption.
         * @return The encrypted data as a byte array.
         */
        fun encryptData(data: String, secretKey: SecretKey): ByteArray {
            return try {
                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encryptedData = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

                // Combine IV and encrypted data
                combineIvAndEncryptedData(iv, encryptedData)
            } catch (e: Exception) {
                Log.e("EncryptionManager", "Encryption failed", e)
                throw e
            }
        }

        /**
         * Decrypts the given encrypted data using the provided secret key.
         *
         * @param encryptedData The encrypted data as a byte array.
         * @param secretKey The secret key to use for decryption.
         * @return The decrypted plaintext data as a string.
         */
        fun decryptData(encryptedData: ByteArray, secretKey: SecretKey): String {
            return try {
                if (encryptedData.size < IV_SIZE) {
                    throw IllegalArgumentException("Encrypted data is too short to contain a valid IV")
                }

                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)

                val iv = ByteArray(IV_SIZE)
                System.arraycopy(encryptedData, 0, iv, 0, iv.size)

                val encryptedBytes = ByteArray(encryptedData.size - iv.size)
                System.arraycopy(encryptedData, iv.size, encryptedBytes, 0, encryptedBytes.size)

                val ivSpec = GCMParameterSpec(TAG_SIZE, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
                String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                // Log the exception for better debugging and tracing
                Log.e("EncryptionManager", "Decryption failed", e)
                throw e
            }
        }

        /**
         * Combines the IV and encrypted data into a single byte array.
         *
         * @param iv The initialization vector.
         * @param encryptedData The encrypted data.
         * @return The combined IV and encrypted data.
         */
        private fun combineIvAndEncryptedData(iv: ByteArray, encryptedData: ByteArray): ByteArray {
            val combined = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)
            return combined
        }

        /**
         * Encodes a SecretKey to a Base64 string for storage or transmission.
         *
         * @param secretKey The SecretKey to encode.
         * @return The Base64 encoded string representation of the SecretKey.
         */
        fun encodeSecretKey(secretKey: SecretKey): String {
            val encodedKey = secretKey.encoded
            return Base64.encodeToString(encodedKey, Base64.NO_WRAP)
        }

        /**
         * Decodes an encoded SecretKey that was stored as Base64 string from the "encodeSecretKey" function back to a SecretKey.
         *
         * @param encodedKey The Base64 encoded string representation of the SecretKey.
         * @return The decoded SecretKey.
         */
        fun decodeSecretKey(encodedKey: String): SecretKey {
            val decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP)
            return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        }
    }

    /**
     * Builder class for constructing an EncryptionManager instance.
     */
    class Builder(private val context: Context) {
        private var keyAlias: String? = null
        private var externalKey: SecretKey? = null

        /**
         * Configures the EncryptionManager to use the Android KeyStore with the specified key alias.
         *
         * @param keyAlias The alias for the encryption key in the KeyStore.
         * @return The Builder instance.
         */
        fun withKeyStore(keyAlias: String) = apply {
            this.keyAlias = keyAlias
            this.externalKey = null // Ensure only one key is set
        }

        /**
         * Configures the EncryptionManager to use the specified external secret key.
         *
         * @param externalKey The external secret key for encryption and decryption.
         * @return The Builder instance.
         */
        fun withExternalKey(externalKey: SecretKey) = apply {
            this.externalKey = externalKey
            this.keyAlias = null // Ensure only one key is set
        }

        /**
         * Builds the EncryptionManager instance with the configured settings.
         *
         * @return The EncryptionManager instance.
         * @throws IllegalArgumentException if neither keyAlias nor externalKey is provided, or both are provided.
         */
        fun build(): EncryptionManager {
            if ((keyAlias == null && externalKey == null) || (keyAlias != null && externalKey != null)) {
                throw IllegalArgumentException("Either keyAlias or externalKey must be provided, but not both.")
            }
            return EncryptionManager(context, keyAlias, externalKey)
        }
    }

    private val keyStore: KeyStore? = keyAlias?.let {
        KeyStore.getInstance(KEYSTORE_TYPE).apply {
            load(null)
            if (!containsAlias(it)) {
                generateSecretKey(it)
            }
        }
    }

    /**
     * Generates a new secret key and stores it in the KeyStore.
     *
     * @param alias The alias for the key.
     */
    private fun generateSecretKey(alias: String) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256) // Using 256-bit key for strong encryption
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    /**
     * Sets an external secret key for encryption and decryption.
     *
     * @param secretKey The external secret key to be used.
     */
    override fun setExternalKey(secretKey: SecretKey) {
        this.externalKey = secretKey
        this.keyAlias = null
    }

    /**
     * Encrypts the given data using the secret key.
     *
     * @param data The plaintext data to encrypt.
     * @return The encrypted data as a byte array.
     */
    override fun encryptData(data: String): ByteArray {
        return Companion.encryptData(data, getKey())
    }

    /**
     * Decrypts the given encrypted data using the secret key.
     *
     * @param encryptedData The encrypted data as a byte array.
     * @return The decrypted plaintext data as a string.
     */
    override fun decryptData(encryptedData: ByteArray): String {
        return Companion.decryptData(encryptedData, getKey())
    }

    /**
     * Encrypts a value and encodes it to a Base64 string.
     *
     * @param value The value to encrypt.
     * @return The encrypted value as a Base64 string.
     */
    override fun <T> encryptValue(value: T): String {
        return Companion.encryptValue(value, getKey())
    }

    /**
     * Decrypts a Base64 encoded string and returns the original value.
     *
     * @param encryptedValue The encrypted value as a Base64 string.
     * @param defaultValue The default value to return if decryption fails.
     * @return The decrypted value.
     */
    override fun <T> decryptValue(encryptedValue: String, defaultValue: T): T {
        return Companion.decryptValue(encryptedValue, defaultValue, getKey())
    }

    /**
     * Encrypts a file from the assets folder and stores the encrypted file in the app's private storage.
     *
     * @param assetFileName The name of the file in the assets folder.
     * @param encryptedFileName The name for the encrypted file to be stored in the app's private storage.
     */
    override fun encryptFileFromAssets(assetFileName: String, encryptedFileName: String) {
        val assetManager: AssetManager = context.assets
        val inputStream: InputStream = assetManager.open(assetFileName)
        val fileContent: ByteArray = inputStream.readBytes()

        // Encode the ByteArray to a Base64 String
        val base64Content = Base64.encodeToString(fileContent, Base64.NO_WRAP)

        // Encrypt the Base64 String
        val encryptedData: ByteArray = Companion.encryptData(base64Content, getKey())

        // Write the encrypted data to the file
        val encryptedFile = File(context.filesDir, encryptedFileName)
        FileOutputStream(encryptedFile).use { it.write(encryptedData) }
    }

    /**
     * Decrypts a previously encrypted file stored in the app's private storage.
     *
     * @param encryptedFileName The name of the encrypted file stored in the app's private storage.
     * @return The decrypted file content as a byte array.
     */
    override fun decryptFile(encryptedFileName: String): ByteArray {
        val encryptedFile = File(context.filesDir, encryptedFileName)
        val encryptedData: ByteArray = encryptedFile.readBytes()

        // Decrypt to get the Base64 String
        val decryptedBase64String: String = Companion.decryptData(encryptedData, getKey())

        // Decode the Base64 String back to a ByteArray
        val fileContent: ByteArray = Base64.decode(decryptedBase64String, Base64.NO_WRAP)

        return fileContent
    }

    /**
     * Retrieves the attestation certificate chain for the key.
     *
     * @param alias The alias of the key to get the attestation for.
     * @return The attestation certificate chain.
     */
    override fun getAttestationCertificateChain(alias: String): Array<Certificate> {
        val entry = keyStore?.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalArgumentException("No key found under alias: $alias")
        return entry.certificateChain
    }

    /**
     * Retrieves the secret key to be used for encryption and decryption.
     *
     * @return The secret key.
     */
    private fun getKey(): SecretKey {
        return externalKey ?: (keyAlias?.let {
            keyStore?.getKey(it, null) as? SecretKey
        } ?: throw IllegalStateException("No key available for encryption/decryption"))
    }

    /**
     * Reads InputStream as ByteArray.
     *
     * @return The ByteArray read from the InputStream.
     */
    private fun InputStream.readBytes(): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var count: Int
        while (this.read(data).also { count = it } != -1) {
            buffer.write(data, 0, count)
        }
        return buffer.toByteArray()
    }
}
