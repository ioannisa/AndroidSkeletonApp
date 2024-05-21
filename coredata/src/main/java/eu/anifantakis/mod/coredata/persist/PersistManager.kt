package eu.anifantakis.mod.coredata.persist

import android.content.Context
import eu.anifantakis.mod.coredata.persist.internal.DataStoreManager
import eu.anifantakis.mod.coredata.persist.internal.SharedPreferencesManager
import kotlin.reflect.KProperty

class PersistManager(context: Context, keyAlias: String) {

    private val encryptionManager = EncryptionManager(keyAlias)
    private val sharedPreferencesManager = SharedPreferencesManager(context)
    private val dataStoreManager = DataStoreManager(context, encryptionManager)

    // Wrapper methods for SharedPreferencesManager

    fun <T> encryptSharedPreference(key: String, value: T) {
        sharedPreferencesManager.put(key, value)
    }

    fun <T> decryptSharedPreference(key: String, defaultValue: T): T {
        return sharedPreferencesManager.get(key, defaultValue)
    }

    fun deleteSharedPreference(key: String) {
        sharedPreferencesManager.delete(key)
    }

    // Wrapper methods for DataStoreManager

    suspend fun <T> putDataStorePreference(key: String, value: T) {
        dataStoreManager.put(key, value)
    }

    suspend fun <T : Any> getDataStorePreference(key: String, defaultValue: T): T {
        return dataStoreManager.get(key, defaultValue)
    }

    suspend fun <T> encryptDataStorePreference(key: String, value: T) {
        dataStoreManager.putEncrypted(key, value)
    }

    suspend fun <T> decryptDataStorePreference(key: String, defaultValue: T): T {
        return dataStoreManager.getEncrypted(key, defaultValue)
    }

    suspend fun deleteDataStorePreference(key: String) {
        dataStoreManager.delete(key)
    }

    // Wrapper for the preference delegate

    class EncryptedPreference<T>(
        private val persist: PersistManager,
        private val defaultValue: T,
        private val key: String? = null
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val preferenceKey = key ?: property.name
            return persist.decryptSharedPreference(preferenceKey, defaultValue)
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            val preferenceKey = key ?: property.name
            persist.encryptSharedPreference(preferenceKey, value)
        }
    }

    fun <T> preference(defaultValue: T): EncryptedPreference<T> = EncryptedPreference(this, defaultValue)

    fun <T> preference(key: String, defaultValue: T): EncryptedPreference<T> = EncryptedPreference(this, defaultValue, key)
}