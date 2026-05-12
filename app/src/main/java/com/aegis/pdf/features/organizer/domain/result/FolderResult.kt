package com.aegis.pdf.features.organizer.domain.result

sealed class FolderResult<out T> {
    data class Success<T>(val data: T) : FolderResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : FolderResult<Nothing>()
    object Loading : FolderResult<Nothing>()
}

sealed class TagResult<out T> {
    data class Success<T>(val data: T) : TagResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : TagResult<Nothing>()
}