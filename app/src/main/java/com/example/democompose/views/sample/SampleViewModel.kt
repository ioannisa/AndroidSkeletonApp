package com.example.democompose.views.sample

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.democompose.views.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.anifantakis.lib.securepersist.PersistManager
import eu.anifantakis.lib.securepersist.compose.mutableStateOf
import eu.anifantakis.lib.securepersist.encryption.EncryptionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor(private val persistManager: PersistManager) : BaseViewModel() {

    // instead of StateFlow we can return directly a state
    // it is private set so it is immutable to the outside, but mutable inside
    var stateNum by mutableIntStateOf(0)


    // for StateFlow "private set" is not enough we use the Backing Property approach
    private var _stateFlowNum = MutableStateFlow<Int>(0)
    val stateFlowNum = _stateFlowNum.asStateFlow()

    // for SharedFlow "private set" is not enough we use the Backing Property approach
    private var _sharedFlowNum = MutableSharedFlow<Int>()
    val sharedFlowNum: SharedFlow<Int> = _sharedFlowNum

    private var collectedNumber = 0
    private var channel = Channel<Int>()

    var count by persistManager.preference(0, "count")

    // using encrypted shared preferences
//    @SharedPref("xxx2")
//    var persistedNumber1 by persistManager.annotatedPreference(1000)


    private var _persistedNumber1 by persistManager.preference<Int>(1000)
    var persistedNumber1State by mutableIntStateOf(_persistedNumber1)
        private set

    // using encrypted datastore preferences, but with direct exposure to state
    var persistedNumber2 by persistManager.mutableStateOf(2000, storage = PersistManager.Storage.SHARED_PREFERENCES)
        private set

    init {
        viewModelScope.launch {
            // from a channel we can observe things sent and act
            channel.consumeEach {
                collectedNumber = it + 1

                // we can emit the collected value here
                _sharedFlowNum.emit(collectedNumber)
            }
        }

        viewModelScope.launch {
            demo_datastore_preferences()
        }
    }

    fun incrementCounters() {
        viewModelScope.launch {
            stateNum += 1;
            _stateFlowNum.value += 1;

            // we can send to channels values
            channel.send(collectedNumber)

            count = stateNum
        }

        // Option 1- updating state with persistence separate property (not recommended)
        persistedNumber1State++
        _persistedNumber1 = persistedNumber1State


        // Option 2- updating state with persistence directly exposed to state (recommended)
        persistedNumber2++
    }

    // DataStore with/without encryption
    suspend fun demo_datastore_preferences() {
        // Define keys
        val stringKey = "example_string_key"
        val intKey = "example_int_key"
        val booleanKey = "example_boolean_key"

        val eStringKey = "e_example_string_key"
        val eIntKey = "e_example_int_key"
        val eBooleanKey = "e_example_boolean_key"

        // Save preferences
//        persistManager.dataStorePrefs.put(stringKey, "exampleString", encrypted = false)
//        persistManager.dataStorePrefs.put(intKey, 123, encrypted = false)
//        persistManager.dataStorePrefs.put(booleanKey, true, encrypted = false)
//
//        persistManager.dataStorePrefs.put(eStringKey, "encryptedString")
//        persistManager.dataStorePrefs.put(eIntKey, 567)
//        persistManager.dataStorePrefs.put(eBooleanKey, true)

        // Retrieve preferences
        val stringValue: String = persistManager.dataStorePrefs.get(stringKey, "", encrypted = false)
        val intValue: Int = persistManager.dataStorePrefs.get(intKey, 0, encrypted = false)
        val booleanValue: Boolean = persistManager.dataStorePrefs.get(booleanKey, false, encrypted = false)

        // we call the getDataStorePreference on an encrypted value to see how it looks encrypted
        val encryptedString: String = persistManager.dataStorePrefs.get(eStringKey, "", encrypted = false)

        // then we do the same for the decrypted value
        val decryptedString: String = persistManager.dataStorePrefs.get(eStringKey, "")
        val decryptedInt: Int = persistManager.dataStorePrefs.get(eIntKey, 0)
        val decryptedBoolean: Boolean = persistManager.dataStorePrefs.get(eBooleanKey, false)

        // Using Delegation
        var delegation1String by persistManager.preference(stringKey, "delegationString1", storage = PersistManager.Storage.SHARED_PREFERENCES)
        var delegation1Int by persistManager.preference(11, intKey, storage = PersistManager.Storage.DATA_STORE)
        var delegation1Boolean by persistManager.preference(true, booleanKey, storage = PersistManager.Storage.DATA_STORE)

        var delegation2String by persistManager.preference("delegationString2", storage = PersistManager.Storage.SHARED_PREFERENCES)
        var delegation2Int by persistManager.preference(22, storage = PersistManager.Storage.DATA_STORE_ENCRYPTED)
        var delegation2Boolean by persistManager.preference(false, storage = PersistManager.Storage.DATA_STORE)

//        delegation1String = "VAL1"
//        delegation1Int = 22
//        delegation1Boolean = false
//
//        delegation2String = "VAL2"
//        delegation2Int = 33
//        delegation2Boolean = true

        Timber.tag("DataStore").d("String value: $stringValue")
        Timber.tag("DataStore").d("Int value: $intValue")
        Timber.tag("DataStore").d("Boolean value: $booleanValue")

        Timber.tag("DataStore").d("Encrypted String base64 : $encryptedString")
        Timber.tag("DataStore").d("Encrypted String value  : ${Base64.decode(encryptedString, Base64.DEFAULT)}")

        Timber.tag("DataStore").d("Decrypted String value : $decryptedString")
        Timber.tag("DataStore").d("Decrypted Int value    : $decryptedInt")
        Timber.tag("DataStore").d("Decrypted Boolean value: $decryptedBoolean")

        Timber.tag("DataStore").d("Delegation1 String value: $delegation1String")
        Timber.tag("DataStore").d("Delegation1 Int value: $delegation1Int")
        Timber.tag("DataStore").d("Delegation1 Boolean value: $delegation1Boolean")

        Timber.tag("DataStore").d("Delegation2 String value: $delegation2String")
        Timber.tag("DataStore").d("Delegation2 Int value: $delegation2Int")
        Timber.tag("DataStore").d("Delegation2 Boolean value: $delegation2Boolean")

        val customKey = EncryptionManager.generateExternalKey()
        Base64.encodeToString(customKey.encoded, Base64.DEFAULT)

        val encryptRaw = EncryptionManager.encryptValue("abcdefg", secretKey =  customKey)

        val encodedKey = EncryptionManager.encodeSecretKey(customKey)
        val decodedKey = EncryptionManager.decodeSecretKey(encodedKey)

        Timber.tag("DataStore").d("Raw Encrypted value for 'abcdefg' value: $encryptRaw")
        Timber.tag("DataStore").d("Raw Decrypted value for 'abcdefg' value: ${EncryptionManager.decryptValue(encryptRaw, "INVALID", secretKey = decodedKey)}")


        // Delete preferences
        persistManager.dataStorePrefs.delete(stringKey)
        persistManager.dataStorePrefs.delete(intKey)
        persistManager.dataStorePrefs.delete(booleanKey)
    }
}