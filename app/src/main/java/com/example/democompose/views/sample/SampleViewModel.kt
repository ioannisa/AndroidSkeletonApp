package com.example.democompose.views.sample

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.democompose.views.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.anifantakis.lib.securepersist.PersistManager
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
class SampleViewModel @Inject constructor(private val encryptedData: PersistManager, private val encryptedManager: EncryptionManager) : BaseViewModel() {

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

    var count by encryptedData.preference("count", 0)

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
            preferences()
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
    }

    // DataStore with/without encryption
    suspend fun preferences() {
        // Define keys
        val stringKey = "example_string_key"
        val intKey = "example_int_key"
        val booleanKey = "example_boolean_key"

        val eStringKey = "e_example_string_key"
        val eIntKey = "e_example_int_key"
        val eBooleanKey = "e_example_boolean_key"

        // Save preferences
//        encryptedData.putDataStorePreference(stringKey, "exampleString")
//        encryptedData.putDataStorePreference(intKey, 123)
//        encryptedData.putDataStorePreference(booleanKey, true)
//
//        encryptedData.encryptDataStorePreference(eStringKey, "encryptedString")
//        encryptedData.encryptDataStorePreference(eIntKey, 567)
//        encryptedData.encryptDataStorePreference(eBooleanKey, true)

        // Retrieve preferences
        val stringValue: String = encryptedData.getDataStorePreference(stringKey, "")
        val intValue: Int = encryptedData.getDataStorePreference(intKey, 0)
        val booleanValue: Boolean = encryptedData.getDataStorePreference(booleanKey, false)

        // we call the getDataStorePreference on an encrypted value to see how it looks encrypted
        val encryptedString: String = encryptedData.getDataStorePreference(eStringKey, "")

        // then we do the same for the decrypted value
        val decryptedString: String = encryptedData.decryptDataStorePreference(eStringKey, "")
        val decryptedInt: Int = encryptedData.decryptDataStorePreference(eIntKey, 0)
        val decryptedBoolean: Boolean = encryptedData.decryptDataStorePreference(eBooleanKey, false)

        // Using Delegation
        var delegation1String by encryptedData.preference(stringKey, "delegationString1")
        var delegation1Int by encryptedData.preference(intKey, 11)
        var delegation1Boolean by encryptedData.preference(booleanKey, true)

        var delegation2String by encryptedData.preference("delegationString2")
        var delegation2Int by encryptedData.preference(22)
        var delegation2Boolean by encryptedData.preference(false)

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

        val encryptRaw = EncryptionManager.encryptValue("abcdefg", secretKey =  customKey)
        Timber.tag("DataStore").d("Raw Encrypted value for 'abcdefg' value: $encryptRaw")
        Timber.tag("DataStore").d("Raw Decrypted value for 'abcdefg' value: ${EncryptionManager.decryptValue(encryptRaw, "INVALID", secretKey = customKey)}")


        // Delete preferences
        encryptedData.deleteDataStorePreference(stringKey)
        encryptedData.deleteDataStorePreference(intKey)
        encryptedData.deleteDataStorePreference(booleanKey)
    }
}