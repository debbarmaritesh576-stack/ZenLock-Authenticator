package com.aegis.pdf.ui.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegis.pdf.core.security.VaultManager
import com.aegis.pdf.core.security.VaultFile
import com.aegis.pdf.data.local.DocumentDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val vaultManager: VaultManager,
    private val documentDataSource: DocumentDataSource
) : ViewModel() {

    private val _vaultFiles = MutableStateFlow<List<VaultFile>>(emptyList())
    val vaultFiles: StateFlow<List<VaultFile>> = _vaultFiles.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    init {
        loadVaultFiles()
    }

    fun lock() { _isLocked.value = true }
    fun unlock() { _isLocked.value = false }

    fun addToVault(uri: Uri, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val tempFile = documentDataSource.copyToTemp(uri)
                tempFile?.let { file ->
                    vaultManager.addToVault(file)
                    file.delete()
                }
                loadVaultFiles()
            }
        }
    }

    fun removeFromVault(file: VaultFile) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                vaultManager.deleteFromVault("${file.name}.aegis")
                loadVaultFiles()
            }
        }
    }

    fun exportFile(file: VaultFile, context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val exportedFile = vaultManager.removeFromVault("${file.name}.aegis")
                exportedFile?.let { file ->
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    // Share or save
                }
                loadVaultFiles()
            }
        }
    }

    private fun loadVaultFiles() {
        _vaultFiles.value = vaultManager.getVaultFiles()
    }
}