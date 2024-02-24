package com.example.democompose.views.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.democompose.views.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor() : BaseViewModel() {

    // instead of StateFlow we can return directly a state
    // it is private set so it is immutable to the outside, but mutable inside
    var stateNum by mutableIntStateOf(0)
        private set

    // for StateFlow "private set" is not enough we use the Backing Property approach
    private var _stateFlowNum = MutableStateFlow<Int>(0)
    val stateFlowNum = _stateFlowNum.asStateFlow()

    // for SharedFlow "private set" is not enough we use the Backing Property approach
    private var _sharedFlowNum = MutableSharedFlow<Int>()
    val sharedFlowNum: SharedFlow<Int> = _sharedFlowNum

    private var collectedNumber = 0
    private var channel = Channel<Int>()


    init {
        viewModelScope.launch {
            // from a channel we can observe things sent and act
            channel.consumeEach {
                collectedNumber = it + 1

                // we can emit the collected value here
                _sharedFlowNum.emit(collectedNumber)
            }
        }
    }

    fun incrementCounters() {
        viewModelScope.launch {
            stateNum += 1;
            _stateFlowNum.value += 1;

            // we can send to channels values
            channel.send(collectedNumber)
        }
    }
}