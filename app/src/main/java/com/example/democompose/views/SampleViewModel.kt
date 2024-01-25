package com.example.democompose.views

import androidx.compose.runtime.mutableStateOf
import com.example.democompose.views.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SampleViewModel @Inject constructor() : BaseViewModel() {

    val selectedNumber = mutableStateOf<Int>(1500)


}