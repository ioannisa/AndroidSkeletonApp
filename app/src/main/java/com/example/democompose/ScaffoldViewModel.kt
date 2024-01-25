package com.example.democompose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ScaffoldViewModel @Inject constructor(): ViewModel() {
    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title

    private val _onBackPress = MutableStateFlow<(() -> Unit)?>(null)
    val onBackPress: StateFlow<(() -> Unit)?> = _onBackPress

    fun updateAppBar(title: String?, onBackPress: (() -> Unit)?) {
        _title.value = title
        _onBackPress.value = onBackPress
    }


    private val _topBarHeight = MutableStateFlow(0)
    val topBarHeight: StateFlow<Int> = _topBarHeight

    private val _bottomBarHeight = MutableStateFlow(0)
    val bottomBarHeight: StateFlow<Int> = _bottomBarHeight

    fun setTopBarHeight(height: Int) {
        _topBarHeight.value = height
    }

    fun setBottomBarHeight(height: Int) {
        _bottomBarHeight.value = height
    }
}