package com.aegis.pdf.ui.cloud  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.data.cloud.CloudFileRepository  
import com.aegis.pdf.data.cloud.CloudFileRepository.Provider  
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
    private val repository: CloudFileRepository  
) : ViewModel() {  
  
    private val _isConnected = MutableStateFlow(false)  
    val isConnected = _isConnected.asStateFlow()  
  
    private val _accountName = MutableStateFlow("")  
    val accountName = _accountName.asStateFlow()  
  
    private val _cloudFiles = MutableStateFlow<List<CloudFile>>(emptyList())  
    val cloudFiles = _cloudFiles.asStateFlow()  
  
    private val _isRefreshing = MutableStateFlow(false)  
    val isRefreshing = _isRefreshing.asStateFlow()  
  
    // Tracking active provider  
    private var activeProvider: Provider? = null  
  
    data class CloudFile(  
        val id: String,  
        val name: String,  
        val size: String,  
        val provider: Provider  
    )  
  
    fun connectProvider(provider: Provider) {  
        viewModelScope.launch {  
            _isRefreshing.value = true  
            try {  
                withContext(Dispatchers.IO) {  
                    val manager = repository.getProvider(provider)  
                    manager.connect()   
                }  
                activeProvider = provider  
                _isConnected.value = true  
                _accountName.value = provider.name.lowercase().capitalize()  
                refreshFiles()  
            } catch (e: Exception) {  
                _isConnected.value = false  
            } finally {  
                _isRefreshing.value = false  
            }  
        }  
    }  
  
    fun refreshFiles() {  
        val current = activeProvider ?: return  
        viewModelScope.launch {  
            _isRefreshing.value = true  
            try {  
                val files = withContext(Dispatchers.IO) {  
                    repository.getProvider(current).listPdfFiles()  
                }  
                _cloudFiles.value = files.map {   
                    CloudFile(it.id, it.name, it.size, current)   
                }  
            } catch (e: Exception) {  
                _cloudFiles.value = emptyList()  
            } finally {  
                _isRefreshing.value = false  
            }  
        }  
    }  
  
    fun downloadFile(file: CloudFile) {  
        viewModelScope.launch {  
            _isRefreshing.value = true // Show loading on download too  
            try {  
                withContext(Dispatchers.IO) {  
                    val downloadedFile = repository.getProvider(file.provider).downloadFile(file.id)  
                    downloadedFile?.let {  
                        // Logic to trigger C++ Engine to open this local file  
                        repository.syncFileToRecent(it, file.provider)  
                    }  
                }  
            } catch (e: Exception) {  
                // Handle download error  
            } finally {  
                _isRefreshing.value = false  
            }  
        }  
    }  
  
    fun disconnect() {  
        activeProvider = null  
        _isConnected.value = false  
        _accountName.value = ""  
        _cloudFiles.value = emptyList()  
    }  
}