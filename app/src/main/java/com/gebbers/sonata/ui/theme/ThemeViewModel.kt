package com.gebbers.sonata.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _seedColor = MutableStateFlow<Color?>(null)
    val seedColor = _seedColor.asStateFlow()

    fun updateColorFromImage(uri: String?) {
        if (uri == null) {
            _seedColor.value = null
            return
        }

        viewModelScope.launch {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.image.toBitmap()
                    // If hardware bitmap, Palette will fail. Coil 3 usually returns software bitmap with toBitmap() or similar.
                    val palette = Palette.from(bitmap).generate()
                    val dominant = palette.getMutedColor(0)
                    if (dominant != 0) {
                        _seedColor.value = Color(dominant)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
