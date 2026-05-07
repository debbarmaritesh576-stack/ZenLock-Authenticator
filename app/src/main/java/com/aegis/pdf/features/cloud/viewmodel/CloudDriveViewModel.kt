package com.aegis.pdf.ui.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.data.cloud.GoogleDriveManager
import com.aegis.pdf.data.cloud.DropboxManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CloudDriveViewModel @Inject constructor(
    private val googleDriveManager: GoogleDriveManager,
    private val dropboxManager: DropboxManager
) : ViewModel() {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _accountName = MutableStateFlow("")
    val accountName: StateFlow<String> = _accountName.asStateFlow()

    private val _cloudFiles = MutableStateFlow<List<CloudFile>>(emptyList())
    val cloudFiles: StateFlow<List<CloudFile>> = _cloudFiles.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    data class CloudFile(
        val id: String,
        val name: String,
        val size: String,
        val type: String
    )

    fun connectGoogleDrive() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    googleDriveManager.connect()
                }
                _isConnected.value = true
                _accountName.value = "Google Drive"
                loadCloudFiles()
            } catch (e: Exception) {
                _isConnected.value = false
            }
        }
    }

    fun connectDropbox() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dropboxManager.connect()
                }
                _isConnected.value = true
                _accountName.value = "Dropbox"
                loadCloudFiles()
            } catch (e: Exception) {
                _isConnected.value = false
            }
        }
    }

    fun disconnect() {
        _isConnected.value = false
        _accountName.value = ""
        _cloudFiles.value = emptyList()
    }

    fun loadCloudFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val files = googleDriveManager.listPdfFiles()
                    _cloudFiles.value = files.map { file ->
                        CloudFile(
                            id = file.id,
                            name = file.name,
                            size = file.size,
                            type = file.type
                        )
                    }
                } catch (e: Exception) {
                    _cloudFiles.value = emptyList()
                }
            }
        }
    }

    fun downloadFile(file: CloudFile) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                withContext(Dispatchers.IO) {
                    googleDriveManager.downloadFile(file.id)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isUploading.value = false
            }
        }
    }
}